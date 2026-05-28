import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.no.arg)
    id("com.codingfeline.buildkonfig").version("0.21.2")
}


/**
 * VersionUpdateCheck
 * Environment Area - App Version
 */
val appVersionDesktop = "0.1.0"

/**
 * tasks to gradle.properties
 */
val properties = Properties()
file("../gradle.properties").inputStream().use { properties.load(it) }

val appVersion: String = SimpleDateFormat("yyyy.MM.dd").format(Date())
val versionCodeFinal = properties.getProperty("APP_VERSION_CODE").toInt() + 1

//BETA | C.BETA | DEV | PRODUCTION
//VersionUpdateCheck
val isForAppStore = true
val isForPlayStore = false
var appProfile = "PRODUCTION" //Please Modify this String ONLY IF NECESSERY
val appVersionCodeName = "Anxiety"

if(isForPlayStore) appProfile = "PRODUCTION_GP"

initGradleProperties()

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    jvm()
    val xcf = XCFramework()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "composeApp"
            isStatic = true
            binaryOption("bundleVersion", versionCodeFinal.toString())
            binaryOption("bundleShortVersionString", appVersion)
            xcf.add(this)
        }
    }

    applyDefaultHierarchyTemplate()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
            implementation(libs.haze)
            implementation(libs.navigation.compose)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.composeIcons.feather)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
        }
    }
}

android {
    namespace = "com.voc2048.sparkle_study"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.voc2048.sparkle_study"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    flavorDimensions += "version"
    productFlavors{
        properties["APP_PLATFORM"] = "Android"

        create("0dev"){
            versionName = "DEV ${appVersion} (${versionCodeFinal})"
        }
        create("beta"){
            applicationId = "com.voc2048.sparkle_study.beta"
            versionName = "BETA ${appVersion} (${versionCodeFinal})"
        }
        create("closeBeta"){
            versionName = "C.BETA ${appVersion} (${versionCodeFinal})"
        }
        create("production_googleplay"){
            versionName = "GP ${appVersion} (${versionCodeFinal})"
        }
        create("production"){
            applicationId = "com.voc2048.sparkle_study"
            versionName = "${appVersion} (${versionCodeFinal})"
        }

        properties.store(file("../gradle.properties").outputStream(),null)
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    bundle {
        language {
            enableSplit = false
        }
    }

    applicationVariants.all {
        outputs.all {
            if (this is ApkVariantOutputImpl) {
                val suffix = outputFileName.split(".").last()
                outputFileName = "SparkleStudy-${appProfile}-${appVersion} (${versionCodeFinal}).$suffix"
            }
        }
    }
}

val macExtraPlistKeys: String
    get() = """
      <key>ITSAppUsesNonExemptEncryption</key>
      <false/>
    """.trimIndent()

/**
 * As far as not planned to support PC/Mac Version rn
 * It may not compile successfully...
 */
compose.desktop {
    application {
        mainClass = "com.voc2048.sparkle_study.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe, TargetFormat.Pkg)
            packageName = "Sparkle Study${if(appProfile.contains("PRODUCTION")) "" else " ($appProfile)"}"
            packageVersion = appVersionDesktop
            copyright = "Copyright © 2026 Vocaloid2048 版權所有"
            description = "Sparkle Study is a multiplatform app developed by Vocaloid2048."
            vendor = "Vocaloid2048"

            val betaIconSuffix = if(
                when(appProfile){
                    "BETA" -> true
                    "C.BETA" -> true
                    "DEV" -> true
                    else -> false
                }
            ) { "_beta" } else { "" }

            linux {
                iconFile.set(project.file("icon/app_icon${betaIconSuffix}.png"))
                shortcut = true
            }
            windows {
                iconFile.set(project.file("icon/app_icon${betaIconSuffix}.ico"))
                shortcut = true
                msiPackageVersion = "1.0.$versionCodeFinal"
                menu = true
                dirChooser = true
            }
            macOS{
                packageName = "Sparkle Study"
                iconFile.set(project.file("icon/app_icon${betaIconSuffix}.icns"))
                packageBuildVersion = versionCodeFinal.toString()
                //ref : https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md#configuring-gradle
                bundleID = "com.voc2048.sparkle_study"
                minimumSystemVersion = "12.0"
                infoPlist{
                    extraKeysRawXml = macExtraPlistKeys
                }
                signing {
                    appStore = isForAppStore //https://youtrack.jetbrains.com/issue/CMP-4272
                    sign.set(isForAppStore) //https://github.com/electron/notarize/issues/120#issuecomment-1605886244
                    identity.set("Chun Man Tsang")
                }

                if(isForAppStore){
                    //ref : https://youtrack.jetbrains.com/issue/CMP-2096
                    //Please don't modify the profile name to other custom name, it will send u a jpackage error :)
                    /**
                     * To Updating certificate provisioning profile, please follow the steps below:
                     * 1. Generate a new CSR file using Keychain Access on your Mac.
                     * 2. Log in to your Apple Developer account and navigate to the "Certificates, Identifiers & Profiles" section.
                     * 3. Navigate to the "SG3 Mac App Provisioning Profile" -> Edit -> Select the Mac App Distribution -> Upload CSR. -> Download
                     * 4. Navigate to the "SG3 Mac App JVM Provisioning Profile" -> Edit -> Select the Mac App Distribution -> Upload CSR. (Probably no need) -> Download
                     *
                     * Rename SG3 Mac App Provisioning Profile to embedded.provisionprofile
                     * Rename SG3 Mac App JVM Provisioning Profile to runtime.provisionprofile
                     * Then put them in composeApp/stores/ folder.
                     */
                    provisioningProfile.set(project.file("stores/embedded.provisionprofile"))
                    runtimeProvisioningProfile.set(project.file("stores/runtime.provisionprofile"))
                    entitlementsFile.set(project.file("stores/entitlements.plist"))
                    runtimeEntitlementsFile.set(project.file("stores/runtime-entitlements.plist"))
                }
            }
        }

        buildTypes.release.proguard {
            isEnabled = false
            version.set("7.5.0")
        }

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs(
            "--add-opens",
            "java.desktop/java.awt.peer=ALL-UNNAMED"
        ) // recommended but not necessary

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}


compose.resources {
    publicResClass = true
    packageOfResClass = "files"
    generateResClass = always
}

// Compose Hot Reload
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}



buildkonfig {
    packageName = "com.voc2048.sparkle_study"
    //Read only
    defaultConfigs {
        buildConfigField(STRING, "appProfile", appProfile)
        buildConfigField(STRING, "appVersionName", appVersion)
        buildConfigField(STRING, "appVersionCodeName", appVersionCodeName)
        buildConfigField(INT, "appVersionCode", properties.getProperty("APP_VERSION_CODE"))
    }
}

fun initGradleProperties(){
    //Write only
    properties["APP_PROFILE"] = appProfile
    properties["APP_VERSION"] = appVersion
    properties["APP_VERSION_CODENAME"] = appVersionCodeName
    properties["APP_VERSION_DESKTOP"] = appVersionDesktop
    properties["APP_VERSION_CODE"] = versionCodeFinal.toString()
    properties.store(file("../gradle.properties").outputStream(),null)
}

