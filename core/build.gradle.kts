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
}

tasks.shadowJar {
    archiveFileName = "core-all.jar"

    manifest {
        attributes["Implementation-Title"] = "HexServer Core"
        attributes["Implementation-Version"] = project.version
    }
}