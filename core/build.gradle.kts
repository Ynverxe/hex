plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.0"
    id("hex-publishing-conventions")
}

dependencies {
    api(libs.configurate.helper)
    api(libs.configurate.hocon)
    compileOnly(libs.minestom)

    // logging
    implementation(project(":logging"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation(libs.minestom)
}

tasks.shadowJar {
    archiveFileName = "core-all.jar"

    manifest {
        attributes["Implementation-Title"] = "HexServer Core"
        attributes["Implementation-Version"] = project.version
    }
}

configurations {
    testImplementation {
        extendsFrom(configurations.implementation.get())
    }
    testRuntimeOnly {
        extendsFrom(configurations.runtimeOnly.get())
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }

    val demoExtensionJarTask = rootProject.project(":demo-extension").tasks.named<Jar>("jar")
    dependsOn(demoExtensionJarTask)

    val demoExtensionJar = demoExtensionJarTask
        .get()
        .archiveFile
        .get()
        .asFile
        .absolutePath

    val demoExtensionDependencyJarTask = rootProject.project(":demo-extension").tasks.named<Jar>("makeDependencyJar")
    dependsOn(demoExtensionDependencyJarTask)

    val demoDependencyExtensionJar = demoExtensionDependencyJarTask
        .get()
        .archiveFile
        .get()
        .asFile
        .absolutePath

    val arguments = "-add-extension=\"${demoExtensionJar}\" -add-extension=\"${demoDependencyExtensionJar}\""
    jvmArgs("-Dserver-arguments=$arguments")
}