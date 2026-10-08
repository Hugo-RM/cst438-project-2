// Fix AGP AndroidLocationsException when both ANDROID_PREFS_ROOT and ANDROID_USER_HOME env vars exist
try {
    val processEnv = Class.forName("java.lang.ProcessEnvironment")
    val theEnvironmentField = processEnv.getDeclaredField("theEnvironment").apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST")
    (theEnvironmentField.get(null) as? MutableMap<String, String>)?.remove("ANDROID_PREFS_ROOT")

    val theCaseInsensitiveField = processEnv.getDeclaredField("theCaseInsensitiveEnvironment").apply { isAccessible = true }
    @Suppress("UNCHECKED_CAST")
    (theCaseInsensitiveField.get(null) as? MutableMap<String, String>)?.remove("ANDROID_PREFS_ROOT")
} catch (_: Exception) {
}

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

rootProject.name = "cst438-project-2"
include(":app")
