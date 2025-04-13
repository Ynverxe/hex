
subprojects {
    version = if (rootProject.extra.has("${project.name}-version")) rootProject.extra["${project.name}-version"] as String else "undefined"
}