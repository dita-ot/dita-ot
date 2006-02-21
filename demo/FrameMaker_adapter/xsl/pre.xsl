<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Space inside "pre" and similar elements is preserved.
     FrameMaker read-write rule "reader line break is forced return" protects line breaks. -->
<xsl:template match="*[contains(@class, ' topic/pre ')
                    or contains(@class, ' sw-d/userinput ')
                    or contains(@class, ' sw-d/systemoutput ')
                    or contains(@class, ' pr-d/codeph ')]//text()">
  <!-- space into &#xA0; non-breaking space.
       hyphen into &#x2011; non-breaking hyphen. -->
  <xsl:value-of select="translate(.,' -','&#xA0;&#x2011;')"/>
</xsl:template>

</xsl:stylesheet>
