plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.adventure.slf4j)
    implementation(libs.adventure.ansi)
    implementation(libs.bundles.logging)
}