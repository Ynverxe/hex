plugins {
    id("java")
}

dependencies {
    compileOnly(project(":core"))
    implementation(project(":util"))
}