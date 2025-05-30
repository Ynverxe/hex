plugins {
    id("io.deepmedia.tools.deployer")
}

deployer {
    projectInfo {
        name.set(project.name)
        description.set(project.description)
        url.set("https://github.com/Ynverxe/hex")
        groupId.set(project.group.toString())
        artifactId.set(project.name)
        scm {
            fromGithub("Ynverxe", "hex")
        }
        license(MIT)
        developer("Ynverxe", "nahubar65@gmail.com", null, "https://github.com/Ynverxe")
    }

    content {
        component {
            fromJava()
        }
    }

    release {
        release.version.set(project.version.toString())
    }

    centralPortalSpec {
        allowMavenCentralSync = false

        auth {
            val tokenUsername = System.getenv("CENTRAL_TOKEN_USERNAME")
            val tokenPassword = System.getenv("CENTRAL_TOKEN_PASSWORD")
            if (tokenUsername == null || tokenPassword == null) {
                logger.warn("Central Portal token values aren not configured (CENTRAL_TOKEN_USERNAME or CENTRAL_TOKEN_PASSWORD)")
            }

            user.set(secret("CENTRAL_TOKEN_USERNAME"))
            password.set(secret("CENTRAL_TOKEN_PASSWORD"))
        }

        signing {
            val secretKey = System.getenv("SIGNING_KEY")
            val secretKeyPassword = System.getenv("SIGNING_KEY_PASSPHRASE")
            if (secretKey == null && secretKeyPassword == null) {
                logger.warn("Signing variables are not configured (SIGNING_KEY & SIGNING_KEY_PASSPHRASE)")
            }

            key.set(secret("SIGNING_KEY"))
            password.set(secret("SIGNING_KEY_PASSPHRASE"))
        }
    }
}