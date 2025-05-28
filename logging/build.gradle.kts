plugins {
    id("java-library")
    id("hex-publishing-conventions")
}

project.description = "Abstract logging logic for hex-server"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.adventure.slf4j)
    implementation(libs.adventure.ansi)
    implementation(libs.bundles.logging)
}