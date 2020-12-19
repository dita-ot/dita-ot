# DITA Open Toolkit [![Build]](http://travis-ci.org/dita-ot/dita-ot) [![Slack]](http://slack.dita-ot.org/)

_DITA Open Toolkit_, or _DITA-OT_ for short, is an open-source publishing engine for XML content authored in the _Darwin Information Typing Architecture_.

Visit the project website at [dita-ot.org][site] for documentation, information about releases, and [download packages][dist].  

For information on additional DITA and DITA-OT resources, see [SUPPORT]. To report a bug or suggest a feature, [create an issue][issue]. For more information on how you can help contribute to the project, see [CONTRIBUTING].

- [Prerequisites: Java 8](#prerequisites-java-8)
- [Installing](#installing)
- [Building output](#building-output)
- [For developers](#for-developers)
- [License](#license)

## Prerequisites: Java 8

- To _build_ DITA-OT, you’ll need Java Development Kit (JDK), version 8 or newer
- To _run_ DITA-OT, the Java Runtime Environment (JRE) is sufficient

You can download the Oracle JRE or JDK from [oracle.com/technetwork/java][java].

## Installing

1.  Download the distribution package from [dita-ot.org/download][dist].
2.  Extract the contents of the package to the directory where you want to install DITA-OT.

### Installing on macOS via Homebrew

On macOS, you can also install DITA-OT using the [Homebrew] package manager:

    brew install dita-ot

Homebrew will automatically download the latest version of the toolkit, install it in a subfolder of the local package Cellar and symlink the `dita` command to `/usr/local/bin/dita`.

## Building output

You can generate output using the `dita` command-line tool included with DITA Open Toolkit.

1.  On the command line, change to the `bin` folder of the DITA-OT installation directory:

        cd path/to/dita-ot-dir/bin

2.  Run the `dita` command to generate output:

        dita --input=input-file --format=format [options]

    where:

    - _`input-file`_ is the DITA map or DITA file that you want to process
    - _`format`_ is the output format (or “transformation type”)

See the [documentation][docs] for arguments and [options].

## For developers

<details>
<summary>Building the toolkit from source code and compiling the distribution package</summary>

1.  Clone the DITA-OT Git repository:

        git clone git://github.com/dita-ot/dita-ot.git

2.  Change to the DITA-OT directory:

        cd dita-ot

3.  Fetch the submodules:

        git submodule update --init --recursive

4.  In the root directory, run Gradle to compile the Java code and install plugins:

        ./gradlew

### Distribution builds

1.  In the root directory, set up the build environment:

        ./gradlew

2.  Build the distribution packages:

        ./gradlew dist

    Distribution packages are built in the `build/distributions` directory.

    If Gradle throws an error like `java.lang.OutOfMemoryError: Java heap space`, you probably need to increase the maximum Java heap size. One way to do this is to set the `GRADLE_OPTS` environment variable to a value like `-Xmx1024m`.

    For more information on the `-Xmx` option, see the [Java SE Documentation][javadoc].

</details>

## License

DITA Open Toolkit is licensed for use under the [Apache License 2.0][apache].

[build]: https://travis-ci.org/dita-ot/dita-ot.svg?branch=develop
[slack]: https://img.shields.io/badge/Slack-Join%20us!-%234A154B?style=flat&logo=slack
[site]: https://www.dita-ot.org/
[dist]: https://www.dita-ot.org/download
[support]: https://github.com/dita-ot/.github/blob/master/SUPPORT.md
[java]: http://www.oracle.com/technetwork/java/javase/downloads
[homebrew]: https://brew.sh
[docs]: https://www.dita-ot.org/dev/
[options]: https://www.dita-ot.org/dev/topics/build-using-dita-command.html
[javadoc]: http://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABHDABI
[apache]: http://www.apache.org/licenses/LICENSE-2.0
[issue]: https://github.com/dita-ot/dita-ot/issues/new/choose
[contributing]: https://github.com/dita-ot/.github/blob/master/CONTRIBUTING.md
