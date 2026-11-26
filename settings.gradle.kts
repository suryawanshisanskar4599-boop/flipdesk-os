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
    }
}

rootProject.name = "FlipDeskOS"
include(":app")
include(":core:design")
include(":core:database")
include(":core:permissions")
include(":core:platform")
include(":core:animations")
include(":core:widgets")
include(":features:home")
include(":features:launcher")
include(":features:capsule")
include(":features:music")
include(":features:quicksettings")
include(":features:customization")
include(":features:foldstate")
