# DITA Open Toolkit [![DITA-OT Discussions][discussions]](https://github.com/orgs/dita-ot/discussions)

_DITA Open Toolkit_, or _DITA-OT_ for short, is an open-source publishing engine for content authored in the _Darwin Information Typing Architecture_.

Visit the project website at [dita-ot.org][site] for documentation, information about releases, and [download packages][dist].

For information on additional DITA and DITA-OT resources, see [SUPPORT]. To report a bug or suggest a feature, [create an issue][issue]. For more information on how you can help contribute to the project, see [CONTRIBUTING].

- [Prerequisites: Java 17](#prerequisites-java-17)
- [Installing](#installing)
- [Building output](#building-output)
- [Development](#development)
    - [Running tests](#running-tests)
    - [Formatting code](#formatting-code)
    - [Distribution builds](#distribution-builds)
- [License](#license)

## Prerequisites: Java 17

To build and run DITA-OT, you’ll need Java Development Kit (JDK), version 17 or newer.

You can download the OpenJDK from [AdoptOpenJDK][adoptopenjdk].

## Installing

1.  Download the distribution package from [dita-ot.org/download][dist].
2.  Extract the contents of the package to the directory where you want to install DITA-OT.

<details>
<summary>Installing via Homebrew</summary>

On macOS and Linux, you can also install DITA-OT using the [Homebrew] package manager:

```shell
brew install dita-ot
```

Homebrew will automatically download the latest version of the toolkit, install it in a subfolder of the local package Cellar and symlink the `dita` command to the `bin` subfolder of the Homebrew installation directory.

> **Note**
>
> Homebrew’s default installation location depends on the operating system architecture:
>
> - `/usr/local` on macOS Intel
> - `/opt/homebrew` on macOS ARM
> - `/home/linuxbrew/.linuxbrew` on Linux

</details>

## Building output

You can generate output using the `dita` command-line tool included with DITA Open Toolkit.

1.  On the command line, change to the `bin` folder of the DITA-OT installation directory:
    ```shell
    cd path/to/dita-ot-dir/bin
    ```
2.  Run the `dita` command to generate output:

    ```shell
    dita --input=input-file --format=format [options]
    ```

    where:

    - _`input-file`_ is the DITA map or DITA file that you want to process
    - _`format`_ is the output format (or “transformation type”)

See the [documentation][docs] for arguments and [options].

## Development

Building the toolkit from source code and compiling the distribution package

1.  Clone the DITA-OT Git repository, including submodules:
    ```shell
    git clone --recurse-submodules git://github.com/dita-ot/dita-ot.git
    ```
2.  Change to the DITA-OT directory:
    ```shell
    cd dita-ot
    ```
3.  In the root directory, run Gradle to compile the Java code and install plugins:
    ```shell
    ./gradlew
    ```

### Running tests

```shell
./gradlew check
```

All tests are run by GitHub Actions [test workflow] on each push and
for every pull request. 

### Formatting code

Requirements:

- Node.js

Prettier is used retain consistent Java formatting.

1.  Run Prettier:
    ```shell
    npm run fmt
    ```

### Distribution builds

1.  In the root directory, set up the build environment:
    ```shell
    ./gradlew
    ```
2.  Build the distribution packages:

    ```shell
    ./gradlew dist
    ```

    Distribution packages are built in the `build/distributions` directory.

    If Gradle throws an error like `java.lang.OutOfMemoryError: Java heap space`, you probably need to increase the maximum Java heap size. One way to do this is to set the `GRADLE_OPTS` environment variable to a value like `-Xmx1024m`.

    For more information on the `-Xmx` option, see the [Java SE Documentation][javadoc].

## License

DITA Open Toolkit is licensed for use under the [Apache License 2.0][apache].

[discussions]: https://img.shields.io/github/discussions/dita-ot/dita-ot?label=DITA-OT%20Discussions
[site]: https://www.dita-ot.org/
[dist]: https://www.dita-ot.org/download
[support]: https://github.com/dita-ot/.github/blob/master/SUPPORT.md
[adoptopenjdk]: https://adoptopenjdk.net/
[homebrew]: https://brew.sh
[docs]: https://www.dita-ot.org/dev/
[options]: https://www.dita-ot.org/dev/topics/build-using-dita-command.html
[javadoc]: http://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABHDABI
[apache]: http://www.apache.org/licenses/LICENSE-2.0
[issue]: https://github.com/dita-ot/dita-ot/issues/new/choose
[contributing]: https://github.com/dita-ot/.github/blob/master/CONTRIBUTING.md
[test workflow]: https://github.com/dita-ot/dita-ot/actions/workflows/test.yml