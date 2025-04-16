plugins {
    id("java")
}

group = "com.github.ynverxe"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":core"))
}