<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:template name="gethref">
    <xsl:param name="ditahref"/>
    
    <xsl:if test="contains($ditahref, '#')">
      <xsl:call-template name="swapext">
        <xsl:with-param name="newhref" select="substring-before($ditahref, '#')"/>
      </xsl:call-template>
    </xsl:if>   
    
    <xsl:if test="not(contains($ditahref, '#'))">
       <xsl:call-template name="swapext">
         <xsl:with-param name="newhref" select="$ditahref"/>
       </xsl:call-template>
    </xsl:if>       
  </xsl:template>

  
  <xsl:template name="swapext">
    <xsl:param name="newhref"/>
    
    <xsl:if test="substring($newhref, string-length($newhref) - string-length('.xml') +1) = '.xml'">
      <xsl:value-of select="concat(substring-before($newhref, '.xml'), '.html')"/>
    </xsl:if>
    <xsl:if test="substring($newhref, string-length($newhref) - string-length('.dita') +1) =
      '.dita'">
      <xsl:value-of select="concat(substring-before($newhref, '.dita'), '.html')"/>
    </xsl:if>
    
  </xsl:template>
  
  
</xsl:stylesheet>
