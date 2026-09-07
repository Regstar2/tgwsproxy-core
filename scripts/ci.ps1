[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Push-Location (Join-Path $root "native/tgwsproxy")
try {
    & go test ./...
    if ($LASTEXITCODE -ne 0) { throw "Go tests failed" }
} finally {
    Pop-Location
}

$gradle = Get-Command gradle -ErrorAction Stop
& $gradle.Source :core:testDebugUnitTest :core:assembleDebug
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed" }

$aar = Join-Path $root "core/build/outputs/aar/core-debug.aar"
if (-not (Test-Path $aar)) { throw "AAR was not produced: $aar" }
Write-Host "AAR: $aar"
