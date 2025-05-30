subprojects {
    plugins.withId("java") {
        apply(plugin = "hex-java-conventions")
    }

    group = "io.github.ynverxe"
    version = if (rootProject.extra.has("${project.name}-version")) rootProject.extra["${project.name}-version"] as String else "undefined"
}