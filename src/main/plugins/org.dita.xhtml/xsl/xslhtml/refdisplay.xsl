<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="2.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
     xmlns:dita2html="http://dita-ot.sourceforge.net/ns/200801/dita2html"
     xmlns:xs="http://www.w3.org/2001/XMLSchema"
     exclude-result-prefixes="related-links xs dita2html">

<!-- == REFERENCE UNIQUE SUBSTRUCTURES == -->

  <xsl:template match="*[contains(@class,' reference/properties ')]" mode="dita2html:get-max-entry-count" as="xs:integer">
    <xsl:sequence select="3"/>
  </xsl:template>

<!-- Process the header row in a properties table -->
<xsl:template match="*[contains(@class,' reference/prophead ')]" name="topic.reference.prophead">
  <tr>
   <xsl:call-template name="setid"/>
   <xsl:call-template name="commonattributes"/>
   <xsl:value-of select="$newline"/>
     <!-- For each of the 3 entry types: If the entry is in this row, apply-templates.
          Otherwise, if it is ever in this table, create empty entry, and add ID for accessibility. -->
     <xsl:choose>      <!-- Process <proptype> -->
       <xsl:when test="*[contains(@class,' reference/proptypehd ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/proptypehd ')]"/>
       </xsl:when>
       <xsl:when test="following-sibling::*/*[contains(@class,' reference/proptype ')]">
         <th>
           <xsl:call-template name="style">
             <xsl:with-param name="contents">
               <xsl:text>vertical-align:bottom;</xsl:text>
               <xsl:call-template name="th-align"/>
             </xsl:with-param>
           </xsl:call-template>           
           <xsl:attribute name="id"><xsl:value-of select="generate-id(parent::*)"/>-type</xsl:attribute>
           <xsl:call-template name="getString">
             <xsl:with-param name="stringName" select="'Type'"/>
           </xsl:call-template>
         </th><xsl:value-of select="$newline"/>
       </xsl:when>
     </xsl:choose>
     <xsl:choose>      <!-- Process <propvalue> -->
       <xsl:when test="*[contains(@class,' reference/propvaluehd ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/propvaluehd ')]"/>
       </xsl:when>
       <xsl:when test="following-sibling::*/*[contains(@class,' reference/propvalue ')]">
         <th>
           <xsl:call-template name="style">
             <xsl:with-param name="contents">
               <xsl:text>vertical-align:bottom;</xsl:text>
               <xsl:call-template name="th-align"/>
             </xsl:with-param>
           </xsl:call-template>
           <xsl:attribute name="id"><xsl:value-of select="generate-id(parent::*)"/>-value</xsl:attribute>
           <xsl:call-template name="getString">
             <xsl:with-param name="stringName" select="'Value'"/>
           </xsl:call-template>
         </th><xsl:value-of select="$newline"/>
       </xsl:when>
     </xsl:choose>
     <xsl:choose>      <!-- Process <propdesc> -->
       <xsl:when test="*[contains(@class,' reference/propdeschd ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/propdeschd ')]"/>
       </xsl:when>
       <xsl:when test="following-sibling::*/*[contains(@class,' reference/propdesc ')]">
         <th>
           <xsl:call-template name="style">
             <xsl:with-param name="contents">
               <xsl:text>vertical-align:bottom;</xsl:text>
               <xsl:call-template name="th-align"/>
             </xsl:with-param>
           </xsl:call-template>           
           <xsl:attribute name="id"><xsl:value-of select="generate-id(parent::*)"/>-desc</xsl:attribute>
           <xsl:call-template name="getString">
             <xsl:with-param name="stringName" select="'Description'"/>
           </xsl:call-template>
         </th><xsl:value-of select="$newline"/>
       </xsl:when>
     </xsl:choose>
  </tr>
</xsl:template>

<!-- Add the headers attribute to a cell inside the properties table. This may be called from either
     a <property> row or from a cell inside the row. -->
<xsl:template name="addPropertiesHeadersAttribute">
  <xsl:param name="classVal"/>
  <xsl:param name="elementType"/>
  <xsl:attribute name="headers">
    <xsl:choose>
      <!-- First choice: if there is a matching cell inside a user-specified header, and it has an ID -->
      <xsl:when test="ancestor::*[contains(@class,' reference/properties ')]/*[1][contains(@class,' reference/prophead ')]/*[contains(@class,$classVal)]/@id">
        <xsl:value-of select="ancestor::*[contains(@class,' reference/properties ')]/*[1][contains(@class,' reference/prophead ')]/*[contains(@class,$classVal)]/@id"/>
      </xsl:when>
      <!-- Second choice: if there is a matching cell inside a user-specified header, use its generated ID -->
      <xsl:when test="ancestor::*[contains(@class,' reference/properties ')]/*[1][contains(@class,' reference/prophead ')]/*[contains(@class,$classVal)]">
        <xsl:value-of select="generate-id(ancestor::*[contains(@class,' reference/properties ')]/*[1][contains(@class,' reference/prophead ')]/*[contains(@class,$classVal)])"/>
      </xsl:when>
      <!-- Third choice: no user-specified header for this column. ID is based on the table's generated ID. -->
      <xsl:otherwise>
        <xsl:value-of select="generate-id(ancestor::*[contains(@class,' reference/properties ')])"/>-<xsl:value-of select="$elementType"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:template>

<!-- Process a standard row in the properties table. Apply-templates on the entries one at a time;
     if one is missing which should be present, create an empty cell. -->
<xsl:template match="*[contains(@class,' reference/property ')]" name="topic.reference.property">
  <!-- If there was no header, then this is the first child of properties; create default headers -->
  <xsl:if test=".=../*[1]">
    <tr><xsl:value-of select="$newline"/>
      <xsl:if test="../*/*[contains(@class,' reference/proptype ')]">
        <th id="{generate-id(parent::*)}-type">
          <xsl:call-template name="style">
            <xsl:with-param name="contents">
              <xsl:text>vertical-align:bottom;</xsl:text>
              <xsl:call-template name="th-align"/>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'Type'"/>
          </xsl:call-template>
        </th><xsl:value-of select="$newline"/>
      </xsl:if>
      <xsl:if test="../*/*[contains(@class,' reference/propvalue ')]">
        <th id="{generate-id(parent::*)}-value">
          <xsl:call-template name="style">
            <xsl:with-param name="contents">
              <xsl:text>vertical-align:bottom;</xsl:text>
              <xsl:call-template name="th-align"/>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'Value'"/>
          </xsl:call-template>
        </th><xsl:value-of select="$newline"/>
      </xsl:if>
      <xsl:if test="../*/*[contains(@class,' reference/propdesc ')]">
        <th id="{generate-id(parent::*)}-desc">
          <xsl:call-template name="style">
            <xsl:with-param name="contents">
              <xsl:text>vertical-align:bottom;</xsl:text>
              <xsl:call-template name="th-align"/>
            </xsl:with-param>
          </xsl:call-template>
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'Description'"/>
          </xsl:call-template>
        </th><xsl:value-of select="$newline"/>
      </xsl:if>
    </tr><xsl:value-of select="$newline"/>
  </xsl:if>
  <tr>
   <xsl:call-template name="setid"/>
   <xsl:call-template name="commonattributes"/>
   <xsl:value-of select="$newline"/>
     <!-- For each of the 3 entry types:
          - If it is in this row, apply
          - Otherwise, if it is in the table at all, create empty entry -->
     <xsl:choose>      <!-- Process or create proptype -->
       <xsl:when test="*[contains(@class,' reference/proptype ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/proptype ')]"/>
       </xsl:when>
       <xsl:when test="../*/*[contains(@class,' reference/proptype ')] | ../*[1]/*[contains(@class,' reference/proptypehd ')]">
         <td>    <!-- Create an empty cell. Add accessiblity attribute. -->
           <xsl:call-template name="addPropertiesHeadersAttribute">
             <xsl:with-param name="classVal"> reference/proptypehd </xsl:with-param>
             <xsl:with-param name="elementType">type</xsl:with-param>
           </xsl:call-template>
           <xsl:text>&#xA0;</xsl:text>
         </td><xsl:value-of select="$newline"/>
       </xsl:when>
     </xsl:choose>
     <xsl:choose>      <!-- Process or create propvalue -->
       <xsl:when test="*[contains(@class,' reference/propvalue ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/propvalue ')]"/>
       </xsl:when>
       <xsl:when test="../*/*[contains(@class,' reference/propvalue ')] | ../*[1]/*[contains(@class,' reference/propvaluehd ')]">
         <td>    <!-- Create an empty cell. Add accessiblity attribute. -->
           <xsl:call-template name="addPropertiesHeadersAttribute">
             <xsl:with-param name="classVal"> reference/propvaluehd </xsl:with-param>
             <xsl:with-param name="elementType">value</xsl:with-param>
           </xsl:call-template>
           <xsl:text>&#xA0;</xsl:text>
         </td><xsl:value-of select="$newline"/>
       </xsl:when>
     </xsl:choose>
     <xsl:choose>      <!-- Process or create propdesc -->
       <xsl:when test="*[contains(@class,' reference/propdesc ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/propdesc ')]"/>
       </xsl:when>
       <xsl:when test="../*/*[contains(@class,' reference/propdesc ')] | ../*[1]/*[contains(@class,' reference/propdeschd ')]">
         <td>    <!-- Create an empty cell. Add accessiblity attribute. -->
           <xsl:call-template name="addPropertiesHeadersAttribute">
             <xsl:with-param name="classVal"> reference/propdeschd </xsl:with-param>
             <xsl:with-param name="elementType">desc</xsl:with-param>
           </xsl:call-template>
           <xsl:text>&#xA0;</xsl:text>
         </td><xsl:value-of select="$newline"/>
       </xsl:when>
     </xsl:choose>
  </tr><xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' reference/proptype ')]" name="topic.reference.proptype">
  <xsl:apply-templates select="." mode="propertiesEntry">
    <xsl:with-param name="elementType">type</xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' reference/propvalue ')]" name="topic.reference.propvalue">
  <xsl:apply-templates select="." mode="propertiesEntry">
    <xsl:with-param name="elementType">value</xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' reference/propdesc ')]" name="topic.reference.propdesc">
  <xsl:apply-templates select="." mode="propertiesEntry">
    <xsl:with-param name="elementType">desc</xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<!-- Template based on the stentry template in dit2htm. Only change is to add elementType
     paramenter, and call addPropertiesHeadersAttribute instead of output-stentry-headers. -->
<xsl:template match="*" mode="propertiesEntry">
  <xsl:param name="elementType"/>
  <td>
    <xsl:call-template name="style">
      <xsl:with-param name="contents">
        <xsl:text>vertical-align:top;</xsl:text>     
      </xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="output-stentry-id"/>
    <xsl:call-template name="addPropertiesHeadersAttribute">
      <xsl:with-param name="classVal"> reference/prop<xsl:value-of select="$elementType"/>hd<xsl:text> </xsl:text></xsl:with-param>
      <xsl:with-param name="elementType"><xsl:value-of select="$elementType"/></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="commonattributes"/>
    <xsl:variable name="localkeycol">
      <xsl:choose>
        <xsl:when test="ancestor::*[contains(@class,' topic/simpletable ')]/@keycol">
          <xsl:value-of select="ancestor::*[contains(@class,' topic/simpletable ')]/@keycol"/>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Determine which column this entry is in. -->
    <xsl:variable name="thiscolnum"><xsl:value-of select="number(count(preceding-sibling::*[contains(@class,' topic/stentry ')])+1)"/></xsl:variable>
    <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
    <xsl:choose>
     <xsl:when test="$thiscolnum=$localkeycol">
      <strong>
        <xsl:call-template name="propentry-templates"/>
      </strong>
     </xsl:when>
     <xsl:otherwise>
       <xsl:call-template name="propentry-templates"/>
     </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </td><xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template name="propentry-templates">
 <xsl:choose>
  <xsl:when test="*[not(contains(@class,' ditaot-d/startprop ') or contains(@class,' dita-ot/endprop '))] | text() | processing-instruction()">
   <xsl:apply-templates/>
  </xsl:when>
  <xsl:otherwise>
    <!-- Add flags, then either @specentry or NBSP -->
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
    <xsl:choose>
      <xsl:when test="@specentry"><xsl:value-of select="@specentry"/></xsl:when>
      <xsl:otherwise>&#160;</xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

  <!-- References have their own group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='reference']" mode="related-links:get-group"
                name="related-links:group.reference"
                as="xs:string">
    <xsl:text>reference</xsl:text>
  </xsl:template>
  
  <!-- Priority of reference group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='reference']" mode="related-links:get-group-priority"
                name="related-links:group-priority.reference"
                as="xs:integer">
    <xsl:sequence select="1"/>
  </xsl:template>
  
  <!-- Reference wrapper for HTML: "Related reference" in <div>. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='reference']" mode="related-links:result-group"
                name="related-links:result.reference" as="element(linklist)">
    <xsl:param name="links"/>
    <xsl:if test="normalize-space(string-join($links, ''))">
      <linklist class="- topic/linklist " outputclass="relinfo relref">
        <title class="- topic/title ">
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'Related reference'"/>
          </xsl:call-template>
        </title>
        <xsl:copy-of select="$links"/>
      </linklist>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
