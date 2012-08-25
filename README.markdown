DITA Open Toolkit [![Build Status](https://secure.travis-ci.org/dita-ot/dita-ot.png?branch=B_MT1-7)](http://travis-ci.org/dita-ot/dita-ot)
=================

The DITA Open Toolkit, or DITA-OT for short, is an open source tool that provides processing for OASIS DITA content. See [dita-ot.sf.net](http://dita-ot.sourceforge.net/) for documentation, information about releases, and download packages.

Prerequisites
-------------

In order to build and use DITA-OT, you’ll need:

* Java Development Kit 5.0 or newer
* Apache Ant 1.8.2 or newer.

Building
--------

1. Clone DITA-OT Git repository.
2. On root directory, build the distribution:

        ant dist
     
Usage (*nix systems)
-----

1. Change directory to `src/main`.
2. Run DITA-OT with:

        start [options]
        
   See [documention](http://dita-ot.sourceforge.net/latest/) for arguments and options.
	NOTE: Documentation has not been updated to reflect the use of the 'start' script.
	Read 'start' for 'ant' in the usage documentation.

Usage (Windows systems)
——

To be developed. When completed, the sequence will be:

1. Change directory to `src/main`.
2. Run DITA-OT with:

	start [options]

This will invoke the start.bat script. See *nix usage, above.

Distribution
------------

1. On root directory, compile Java code:

        ant jar
     
2. Run plug-in installation:

        ant -f src/main/integrator.xml

3. Add the following files and directories to `CLASSPATH` system variable:
   * `src/main/`
   * `src/main/lib/`
   * `src/main/lib/dost.jar`
   * `src/main/lib/xercesImpl.jar`
   * `src/main/lib/xml-apis.jar`
   * `src/main/lib/commons-codec-1.4.jar`
   * `src/main/lib/saxon/saxon9-dom.jar`
   * `src/main/lib/saxon/saxon9.jar`
   * `src/main/lib/resolver.jar`

4. Build distribution packages:

        ant dist
   
   Distribution packages are build into `target` directory.
   
License
-------

The DITA Open Toolkit is licensed for use, at the user's election, under the [Common Public License](http://www.opensource.org/licenses/cpl1.0.php) 1.0 (CPL) or [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

