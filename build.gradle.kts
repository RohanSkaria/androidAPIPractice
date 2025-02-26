// Top-level build.gradle.kts
// Usually located in the root of your project.

plugins {
    // If you're using a version catalog for the Android application plugin:
    alias(libs.plugins.android.application) apply false

    // Add the Google Services Gradle plugin (so we can apply it in the app module)
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// If you need repositories here, you can add them too (depending on your setup):
// pluginManagement {
//     repositories {
//         gradlePluginPortal()
//         google()
//         mavenCentral()
//     }
// }
// or in your `settings.gradle.kts`.
