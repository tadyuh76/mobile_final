pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials.username = "mapbox"
            // The password should be your Mapbox secret token
            // Set MAPBOX_DOWNLOADS_TOKEN in gradle.properties or environment
            credentials.password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN")
                .orElse(providers.environmentVariable("MAPBOX_DOWNLOADS_TOKEN"))
                .orElse("")
                .get()
            authentication.create<BasicAuthentication>("basic")
        }
    }
}

rootProject.name = "mobile_final"
include(":app")
