plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(project(":core"))

    implementation(libs.tiny.log.impl)
    implementation(libs.tiny.log.slf4j)

    //implementation("org.jetbrains:annotations:24.0.0")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.ynverxe.hexserver.launcher.HexServerLauncher"
    }
}