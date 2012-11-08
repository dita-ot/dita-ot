DITA Open Toolkit [![Build Status](https://secure.travis-ci.org/dita-ot/dita-ot.png?branch=develop)](http://travis-ci.org/dita-ot/dita-ot)
=================

The DITA Open Toolkit, or DITA-OT for short, is an open source tool that provides processing for OASIS DITA content. See [dita-ot.sf.net](http://dita-ot.sourceforge.net/) for documentation, information about releases, and download packages.

Prerequisites
-------------

In order to build and use DITA-OT, you’ll need:

* Java Development Kit 6 or newer
* Apache Ant 1.8.2 or newer.

   Please note that if you receive errors like `unknown protocol: plugin` or `unknown protocol: cfg` then you may have some missing libraries from your `ant` installation. In that case please download a recent distribution of `ant` and use that instead.

Building
--------

1. Clone DITA-OT Git repository.
2. On root directory, compile Java code:

        ant jar

3. Make sure the following files and directories are added to your `CLASSPATH` system variable:
   * `src/main/lib/`
     
3. Run plug-in installation:

        ant -f src/main/integrator.xml
 
Usage
-----

1. Add the following files and directories to `CLASSPATH` system variable:
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
2. Change directory to `src/main`.
3. Run DITA-OT with:

        ant [options]
        
   See [documention](http://dita-ot.sourceforge.net/latest/) for arguments and options.

Distribution
------------

1. On root directory, compile Java code:

        ant jar
     
2. Add the following files and directories to `CLASSPATH` system variable:
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

3. Run plug-in installation:

        ant -f src/main/integrator.xml

4. Build distribution packages:

        ant dist
   
   Distribution packages are build into `target` directory.

   On some systems you may encounter an `java.lang.OutOfMemoryError: Java heap space`. In that case you need to provide more memory to the `ant` process. One way of doing that is by setting the `ANT_OPTS` system variable to specify more memory, for example setting that to `-Xmx1000m` should be enough.
   
License
-------

The DITA Open Toolkit is licensed for use, at the user's election, under the [Common Public License](http://www.opensource.org/licenses/cpl1.0.php) 1.0 (CPL) or [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).