import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.tiny.log.impl)
    implementation(libs.tiny.log.slf4j)
    implementation(libs.adventure.ansi)
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

tasks.named<ShadowJar>("shadowJar") {
    exclude("META-INF/services/net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider")

    mergeServiceFiles {
    }

    val serviceFile = file("src/main/resources/META-INF/services/net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider")
    val tmpDir = temporaryDir.resolve("META-INF/services")
    tmpDir.mkdirs()
    Files.copy(serviceFile.toPath(), tmpDir.resolve("net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider").toPath(), StandardCopyOption.REPLACE_EXISTING)
    from(tmpDir) {
        into("META-INF/services")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.ynverxe.hexserver.launcher.HexServerLauncher"
    }
}