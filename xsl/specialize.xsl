<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  specialize.xsl
 | Convert "generalized" DITA topics back into specialized form
 *-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
     <xsl:output method="xml" indent="no"/>
     <!--Find the class attribute within the XML instance document. -->
     <xsl:template match="*[@class]">
          <xsl:call-template name="specialize">
               <xsl:with-param name="class" select="@class"/>
          </xsl:call-template>
     </xsl:template>
     <!--Look for the last class definition, i.e, task/steps. Replace existing generalized element name
      with specialized element name -->
     <xsl:template name="specialize">
          <xsl:param name="class"/>
          <xsl:choose>
               <xsl:when test="contains(normalize-space($class),' ')">
                    <xsl:call-template name="specialize">
                         <xsl:with-param name="class" select="substring-after($class,' ')"/>
                    </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                    <xsl:variable name="element_name" select="substring-after(normalize-space($class),'/')" />
                         <xsl:element name="{$element_name}">
                                   <xsl:copy-of select="@*[local-name() != 'class']"/>
                              <xsl:apply-templates/>
                         </xsl:element>
               </xsl:otherwise>
          </xsl:choose>
     </xsl:template>
    <!--pick up everything from the element -->
     <xsl:template match="*|@*|comment()|processing-instruction()|text()">
       <xsl:copy>
         <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
       </xsl:copy>
     </xsl:template>

</xsl:stylesheet>

