<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--  pretty.xsl
 | This stylesheet is the standard "identity transform" from the
 | XSLT Recommendation, augmented by the one property, indent="yes",
 | and a filter to keep out some fixed values from the DITA DTDs.
 | This stylesheet copies an input XML source file into its indented
 | equivalent, with the exception that fixed/defaulted attributes are
 | filtered out.  Remove the [] block in line 19 to
 | re-enable the copying through of these attributes.
 +-->

<xsl:output method="xml" indent="yes" />

<xsl:template match="*">
   <xsl:variable name="class" select="generate-id(@class)"/>
   <xsl:variable name="space" select="generate-id(@xml:space)"/>
   <xsl:copy>
      <xsl:copy-of select="@*[(generate-id(.)!=$class) and (generate-id(.)!=$space)]" />
      <xsl:apply-templates />
   </xsl:copy>
</xsl:template>

<xsl:template match="comment()|processing-instruction()">
   <xsl:copy />
</xsl:template>

</xsl:stylesheet>
