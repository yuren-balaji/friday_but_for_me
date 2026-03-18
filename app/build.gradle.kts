plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("io.objectbox")
}

android {
    namespace = "com.example.first_app_0_0_1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.first_app_0_0_1"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// ObjectBox plugin adds objectbox-android to implementation by default.
// This causes duplicate class issues when also using the objectbrowser.
configurations.all {
    exclude(group = "io.objectbox", module = "objectbox-android")
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))

    // Core Android & Kotlin
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Compose UI & Navigation
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.3.1")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // WorkManager for background automations
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room Persistence Library
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // ObjectBox Vector Search
    // We manually add the correct dependency for each build type to avoid duplication.
    debugImplementation("io.objectbox:objectbox-android-objectbrowser:4.0.3")
    releaseImplementation("io.objectbox:objectbox-android:4.0.3")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.3")
    implementation("org.tensorflow:tensorflow-lite-task-audio:0.4.3")

    // JSON Serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // Encryption for external storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
