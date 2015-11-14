DITA Open Toolkit [![Build Status](https://secure.travis-ci.org/dita-ot/dita-ot.png?branch=develop)](http://travis-ci.org/dita-ot/dita-ot)
=================

The _DITA Open Toolkit_, or _DITA-OT_ for short, is an open-source tool that provides processing for OASIS DITA content. See [dita-ot.org](http://www.dita-ot.org/) for documentation, information about releases, and download packages.

Prerequisites
-------------

To build and use DITA-OT, you’ll need:

* Java Development Kit 7 or newer

Building
--------

1. Clone the DITA-OT Git repository:

        git clone git://github.com/dita-ot/dita-ot.git

2. Move to the DITA-OT directory:

        cd dita-ot

3. Fetch the submodules:

        git submodule update --init --recursive

4. In the root directory, run Gradle to compile the Java code and install plugins:

        ./gradlew
 
Usage
-----

1. Run the `dita` command to generate output:

        src/main/bin/dita [options]
        
   See the [documentation](http://www.dita-ot.org/2.1/) for arguments and [options](http://www.dita-ot.org/2.1/getting-started/using-dita-command.html).

Distribution
------------

1. In the root directory, set up build environment:

        ./gradlew

2. Build distribution packages:

        ./gradlew dist
   
   Distribution packages are built in the `build/distributions` directory.

   If Gradle throws an error like `java.lang.OutOfMemoryError: Java heap space`, you probably need to increase the maximum Java heap size. One way to do this is to set the `GRADLE_OPTS` environment variable to a value like `-Xmx1024m`.

   For more information on the `-Xmx` option, see [Java SE Documentation](http://docs.oracle.com/javase/6/docs/technotes/tools/windows/java.html#nonstandard).

License
-------

The DITA Open Toolkit is licensed for use under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
