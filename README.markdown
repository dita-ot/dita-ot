DITA Open Toolkit [![Build Status](https://secure.travis-ci.org/dita-ot/dita-ot.png?branch=develop)](http://travis-ci.org/dita-ot/dita-ot)
=================

The DITA Open Toolkit, or DITA-OT for short, is an open-source tool that provides processing for OASIS DITA content. See [dita-ot.org](http://www.dita-ot.org/) for documentation, information about releases, and download packages.

Prerequisites
-------------

To build and use DITA-OT, you’ll need:

* Java Development Kit 7 or newer
* Apache Ant 1.9.4 or newer
* Apache Ivy 2.3.0 or newer

If Ant throws an error like `unknown protocol: plugin` or `unknown protocol: cfg`, your Ant installation may be outdated. Try installing [a newer version of Ant](http://ant.apache.org/).

Building
--------

1. Clone the DITA-OT Git repository:

        git clone git://github.com/dita-ot/dita-ot.git

2. Fetch the submodules:

        git submodule update --init --recursive

3. In the root directory, run `ant` to compile the Java code and install plugins:

        ant
 
Usage
-----

1. Run the `dita` command to generate output:

        src/main/bin/dita [options]
        
   See the [documentation](http://www.dita-ot.org/2.0/) for arguments and [options](http://www.dita-ot.org/2.0/readme/using-dita-command.html).

Distribution
------------

1. In the root directory, run `ant` to compile the Java code and install plugins:

        ant
     
2. Add these files and directories to the `CLASSPATH` environment variable:
   * `src/main/`
   * `src/main/lib/dost.jar`
   * `src/main/lib/dost-configuration.jar`
   * `src/main/lib/xercesImpl.jar`
   * `src/main/lib/xml-apis.jar`
   * `src/main/lib/commons-codec.jar`
   * `src/main/lib/commons-io.jar`
   * `src/main/lib/saxon-dom.jar`
   * `src/main/lib/saxon.jar`
   * `src/main/lib/xml-resolver.jar`
   * `src/main/lib/icu4j.jar`

3. Build distribution packages:

        ant dist
   
   Distribution packages are built in the `target` directory.

   If Ant throws an error like `java.lang.OutOfMemoryError: Java heap space`, you probably need to increase the maximum Java heap size. One way to do this is to set the `ANT_OPTS` environment variable to a value like `-Xmx1024m`.

   For more information on the `-Xmx` option, see [Java SE Documentation](http://docs.oracle.com/javase/6/docs/technotes/tools/windows/java.html#nonstandard).

License
-------

The DITA Open Toolkit is licensed for use under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
