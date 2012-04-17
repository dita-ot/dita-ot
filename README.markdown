DITA Open Toolkit
=================

The DITA Open Toolkit, or DITA-OT for short, is an open source tool that provides processing for OASIS DITA content. See [dita-ot.sf.net](http://dita-ot.sourceforge.net/) for documentation, information about releases, and download packages.

Prerequisites
-------------

In order to build and use DITA-OT, you’ll need:

* Java Development Kit 5.0 or newer
* Apache Ant 1.7.1 or newer.

Building
--------

1. Clone DITA-OT Git repository.
2. On root directory, compile Java code:

        ant jar
     
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
2. Change directory to `src/main`.
3. Run DITA-OT with:

        ant [options]
        
   See [documention](http://dita-ot.sourceforge.net/latest/) for arguments and options.

License
-------

The DITA Open Toolkit is licensed for use, at the user's election, under the [Common Public License](http://www.opensource.org/licenses/cpl1.0.php) 1.0 (CPL) or [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).