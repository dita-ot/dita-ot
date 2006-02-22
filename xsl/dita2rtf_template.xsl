<?xml version="1.0" encoding="UTF-8"?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:import href="xslrtf/dita2rtfImpl.xsl"/>
<dita:extension id="dita.xsl.rtf" behavior="org.dita.dost.platform.ImportAction" xmlns:dita="http://dita-ot.sourceforge.net"/>
<xsl:output method="text" encoding="cp1252"/>

<xsl:template match="/">
<xsl:apply-imports/>
</xsl:template>

</xsl:stylesheet>