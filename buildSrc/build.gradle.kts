plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("io.deepmedia.tools.deployer:io.deepmedia.tools.deployer.gradle.plugin:0.18.0")
}