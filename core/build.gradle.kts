plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    api(libs.bundles.core.base)
    api(libs.minestom.extensions)
    compileOnlyApi(libs.minestom)

    // logging
    implementation(libs.adventure.ansi)
    implementation(libs.tiny.log.impl)
    implementation(libs.tiny.log.slf4j)

    runtimeOnly(kotlin("stdlib", "1.5.0"))
    runtimeOnly("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-depchain:3.1.4")
}

tasks.shadowJar {
    archiveFileName = "core-all.jar"
}