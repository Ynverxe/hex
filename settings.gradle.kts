dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.hypera.dev/snapshots/")
    }
}

pluginManagement {
    includeBuild("hex-plugin")
}

plugins {
    id("com.gradle.develocity") version "3.17.6" // o la versión más reciente
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
    }
}

rootProject.name = "hex-server"

val projects = arrayOf("core", "launcher", "logging", "demo-extension", "lab")

projects.forEach {
    include(":$it")
    project(":$it").name = "hex-$it"
}