plugins {
    id("java")
}

repositories {
    mavenCentral()
}

sourceSets {
    create("dependency")
}

dependencies {
    compileOnly(libs.minestom)
    compileOnly(project(":hex-core"))
    compileOnly(sourceSets["dependency"].output.classesDirs)

    add("dependencyCompileOnly", project(":hex-core"))
    add("dependencyCompileOnly", libs.minestom)
}

tasks.create<Jar>("makeDependencyJar") {
    group = "build"
    archiveClassifier.set("dependency")

    from(sourceSets["dependency"].output.classesDirs)
    from(sourceSets["dependency"].resources)
}