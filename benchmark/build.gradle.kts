// Macrobenchmark module for the Aether app — measures startup, frame timing, etc.
// Added on feature/poc-test-instrumentation for the native-frameworks POC.
//
// Run with:
//   ./gradlew :benchmark:connectedBenchmarkAndroidTest
//
// Reports land at benchmark/build/outputs/connected_android_test_additional_output/.

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.zhousl.aether.benchmark"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        // Macrobenchmark uses the standard JUnit runner; the
        // androidx.benchmark gradle plugin (and its custom runner) is only
        // for Microbenchmark on com.android.library modules.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        // benchmark module only emits a test APK; the "release" buildType here
        // mirrors the app's release config so Macrobenchmark can target it.
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
