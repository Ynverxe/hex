plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(project(":core"))
    //implementation(project(":world-manager"))

    implementation(libs.bundles.core.base)
    implementation(libs.tiny.log.impl)
    implementation(libs.tiny.log.slf4j)
}

tasks {
    processResources {
        mustRunAfter(":core:processResources")
    }
}

sourceSets {
    main {
        resources.srcDir("$projectDir/../core/build/resources/main")
    }
}