About fo/Customization
======================

This directory is where the custom files live that make up customized
versions of the OpenTopic publishing outputs.  The FO publishing output 
will look for certain files here to override the standard ones.  Things 
you can currently override include:

  - Custom XSL via fo/xsl/custom.xsl, xhtml/xsl/custom.xsl and 
  	fo/attrs/custom.xsl
  - Font overrides via fo/font-mappings.xml
  - Per-locale variable overrides via common/vars/[locale].xml
  - I18N configuration via fo/i18n/[locale].xml
  - Index configuration via fo/index/[locale].xml

When customizing any of these areas, modify the relevant file(s) in
fo/Customization.  Then, to enable the changes in the publishing process, 
you find the corresponding entry for each file you modified in 
fo/Customization/catalog.xml.  It should look like this:

    <!--uri name="cfg:fo/attrs/custom.xsl" uri="fo/attrs/custom.xsl"/-->

Remove the comment markers "!--" and "--" to enable the change:

    <uri name="cfg:fo/attrs/custom.xsl" uri="fo/attrs/custom.xsl"/>

Your customization should now be enabled as part of the publishing process.

We have provided template files that you can start with throughout
this directory structure.  These files end in the suffix ".orig" (for
example, "catalog.xml.orig").  To enable these files, make a copy of
them and remove the ".orig" suffix.  For example, copy
"catalog.xml.orig" to "catalog.xml".  You can then make modifications
to the copy.

The Fo output also provides a general configuration file called
"build.properties" that allows you to control the publishing process.
To modify these settings, copy "build.properties.orig" to
"build.properties" and then modify the relevant options.
