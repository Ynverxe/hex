plugins {
    id("java")
}

repositories {
    mavenCentral()
}

sourceSets {
    create("dependency") {
        java.srcDirs("src/dependency/java")
        resources.srcDirs("src/dependency/resources")
    }
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(sourceSets["dependency"].output)
    add("dependencyCompileOnly", project(":core"))
}

tasks.replace("processDependencyResources", ProcessResources::class.java).apply {
    enabled = false
}

tasks.create<ProcessResources>("dependencyResources").apply {
    group = "build"
}

tasks.create<Jar>("makeDependencyJar") {
    dependsOn("dependencyResources")

    group = "build"
    archiveClassifier.set("dependency")

    from(sourceSets["dependency"].output.classesDirs)
    from(sourceSets["dependency"].output.resourcesDir)
}