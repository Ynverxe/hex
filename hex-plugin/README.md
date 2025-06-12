## Overview

```hex-plugin``` defines a Gradle plugin which adds two useful tasks: ``generateLauncher`` and ``runHexServer``.

### generateLauncher (GenerateLauncherTask)

GenerateLauncherTask extends from [org.gradle.jvm.tasks.Jar](https://docs.gradle.org/current/javadoc/org/gradle/jvm/tasks/Jar.html). By default, this class includes the generated jar by ```:launcher:shadowJar``` into
the copy-spec of the task. Additionally, the tasks has the following behaviour:
- It includes the resource files from the "main" source set (which may be removed in the future)
- Allows defining configuration files ``config.conf`` and ``minestom-source.conf``.

### runHexServer (RunHexServerTask)

RunHexServerTask extends from [org.gradle.api.tasks.JavaExec](https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api.tasks/-java-exec/index.html). It allows
you to run a hex-server on a Gradle task for testing purposes. The JAR used to run the server must be specified manually, it can be a Jar task or a path to a JAR file.
Additionally, this task allows you to add extensions generated from a Jar task.

You can see an example of how to use those tasks [here](https://github.com/Ynverxe/hex/blob/main/lab/build.gradle.kts).

## Using the plugin

The plugin isn't published yet. So, you have to include it manually.

### 1. Fork the project
```bash
git clone https://github.com/Ynverxe/hex
```

### 2. Use it as an included build in your ``settings.gradle.kts``
```kotlin
pluginManagement {
  includeBuild("path-to-the-project/hex-plugin")
}
```

### 3. Apply it in your ``build.gradle.kts``
```kotlin
plugins {
  id("hex-plugin")
}
```
