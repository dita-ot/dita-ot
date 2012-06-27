<?xml version='1.0'?>

<xsl:stylesheet 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:fo="http://www.w3.org/1999/XSL/Format" 
xmlns:ot-placeholder="http://suite-sol.com/namespaces/ot-placeholder"
exclude-result-prefixes="ot-placeholder"
version="2.0">

    <xsl:template match="ot-placeholder:glossarylist">
    
            <fo:page-sequence master-reference="glossary-sequence" xsl:use-attribute-sets="__force__page__count">
                <xsl:call-template name="insertGlossaryStaticContents"/>
                <fo:flow flow-name="xsl-region-body">
                    <fo:marker marker-class-name="current-header">
                      <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Glossary'"/>
                      </xsl:call-template>
                    </fo:marker>

                    <fo:block xsl:use-attribute-sets="__glossary__label" id="{$id.glossary}">
                        <xsl:call-template name="insertVariable">
                            <xsl:with-param name="theVariableID" select="'Glossary'"/>
                        </xsl:call-template>
                    </fo:block>

                    <xsl:apply-templates/>

                    <!--xsl:apply-templates select="/descendant-or-self::*[contains(@class, ' glossentry/glossentry ')]" mode="glossary"/-->
               
                </fo:flow>
            </fo:page-sequence>
       
    </xsl:template>

    <xsl:template match="ot-placeholder:glossarylist//*[contains(@class, ' glossentry/glossentry ')]">
        <fo:block>
            <xsl:call-template name="commonattributes"/>
            <fo:block>
                <xsl:attribute name="id">
                    <xsl:call-template name="generate-toc-id"/>
                </xsl:attribute>
                <fo:block xsl:use-attribute-sets="__glossary__term">
                    <xsl:apply-templates select="*[contains(@class, ' glossentry/glossterm ')]/node()"/>
                </fo:block>
                <fo:block xsl:use-attribute-sets="__glossary__def">
                    <xsl:apply-templates select="*[contains(@class, ' glossentry/glossdef ')]/node()"/>
                </fo:block>
            </fo:block>
        </fo:block>
    </xsl:template>

</xsl:stylesheet>
