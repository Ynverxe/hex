import io.github.ynverxe.hexserver.plugin.file.DefaultConfiguration

plugins {
    id("java")
    id("hex-plugin")
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.minestom)
    compileOnly(project(":hex-core"))
}

tasks.generateLauncher {
    overrideJarTask(":launcher:shadowJar")
    serverFiles {
        handleConfiguration(DefaultConfiguration.MINESTOM_SOURCE) {
            val minestom = libs.minestom.asProvider()
                .get()

            coordinates(minestom.group + ":" + minestom.name + ":" + minestom.version)
            mavenCentral()
        }

        handleConfiguration(DefaultConfiguration.SERVER_CONFIGURATION) {
            brandName("laboratory")
        }
    }
}

tasks.runHexServer {
    workingDir("run")
    serverJarProviderTask("generateLauncher")
    addExtensionFromTask("shadowJar")
}