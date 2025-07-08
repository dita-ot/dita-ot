<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                version="3.0">

   <!--
      Utils for compiling x:expect
   -->

   <!-- Returns true if x:expect has any comparison factor -->
   <xsl:function name="x:has-comparison" as="xs:boolean">
      <xsl:param name="expect" as="element(x:expect)" />

      <xsl:sequence select="$expect/(@as or @href or @select or (node() except x:label))" />
   </xsl:function>

   <!-- Returns an error string for boolean @test with any comparison factor -->
   <xsl:function name="x:boolean-with-comparison" as="xs:string">
      <xsl:param name="expect" as="element(x:expect)" />

      <xsl:for-each select="$expect">
         <xsl:call-template name="x:prefix-diag-message">
            <xsl:with-param name="message"
               select="'Boolean @test must not be accompanied by @as, @href, @select, or child node.'" />
         </xsl:call-template>
      </xsl:for-each>
   </xsl:function>

   <!-- Returns an error string for non-boolean @test with no comparison factors -->
   <xsl:function name="x:non-boolean-without-comparison" as="xs:string">
      <xsl:param name="expect" as="element(x:expect)" />

      <xsl:for-each select="$expect">
         <xsl:call-template name="x:prefix-diag-message">
            <xsl:with-param name="message"
               select="'Non-boolean @test must be accompanied by @as, @href, @select, or child node.'" />
         </xsl:call-template>
      </xsl:for-each>
   </xsl:function>

</xsl:stylesheet>