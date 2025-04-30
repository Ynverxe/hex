import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
    `hex-publishing-conventions`
}

dependencies {
    // logging
    implementation(libs.adventure.slf4j)
    implementation(project(":logging"))

    // local and remote repository handling
    implementation(libs.bundles.maven)

    // configuration files
    implementation(libs.configurate.json)

    // annotations
    compileOnly(libs.jetbrains.annotations)

    // testing
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

task<ShadowJar>("generateLauncher") {
    dependsOn(":core:shadowJar")
    configurations = project.configurations.runtimeClasspath.map { listOf(it) }
    from(sourceSets.main.get().output)

    doFirst {
        val outputDir = System.getProperty("hex.generator.output.dir") ?: return@doFirst
        val extraResourcesDir = System.getProperty("hex.generator.input.extra") ?: return@doFirst

        val outputDirFile = File(outputDir)
        println(outputDirFile)
        if (!outputDirFile.exists()) {
            assert(outputDirFile.mkdirs()) { "Cannot create $outputDir" }
        }

        val extraResourcesDirFile = File(extraResourcesDir)
        println(extraResourcesDirFile)
        assert(extraResourcesDirFile.exists()) { "$extraResourcesDir doesn't exists" }

        val filename = System.getProperty("hex.generator.output.filename")
        if (filename != null) {
            archiveFileName.set(filename)
        }

        destinationDirectory.set(outputDirFile)

        // Include core-all.jar inside launcher's fatJar
        val coreFatJar = project.project(":core")
            .tasks
            .named<Jar>("shadowJar")
            .get()
            .archiveFile
            .get()

        from(coreFatJar)
        from(extraResourcesDirFile)

        mergeServiceFiles()
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.ynverxe.hexserver.launcher.HexServerLauncher"
        attributes["Implementation-Title"] = "HexServer Launcher"
        attributes["Implementation-Version"] = project.version
    }
}