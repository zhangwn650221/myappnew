// 文件路径: <项目根目录>/app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.ksp)
}

// It's generally better to place imports at the very top of the file,
// but for Gradle Kotlin DSL, after plugins {} and before other blocks is common.
import java.util.Properties

// Properties loading should be at a level where it can be accessed by android {} block
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    try {
        localPropertiesFile.inputStream().use { input ->
            localProperties.load(input)
        }
    } catch (e: java.io.IOException) {
        logger.warn("Warning: Could not load local.properties file: ${e.message}")
    }
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.example.myappnew" // 确保这里的包名和您项目的一致
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.myappnew" // 确保这里的包名和您项目的一致
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Expose API keys from local.properties as BuildConfig fields
        // Default to an empty string if not found
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${localProperties.getProperty("DEEPSEEK_API_KEY", "")}\"")
        // Keep the old LLM_API_KEY for now, can be removed later if fully migrated
        buildConfigField("String", "LLM_API_KEY", "\"${localProperties.getProperty("LLM_API_KEY", localProperties.getProperty("GEMINI_API_KEY", ""))}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Explicitly define for release. defaultConfig handles debug.
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
            buildConfigField("String", "DEEPSEEK_API_KEY", "\"${localProperties.getProperty("DEEPSEEK_API_KEY", "")}\"")
            buildConfigField("String", "LLM_API_KEY", "\"${localProperties.getProperty("LLM_API_KEY", localProperties.getProperty("GEMINI_API_KEY", ""))}\"")
        }
        debug {
            // Inherits buildConfigFields from defaultConfig.
            // If you needed different keys for debug, you would define them here.
            // For example:
            // buildConfigField("String", "GEMINI_API_KEY", "\"debug_gemini_key\"")
        }
    }
    compileOptions {
    }
    kotlinOptions {
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
     implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.mock)
    // implementation(libs.openai.kotlin.client) // Commented out as we are using Gemini
    implementation(libs.google.ai.client)

}
