<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2010 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
    xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
    exclude-result-prefixes="related-links">

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
