plugins {
    id("java-library")
}

dependencies {
    api(libs.bundles.core.base)
    compileOnly(libs.luckperms)
    api(libs.minestom.extensions)
    implementation(kotlin("stdlib", "1.5.0"))
    implementation("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-depchain:3.1.4")
}