// This file defines the project structure and module configuration
// It tells Gradle which repositories to use for downloading dependencies

pluginManagement {
    // Define where to look for Gradle plugins
    repositories {
        google()        // Google's Maven repository (for Android libraries)
        mavenCentral()  // Maven Central repository (for general Java/Kotlin libraries)
        gradlePluginPortal()  // Official Gradle plugin repository
    }
}

dependencyResolutionManagement {
    // Use the same repositories for all project dependencies
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()        // Google's Android libraries
        mavenCentral()  // Standard Java/Kotlin libraries
    }
}

// Set the root project name
rootProject.name = "VolumeButtonFix"

// Include the app module in this project
// The app module contains all our application code
include(":app")