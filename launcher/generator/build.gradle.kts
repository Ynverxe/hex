plugins {
    `kotlin-dsl`
    `hex-publishing-conventions`
    kotlin("jvm") version "2.1.0"
}

dependencies {
    compileOnly("com.gradleup.shadow:shadow-gradle-plugin:9.0.0-beta12")
    compileOnly(project(":launcher"))
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("launcher-generator") {
            id = "launcher-generator"
            implementationClass = "com.github.ynverxe.hexserver.LauncherGeneratorPlugin"
            description = "Generates a hex-server launcher with custom packaged resources"
        }
    }
}