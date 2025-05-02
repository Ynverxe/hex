plugins {
    `kotlin-dsl`
    `hex-publishing-conventions`
    kotlin("jvm") version "2.1.0"
}

dependencies {
    compileOnly("com.gradleup.shadow:shadow-gradle-plugin:9.0.0-beta12")
    compileOnly(project(":launcher"))
    implementation(libs.configurate.hocon)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("launcher-helper") {
            id = "launcher-helper"
            implementationClass = "com.github.ynverxe.hexserver.helper.HexHelperPlugin"
            description = "Generates util tasks to generate and run a launcher"
        }
    }
}