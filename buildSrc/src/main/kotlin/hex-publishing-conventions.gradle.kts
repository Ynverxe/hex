plugins {
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = "launcher-generator"
            version = project.version.toString()

            from(components["java"])

            pom {
                name = "hex-server" + project.displayName
                developers {
                    developer {
                        id = "ynverxe"
                        url = "https://github.com/Ynverxe"
                    }
                }

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/license/mit"
                    }
                }
            }
        }
    }
}