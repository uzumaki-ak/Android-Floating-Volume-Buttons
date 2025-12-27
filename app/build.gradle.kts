// App-level build configuration file
// This file defines how your app is compiled, packaged, and what libraries it uses

plugins {
    id("com.android.application")  // Makes this an Android app module
    id("org.jetbrains.kotlin.android")  // Enables Kotlin for Android
}

android {
    namespace = "com.volumebuttonfix"  // Your app's unique package name
    compileSdk = 34  // Android SDK version used to compile the app (Android 14)

    defaultConfig {
        applicationId = "com.volumebuttonfix"  // Unique identifier for your app on devices and Play Store
        minSdk = 26  // Minimum Android version supported (Android 8.0 Oreo)
        targetSdk = 34  // Target Android version (latest features, Android 14)
        versionCode = 1  // Internal version number (increment for each release)
        versionName = "1.0"  // Version name shown to users

        // Test runner configuration (not needed for this project but required by build system)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Release build configuration (for production APK)
        release {
            isMinifyEnabled = false  // Disable code shrinking for simplicity
            // ProGuard rules would go here if we enabled minification
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // Debug build is configured automatically
    }

    compileOptions {
        // Use Java 17 features
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // Configure Kotlin compiler to target Java 17 bytecode
        jvmTarget = "17"
    }

    buildFeatures {
        // Enable View Binding for easier view access (optional, but recommended)
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core Library - Essential Android components
    implementation("androidx.core:core-ktx:1.12.0")

    // AppCompat - Backward compatibility for newer Android features
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material Design Components - Modern UI components
    implementation("com.google.android.material:material:1.11.0")

    // ConstraintLayout - Flexible layout system
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle components - For managing app lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")

    // Activity KTX - Kotlin extensions for activities
    implementation("androidx.activity:activity-ktx:1.8.2")

    // Preferences - For settings screen
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Coroutines - For asynchronous programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing libraries (optional, for future unit tests)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}