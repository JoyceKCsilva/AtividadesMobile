import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    val xcf = XCFramework()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.sqldelight.runtime)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.activity.compose)
            implementation(libs.core.ktx)
            implementation(libs.sqldelight.android)
        }
        nativeMain.dependencies {
            implementation(libs.sqldelight.native)
        }
    }
}

android {
    namespace = "com.example.todo"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.example.todo"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidCompileSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

compose.resources {
    publicResClass = true
}

sqldelight {
    databases {
        create("TodoDatabase") {
            packageName.set("com.example.todo.db")
        }
    }
}
