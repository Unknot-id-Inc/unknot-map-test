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
        mavenLocal()
        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
        /*maven {
            url = uri("https://gitlab.com/api/v4/groups/locuslabs/-/packages/maven")
            name = "GitLab"
            val gitLabDeployToken: String by settings
            credentials(HttpHeaderCredentials::class) {
                name = "Deploy-Token"
                value = gitLabDeployToken // gitLabDeployToken is a variable defined in gradle.properties, each customer receives their own
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
                //header(HttpHeaderAuthentication)
            }
        }
        maven {
            url = uri("https://jitpack.io")
        }*/
    }
}

rootProject.name = "MapBoxTest"
include(":app")
 