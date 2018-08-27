# DITA Open Toolkit [![Build Status][1]](http://travis-ci.org/dita-ot/dita-ot) [![Slack][7]](http://slack.dita-ot.org/)

The _DITA Open Toolkit_, or _DITA-OT_ for short, is an open-source publishing engine for XML content authored in the _Darwin Information Typing Architecture_.

See [dita-ot.org][2] for documentation, information about releases, and download packages.

For information on additional DITA and DITA-OT resources, see [SUPPORT][8].

## Prerequisites

To build and use DITA-OT, you’ll need:

- Java Development Kit 8 or newer

## Installing

1.  Download the distribution package from [dita-ot.org/download][9].
2.  Extract the contents of the package to the directory where you want to install the DITA-OT.

### Installing on macOS via Homebrew

On macOS, you can also install DITA-OT using the [Homebrew][10] package manager:

    brew install dita-ot

Homebrew will automatically download the latest version of the toolkit, install it in a subfolder of the local package Cellar and symlink the `dita` command to `/usr/local/bin/dita`.

## Building output

You can generate output using the DITA Open Toolkit `dita` command-line tool.

1.  On the command line, change to the `bin` folder of the DITA-OT installation directory:

        cd path/to/dita-ot-dir/bin

2.  Run the `dita` command to generate output:

        dita --input=input-file --format=format [options]

    where:

    - _`input-file`_ is the DITA map or DITA file that you want to process
    - _`format`_ is the output format (or “transformation type”)

See the [documentation][3] for arguments and [options][4].

## Building

1.  Clone the DITA-OT Git repository:

        git clone git://github.com/dita-ot/dita-ot.git

2.  Move to the DITA-OT directory:

        cd dita-ot

3.  Fetch the submodules:

        git submodule update --init --recursive

4.  In the root directory, run Gradle to compile the Java code and install plugins:

        ./gradlew

## Distribution

1.  In the root directory, set up build environment:

        ./gradlew

2.  Build distribution packages:

        ./gradlew dist

    Distribution packages are built in the `build/distributions` directory.

    If Gradle throws an error like `java.lang.OutOfMemoryError: Java heap space`, you probably need to increase the maximum Java heap size. One way to do this is to set the `GRADLE_OPTS` environment variable to a value like `-Xmx1024m`.

    For more information on the `-Xmx` option, see the [Java SE Documentation][5].

## License

The DITA Open Toolkit is licensed for use under the [Apache License 2.0][6].

[1]: https://travis-ci.org/dita-ot/dita-ot.svg?branch=develop
[2]: https://www.dita-ot.org/
[3]: https://www.dita-ot.org/dev/
[4]: https://www.dita-ot.org/dev/topics/build-using-dita-command.html
[5]: http://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABHDABI
[6]: http://www.apache.org/licenses/LICENSE-2.0
[7]: http://slack.dita-ot.org/badge.svg
[8]: https://github.com/dita-ot/dita-ot/blob/develop/.github/SUPPORT.md
[9]: https://www.dita-ot.org/download
[10]: https://brew.sh
