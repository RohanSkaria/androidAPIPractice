plugins {
    // Apply the Android application plugin (from your version catalog)
    alias(libs.plugins.android.application)

    // Apply the Google Services plugin to this module
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.northeastern.myapplication"  // adjust if needed
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.northeastern.myapplication" // adjust if needed
        minSdk = 27
        targetSdk = 34
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
    // Your existing dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Use the Firebase BoM for version alignment
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")

    // Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx")
}
