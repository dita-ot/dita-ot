<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<xsl:template match="*[contains(@class,' sw-d/msgph ')]">
{\f5 <xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/varname ')]">
{\i <xsl:apply-templates/>}
</xsl:template>

</xsl:stylesheet>
