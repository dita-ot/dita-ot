About fo/Customization/common/vars
===========================================

This folder houses custom variable files that override the standard ones in
fo/cfg/common/vars.  Each file contains data for a single locale, and should 
take that locale's ISO code as its name (for example, de_DE.xml).

Variable files contain a set of <variable> elements, identified by their id
attribute.  The variable definitions are used to store static text that is used
as part of the published outputs.  For example, page headers, hyperlinks, etc.
The id attribute for each variable should make it clear how the variable text
is being used.

Some variables contain <param> elements which indicate parameter values which
are substituted in at publish time by the XSL.  For example, a page number that
is being generated as part of the publishing process might be identified by 
    <param ref-name="number"/>
When editing or translating a variable file, these should be included in the
translation, though they can be moved and rearranged within the <variable>
content as needed.

The best way to start editing a custom variables file is by making a copy of
the original from fo/cfg/common/vars and making changes as desired.  When 
adding a new locale, start from an existing locale's list of variables and 
translate each entry as needed.

Note that unchanged <variable> elements can be omitted: the custom variables
file need only include those <variable> elements which you have modified.
Variables not found in the custom file will are taken from the standard
OpenTopic variable files.

Applying a custom variable does not require modifying the
fo/Customization/catalog.xml file.  The publishing process will automatically 
use any custom variables definitions in place of the original ones.
