// 文件路径: <项目根目录>/app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.hilt)
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
        }
        debug {
            // Inherits buildConfigFields from defaultConfig by default.
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

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Network
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.mock)

    // AI/LLM
    implementation(libs.google.ai.client)

    // Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx) // For by viewModels()

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Guava - Consider removing if not essential
    implementation(libs.guava)

    // RxJava3
    implementation(libs.rxjava3.rxjava)
    implementation(libs.rxjava3.rxandroid)

    // JavaPoet (explicitly added to resolve potential Hilt/KSP conflicts)
    implementation(libs.javapoet)
}
