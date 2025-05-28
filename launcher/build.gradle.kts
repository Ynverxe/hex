plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("hex-publishing-conventions")
}

dependencies {
    compileOnly(libs.minestom)

    // logging
    implementation(libs.adventure.slf4j)
    implementation(project(":logging"))

    // local and remote repository handling
    implementation(libs.bundles.maven)

    // configuration files
    implementation(libs.configurate.gson)
    implementation(libs.configurate.hocon)

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

tasks.shadowJar {
    dependsOn(":core:shadowJar")
    archiveFileName = "launcher-all.jar"

    // Include core-all.jar inside launcher's fatJar
    val coreFatJar = project.project(":core")
        .tasks
        .named<Jar>("shadowJar")
        .get()
        .archiveFile
        .get()

    from(coreFatJar)

    mergeServiceFiles()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.ynverxe.hexserver.launcher.HexServerLauncher"
        attributes["Implementation-Title"] = "HexServer Launcher"
        attributes["Implementation-Version"] = project.version
    }
}