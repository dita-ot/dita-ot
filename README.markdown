DITA Open Toolkit [![Build Status](https://secure.travis-ci.org/dita-ot/dita-ot.png?branch=B_MT1-6)](http://travis-ci.org/dita-ot/dita-ot)
=================

The DITA Open Toolkit, or DITA-OT for short, is an open source tool that provides processing for OASIS DITA content. See [dita-ot.sf.net](http://dita-ot.sourceforge.net/) for documentation, information about releases, and download packages.

Prerequisites
-------------

In order to build and use DITA-OT, you’ll need:

* Java Development Kit 5.0 or newer

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

License
-------

The DITA Open Toolkit is licensed for use, at the user's election, under the [Common Public License](http://www.opensource.org/licenses/cpl1.0.php) 1.0 (CPL) or [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).