plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystoreFilePath = System.getenv("KEYSTORE_FILE") ?: "release.jks"
val keystoreFile = file(keystoreFilePath)
val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
val keyAlias = System.getenv("KEY_ALIAS")
val keyPassword = System.getenv("KEY_PASSWORD")

android {
    namespace = "com.radiostic.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.radiostic.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (keystoreFile.exists() && !keystorePassword.isNullOrBlank() && !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
            create("release") {
                storeFile = keystoreFile
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val hasReleaseSigning = signingConfigs.findByName("release") != null
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INDEX"
            excludes += "/META-NOTICES"
        }
    }
}

dependencies {
    implementation("com.google.guava:guava:32.1.3-android")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation("androidx.media:media:1.7.0")
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.media3.exoplayer)

    debugImplementation(libs.androidx.ui.tooling)
}
