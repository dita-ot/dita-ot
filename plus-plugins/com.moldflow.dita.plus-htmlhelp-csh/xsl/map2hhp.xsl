<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:modular="http://www.moldflow.com/namespace/2008/dita/plus-htmlhelp-hhp-modular"
    exclude-result-prefixes="modular">
    
    <xsl:template match="/ | node()" mode="modular:alias">
        <xsl:text>#include alias.h
</xsl:text>
        <!-- Daisy-chain on to other code that needs to add to [ALIAS]. -->
        <xsl:next-match>
            <xsl:fallback>
                <xsl:message terminate="no">
                    <xsl:text>Cannot use xsl:next-match in XSLT 1.0 processor</xsl:text>
                </xsl:message>
            </xsl:fallback>
        </xsl:next-match>
    </xsl:template>
    
</xsl:stylesheet>
