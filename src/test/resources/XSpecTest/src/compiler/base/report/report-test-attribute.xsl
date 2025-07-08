<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:x="http://www.jenitennison.com/xslt/xspec"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                version="3.0">

   <xsl:template name="x:report-test-attribute" as="node()+">
      <xsl:context-item as="element(x:expect)" use="required" />

      <xsl:variable name="expect-test" as="element(x:expect)">
         <!-- Do not set xsl:copy/@copy-namespaces="no". @test may use namespace prefixes and/or the
            default namespace such as xs:QName('foo') -->
         <xsl:copy>
            <xsl:sequence select="@test" />
         </xsl:copy>
      </xsl:variable>

      <!-- Undeclare the default namespace in the wrapper element, because @test may use the default
         namespace such as xs:QName('foo'). -->
      <xsl:call-template name="x:wrap-node-constructors-and-undeclare-default-ns">
         <xsl:with-param name="wrapper-name" select="local-name() || '-test-wrap'" />
         <xsl:with-param name="node-constructors" as="node()+">
            <xsl:apply-templates select="$expect-test" mode="x:node-constructor" />
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>

</xsl:stylesheet>