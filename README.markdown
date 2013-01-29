DITA Open Toolkit [![Build Status](https://secure.travis-ci.org/dita-ot/dita-ot.png?branch=develop)](http://travis-ci.org/dita-ot/dita-ot)
=================

The DITA Open Toolkit, or DITA-OT for short, is an open-source tool that provides processing for OASIS DITA content. See [dita-ot.sf.net](http://dita-ot.sourceforge.net/) for documentation, information about releases, and download packages.

Prerequisites
-------------

To build and use DITA-OT, you’ll need:

* Java Development Kit 6 or newer
* Apache Ant 1.8.2 or newer

   If Ant throws an error like `unknown protocol: plugin` or `unknown protocol: cfg`, your Ant installation may be outdated. Try installing [a newer version of Ant](http://ant.apache.org/).

Building
--------

1. Clone the DITA-OT Git repository:

        git clone git://github.com/dita-ot/dita-ot.git

2. In the root directory, compile the Java code:

        ant jar jar.plug-ins

3. Add these files into the `CLASSPATH` environment variable:
   * `src/main/lib/icu4j.jar`
   * `src/main/lib/resolver.jar`

3. Install plugins:

        ant -f src/main/integrator.xml
 
Usage
-----

1. Add these files and directories into the `CLASSPATH` environment variable:
   * `src/main/`
   * `src/main/lib/`
   * `src/main/lib/dost.jar`
   * `src/main/lib/xercesImpl.jar`
   * `src/main/lib/xml-apis.jar`
   * `src/main/lib/commons-codec-1.4.jar`
   * `src/main/lib/saxon/saxon9-dom.jar`
   * `src/main/lib/saxon/saxon9.jar`
   * `src/main/lib/resolver.jar`
   * `src/main/lib/icu4j.jar`

2. Change the directory to `src/main`.
3. Run DITA-OT:

        ant [options]
        
   See the [documentation](http://dita-ot.sourceforge.net/latest/) for arguments and options.

Distribution
------------

1. In the root directory, compile the Java code:

        ant jar jar.plug-ins
     
2. Add these files and directories into the `CLASSPATH` environment variable:
   * `src/main/`
   * `src/main/lib/`
   * `src/main/lib/dost.jar`
   * `src/main/lib/xercesImpl.jar`
   * `src/main/lib/xml-apis.jar`
   * `src/main/lib/commons-codec-1.4.jar`
   * `src/main/lib/saxon/saxon9-dom.jar`
   * `src/main/lib/saxon/saxon9.jar`
   * `src/main/lib/resolver.jar`
   * `src/main/lib/icu4j.jar`

3. Install plugins:

        ant -f src/main/integrator.xml

4. Build distribution packages:

        ant dist
   
   Distribution packages are build into the `target` directory.

   If Ant throws an error like `java.lang.OutOfMemoryError: Java heap space`, you probably need to increase the maximum Java heap size. One way to do this is to set the `ANT_OPTS` environment variable to a value like `-Xmx1024m`.

   For more information on the `-Xmx` option, see [Java SE Documentation](http://docs.oracle.com/javase/6/docs/technotes/tools/windows/java.html#nonstandard).

License
-------

The DITA Open Toolkit is licensed for use, at the user's election, under the [Common Public License](http://www.opensource.org/licenses/cpl1.0.php) 1.0 (CPL) or [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).