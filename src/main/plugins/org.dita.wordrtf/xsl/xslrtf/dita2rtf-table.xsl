<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="text"/>
<xsl:strip-space elements="*"/>

<!-- =========== CALS (OASIS) TABLE =========== -->
<xsl:template match="*[contains(@class,' topic/table ')]" name="topic.table"> 
<xsl:call-template name="gen-id"/><xsl:call-template name="dotable"/> 
</xsl:template>

<xsl:template name="dotable">
  <xsl:text>\par </xsl:text>
  <xsl:apply-templates/>
  <xsl:text>\pard \qj \li0\ri0\nowidctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 {
\par }</xsl:text>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')]">
<xsl:variable name="ancestorlang">
<xsl:call-template name="getLowerCaseLang"/>
</xsl:variable>
  <xsl:variable name="tbl-count-actual" select="count(preceding::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]/*[contains(@class,' topic/title ')])+1"/>
<xsl:call-template name="gen-id"/>\pard \plain\s9 \qc\f4\fs24\b <xsl:choose><!-- Hungarian: "1.
Table " --><xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) or
(string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )"><xsl:value-of
select="$tbl-count-actual"/><xsl:text>. </xsl:text><xsl:call-template
name="getString"><xsl:with-param name="stringName" select="'Table'"/></xsl:call-template><xsl:text>
</xsl:text></xsl:when><xsl:otherwise><xsl:call-template name="getStringRTF"><xsl:with-param
name="stringName" select="'Table'"/></xsl:call-template><xsl:text> </xsl:text><xsl:value-of
select="$tbl-count-actual"/><xsl:text>. </xsl:text></xsl:otherwise></xsl:choose><xsl:call-template
  name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="."/></xsl:with-param></xsl:call-template>\par \plain\s0 \qj\f2\fs24
</xsl:template>

<xsl:template match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/desc ')]">
<xsl:call-template name="gen-id"/>\pard \plain\s0 \f2\fs24 <xsl:apply-templates/>\par \plain\s0 \f2\fs24
</xsl:template>

<xsl:template match="*[contains(@class,' topic/tgroup ')]" name="topic.tgroup">
<xsl:call-template name="gen-id"/><xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/colspec ')]"></xsl:template>

<xsl:template match="*[contains(@class,' topic/spanspec ')]"></xsl:template>

<xsl:template match="*[contains(@class,' topic/thead ')]" name="topic.thead">
<xsl:call-template name="gen-id"/><xsl:apply-templates/>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/tfoot ')]"/>

<xsl:template match="*[contains(@class,' topic/tbody ')]" name="topic.tbody">
<xsl:call-template name="gen-id"/><xsl:apply-templates/>
    <!-- process table footer -->
    <xsl:apply-templates select="../*[contains(@class,' topic/tfoot ')]" mode="gen-tfoot" />
</xsl:template>

<!-- special mode for table footers -->
<xsl:template match="*[contains(@class,' topic/tfoot ')]" mode="gen-tfoot">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/row ')]" name="topic.row">
<xsl:call-template name="gen-id"/>
<xsl:text>\trowd \trgaph108\trleft-108\trbrdrt\brdrs\brdrw10 </xsl:text>
<xsl:if test="parent::*[contains(@class,' topic/thead ')]"><xsl:text>\trhdr </xsl:text></xsl:if>
<xsl:text>\trbrdrl\brdrs\brdrw10 \trbrdrb\brdrs\brdrw10 \trbrdrr\brdrs\brdrw10 
\trbrdrh\brdrs\brdrw10 \trbrdrv\brdrs\brdrw10 
\trftsWidth1\trautofit1\trpaddl108\trpaddr108\trpaddfl3\trpaddfr3</xsl:text>
  <xsl:apply-templates select="*[contains(@class,' topic/entry ')][1]" mode="emit-cell-style"/>
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class,' topic/thead ')]"><xsl:text>\plain \s7\f4\fs24\b \qc</xsl:text></xsl:when>
    <xsl:otherwise>\plain \s0\f4\fs24 <xsl:value-of select="$table-row-default-align"/></xsl:otherwise>
  </xsl:choose>
\li0\fi0\ri0\nowidctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0
<xsl:text>{</xsl:text><xsl:apply-templates select="*[contains(@class,' topic/entry ')][1]"/><xsl:text>}\row</xsl:text>
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
  <xsl:variable name="col-max-num">
    <xsl:call-template name="count-colmax"/>    
  </xsl:variable>
  <xsl:variable name="startpos"><xsl:call-template name="find-entry-start-position"/></xsl:variable>
  <xsl:variable name="endpos">
    <xsl:call-template name="find-entry-end-position">
      <xsl:with-param name="startposition" select="$startpos"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:if test="$startpos &gt; $currentpos">
    <xsl:call-template name="emit-empty-cell">
      <xsl:with-param name="startpos" select="$startpos"/>
      <xsl:with-param name="currentpos" select="$currentpos"/>
    </xsl:call-template>
  </xsl:if>
  <xsl:choose>
      <xsl:when test="parent::*/parent::*[contains(@class,' topic/thead ')]">
          <xsl:call-template name="topic.thead_entry"/>
      </xsl:when>
      <xsl:otherwise>
          <xsl:call-template name="topic.tbody_entry"/>
      </xsl:otherwise>
  </xsl:choose>
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
<xsl:apply-templates/><xsl:text>\cell </xsl:text>
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
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=current()/@colname]/preceding-sibling::*)+1)"/>
    </xsl:when>

    <!-- If the starting column is defined, check colspans to determine position -->
    <xsl:when test="@namest">
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=current()/@namest]/preceding-sibling::*)+1)"/>
    </xsl:when>

    <!-- Need a test for spanspec -->
    <xsl:when test="@spanname">
      <xsl:variable name="startspan">  <!-- starting column for this span -->
        <xsl:value-of select="../../../*[contains(@class,' topic/spanspec ')][@spanname=current()/@spanname]/@namest"/>
      </xsl:variable>
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$startspan]/preceding-sibling::*)+1)"/>
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
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=current()/@nameend]/preceding-sibling::*)+1)"/>
    </xsl:when>
    <xsl:when test="@spanname">
      <xsl:variable name="endspan">  <!-- starting column for this span -->
        <xsl:value-of select="../../../*[contains(@class,' topic/spanspec ')][@spanname=current()/@spanname]/@nameend"/>
      </xsl:variable>
      <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$endspan]/preceding-sibling::*)+1)"/>
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
   <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$startcolname]/preceding-sibling::*)+1)"/>
  </xsl:variable>
  <xsl:variable name="endpos">
   <xsl:value-of select="number(count(../../../*[contains(@class,' topic/colspec ')][@colname=$endcolname]/preceding-sibling::*)+1)"/>
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
      <xsl:text>\cell </xsl:text>
      <xsl:if test="not($colspan='') and ($startpos &gt; ($currentpos + $colspan))">
        <xsl:call-template name="emit-empty-cell">
          <xsl:with-param name="startpos" select="$startpos"/>
          <xsl:with-param name="currentpos" select="$currentpos + $colspan"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/entry ')]" mode="emit-cell-style">
    <xsl:param name="copy-style" select="'false'"/>
    <xsl:param name="left-border" select="0"/>
    <xsl:param name="currentpos" select="1"/>
    <xsl:variable name="col-max-num">
      <xsl:call-template name="count-colmax"/>    
    </xsl:variable>
    <xsl:variable name="valign">
      <xsl:choose>
        <xsl:when test="@valign and not(@valign='')">
          <xsl:choose>
            <xsl:when test="@valign='top'">\clvertalt</xsl:when>
            <xsl:when test="@valign='middle'">\clvertalc</xsl:when>
            <xsl:when test="@valign='bottom'">\clvertalb</xsl:when>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="../@valign and not(../@valign='')">
              <xsl:choose>
                <xsl:when test="@valign='top'">\clvertalt</xsl:when>
                <xsl:when test="@valign='middle'">\clvertalc</xsl:when>
                <xsl:when test="@valign='bottom'">\clvertalb</xsl:when>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="table-cell-default-valign"/></xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>    
    <xsl:variable name="colspan-num">
      <xsl:call-template name="find-colspan"/>
    </xsl:variable>
    <xsl:variable name="startpos"><xsl:call-template name="find-entry-start-position"/></xsl:variable>
    <xsl:variable name="endpos">
      <xsl:call-template name="find-entry-end-position">
        <xsl:with-param name="startposition" select="$startpos"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="nextpos">
      <xsl:choose>
        <xsl:when test="$endpos &gt; $startpos">
          <xsl:value-of select="$endpos + 1"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$startpos + 1"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="totalwidth">
      <xsl:choose>
        <xsl:when test="../../../*[contains(@class,' topic/colspec ')]">
          <xsl:apply-templates select="../../../*[contains(@class,' topic/colspec ')][1]" mode="count-rowwidth"/>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="$col-max-num"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="thiswidth">
      <xsl:choose>
        <xsl:when test="../../../*[contains(@class,' topic/colspec ')]">
          <xsl:apply-templates select="../../../*[contains(@class,' topic/colspec ')][number($startpos)]" mode="count-colwidth">
            <xsl:with-param name="colnum" select="$colspan-num"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="thiswidth-twips">
      <!--xsl:choose>
        <xsl:when test="following-sibling::*[contains(@class,' topic/entry ')]"-->
          <xsl:value-of select="round(($thiswidth div $totalwidth) * $table-row-width)"/>
        <!--/xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="($table-row-width) - ($left-border)"/>
        </xsl:otherwise>
      </xsl:choose-->      
    </xsl:variable>
    <xsl:variable name="left-to-right">
      <xsl:choose>
        <xsl:when test="../../../*[contains(@class,' topic/colspec ')]">
      <xsl:apply-templates select="../../../*[contains(@class,' topic/colspec ')][number($currentpos)]" mode="count-colwidth">
        <xsl:with-param name="colnum" select="$colspan-num + $startpos - $currentpos"/>
      </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$thiswidth"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="right-border">
      <xsl:value-of select="$left-border + round(($left-to-right div $totalwidth) * $table-row-width)"/>
    </xsl:variable>
    <xsl:if test="$startpos &gt; $currentpos">
      <!-- the start position of the entry is after current position
      we need to emit merged entry style -->
      <xsl:call-template name="emit-vmerged-style">
        <xsl:with-param name="left-border" select="$left-border"/>
        <xsl:with-param name="startpos" select="$startpos"/>
        <xsl:with-param name="currentpos" select="$currentpos"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="@morerows and $copy-style='false'"><xsl:text>\clvmgf</xsl:text></xsl:if>
    
    <xsl:value-of select="$valign"/><xsl:text>\clbrdrt\brdrs\brdrw10 \clbrdrl\brdrs\brdrw10 \clbrdrb\brdrs\brdrw10 \clbrdrr\brdrs\brdrw10 \clftsWidth3\clwWidth</xsl:text><xsl:value-of select="$thiswidth-twips"/>
    <xsl:text>\cellx</xsl:text><xsl:value-of select="$right-border"/><xsl:text> </xsl:text>

    <xsl:if test="following-sibling::*[contains(@class,' topic/entry ')] and $copy-style='false'">
      <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/entry ')][1]" mode="emit-cell-style">
        <xsl:with-param name="left-border" select="$right-border"/>
        <xsl:with-param name="currentpos" select="$nextpos"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="not(following-sibling::*[contains(@class,' topic/entry ')]) and not($nextpos &gt; $col-max-num)">
      <!-- if this is the last entry in current row and next position is not greater than the number of columns in a row-->
      <xsl:call-template name="emit-vmerged-style">
        <xsl:with-param name="left-border" select="$right-border"/>
        <xsl:with-param name="startpos" select="$col-max-num + 1"/> <!-- make sure the remaining columns will be generated -->
        <xsl:with-param name="currentpos" select="$nextpos"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="emit-vmerged-style">
    <xsl:param name="left-border" select="0"/>
    <xsl:param name="startpos" select="1"/>
    <xsl:param name="currentpos" select="1"/>
    <xsl:if test="$startpos &gt; $currentpos">
      <xsl:variable name="colspan">
        <xsl:apply-templates select="../preceding-sibling::*[*[contains(@class,' topic/entry ')][@morerows][@colnum=$currentpos or @colname=concat('col',$currentpos) or @namest=concat('col',$currentpos)]][1]/*[contains(@class,' topic/entry ')][@morerows][@colnum=$currentpos or @colname=concat('col',$currentpos) or @namest=concat('col',$currentpos)]" mode="find-colspan"/>
      </xsl:variable>
      <xsl:variable name="col-max-num">
        <xsl:call-template name="count-colmax"/>    
      </xsl:variable>
      <xsl:variable name="totalwidth">
        <xsl:choose>
          <xsl:when test="../../../*[contains(@class,' topic/colspec ')]">
            <xsl:apply-templates select="../../../*[contains(@class,' topic/colspec ')][1]" mode="count-rowwidth"/>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="$col-max-num"/></xsl:otherwise>
        </xsl:choose>      
    </xsl:variable>
    <xsl:variable name="thiswidth">
      <xsl:choose>
        <xsl:when test="../../../*[contains(@class,' topic/colspec ')]">
      <xsl:apply-templates select="../../../*[contains(@class,' topic/colspec ')][number($currentpos)]" mode="count-colwidth">
        <xsl:with-param name="colnum" select="$colspan"/>
      </xsl:apply-templates></xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="thiswidth-twips">
      <xsl:value-of select="round(($thiswidth div $totalwidth) * $table-row-width)"/>
    </xsl:variable>
    <xsl:text>\clvmrg</xsl:text>
      <xsl:apply-templates select="../preceding-sibling::*[*[contains(@class,' topic/entry ')][@morerows][@colnum=$currentpos or @colname=concat('col',$currentpos) or @namest=concat('col',$currentpos)]][1]/*[contains(@class,' topic/entry ')][@morerows][@colnum=$currentpos or @colname=concat('col',$currentpos) or @namest=concat('col',$currentpos)]" mode="emit-cell-style">
        <xsl:with-param name="left-border" select="$left-border"/>
        <xsl:with-param name="copy-style" select="'true'"/>
        <xsl:with-param name="currentpos" select="$currentpos"/>
      </xsl:apply-templates>
      
      <xsl:if test="not($colspan='') and ($startpos &gt; ($currentpos + $colspan))">
        <xsl:call-template name="emit-vmerged-style">
          <xsl:with-param name="left-border" select="$left-border + $thiswidth-twips"/>
          <xsl:with-param name="startpos" select="$startpos"/>
          <xsl:with-param name="currentpos" select="$currentpos + $colspan"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/entry ')]" mode="find-colspan">
    <xsl:call-template name="find-colspan"/>
  </xsl:template>
  
<!--xsl:template name="emit-merged-cstyle">
    <xsl:param name="startpos" select="1"/>
    <xsl:param name="num" select="0"/>
    <xsl:if test="$num &gt; 0">
      <xsl:text>\clmrg</xsl:text><xsl:apply-templates select="../../../*[contains(@class,' topic/colspec ')][number($startpos)]" mode="emit-cell-style"></xsl:apply-templates>
      <xsl:call-template name="emit-merged-cstyle">
        <xsl:with-param name="startpos" select="$startpos + 1"/>
        <xsl:with-param name="num" select="$num - 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/colspec ')]" mode="emit-cell-style">
    <xsl:param name="left-border" select="0"/>
    <xsl:param name="colnum" select="1"/>
    <xsl:param name="valign" select="$table-cell-default-valign"/>
    <xsl:variable name="totalwidth">
      <xsl:apply-templates select="../*[contains(@class,' topic/colspec ')][1]" mode="count-rowwidth"/>
    </xsl:variable>
    <xsl:variable name="thiswidth">
      <xsl:call-template name="count-colwidth">
        <xsl:with-param name="colnum" select="$colnum"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="thiswidth-twips">
      <xsl:value-of select="($thiswidth div $totalwidth) * $table-row-width"/>
    </xsl:variable>
    <xsl:value-of select="$valign"/><xsl:text>\clbrdrt\brdrs\brdrw10 \clbrdrl\brdrs\brdrw10 \clbrdrb\brdrs\brdrw10 \clbrdrr\brdrs\brdrw10 \clftsWidth3\clwWidth</xsl:text><xsl:value-of select="round($thiswidth-twips)"/>
    <xsl:text>\cellx </xsl:text><xsl:value-of select="$left-border + "/>
  </xsl:template-->
  
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
<xsl:template match="*[contains(@class,' topic/simpletable ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="dotable"/> 
</xsl:template>
    
<xsl:template match="*[contains(@class,' topic/simpletable ')]/*[contains(@class,' topic/title ')]">
  <xsl:variable name="ancestorlang">
    <xsl:call-template name="getLowerCaseLang"/>
  </xsl:variable>
  <xsl:variable name="tbl-count-actual" select="count(preceding::*[contains(@class,' topic/table ')
    or contains(@class,' topic/simpletable ')]/*[contains(@class,' topic/title ')])+1"/>
  <xsl:call-template name="gen-id"/>\pard \plain\s9 \qc\f4\fs24\b <xsl:choose><!-- Hungarian: "1.Table " --><xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) or (string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )"><xsl:value-of select="$tbl-count-actual"/><xsl:text>. </xsl:text><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Table'"/></xsl:call-template><xsl:text> </xsl:text></xsl:when><xsl:otherwise><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Table'"/></xsl:call-template><xsl:text> </xsl:text><xsl:value-of select="$tbl-count-actual"/><xsl:text>. </xsl:text></xsl:otherwise></xsl:choose><xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="."/></xsl:with-param></xsl:call-template>\par \plain\s0 \qj\f2\fs24
</xsl:template>

<xsl:template match="*[contains(@class,' topic/sthead ')]">
<xsl:call-template name="gen-id"/>
<xsl:text>\trowd \trgaph108\trleft-108\trbrdrt\brdrs\brdrw10 \trhdr </xsl:text>
<xsl:text>\trbrdrl\brdrs\brdrw10 \trbrdrb\brdrs\brdrw10 \trbrdrr\brdrs\brdrw10 
\trbrdrh\brdrs\brdrw10 \trbrdrv\brdrs\brdrw10 
\trftsWidth1\trautofit1\trpaddl108\trpaddr108\trpaddfl3\trpaddfr3</xsl:text>
<xsl:apply-templates mode="emit-cell-style"/>
<xsl:text>\plain \s7\f4\fs24\b \qc</xsl:text>
\li0\fi0\ri0\nowidctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0
<xsl:text>{</xsl:text><xsl:apply-templates/><xsl:text>}\row</xsl:text>
</xsl:template>
  
<xsl:template match="*[contains(@class,' topic/strow ')]">
<xsl:call-template name="gen-id"/>
<xsl:text>\trowd \trgaph108\trleft-108\trbrdrt\brdrs\brdrw10 </xsl:text>
<xsl:text>\trbrdrl\brdrs\brdrw10 \trbrdrb\brdrs\brdrw10 \trbrdrr\brdrs\brdrw10 
\trbrdrh\brdrs\brdrw10 \trbrdrv\brdrs\brdrw10 
\trftsWidth1\trautofit1\trpaddl108\trpaddr108\trpaddfl3\trpaddfr3</xsl:text>
<xsl:apply-templates mode="emit-cell-style"/>
<xsl:text>\plain \s0\f4\fs24 </xsl:text><xsl:value-of select="$table-row-default-align"/>
\li0\fi0\ri0\nowidctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0
<xsl:text>{</xsl:text><xsl:apply-templates/><xsl:text>}\row</xsl:text>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/stentry ')]" mode="emit-cell-style">
<xsl:param name="valign" select="$table-cell-default-valign"/>
<xsl:variable name="totalcols" select="count(../*[contains(@class,' topic/stentry ')])">
  <!--xsl:apply-templates select="../*[contains(@class,' topic/stentry ')][1]" mode="count-rowwidth"/-->
</xsl:variable>
    
    <xsl:variable name="thiswidth-twips">
      <xsl:value-of select="$table-row-width div $totalcols"/>
    </xsl:variable>
    <xsl:value-of select="$valign"/><xsl:text>\clbrdrt\brdrs\brdrw10 \clbrdrl\brdrs\brdrw10 \clbrdrb\brdrs\brdrw10 \clbrdrr\brdrs\brdrw10 \clftsWidth3\clwWidth</xsl:text><xsl:value-of select="round($thiswidth-twips)"/>
    <xsl:text>\cellx </xsl:text>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/stentry ')]">
<xsl:apply-templates/><xsl:text>\cell </xsl:text>
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

</xsl:stylesheet>