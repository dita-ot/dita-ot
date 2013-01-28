<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->
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
  >

<xsl:output method="xml"/>
<xsl:output indent="yes"/>
<xsl:strip-space elements="*"/>

<!-- =========== CALS (OASIS) TABLE =========== -->
<xsl:template match="*[contains(@class,' topic/table ')]" name="topic.table">
  
  <!-- render table -->
  <xsl:call-template name="render_table"/>
  
</xsl:template>

  
<xsl:template name="create_columns_for_table">
  <xsl:param name="column" select="0"/>
  <xsl:if test="$column &gt; 0">
    <table:table-column/>
    <xsl:call-template name="create_columns_for_table">
      <xsl:with-param name="column" select="$column - 1"></xsl:with-param>
    </xsl:call-template>
  </xsl:if>  
</xsl:template>

<xsl:template name="dotable">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/tgroup ')]" name="topic.tgroup">
  <xsl:variable name="tablenameId" select="generate-id(.)"/>
  
  <xsl:variable name="columnNum">
    <xsl:call-template name="count_columns_for_table"/>
  </xsl:variable>
  
  <!-- start flagging -->
  <xsl:apply-templates select="parent::*[contains(@class, ' topic/table ')]" mode="start-add-odt-flags">
    <xsl:with-param name="family" select="'_table'"/>
  </xsl:apply-templates>
  
  <xsl:element name="table:table">
    <xsl:attribute name="table:name">
      <xsl:value-of select="concat('Table', $tablenameId)"/>
    </xsl:attribute>
    <!-- table background flagging -->
    <xsl:apply-templates select="parent::*[contains(@class, ' topic/table ')]" mode="start-add-odt-flags">
      <xsl:with-param name="family" select="'_table_attr'"/>
    </xsl:apply-templates>
    <xsl:call-template name="create_columns_for_table">
      <xsl:with-param name="column" select="$columnNum"/>
    </xsl:call-template>
    <xsl:call-template name="dotable"/> 
  </xsl:element>
  <!-- end flagging -->
  <xsl:apply-templates select="parent::*[contains(@class, ' topic/table ')]" mode="end-add-odt-flags">
    <xsl:with-param name="family" select="'_table'"/>
  </xsl:apply-templates>

</xsl:template>
  
  
  <xsl:template name="count_columns_for_table">
    <xsl:choose>
      <xsl:when test="@cols">
        <xsl:value-of select="@cols"/>
      </xsl:when>
      <xsl:when test="not(child::*[contains(@class, ' topic/colspec ')])">
        <xsl:choose>
          <xsl:when test="child::*[contains(@class, ' topic/thead ')]">
            <xsl:value-of select="count(child::*[contains(@class, ' topic/thead ')][1]
              /child::*[contains(@class, ' topic/row ')][1]
              /child::*[contains(@class, ' topic/entry ')])"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="count(descendant::*[contains(@class, ' topic/row ')][1]
              /child::*[contains(@class, ' topic/entry ')])"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="count(child::*[contains(@class, ' topic/colspec ')])"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<xsl:template match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')]">
<xsl:variable name="ancestorlang">
<xsl:call-template name="getLowerCaseLang"/>
</xsl:variable>
  <xsl:variable name="tbl-count-actual" select="count(preceding::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]/*[contains(@class,' topic/title ')])+1"/>

  
  <xsl:element name="text:p">
    <xsl:attribute name="text:style-name">center</xsl:attribute>
    <xsl:element name="text:span">
      <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:choose>
          <!-- Hungarian: "1.
          Table " -->
          <xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) or
            (string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )">
            <xsl:value-of
            select="$tbl-count-actual"/><xsl:text>. </xsl:text>
            <xsl:call-template
            name="getString">
              <xsl:with-param name="stringName" select="'Table'"/>
            </xsl:call-template>
            <xsl:text>
      </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="getStringODT">
              <xsl:with-param
              name="stringName" select="'Table'"/>
            </xsl:call-template>
            <xsl:text> </xsl:text>
            <xsl:value-of select="$tbl-count-actual"/><xsl:text>. </xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="get-ascii">
          <xsl:with-param name="txt"><xsl:value-of select="."/></xsl:with-param>
        </xsl:call-template>
    </xsl:element>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/desc ')]">
  <xsl:element name="text:p">
    <xsl:attribute name="text:style-name">center</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/colspec ')]"/>


<xsl:template match="*[contains(@class,' topic/spanspec ')]"></xsl:template>

<xsl:template match="*[contains(@class,' topic/thead ')]" name="topic.thead">
  <xsl:apply-templates/>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/tfoot ')]"/>

<xsl:template match="*[contains(@class,' topic/tbody ')]" name="topic.tbody">
  <xsl:apply-templates/>
    <!-- process table footer -->
    <xsl:apply-templates select="../*[contains(@class,' topic/tfoot ')]" mode="gen-tfoot" />
</xsl:template>

<!-- special mode for table footers -->
<xsl:template match="*[contains(@class,' topic/tfoot ')]" mode="gen-tfoot">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/row ')]" name="topic.row">
  
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class,' topic/thead ')]">
      <xsl:element name="table:table-header-rows">
        <xsl:element name="table:table-row">
          <xsl:apply-templates select="*[contains(@class,' topic/entry ')][1]"/>
        </xsl:element>
      </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <xsl:element name="table:table-row">
        <xsl:apply-templates select="*[contains(@class,' topic/entry ')][1]"/>
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="count-colmax">
  <xsl:choose>
    <xsl:when test="../../../*[contains(@class,' topic/colspec ')]">
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')]))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="number(count(../../*[contains(@class,' topic/row ')][1]/*[contains(@class,' topic/entry ')]))"/>
    </xsl:otherwise>
  </xsl:choose>  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/entry ')]" name="topic.entry">
  <xsl:param name="currentpos" select="1"/>
    <xsl:variable name="rowspan">
      <xsl:call-template name="get-rows-span"/>
    </xsl:variable>
    <xsl:variable name="col-max-num">
      <xsl:call-template name="count-colmax"/>    
    </xsl:variable>
    <xsl:variable name="startpos">
      <xsl:call-template name="find-entry-start-position"/>
    </xsl:variable>
    <xsl:variable name="endpos">
      <xsl:call-template name="find-entry-end-position">
        <xsl:with-param name="startposition" select="$startpos"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="$startpos &gt; $currentpos">
      <!-- render col/row spanned cell.-->
      <xsl:call-template name="emit-empty-cell">
        <xsl:with-param name="startpos" select="$startpos"/>
        <xsl:with-param name="currentpos" select="$currentpos"/>
      </xsl:call-template>
    </xsl:if>
    <!-- finding colspan -->
    <xsl:variable name="colspan">
      <xsl:call-template name="find-colspan"/>
    </xsl:variable>
    <!-- 
    <xsl:choose>
        <xsl:when test="parent::*/parent::*[contains(@class,' topic/thead ')]">
            <xsl:call-template name="topic.thead_entry"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="topic.tbody_entry"/>
        </xsl:otherwise>
    </xsl:choose>
    -->
    <xsl:element name="table:table-cell">
      <xsl:attribute name="office:value-type">string</xsl:attribute>
      <xsl:if test="$colspan &gt; 1">
        <xsl:attribute name="table:number-columns-spanned">
          <xsl:value-of select="$colspan"/>
        </xsl:attribute>
      </xsl:if>
      
      <xsl:if test="$rowspan &gt; 1">
        <xsl:attribute name="table:number-rows-spanned">
          <xsl:value-of select="$rowspan"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="create_style_table"/>
      
      <xsl:element name="text:p">
      	<xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>  
      	<xsl:apply-templates/>
      </xsl:element>
      
    </xsl:element>
    <!-- render col spanned cell.-->
    <!-- 
    <xsl:call-template name="emit-empty-cell-for-colspan">
      <xsl:with-param name="span" select="$colspan"/>
    </xsl:call-template>
    -->
    <xsl:if test="following-sibling::*[contains(@class,' topic/entry ')]">
      <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/entry ')][1]">
        <xsl:with-param name="currentpos" select="$endpos + 1"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="not(following-sibling::*[contains(@class,' topic/entry ')]) and not(($endpos + 1) &gt; $col-max-num)">
        <!-- if this is the last entry in current row and next position is not greater than the number of columns in a row-->
        <xsl:call-template name="emit-empty-cell">
          <xsl:with-param name="startpos" select="$col-max-num + 1"/> <!-- make sure the remaining columns will be generated -->
          <xsl:with-param name="currentpos" select="$endpos + 1"/>
        </xsl:call-template>
    </xsl:if>
    <!-- get text node -->
    
</xsl:template>
<!-- create style for table
  for simpletable refer create_style_stable template 
-->  
<xsl:template name="create_style_table">
  <!-- create style attribute -->
  <xsl:variable name="colpos" select="number(substring-after(@colname, 'col'))"/>
  <xsl:variable name="rowpos">
    <xsl:choose>
      <!-- row belongs to thead -->
      <xsl:when test="parent::*[contains(@class, ' topic/row ')]
        /parent::*[contains(@class, ' topic/thead ')]">
        <xsl:value-of select="1"/>
      </xsl:when>
      <!-- there's no thead -->
      <xsl:when test="not(parent::*[contains(@class, ' topic/row ')]
        /parent::*[contains(@class, ' topic/tbody ')]
        /preceding-sibling::*[contains(@class, ' topic/thead ')])">
        <xsl:value-of select="count(parent::*[contains(@class, ' topic/row ')]/
          preceding-sibling::*[contains(@class, ' topic/row ')]) + 1"/>
      </xsl:when>
      <!-- there is thead and row belongs to tbody -->
      <xsl:when test="parent::*[contains(@class, ' topic/row ')]
        /parent::*[contains(@class, ' topic/tbody ')]
        /preceding-sibling::*[contains(@class, ' topic/thead ')]">
        <xsl:value-of select="2"/>
      </xsl:when>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:choose>
    <!-- first cell in the first row -->
    <xsl:when test="$rowpos = 1 and $colpos = 1 ">
      <xsl:attribute name="table:style-name">cell_style_1</xsl:attribute>
    </xsl:when>
    <!-- not first cell but in the first row -->
    <xsl:when test="$rowpos = 1 and $colpos != 1 ">
      <xsl:attribute name="table:style-name">cell_style_2</xsl:attribute>
    </xsl:when>
    <!-- first cell but not in the first row -->
    <xsl:when test="$rowpos != 1 and $colpos = 1 ">
      <xsl:attribute name="table:style-name">cell_style_3</xsl:attribute>
    </xsl:when>
    <!-- other cells -->
    <xsl:otherwise>
      <xsl:attribute name="table:style-name">cell_style_4</xsl:attribute>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- do header entries -->
<xsl:template name="topic.thead_entry">
  <xsl:call-template name="doentry"/>
</xsl:template>

<!-- do body entries -->
<xsl:template name="topic.tbody_entry">
  <xsl:call-template name="doentry"/>  
</xsl:template>

<xsl:template name="doentry">
  <!--xsl:variable name="colspan-num">
    <xsl:call-template name="find-colspan"></xsl:call-template>
  </xsl:variable>
  <xsl:variable name="empty-cell-num" select="$colspan-num - 1"/-->
<xsl:apply-templates/>
  <!--xsl:call-template name="emit-empty-cell">
    <xsl:with-param name="num" select="$empty-cell-num"/>
  </xsl:call-template-->
</xsl:template>

<xsl:template match="*[contains(@class,' topic/colspec ')]" mode="count-rowwidth">
  <xsl:param name="totalwidth">0</xsl:param> <!-- Total counted width so far -->
  <xsl:variable name="thiswidth">            <!-- Width of this column -->
    <xsl:choose>
      <xsl:when test="@colwidth and contains(@colwidth,'*') and not(@colwidth='*')"><xsl:value-of select="substring-before(@colwidth,'*')"/></xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <!-- If there are more colspecs, continue, otherwise return the current count -->
  <xsl:choose>
    <xsl:when test="following-sibling::*[contains(@class,' topic/colspec ')]">
      <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/colspec ')][1]" mode="count-rowwidth">
        <xsl:with-param name="totalwidth" select="$totalwidth + $thiswidth"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$totalwidth + $thiswidth"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Find the starting column of an entry in a row. -->
<xsl:template name="find-entry-start-position">
  <xsl:choose>

    <!-- if the column number is specified, use it -->
    <xsl:when test="@colnum">
      <xsl:value-of select="@colnum"/>
    </xsl:when>

    <xsl:when test="not(../../../*[contains(@class,' topic/colspec ')])">
      <xsl:variable name="prev-sib">
        <xsl:value-of select="count(preceding-sibling::*)"/>
      </xsl:variable>
      <xsl:value-of select="$prev-sib+1"/>
    </xsl:when>

    <!-- If there is a defined column name, check the colspans to determine position -->
    <xsl:when test="@colname">
      <!-- count the number of colspans before the one this entry references, plus one -->
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=current()/@colname]/preceding-sibling::*[contains(@class, ' topic/colspec ')])+1)"/>
    </xsl:when>

    <!-- If the starting column is defined, check colspans to determine position -->
    <xsl:when test="@namest">
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=current()/@namest]/preceding-sibling::*[contains(@class, ' topic/colspec ')])+1)"/>
    </xsl:when>

    <!-- Need a test for spanspec -->
    <xsl:when test="@spanname">
      <xsl:variable name="startspan">  <!-- starting column for this span -->
        <xsl:value-of select="../../../*[contains(@class,' topic/spanspec ')][@spanname=current()/@spanname]/@namest"/>
      </xsl:variable>
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$startspan]/preceding-sibling::*[contains(@class, ' topic/colspec ')])+1)"/>
    </xsl:when>

    <!-- Otherwise, just use the count of cells in this row -->
    <xsl:otherwise>
      <xsl:variable name="prev-sib">
        <xsl:value-of select="count(preceding-sibling::*)"/>
      </xsl:variable>
      <xsl:value-of select="$prev-sib+1"/>
    </xsl:otherwise>

  </xsl:choose>
</xsl:template>

<!-- Find the end column of a cell. If the cell does not span any columns,
     the end position is the same as the start position. -->
<xsl:template name="find-entry-end-position">
  <xsl:param name="startposition" select="0"/>
  <xsl:choose>
    <xsl:when test="not(../../../*[contains(@class,' topic/colspec ')])">
      <xsl:value-of select="$startposition"/>
    </xsl:when>
    <xsl:when test="@nameend">
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=current()/@nameend]/preceding-sibling::*[contains(@class, ' topic/colspec ')])+1)"/>
    </xsl:when>
    <xsl:when test="@spanname">
      <xsl:variable name="endspan">  <!-- starting column for this span -->
        <xsl:value-of select="../../../*[contains(@class,' topic/spanspec ')][@spanname=current()/@spanname]/@nameend"/>
      </xsl:variable>
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$endspan]/preceding-sibling::*[contains(@class, ' topic/colspec ')])+1)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$startposition"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Find the number of column spans between name-start and name-end attrs -->
<xsl:template name="find-colspan">
  <xsl:variable name="startpos">
    <xsl:call-template name="find-entry-start-position"/>
  </xsl:variable>
  <xsl:variable name="endpos">
    <xsl:call-template name="find-entry-end-position">
      <xsl:with-param name="startposition" select="$startpos"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:value-of select="$endpos - $startpos + 1"/>
</xsl:template>

<xsl:template name="find-spanspec-colspan">
  <xsl:variable name="spanname"><xsl:value-of select="@spanname"/></xsl:variable>
  <xsl:variable name="startcolname">
    <xsl:value-of select="../../../*[contains(@class,' topic/spanspec ')][@spanname=$spanname][1]/@namest"/>
  </xsl:variable>
  <xsl:variable name="endcolname">
    <xsl:value-of select="../../../*[contains(@class,' topic/spanspec ')][@spanname=$spanname][1]/@nameend"/>
  </xsl:variable>
  <xsl:variable name="startpos">
    <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$startcolname]/preceding-sibling::*[contains(@class, ' topic/colspec ')])+1)"/>
  </xsl:variable>
  <xsl:variable name="endpos">
    <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$endcolname]/preceding-sibling::*[contains(@class, ' topic/colspec ')])+1)"/>
  </xsl:variable>
  <xsl:value-of select="$endpos - $startpos + 1"/>
</xsl:template>

  <xsl:template name="emit-empty-cell">
    <xsl:param name="currentpos" select="1"/>
    <xsl:param name="startpos" select="1"></xsl:param>
    <xsl:if test="$startpos &gt; $currentpos">
      <xsl:variable name="colspan">
        <xsl:apply-templates select="../preceding-sibling::*[*[contains(@class,' topic/entry ')][@morerows][@colnum=$currentpos or @colname=concat('col',$currentpos) or @namest=concat('col',$currentpos)]][1]/*[contains(@class,' topic/entry ')][@morerows][@colnum=$currentpos or @colname=concat('col',$currentpos) or @namest=concat('col',$currentpos)]" mode="find-colspan"/>
      </xsl:variable>
      <table:covered-table-cell/>
      <xsl:if test="not($colspan='') and ($startpos &gt; ($currentpos + $colspan))">
        <xsl:call-template name="emit-empty-cell">
          <xsl:with-param name="startpos" select="$startpos"/>
          <xsl:with-param name="currentpos" select="$currentpos + $colspan"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  <!--  
  <xsl:template name="emit-empty-cell-for-colspan">
    <xsl:param name="span" select="0"/>
    <xsl:if test="$span &gt; 1">
      <table:covered-table-cell/>
      <xsl:call-template name="emit-empty-cell-for-colspan">
        <xsl:with-param name="span" select="$span - 1"></xsl:with-param>
      </xsl:call-template>
    </xsl:if>  
  </xsl:template>
  -->
  
  <xsl:template name="get-rows-span">
    <xsl:choose>
      <xsl:when test="@morerows">
        <xsl:value-of select="number(@morerows + 1)"/>
      </xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/entry ')]" mode="find-colspan">
    <xsl:call-template name="find-colspan"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/colspec ')]" mode="count-colwidth">
    <xsl:param name="colnum" select="1"/>
    <xsl:param name="span-width" select="0"></xsl:param>
    <xsl:variable name="width">
      <xsl:choose>
        <xsl:when test="@colwidth and not(@colwidth='' or @colwidth='*')">
          <xsl:value-of select="substring-before(@colwidth,'*')"/>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$colnum &gt; 1">
        <xsl:apply-templates select="following-sibling::*[1]" mode="count-colwidth">
          <xsl:with-param name="colnum" select="$colnum - 1"/>
          <xsl:with-param name="span-width" select="$span-width + $width"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$span-width + $width"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
<!--xsl:template name="count-colwidth">
    <xsl:param name="colnum" select="1"/>
    <xsl:param name="span-width" select="0"/>
    <xsl:variable name="width">
      <xsl:choose>
        <xsl:when test="@colwidth and not(@colwidth='')">
          <xsl:value-of select="substring-before(@colwidth,'*')"/>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$colnum &gt; 1">
        <xsl:call-template name="count-colwidth">
          <xsl:with-param name="colnum" select="$colnum - 1"/>
          <xsl:with-param name="span-width" select="$span-width + $width"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$span-width + $width"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template-->

<!-- Simple Table -->
<xsl:template match="*[contains(@class,' topic/simpletable ')]" name="topic.simpletable">
  
  <!-- render stable -->
  <xsl:call-template name="render_simpletable"/>
  
</xsl:template>

<xsl:template name="create_simpletable">

  <xsl:variable name="tablenameId" select="generate-id(.)"/>
  
  <!-- start flagging -->
  <xsl:apply-templates select="." mode="start-add-odt-flags">
    <xsl:with-param name="family" select="'_table'"/>
  </xsl:apply-templates>
  
  <xsl:element name="table:table">
    <xsl:attribute name="table:name">
      <xsl:value-of select="concat('Table', $tablenameId)"/>
    </xsl:attribute>
    <!-- table background flagging -->
    <xsl:apply-templates select="." mode="start-add-odt-flags">
      <xsl:with-param name="family" select="'_table_attr'"/>
    </xsl:apply-templates>
    <xsl:variable name="colnumNum">
      <xsl:call-template name="count_columns_for_simpletable"/>
    </xsl:variable>
    <xsl:call-template name="create_columns_for_simpletable">
      <xsl:with-param name="column" select="$colnumNum"/>
    </xsl:call-template>
    <xsl:call-template name="dotable"/> 
  </xsl:element>
  <!-- end flagging -->
  <xsl:apply-templates select="." mode="end-add-odt-flags">
    <xsl:with-param name="family" select="'_table'"/>
  </xsl:apply-templates>
  
</xsl:template>

<xsl:template name="count_columns_for_simpletable">
  <xsl:choose>
    <xsl:when test="child::*[contains(@class, ' topic/sthead ')]">
      <xsl:value-of select="count(child::*[contains(@class, ' topic/sthead ')][1]
        /child::*[contains(@class, ' topic/stentry ')])"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="count(child::*[contains(@class, ' topic/strow ')][1]
        /child::*[contains(@class, ' topic/stentry ')])"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="create_columns_for_simpletable">
  <xsl:param name="column" select="0"/>
  <xsl:if test="$column &gt; 0">
    <table:table-column/>
    <xsl:call-template name="create_columns_for_simpletable">
      <xsl:with-param name="column" select="$column - 1"></xsl:with-param>
    </xsl:call-template>
  </xsl:if>  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/simpletable ')]/*[contains(@class,' topic/title ')]">
  <xsl:variable name="ancestorlang">
    <xsl:call-template name="getLowerCaseLang"/>
  </xsl:variable>
  <xsl:variable name="tbl-count-actual" select="count(preceding::*[contains(@class,' topic/table ')
    or contains(@class,' topic/simpletable ')]/*[contains(@class,' topic/title ')])+1"/>

  <xsl:element name="text:p">
    <xsl:attribute name="text:style-name">bold</xsl:attribute>
  <xsl:choose>
    <!-- Hungarian: "1.Table " -->
    <xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) or (string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )">
      <xsl:value-of select="$tbl-count-actual"/><xsl:text>. </xsl:text>
      <xsl:call-template name="getStringODT">
        <xsl:with-param name="stringName" select="'Table'"/>
      </xsl:call-template><xsl:text> 
      </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="getStringODT">
        <xsl:with-param name="stringName" select="'Table'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:value-of select="$tbl-count-actual"/>
      <xsl:text>. </xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:call-template name="get-ascii">
    <xsl:with-param name="txt">
      <xsl:value-of select="."/>
    </xsl:with-param>
  </xsl:call-template>
 </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/sthead ')]">

  <xsl:element name="table:table-header-rows">
    <xsl:element name="table:table-row">
      <xsl:apply-templates mode="emit-cell-style"/>
    </xsl:element>
  </xsl:element>
</xsl:template>
  
<xsl:template match="*[contains(@class,' topic/strow ')]">

  <xsl:element name="table:table-row">
    <xsl:apply-templates mode="emit-cell-style"/>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/stentry ')]" mode="emit-cell-style">

  <xsl:variable name="totalcols" select="count(../*[contains(@class,' topic/stentry ')])">
    <!--xsl:apply-templates select="../*[contains(@class,' topic/stentry ')][1]" mode="count-rowwidth"/-->
  </xsl:variable>
   <!--  
    <xsl:variable name="thiswidth-twips">
      <xsl:value-of select="$table-row-width div $totalcols"/>
    </xsl:variable>
    <xsl:value-of select="$valign"/>
   -->
  <xsl:element name="table:table-cell">
    <xsl:attribute name="office:value-type">string</xsl:attribute>
    <xsl:call-template name="create_style_stable"/>
    
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>  
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:element>
</xsl:template>
  
<xsl:template match="text()" mode="tags_in_sthead">
  <xsl:element name="text:span">
    <xsl:attribute name="text:style-name">bold</xsl:attribute>
    <xsl:apply-templates select="."/>
  </xsl:element>  
</xsl:template>
  
<xsl:template match="*" mode="tags_in_sthead">
  <xsl:apply-templates select="."/>
</xsl:template>
  
  

<xsl:template name="create_style_stable">
  <!-- create style attribute -->
  <xsl:variable name="colpos">
    <xsl:value-of select="position()"/>
  </xsl:variable>
  
  <xsl:variable name="rowpos">
    <xsl:choose>
      <!-- row belongs to thead -->
      <xsl:when test="parent::*[contains(@class, ' topic/sthead ')]">
        <xsl:value-of select="1"/>
      </xsl:when>
      <!-- there's no thead -->
      <xsl:when test="not(parent::*[contains(@class, ' topic/strow ')]
        /preceding-sibling::*[contains(@class, ' topic/sthead ')])">
        <xsl:value-of select="count(parent::*[contains(@class, ' topic/strow ')]/
          preceding-sibling::*[contains(@class, ' topic/strow ')]) + 1"/>
      </xsl:when>
      <!-- there is thead and row belongs to tbody -->
      <xsl:when test="parent::*[contains(@class, ' topic/strow ')]
        /preceding-sibling::*[contains(@class, ' topic/sthead ')]">
        <xsl:value-of select="2"/>
      </xsl:when>
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


<xsl:template match="*[contains(@class,' topic/stentry ')]" mode="count-rowwidth">
  <xsl:param name="totalcols">0</xsl:param> <!-- Total counted columns so far -->
  <xsl:choose>
    <xsl:when test="following-sibling::*[contains(@class,' topic/stentry ')]">
      <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/stentry ')][1]" mode="count-rowwidth">
        <xsl:with-param name="totalwidth" select="$totalcols + 1"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$totalcols + 1"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
<xsl:template name="output-stentry-id">
  <!-- Find the position in this row -->
  <xsl:variable name="thiscolnum"><xsl:number level="single" count="*"/></xsl:variable>
  <xsl:choose>
    <xsl:when test="@id">    <!-- If ID is specified, always use it -->
      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
    </xsl:when>
    <!-- If no ID is specified, and this is a header cell, generate an ID -->
    <xsl:when test="parent::*[contains(@class,' topic/sthead ')] or
      (parent::*/parent::*/@keycol and number(parent::*/parent::*/@keycol)=number($thiscolnum))">
      <xsl:attribute name="id"><xsl:value-of select="generate-id(.)"/></xsl:attribute>
    </xsl:when>
  </xsl:choose>
</xsl:template>
  
<xsl:template name="create_table_cell_styles">
  <!-- for normal table -->
  <style:style style:name="cell_style_1" style:family="table-cell">
    <style:table-cell-properties fo:padding="0.0201in" fo:border="0.0007in solid #000000"/>
  </style:style>
  <style:style style:name="cell_style_2" style:family="table-cell">
    <style:table-cell-properties fo:padding="0.0201in" fo:border-left="none" 
      fo:border-right="0.0007in solid #000000" fo:border-top="0.0007in solid #000000" 
      fo:border-bottom="0.0007in solid #000000" />
  </style:style>
  <style:style style:name="cell_style_3" style:family="table-cell">
    <style:table-cell-properties fo:padding="0.0201in" 
      fo:border-right="0.0007in solid #000000" fo:border-left="0.0007in solid #000000" fo:border-top="none"
      fo:border-bottom="0.0007in solid #000000"/>
  </style:style>
  <style:style style:name="cell_style_4" style:family="table-cell">
    <style:table-cell-properties fo:padding="0.0201in" 
      fo:border-right="0.0007in solid #000000" fo:border-bottom="0.0007in solid #000000"
      fo:border-top="none" fo:border-left="none"/>
  </style:style>
  
  <!-- for task choice table without head, it is simple.-->
  <style:style style:name="cell_style_1_task" style:family="table-cell">
    <style:table-cell-properties fo:padding="0.0201in" fo:border-left="0.0007in solid #000000" 
      fo:border-right="0.0007in solid #000000" fo:border-top="0.0007in solid #000000" 
      fo:border-bottom="none" />
  </style:style>
  <style:style style:name="cell_style_2_task" style:family="table-cell">
    <style:table-cell-properties fo:padding="0.0201in" fo:border-left="none" 
      fo:border-right="0.0007in solid #000000" fo:border-top="0.0007in solid #000000"
      fo:border-bottom="none"/>
  </style:style>
</xsl:template>

</xsl:stylesheet>