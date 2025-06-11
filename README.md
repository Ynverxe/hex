## Overview

_Hex_ is an extension based server for Minecraft which uses the [Minestom](https://github.com/Minestom/Minestom) library. The main goals is to provide a conventional server software
where external functionallities are separated from the server logic using **Extensions** (also called plugin or mod in other server softwares), and provide a comfortable environment
to test extensions and configure the server behaviour.

## Running a server

In order to run a hex server, first you have to build the [launcher module](https://github.com/Ynverxe/hex/tree/main/launcher) yourself.

```console
./gradlew launcher:shadowJar
```

## Launcher behaviour

A final step is required in order to run the generated launcher: you must specify the Minestom artifact to be used at runtime. By default, neither the ``core`` nor the ``core`` packages include Minestom. Since Minestom is frequently updated and often modified by third parties, bundling it into the launcher would require rebuilding the launcher every time a bug is patched or a new feature is added — which is not very efficient.
To specify the Minestom library to use, create a minestom-source.conf file inside the server directory. This file should contain the Maven coordinates of the artifact and the Maven repository from which the artifact will be downloaded.

### ```minestom-source.conf``` example:

```hocon
coordinates = "net.minestom:minestom-snapshots:ebaa2bbf64"
repositories = [
    "https://repo.maven.apache.org/maven2/"
]
```

## Modules

| Name | Directory Name | Category | Description | Published |
|------|----------------|----------|-------------|-----------|
| hex-core | core | Production | Defines main functionallity of the server | ✔️ |
| hex-launcher | launcher | Production | Defines logic to launch :core | ✔️ |
| hex-logging | logging | Production | Defines logging logic which is used in :core and :launcher | ✔️ |
| hex-demo-extension | demo-extension | Testing | Defines an extension which is used in :core unit tests | ❌ | 
| hex-lab | lab | Testing | Defines an extension for manual testing purposes | ❌ |
| hex-launcher-plugin | launcher-plugin | Production | Defines a Gradle plugin | ❌ |

## The launcher-plugin
TODO

## License
This project is licensed under the MIT-License.
