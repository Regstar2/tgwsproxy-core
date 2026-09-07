[CmdletBinding()]
param(
    [int]$ApiLevel = 21,
    [string]$Output = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$sourceDir = Join-Path $repoRoot "native/tgwsproxy"
$artifactDir = Join-Path $repoRoot "artifacts/native/arm64-v8a"
$outLib = Join-Path $artifactDir "libtgwsproxy.so"

if ([string]::IsNullOrWhiteSpace($Output)) {
    $Output = Join-Path $repoRoot "core/src/main/jniLibs/arm64-v8a/libtgwsproxy.so"
}
$Output = [System.IO.Path]::GetFullPath($Output)

$go = Get-Command go -ErrorAction Stop
$ndkRoot = if ($env:ANDROID_NDK_ROOT -and (Test-Path $env:ANDROID_NDK_ROOT)) {
    $env:ANDROID_NDK_ROOT
} elseif ($env:ANDROID_NDK_HOME -and (Test-Path $env:ANDROID_NDK_HOME)) {
    $env:ANDROID_NDK_HOME
} else {
    if (-not $env:ANDROID_SDK_ROOT) { throw "ANDROID_SDK_ROOT is not set" }
    $ndkBase = Join-Path $env:ANDROID_SDK_ROOT "ndk"
    $latest = Get-ChildItem $ndkBase -Directory | Sort-Object Name -Descending | Select-Object -First 1
    if (-not $latest) { throw "Android NDK not found under $ndkBase" }
    $latest.FullName
}

$hostTag = if ($env:OS -eq "Windows_NT") {
    "windows-x86_64"
} else {
    switch ((& uname -s).Trim()) {
        "Linux" { "linux-x86_64" }
        "Darwin" { "darwin-x86_64" }
        default { throw "Unsupported build host" }
    }
}

$clangName = "aarch64-linux-android$ApiLevel-clang"
if ($env:OS -eq "Windows_NT") { $clangName += ".cmd" }
$clang = Join-Path $ndkRoot "toolchains/llvm/prebuilt/$hostTag/bin/$clangName"
if (-not (Test-Path $clang)) { throw "Android clang toolchain not found: $clang" }

New-Item -ItemType Directory -Force -Path $artifactDir | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $Output) | Out-Null

$oldGoos = $env:GOOS
$oldGoarch = $env:GOARCH
$oldCgo = $env:CGO_ENABLED
$oldCc = $env:CC

try {
    $env:GOOS = "android"
    $env:GOARCH = "arm64"
    $env:CGO_ENABLED = "1"
    $env:CC = $clang
    Push-Location $sourceDir
    try {
        & $go.Source build -buildmode=c-shared -trimpath -o $outLib .
        if ($LASTEXITCODE -ne 0) { throw "go build failed with exit code $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
    Copy-Item -Force $outLib $Output
    Write-Host "Native library: $Output"
} finally {
    $env:GOOS = $oldGoos
    $env:GOARCH = $oldGoarch
    $env:CGO_ENABLED = $oldCgo
    $env:CC = $oldCc
}
