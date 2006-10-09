<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- This stylesheet is a part of the Serna Interactive Example. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:se="http://www.syntext.com/XSL/Format-1.0"
                version='1.0'>

 <!-- Make a Table of Contents -->
 <xsl:template name="make.toc">
  <fo:block background-color="#e0e0e0">
    <fo:block font-size="20pt" font-weight="bold">
        <xsl:text>Table of Contents:</xsl:text>
    </fo:block>
        <xsl:apply-templates mode="toc" select="topic" se:sections="omit"/>
   </fo:block>
 </xsl:template> 

 <xsl:template match="*[contains(@class, ' topic/title ')]" mode="toc">
    <fo:block start-indent="{count(ancestor-or-self::topic) * 10}pt">
        <xsl:number count="sect" format="1. " level="multiple"/>
        <xsl:apply-templates select="text()"/>
    </fo:block>
 </xsl:template>

 <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="toc">
    <xsl:apply-templates mode="toc" select="*[self::sect or self::title]"/>
 </xsl:template>

</xsl:stylesheet>
