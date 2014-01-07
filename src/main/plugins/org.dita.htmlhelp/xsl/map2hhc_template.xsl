<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corporation 2010 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Import the main ditamap to HTML Help Contents conversion -->
<xsl:import href="map2htmlhelp/map2hhcImpl.xsl"/>

<dita:extension id="dita.xsl.htmlhelp.map2hhc" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

<xsl:output method="xml"
            encoding="UTF-8"
            indent="no"
/>

</xsl:stylesheet>
