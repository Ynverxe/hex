dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.hypera.dev/snapshots/")
    }
}

rootProject.name = "hex-server"

include("core")
include("world-manager")

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
include("luckperms-as-extension")
include("launcher")
include("util")
include("logging")
include("launcher:generator")
findProject(":launcher:generator")?.name = "generator"
