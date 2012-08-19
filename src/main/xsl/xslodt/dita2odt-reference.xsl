<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

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
  xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
  exclude-result-prefixes="related-links">
  
  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>

<!-- == REFERENCE UNIQUE SUBSTRUCTURES == -->
  
<!-- Simple Table -->
  <xsl:template match="*[contains(@class,' reference/properties ')]">
  
  	<xsl:variable name="tablenameId" select="generate-id(.)"/>
    
    <!-- start flagging --> 
  	<xsl:apply-templates select="." mode="start-add-odt-flags">
      <xsl:with-param name="family" select="'_table'"/>
    </xsl:apply-templates>  
  	
  	<xsl:choose>
    <xsl:when test="parent::*[contains(@class, ' reference/refbody ')]">
      <xsl:element name="table:table">
        <xsl:attribute name="table:name">
          <xsl:value-of select="concat('Table', $tablenameId)"/>
        </xsl:attribute>
        <xsl:attribute name="table:style-name">table_style</xsl:attribute>
        
        <xsl:variable name="colnumNum">
          <xsl:call-template name="count_columns_for_properties"/>
        </xsl:variable>
        <xsl:call-template name="create_columns_for_simpletable">
          <xsl:with-param name="column" select="$colnumNum"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:when>
    
    <xsl:otherwise>
      <xsl:variable name="span_depth">
        <xsl:call-template name="calculate_span_depth"/>
      </xsl:variable>
      <!-- break span tags -->
      <xsl:call-template name="break_span_tags">
        <xsl:with-param name="depth" select="$span_depth"/>
        <xsl:with-param name="order" select="'0'"/>
      </xsl:call-template>
      <!-- break first p tag if there are span tags -->
      <xsl:if test="$span_depth &gt;= 0">
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
      </xsl:if>
      <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
      <!-- start render table -->
      <xsl:element name="table:table">
        <xsl:attribute name="table:name">
          <xsl:value-of select="concat('Table', $tablenameId)"/>
        </xsl:attribute>
        <xsl:attribute name="table:style-name">table_style</xsl:attribute>
        
        <xsl:variable name="colnumNum">
          <xsl:call-template name="count_columns_for_properties"/>
        </xsl:variable>
        <xsl:call-template name="create_columns_for_simpletable">
          <xsl:with-param name="column" select="$colnumNum"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:element>
      <!-- start p tag again if there are span tags-->
      <xsl:if test="$span_depth &gt;= 0">
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
      </xsl:if>
      <!--  span tags span tags again-->
      <xsl:call-template name="break_span_tags">
        <xsl:with-param name="depth" select="$span_depth"/>
        <xsl:with-param name="order" select="'1'"/>
      </xsl:call-template>
    </xsl:otherwise>
    
  </xsl:choose>
  <text:p/>
  
  <!-- end flagging -->
  <xsl:apply-templates select="." mode="end-add-odt-flags">
      <xsl:with-param name="family" select="'_table'"/>
  </xsl:apply-templates>
</xsl:template>  
  
<xsl:template match="*[contains(@class, ' reference/proptypehd ')]
  |*[contains(@class, ' reference/propvaluehd ')]
  |*[contains(@class, ' reference/propdeschd ')]" mode="create_header">
  <xsl:element name="table:table-cell">
    <xsl:attribute name="office:value-type">string</xsl:attribute>
    <xsl:call-template name="create_style_properties"/>
    
    <xsl:apply-templates/>
  
  </xsl:element>
  
</xsl:template>

<!-- Process the header row in a properties table -->
<xsl:template match="*[contains(@class,' reference/prophead ')]" name="topic.reference.prophead">
  <xsl:param name="width-multiplier"/>
  <table:table-header-rows>
    <table:table-row>
      <!-- create proptype header -->
      <xsl:choose>      
        <xsl:when test="*[contains(@class,' reference/proptypehd ')]">
          <xsl:apply-templates select="*[contains(@class,' reference/proptypehd ')]" mode="create_header"/>
        </xsl:when>
        <xsl:when test="following-sibling::*/*[contains(@class,' reference/proptype ')]">
          <xsl:element name="table:table-cell">
            <xsl:attribute name="office:value-type">string</xsl:attribute>
            <!-- propertype is always 1nd column in the 1st row -->
            <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
            <xsl:element name="text:p">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'Type'"/>
                </xsl:call-template>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:when>
      </xsl:choose>
      <!-- create propvalue header -->
      <xsl:choose>     
        <xsl:when test="*[contains(@class,' reference/propvaluehd ')]">
          <xsl:apply-templates  select="*[contains(@class,' reference/propvaluehd ')]" mode="create_header"/>
        </xsl:when>
        <xsl:when test="following-sibling::*/*[contains(@class,' reference/propvalue ')]">
          <xsl:element name="table:table-cell">
            <xsl:attribute name="office:value-type">string</xsl:attribute>
            <!-- check column location -->
            <xsl:choose>
              <!-- 1nd column in the 1st row -->
              <xsl:when test="not(following-sibling::*/*[contains(@class,' reference/proptype ')])">
                <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <!-- other columns in the 1st row -->
                <xsl:attribute name="table:style-name">cell_style_2</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:element name="text:p">
              <!-- cell belongs to sthead -->
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'Value'"/>
                </xsl:call-template>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:when>
      </xsl:choose>
      
      <!-- create propdesc header -->
      <xsl:choose>
        <xsl:when test="*[contains(@class,' reference/propdeschd ')]">
          <xsl:apply-templates select="*[contains(@class,' reference/propdeschd ')]" mode="create_header"/>
        </xsl:when>
        <xsl:when test="following-sibling::*/*[contains(@class,' reference/propdesc ')]">
          <xsl:element name="table:table-cell">
            <xsl:attribute name="office:value-type">string</xsl:attribute>
            <!-- check column location -->
            <xsl:choose>
              <!-- 1nd column in the 1st row -->
              <xsl:when test="not(following-sibling::*/*[contains(@class,' reference/proptype ')]) 
                and not(following-sibling::*/*[contains(@class,' reference/propvalue ')])">
                <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <!-- other columns in the 1st row -->
                <xsl:attribute name="table:style-name">cell_style_2</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:element name="text:p">
              <!-- cell belongs to sthead -->
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'Description'"/>
                </xsl:call-template>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:when>
      </xsl:choose>
     </table:table-row>
  </table:table-header-rows>
</xsl:template>

<!-- Process a standard row in the properties table. Apply-templates on the entries one at a time;
     if one is missing which should be present, create an empty cell. -->
<xsl:template match="*[contains(@class,' reference/property ')]" name="topic.reference.property">
  <xsl:param name="width-multiplier"/>
  <!-- If there was no header, then this is the first child of properties; create default headers -->
  <xsl:if test=".=../*[1]">
    <table:table-header-rows>
      <table:table-row>
      <xsl:if test="../*/*[contains(@class,' reference/proptype ')]">
        <xsl:element name="table:table-cell">
          <xsl:attribute name="office:value-type">string</xsl:attribute>
          <!-- propertype is always 1nd column in the 1st row -->
          <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
          <xsl:element name="text:p">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Type'"/>
              </xsl:call-template>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:if>
      <xsl:if test="../*/*[contains(@class,' reference/propvalue ')]">
        <xsl:element name="table:table-cell">
          <xsl:attribute name="office:value-type">string</xsl:attribute>
          <!-- check column location -->
          <xsl:choose>
            <!-- 1nd column in the 1st row -->
            <xsl:when test="not(../*/*[contains(@class,' reference/proptype ')])">
              <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <!-- other columns in the 1st row -->
              <xsl:attribute name="table:style-name">cell_style_2</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:element name="text:p">
            <!-- cell belongs to sthead -->
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Value'"/>
              </xsl:call-template>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:if>
      <xsl:if test="../*/*[contains(@class,' reference/propdesc ')]">
        <xsl:element name="table:table-cell">
          <xsl:attribute name="office:value-type">string</xsl:attribute>
          <!-- check column location -->
          <xsl:choose>
            <!-- 1nd column in the 1st row -->
            <xsl:when test="not(../*/*[contains(@class,' reference/proptype ')]) 
              and not(../*/*[contains(@class,' reference/propvalue ')])">
              <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <!-- other columns in the 1st row -->
              <xsl:attribute name="table:style-name">cell_style_2</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:element name="text:p">
            <!-- cell belongs to sthead -->
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Description'"/>
              </xsl:call-template>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:if>
      </table:table-row>
    </table:table-header-rows>
  </xsl:if>
     <!-- For each of the 3 entry types:
          - If it is in this row, apply
          - Otherwise, if it is in the table at all, create empty entry -->
  <table:table-row>
     <xsl:choose>      <!-- Process or create proptype -->
       <xsl:when test="*[contains(@class,' reference/proptype ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/proptype ')]">
           <xsl:with-param name="width-multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
         </xsl:apply-templates>
       </xsl:when>
       <!-- create an empty proptype -->
       <xsl:when test="../*/*[contains(@class,' reference/proptype ')] | ../*[1]/*[contains(@class,' reference/proptypehd ')]">
         <xsl:element name="table:table-cell">
           <xsl:attribute name="office:value-type">string</xsl:attribute>
           <!-- this cell is in the first column. -->
           <xsl:attribute name="table:style-name">cell_style_3</xsl:attribute>
             <xsl:element name="text:p">
               <!-- Create an empty cell. Add accessiblity attribute. -->
               <!-- 
               <xsl:call-template name="addPropertiesHeadersAttribute">
                 <xsl:with-param name="classVal"> reference/proptypehd </xsl:with-param>
                 <xsl:with-param name="elementType">type</xsl:with-param>
               </xsl:call-template>
               -->
               <!-- 
               <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
               -->
               <xsl:text> </xsl:text>
             </xsl:element>
           </xsl:element>
       </xsl:when>
     </xsl:choose>
     <xsl:choose>      <!-- Process or create propvalue -->
       <xsl:when test="*[contains(@class,' reference/propvalue ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/propvalue ')]">
           <xsl:with-param name="width-multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
         </xsl:apply-templates>
       </xsl:when>
       <!-- create an empty propvalue -->
       <xsl:when test="../*/*[contains(@class,' reference/propvalue ')] | ../*[1]/*[contains(@class,' reference/propvaluehd ')]">
         <xsl:element name="table:table-cell">
           <xsl:attribute name="office:value-type">string</xsl:attribute>
           <xsl:choose>
             <!-- this cell is in the first column. -->
             <xsl:when test="not(../*/*[contains(@class,' reference/proptype ')]) and 
               not(../*[1]/*[contains(@class,' reference/proptypehd ')])">
               <xsl:attribute name="table:style-name">cell_style_3</xsl:attribute>
             </xsl:when>
             <xsl:otherwise>
               <xsl:attribute name="table:style-name">cell_style_4</xsl:attribute>
             </xsl:otherwise>
           </xsl:choose>
             <xsl:element name="text:p">
               <xsl:call-template name="addPropertiesHeadersAttribute">
                 <xsl:with-param name="classVal"> reference/propvaluehd </xsl:with-param>
                 <xsl:with-param name="elementType">value</xsl:with-param>
               </xsl:call-template>
               <xsl:text> </xsl:text>
             </xsl:element>
           </xsl:element>
       </xsl:when>
     </xsl:choose>
     <xsl:choose>      <!-- Process or create propdesc -->
       <xsl:when test="*[contains(@class,' reference/propdesc ')]">
         <xsl:apply-templates select="*[contains(@class,' reference/propdesc ')]">
           <xsl:with-param name="width-multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
         </xsl:apply-templates>
       </xsl:when>
       <!-- create an empty propdesc -->
       <xsl:when test="../*/*[contains(@class,' reference/propdesc ')] | ../*[1]/*[contains(@class,' reference/propdeschd ')]">
         <xsl:element name="table:table-cell">
           <xsl:attribute name="office:value-type">string</xsl:attribute>
           <xsl:choose>
             <!-- this cell is in the first column. -->
             <xsl:when test="not(../*/*[contains(@class,' reference/proptype ')]) and 
               not(../*[1]/*[contains(@class,' reference/proptypehd ')]) and 
               not(../*/*[contains(@class,' reference/propvalue ')]) and 
               not(../*[1]/*[contains(@class,' reference/propvaluehd ')])">
               <xsl:attribute name="table:style-name">cell_style_3</xsl:attribute>
             </xsl:when>
             <xsl:otherwise>
               <xsl:attribute name="table:style-name">cell_style_4</xsl:attribute>
             </xsl:otherwise>
           </xsl:choose>
           
           <xsl:element name="text:p">
             <xsl:call-template name="addPropertiesHeadersAttribute">
               <xsl:with-param name="classVal"> reference/propdeschd </xsl:with-param>
               <xsl:with-param name="elementType">desc</xsl:with-param>
             </xsl:call-template>
             <xsl:text> </xsl:text>
           </xsl:element>
         </xsl:element>
       </xsl:when>
     </xsl:choose>
    </table:table-row>
</xsl:template>

<xsl:template match="*[contains(@class,' reference/proptype ')]" name="topic.reference.proptype">
  <xsl:param name="width-multiplier">0</xsl:param>
  <xsl:apply-templates select="." mode="propertiesEntry">
    <xsl:with-param name="width-multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
    <xsl:with-param name="elementType">type</xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' reference/propvalue ')]" name="topic.reference.propvalue">
  <xsl:param name="width-multiplier">0</xsl:param>
  <xsl:apply-templates select="." mode="propertiesEntry">
    <xsl:with-param name="width-multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
    <xsl:with-param name="elementType">value</xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' reference/propdesc ')]" name="topic.reference.propdesc">
  <xsl:param name="width-multiplier">0</xsl:param>
  <xsl:apply-templates select="." mode="propertiesEntry">
    <xsl:with-param name="width-multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
    <xsl:with-param name="elementType">desc</xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<!-- Add the headers attribute to a cell inside the properties table. This may be called from either
  a <property> row or from a cell inside the row. -->
<xsl:template name="addPropertiesHeadersAttribute">
  <xsl:param name="classVal"/>
  <xsl:param name="elementType"/>
  <xsl:attribute name="id">
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


<!-- Template based on the stentry template in dit2htm. Only change is to add elementType
     paramenter, and call addPropertiesHeadersAttribute instead of output-stentry-headers. -->
<xsl:template match="*" mode="propertiesEntry">
  <xsl:param name="width-multiplier">0</xsl:param>
  <xsl:param name="elementType"/>
  <xsl:element name="table:table-cell">
    <xsl:attribute name="office:value-type">string</xsl:attribute>
    <xsl:call-template name="create_style_properties"/>

    <xsl:call-template name="output-stentry-id"/>
    <!-- 
    <xsl:call-template name="addPropertiesHeadersAttribute">
      <xsl:with-param name="classVal"> reference/prop<xsl:value-of select="$elementType"/>hd<xsl:text> </xsl:text></xsl:with-param>
      <xsl:with-param name="elementType"><xsl:value-of select="$elementType"/></xsl:with-param>
    </xsl:call-template>
    -->
    
    <xsl:variable name="localkeycol">
      <xsl:choose>
        <xsl:when test="ancestor::*[contains(@class,' topic/simpletable ')]/@keycol">
          <xsl:value-of select="ancestor::*[contains(@class,' topic/simpletable ')]/@keycol"/>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
      <!-- Determine which column this entry is in. -->
      <xsl:variable name="thiscolnum"><xsl:value-of select="number(count(preceding-sibling::*)+1)"/></xsl:variable>
    <!-- Determine which column this entry is in. -->
    <xsl:choose>
     <xsl:when test="$thiscolnum=$localkeycol">
       <xsl:element name="text:span">
         <xsl:attribute name="text:style-name">bold</xsl:attribute>
         <xsl:call-template name="propentry-templates"/>
       </xsl:element>
     </xsl:when>
     <xsl:otherwise>
      <xsl:call-template name="propentry-templates"/>
     </xsl:otherwise>
    </xsl:choose>
      
  </xsl:element>
</xsl:template>

<xsl:template name="propentry-templates">
 <xsl:choose>
  <xsl:when test="*|text()|processing-instruction()">
   <xsl:apply-templates/>
  </xsl:when>
  <xsl:when test="@specentry">
   <xsl:value-of select="@specentry"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>  <!-- nbsp -->
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>
  
  <xsl:template name="create_style_properties">
    <!-- create style attribute -->
    <xsl:variable name="colpos">
      <xsl:choose>
        <xsl:when test="contains(@class, ' reference/proptype ') or contains(@class, ' reference/proptypehd ')">
          <xsl:value-of select="1"/>
        </xsl:when>
        <xsl:when test="contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propvaluehd ')">
          <xsl:choose>
            <xsl:when test="../../*/*[contains(@class,' reference/proptype ')] | ../../*[1]/*[contains(@class,' reference/proptypehd ')]">
              <xsl:value-of select="2"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="1"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="contains(@class, ' reference/propdesc ') or contains(@class, ' reference/propdeschd')">
          <xsl:choose>
            <xsl:when test="../../*/*[contains(@class,' reference/propvalue ')] | ../../*[1]/*[contains(@class,' reference/propvaluehd ')] |
              ../../*/*[contains(@class,' reference/proptype ')] | ../../*[1]/*[contains(@class,' reference/proptypehd ')]">
              <xsl:value-of select="2"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="1"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="rowpos">
      <xsl:choose>
        <!-- row belongs to thead -->
        <xsl:when test="parent::*[contains(@class, ' topic/sthead ')]">
          <xsl:value-of select="1"/>
        </xsl:when>
        <!-- there's no thead -->
        <!-- thead will be created in properties. -->
        <xsl:otherwise>
          <xsl:value-of select="2"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:choose>
      <!-- first column and first row -->
      <xsl:when test="$rowpos = 1 and $colpos = 1 ">
        <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
      </xsl:when>
      <!-- not first column but first row -->
      <xsl:when test="$rowpos = 1 and $colpos != 1 ">
        <xsl:attribute name="table:style-name">cell_style_2</xsl:attribute>
      </xsl:when>
      <!-- first column but not first row -->
      <xsl:when test="$rowpos != 1 and $colpos = 1 ">
        <xsl:attribute name="table:style-name">cell_style_3</xsl:attribute>
      </xsl:when>
      <!-- other cells -->
      <xsl:otherwise>
        <xsl:attribute name="table:style-name">cell_style_4</xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="count_columns_for_properties">
    <xsl:choose>
      <!-- has desc -->
      <xsl:when test="child::*[contains(@class, ' reference/prophead ')]
        /child::*[contains(@class, ' reference/propdeschd ')] | 
        child::*[contains(@class, ' reference/property ')]
        /child::*[contains(@class, ' reference/propdesc ')]">
        <xsl:value-of select="3"/>
        
      </xsl:when>
      <!-- only has value -->
      <xsl:when test="child::*[contains(@class, ' reference/prophead ')]
        /child::*[contains(@class, ' reference/propvaluehd ')] | 
        child::*[contains(@class, ' reference/property ')]
        /child::*[contains(@class, ' reference/propvalue ')]">
        <xsl:value-of select="2"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="1"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!--Get Related Information Reference-->
  <!-- References have their own group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='reference']" mode="related-links:get-group" name="related-links:group.reference">
    <xsl:text>reference</xsl:text>
  </xsl:template>
  
  <!-- Priority of reference group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='reference']" mode="related-links:get-group-priority" name="related-links:group-priority.reference">
    <xsl:value-of select="1"/>
  </xsl:template>
  
  <!-- Reference wrapper for HTML: "Related reference" in <div>. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='reference']" mode="related-links:result-group" name="related-links:result.reference">
    <xsl:param name="links"/>
    
    <xsl:variable name="samefile">
      <xsl:call-template name="check_file_location"/>
    </xsl:variable>
    <xsl:variable name="href-value">
      <xsl:call-template name="format_href_value"/>
    </xsl:variable>
    
    <xsl:element name="text:p">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Related references'"/>
        </xsl:call-template>
      </xsl:element>
    </xsl:element>
    
    <xsl:element name="text:p">
      <xsl:call-template name="create_related_links">
        <xsl:with-param name="samefile" select="$samefile"/>
        <xsl:with-param name="href-value" select="$href-value"/>
      </xsl:call-template>
    </xsl:element>
  </xsl:template>
  
  <!-- 
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='reference']">
    <xsl:param name="links"/>
    
    <xsl:variable name="samefile">
      <xsl:call-template name="check_file_location"/>
    </xsl:variable>
    <xsl:variable name="href-value">
      <xsl:call-template name="format_href_value"/>
    </xsl:variable>
    
    
    <xsl:element name="text:span">
      <xsl:attribute name="text:style-name">bold</xsl:attribute>
      <xsl:call-template name="getStringODT">
        <xsl:with-param name="stringName" select="'Related references'"/>
      </xsl:call-template>
    </xsl:element>
    <xsl:element name="text:line-break"/>
    
    <xsl:call-template name="create_related_links">
      <xsl:with-param name="samefile" select="$samefile"/>
      <xsl:with-param name="href-value" select="$href-value"/>
    </xsl:call-template>
  </xsl:template>
  -->
</xsl:stylesheet>
