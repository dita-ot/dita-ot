About fo/Customization/fo/attrs
=======================================

This folder houses custom configuration files that override the
standard ones in fo/cfg/fo/attrs.  These files that define the appearance 
of different elements in XML assets when they are rendered as PDF outputs.  
The different DITA elements are organized into files by element type -- 
index-related definitions in index-attr.xsl, table-related definitions in 
tables-attr.xsl, etc.

Idiom has provided template files that you can start with throughout
this directory structure.  These files end in the suffix ".orig" (for
example, "catalog.xml.orig").  To enable these files, make a copy of
them and remove the ".orig" suffix.  For example, copy
"custom.xsl.orig" to "custom.xsl".  You can then make modifications
to the copy.

The files in this directory will override the out-of-the-box settings.

