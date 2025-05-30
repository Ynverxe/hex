plugins {
    `kotlin-dsl`
    `maven-publish`
    kotlin("jvm") version "2.1.0"
}

group = "io.github.ynverxe"
version = "0.1.0-indev"

dependencies {
    compileOnly("com.gradleup.shadow:shadow-gradle-plugin:9.0.0-beta12")
    implementation("org.spongepowered:configurate-hocon:4.0.0")
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            version = project.version.toString()
            groupId = project.group.toString()
            artifactId = project.name
        }
    }
}