plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    compileOnly(project(":core"))
    implementation(libs.luckperms)
}