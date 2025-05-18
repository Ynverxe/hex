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
    compileOnly(project(":core"))
    compileOnly(sourceSets["dependency"].output.classesDirs)
    add("dependencyCompileOnly", project(":core"))
}

tasks.create<Jar>("makeDependencyJar") {
    group = "build"
    archiveClassifier.set("dependency")

    from(sourceSets["dependency"].output.classesDirs)
    from(sourceSets["dependency"].resources)
}