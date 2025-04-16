plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(project(":core"))

    implementation(libs.configurate.json)
    //implementation("org.jetbrains:annotations:24.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.ynverxe.hexserver.launcher.HexServerLauncher"
    }
}