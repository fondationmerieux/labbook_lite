plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)  // for Room
}

android {
    namespace = "org.fondationmerieux.labbooklite"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.fondationmerieux.labbooklite"
        minSdk = 29
        targetSdk = 35
        versionCode = 5
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
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
        freeCompilerArgs += "-Xcontext-receivers"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    // Compose BOM (must be declared first to manage Compose versions)
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewBinding interop (if needed)
    implementation(libs.androidx.ui.viewbinding)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Core / AppCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Activity Compose
    implementation(libs.androidx.activity.compose)

    // ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    // Room (SQLite management)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // SQLCipher (for encrypted SQLite)
    implementation(libs.android.database.sqlcipher)
    implementation(libs.androidx.sqlite.ktx)

    // Security (Keystore / EncryptedSharedPreferences)
    implementation(libs.androidx.security.crypto)

    // Networking (OkHttp)
    implementation(libs.okhttp)

    // JSON (Moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Testing (Unit + Instrumented)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    implementation(libs.androidx.foundation)

    implementation(libs.itext7.kernel)
    implementation(libs.itext7.layout)
    implementation(libs.itext7.bouncycastle.adapter)
}
