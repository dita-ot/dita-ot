<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2010 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Import the main ditamap to HTML Help Contents conversion -->
<xsl:import href="map2htmlhelp/map2hhcImpl.xsl"/>

<dita:extension id="dita.xsl.htmlhelp.map2hhc" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

<xsl:output method="html"
            encoding="UTF-8"
            indent="no"
            omit-xml-declaration="yes"
/>

</xsl:stylesheet>
