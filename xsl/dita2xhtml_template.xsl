<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon"                
                >

<!-- idit2htm.xsl   main stylesheet
 | Convert DITA topic to HTML; "single topic to single web page"-level view
 |
-->

<!-- stylesheet imports -->
<!-- the main dita to xhtml converter -->
<xsl:import href="xslhtml/dita2htmlImpl.xsl"/>

<!-- the dita to xhtml converter for task documents -->
<xsl:import href="xslhtml/taskdisplay.xsl"/>

<!-- the dita to xhtml converter for reference documents -->
<xsl:import href="xslhtml/refdisplay.xsl"/>

<!-- user technologies domain -->
<xsl:import href="xslhtml/ut-d.xsl"/>
<!-- software domain -->
<xsl:import href="xslhtml/sw-d.xsl"/>
<!-- programming domain -->
<xsl:import href="xslhtml/pr-d.xsl"/>
<!-- ui domain -->
<xsl:import href="xslhtml/ui-d.xsl"/>
<!-- highlighting domain -->
<xsl:import href="xslhtml/hi-d.xsl"/>

<dita:extension id="dita.xsl.xhtml" behavior="org.dita.dost.platform.ImportAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

<!-- the dita to xhtml converter for element reference documents - not used now -->
<!--<xsl:import href="elementrefdisp.xsl"/>-->

<!-- Output XHTML with XML syntax, use UFT-8 encoding="UTF-8", transitional XHTML.
     Prevent indenting to conserve space on output. -->
<!--xsl:output method="saxon:xhtml" -->
<xsl:output method="xml" 
            encoding="UTF-8"
            indent="no"
            doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
            doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
/>

<!-- DITAEXT file extension name of dita topic file -->
<xsl:param name="DITAEXT" select="'.xml'"/>

<!-- root rule -->
<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
