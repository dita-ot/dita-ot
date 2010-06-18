<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
    xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
    xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
    xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
    xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
    xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
    xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
    xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
    xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
    xmlns:math="http://www.w3.org/1998/Math/MathML"
    xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
    xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
    xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
    xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
    xmlns:prodtools="http://www.ibm.com/xmlns/prodtools" 
    version="1.0" 
    xmlns:java="org.dita.dost.util.ImgUtils" 
    xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
    exclude-result-prefixes="java related-links">
    
    <xsl:output method="xml"/>
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <!-- Glossary entries belong in the group with concepts. -->
    <xsl:template match="*[contains(@class, ' topic/link ')][@type='glossentry']" mode="related-links:get-group" name="related-links:group.glossentry">
        <xsl:call-template name="related-links:group.concept"/>
    </xsl:template>
    
    <!-- Priority of glossary group is same as concept group. -->
    <xsl:template match="*[contains(@class, ' topic/link ')][@type='glossentry']" mode="related-links:get-group-priority" name="related-links:group-priority.glossentry">
        <xsl:call-template name="related-links:group-priority.concept"/>
    </xsl:template>
    
    <!-- Wrapper for glossentry (concept) group: "Related concepts" in a <div>. -->
    <xsl:template match="*[contains(@class, ' topic/link ')][@type='glossentry']" mode="related-links:result-group" name="related-links:result.glossentry">
        <xsl:param name="links"/>
        <xsl:call-template name="related-links:result.concept">
          <xsl:with-param name="links" select="$links"/>
        </xsl:call-template>
    </xsl:template>
    
</xsl:stylesheet>
