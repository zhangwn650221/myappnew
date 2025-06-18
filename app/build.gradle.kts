plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // Added kotlin-android plugin
    alias(libs.plugins.kotlinKapt) // Updated to new alias name
}

android {
    namespace = "com.example.myappnew"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myappnew"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp
    implementation(libs.okhttp)

    // Gson
    implementation(libs.gson)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Room
    implementation(libs.room.runtime) // Corrected alias
    kapt(libs.room.compiler)      // Corrected alias
    implementation(libs.room.ktx)       // Corrected alias
}