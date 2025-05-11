plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    api(libs.configurate.helper)
    api(libs.configurate.hocon)
    api(libs.minestom.extensions)
    compileOnlyApi(libs.minestom)

    // logging
    implementation(project(":logging"))

    runtimeOnly(kotlin("stdlib", "1.5.0"))
    runtimeOnly("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-depchain:3.1.4")

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
}