plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val buildNativeAndroid by tasks.registering(org.gradle.api.tasks.Exec::class) {
    val script = rootProject.file("scripts/build-native-android.ps1")
    val nativeDir = rootProject.file("native/tgwsproxy")
    val output = project.file("src/main/jniLibs/arm64-v8a/libtgwsproxy.so")
    val shell = if (System.getProperty("os.name").lowercase().contains("windows")) "powershell" else "pwsh"

    inputs.file(script)
    inputs.files(rootProject.fileTree(nativeDir) {
        include("**/*.go")
        include("go.mod")
        include("go.sum")
    })
    outputs.file(output)

    workingDir = rootProject.projectDir
    commandLine(shell, "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", script.absolutePath, "-Output", output.absolutePath)
}

tasks.named("preBuild") {
    dependsOn(buildNativeAndroid)
}

android {
    namespace = "io.github.regstar2.tgwsproxy.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        ndk {
            abiFilters.add("arm64-v8a")
        }
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/jniLibs")
        }
    }
}

dependencies {
    implementation("net.java.dev.jna:jna:5.14.0@aar")
    testImplementation("junit:junit:4.13.2")
}
