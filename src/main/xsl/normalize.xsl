<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--  normalize.xsl
 | This stylesheet is the standard "identity transform" from the
 | XSLT Recommendation without indentation.
 | Note: the output from the DITA identity transforms is dtd-neutral,
 | therefore the original doctype cannot be copied through.  If you
 | use identity transforms from a script, you can add logic to the
 | script to re-insert the desired doctype for use by editors, browers, etc..
 +-->

<xsl:output method="xml" indent="no" />

<xsl:template match="*">
   <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates />
   </xsl:copy>
</xsl:template>

<xsl:template match="comment()|processing-instruction()">
   <xsl:copy />
</xsl:template>

</xsl:stylesheet>
