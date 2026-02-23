import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(npm("@js-joda/core", "5.6.1"))
            implementation(npm("@js-joda/timezone", "2.18.2"))
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(npm("@js-joda/core", "5.6.1"))
            implementation(npm("@js-joda/timezone", "2.18.2"))
        }
    }
}


// Copy temperatures.json into webpack dev server directories for local testing
tasks.register<Copy>("copyTemperaturesForDev") {
    from(rootProject.file("dist/temperatures.json"))
    into(layout.buildDirectory.dir("kotlin-webpack/wasmJs/developmentExecutable"))
}

tasks.register<Copy>("copyTemperaturesForJsDev") {
    from(rootProject.file("dist/temperatures.json"))
    into(layout.buildDirectory.dir("kotlin-webpack/js/developmentExecutable"))
}

tasks.configureEach {
    if (name == "wasmJsBrowserDevelopmentRun") {
        dependsOn("copyTemperaturesForDev")
    }
    if (name == "jsBrowserDevelopmentRun") {
        dependsOn("copyTemperaturesForJsDev")
    }
}

compose.desktop {
    application {
        mainClass = "edu.emailman.us_temperatures.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "edu.emailman.us_temperatures"
            packageVersion = "1.0.0"
        }
    }
}
