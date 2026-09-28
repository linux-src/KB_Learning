import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

// CoinGecko Demo API key: read from local.properties (gitignored, never committed) or
// an env var. Register for a free Demo key at https://www.coingecko.com/en/api/pricing
// and set coinGecko.apiKey in local.properties, or set COINGECKO_API_KEY env var.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val coinGeckoApiKey: String = localProperties.getProperty("coinGecko.apiKey")
    ?: System.getenv("COINGECKO_API_KEY")
    ?: ""

val generatedApiKeysDir = layout.buildDirectory.dir("generated/apiKeys/commonMain/kotlin")

val generateApiKeys by tasks.registering {
    val outputDir = generatedApiKeysDir
    // Without this, Gradle has no idea the generated file depends on
    // local.properties/the env var, so it happily serves a stale, cached
    // ApiKeys.kt after either one changes — which is exactly how an updated
    // key silently kept building against the old (dead) one.
    inputs.property("coinGeckoApiKey", coinGeckoApiKey)
    outputs.dir(outputDir)
    doLast {
        val packageDir = outputDir.get().asFile.resolve("dev/kbwallet/app/core/network")
        packageDir.mkdirs()
        packageDir.resolve("ApiKeys.kt").writeText(
            """
            |package dev.kbwallet.app.core.network
            |
            |internal object ApiKeys {
            |    const val COIN_GECKO: String = "$coinGeckoApiKey"
            |}
            """.trimMargin()
        )
    }
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.android)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.biometric)
        }
        commonMain {
            kotlin.srcDir(files(generatedApiKeysDir).builtBy(generateApiKeys))
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.kotlin.date.time)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.bundles.ktor)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation)

            implementation(libs.coil.compose.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.core)
            implementation(libs.coil.svg)
            implementation(libs.coil.network.ktor)
        }
        iosMain.dependencies {
            implementation(libs.ktor.ios)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.cio)
            // Supplies Dispatchers.Main for Compose Desktop (AWT/Swing event loop) —
            // Android gets this from kotlinx-coroutines-android, iOS from the native
            // main dispatcher; the JVM target needs it declared explicitly.
            implementation(libs.kotlinx.coroutines.swing)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlin.test)
            implementation(libs.test.turbine)
            implementation(libs.test.assertk)

            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
    }
}


// Release signing: never commit a keystore or its passwords. Copy
// keystore.properties.example to keystore.properties (gitignored), fill in
// your own values, and point storeFile at a .jks kept outside the repo.
// Without keystore.properties, the release build type is simply unsigned —
// it still builds locally, it just can't be published as-is.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}

android {
    namespace = "dev.kbwallet.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.kbwallet.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "2.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Name the output APK "KB Learning.apk"
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "KB Learning.apk"
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.desktop {
    application {
        mainClass = "dev.kbwallet.app.MainKt"

        // The bundled ProGuard (7.2.2, via the Compose Gradle plugin) can't parse
        // class files newer than Java 18 — it throws
        // `UnsupportedOperationException: Unsupported version number [65.0]` the
        // moment it opens a Java 21 class on the library/runtime classpath, which
        // packageReleaseDeb/packageReleaseDmg pull in on any JDK 21 toolchain.
        // Minification isn't load-bearing for a desktop app like this one, so
        // disabling it trades a slightly larger distributable for release
        // packaging that actually completes on JDK 21 (matches what both this
        // machine and most current CI runners default to).
        buildTypes.release.proguard {
            isEnabled.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "KB Wallet"
            packageVersion = "2.0.0"
            description = "KB Wallet — crypto portfolio & trading simulator"
            vendor = "KB Wallet"

            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspDesktop", libs.room.compiler)
    debugImplementation(compose.uiTooling)
}