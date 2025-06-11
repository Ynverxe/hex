plugins {
    `kotlin-dsl`
    `maven-publish`
    kotlin("jvm") version "2.1.0"
}

group = "io.github.ynverxe"
version = "1.0.0-indev"

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
        create("hex-plugin") {
            id = "hex-plugin"
            implementationClass = "io.github.ynverxe.hexserver.plugin.HexPlugin"
            description = "Gradle plugin which implements utility task for hex-server"
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
