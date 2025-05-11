dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.hypera.dev/snapshots/")
    }
}

pluginManagement {
    includeBuild("launcher-plugin")
}

rootProject.name = "hex-server"

include("core")

includeBuild("../configurate-helper") {
    dependencySubstitution {
        substitute(module("com.github.ynverxe:configurate-helper"))
            .using(project(":"))
    }
}

includeBuild("../Minestom") {
    dependencySubstitution {
        substitute(module("net.minestom:Minestom"))
            .using(project(":"))
    }
}
include("launcher")
include("logging")