About fo/Customization/common/index
===========================================

This folder houses custom index definition files that override the standard
ones in fo/cfg/common/index.  Each file contains data for a single locale, and 
should take that locale's ISO code as its name (for example, de_DE.xml).

The index files consist of <index.group> elements which contain sorting
information on one or more characters.  Index groups are listed in sort order
("specials" before numbers, numbers before the letter 'A', etc), and the
<char.set> entries they contain are also listed in sort order (uppercase before
lowercase).

The best way to start editing a custom index file is by making a copy of the
original from fo/cfg/common/index and making changes as desired.

In order to apply a custom index definition to your publishing outputs, you
must edit fo/Customization/catalog.xml and uncomment the appropriate
entry in the "Index configuration override entries" section.
