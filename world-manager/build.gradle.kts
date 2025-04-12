plugins {
    id("java")
}

dependencies {
    implementation(libs.bundles.core.base)
    implementation(project(":core"))
}