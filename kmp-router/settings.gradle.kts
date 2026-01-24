pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven {
            url = uri("https://mvn.cloud.alipay.com/nexus/content/repositories/open/")
            name = "alipay"
        }
        maven {
            credentials {
                username = "admin"
                password = "**"
            }
            isAllowInsecureProtocol = true
            url = uri("http://mvn.dev.alipay.net:8081/artifactory/content/groups/alipay-mobile")
        }
        maven {
            credentials {
                username = "admin"
                password = "**"
            }
            isAllowInsecureProtocol = true
            url = uri("http://mvn.test.alipay.net/artifactory/content/groups/alipay-mobile")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven {
            url = uri("https://mvn.cloud.alipay.com/nexus/content/repositories/open/")
            name = "alipay"
        }
        maven {
            isAllowInsecureProtocol = true
            url = uri("http://mvn.test.alipay.net/artifactory/content/repositories/mobile/")
        }
        maven {
            credentials {
                username = "admin"
                password = "**"
            }
            isAllowInsecureProtocol = true
            url = uri("http://mvn.dev.alipay.net:8081/artifactory/content/groups/alipay-mobile")
        }
        maven {
            credentials {
                username = "admin"
                password = "**"
            }
            isAllowInsecureProtocol = true
            url = uri("http://mvn.test.alipay.net/artifactory/content/groups/alipay-mobile")
        }
    }
}

rootProject.name = "kmp-router"