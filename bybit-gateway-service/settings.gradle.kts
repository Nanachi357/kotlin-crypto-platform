// Project Settings
rootProject.name = "bybit-gateway-service"

// Plugin Management
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

// Dependency Resolution Management  
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        
        // GitHub Packages (for future internal dependencies)
        // maven {
        //     name = "GitHubPackages"
        //     url = uri("https://maven.pkg.github.com/yourusername/kotlin-crypto-platform")
        //     credentials {
        //         username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        //         password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        //     }
        // }
    }
}

// Enable Gradle feature previews (commented out for Gradle 9.0 compatibility)
// enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// enableFeaturePreview("VERSION_CATALOGS")