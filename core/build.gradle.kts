plugins {
    id("java-library")
}

dependencies {
    api(libs.bundles.core.base)
    api(libs.minestom.extensions)

    // logging
    implementation(libs.adventure.ansi)
    implementation(libs.tiny.log.impl)
    implementation(libs.tiny.log.slf4j)

    implementation(kotlin("stdlib", "1.5.0"))
    implementation("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-depchain:3.1.4")
}