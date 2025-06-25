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

        // API keys from local.properties (ensure these are without quotes in local.properties)
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY", "")
        val deepSeekApiKey = localProperties.getProperty("DEEPSEEK_API_KEY", "")
        // Fallback for LLM_API_KEY to geminiApiKey if LLM_API_KEY itself is not in local.properties
        val llmApiKeyDefault = localProperties.getProperty("LLM_API_KEY", geminiApiKey)

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepSeekApiKey\"")
        buildConfigField("String", "LLM_API_KEY", "\"$llmApiKeyDefault\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Re-apply for release or ensure they are correctly inherited if values differ
            // For simplicity, if release uses the same keys as debug/defaultConfig,
            // these re-declarations might not be strictly necessary if already in defaultConfig.
            // However, to be explicit for release:
            val geminiApiKeyRelease = localProperties.getProperty("GEMINI_API_KEY", "")
            val deepSeekApiKeyRelease = localProperties.getProperty("DEEPSEEK_API_KEY", "")
            val llmApiKeyReleaseDefault = localProperties.getProperty("LLM_API_KEY", geminiApiKeyRelease)

            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKeyRelease\"")
            buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepSeekApiKeyRelease\"")
            buildConfigField("String", "LLM_API_KEY", "\"$llmApiKeyReleaseDefault\"")
        }
        debug {
            // Inherits buildConfigFields from defaultConfig by default.
            // If you needed different keys for debug, you would define/override them here, e.g.:
            // val geminiApiKeyDebug = localProperties.getProperty("DEBUG_GEMINI_API_KEY", "")
            // buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKeyDebug\"")
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
    implementation(libs.guava) // Added Guava dependency
    implementation(libs.androidx.security.crypto)

}
