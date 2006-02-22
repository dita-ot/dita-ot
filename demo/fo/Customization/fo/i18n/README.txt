About fo/Customization/fo/i18n
======================================

This folder houses custom configuration files that override the standard ones
in fo/cfg/fo/i18n.  Each file contains data for a single
locale, and should take that locale's ISO code as its name (for example,
de_DE.xml).  These files are used only in the generation of PDF outputs.

Each configuration file contains mappings of certain symbols to the Unicode
codepoint which should be used to represent them in the given locale.

The best way to start editing a custom configuration is by making a copy of the
original from fo/cfg/fo/i18n and making changes as desired.

In order to apply a custom configuration to your publishing outputs, you must
edit fo/Customization/catalog.xml and uncomment the appropriate entry
in the "I18N configuration override entries" section.
