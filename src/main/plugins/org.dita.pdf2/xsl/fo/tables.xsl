<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
  exclude-result-prefixes="opentopic-func xs dita2xslfo"
  version="2.0">

    <xsl:variable name="tableAttrs" select="'../../cfg/fo/attrs/tables-attr.xsl'"/>

    <xsl:param name="tableSpecNonProportional" select="'false'"/>

    <!-- XML Exchange Table Model Document Type Definition default is all -->
    <xsl:variable name="table.frame-default" select="'all'"/>
    <!-- XML Exchange Table Model Document Type Definition default is 1 -->
    <xsl:variable name="table.rowsep-default" select="'0'"/>
    <!-- XML Exchange Table Model Document Type Definition default is 1 -->
    <xsl:variable name="table.colsep-default" select="'0'"/>

    <!--Definition list-->
    <xsl:template match="*[contains(@class, ' topic/dl ')]">
        <fo:table xsl:use-attribute-sets="dl">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="*[contains(@class, ' topic/dlhead ')]"/>
            <fo:table-body xsl:use-attribute-sets="dl__body">
                <xsl:choose>
                    <xsl:when test="contains(@otherprops,'sortable')">
                        <xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]">
                            <xsl:sort select="opentopic-func:getSortString(normalize-space( opentopic-func:fetchValueableText(*[contains(@class, ' topic/dt ')]) ))" lang="{$locale}"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]"/>
                    </xsl:otherwise>
                </xsl:choose>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dl ')]/*[contains(@class, ' topic/dlhead ')]">
        <fo:table-header xsl:use-attribute-sets="dl.dlhead">
            <xsl:call-template name="commonattributes"/>
            <fo:table-row xsl:use-attribute-sets="dl.dlhead__row">
                <xsl:apply-templates/>
            </fo:table-row>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dlhead ')]/*[contains(@class, ' topic/dthd ')]">
        <fo:table-cell xsl:use-attribute-sets="dlhead.dthd__cell">
            <xsl:call-template name="commonattributes"/>
            <fo:block xsl:use-attribute-sets="dlhead.dthd__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dlhead ')]/*[contains(@class, ' topic/ddhd ')]">
        <fo:table-cell xsl:use-attribute-sets="dlhead.ddhd__cell">
            <xsl:call-template name="commonattributes"/>
            <fo:block xsl:use-attribute-sets="dlhead.ddhd__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dlentry ')]">
        <fo:table-row xsl:use-attribute-sets="dlentry">
            <xsl:call-template name="commonattributes"/>
            <fo:table-cell xsl:use-attribute-sets="dlentry.dt">
                <xsl:apply-templates select="*[contains(@class, ' topic/dt ')]"/>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="dlentry.dd">
                <xsl:apply-templates select="*[contains(@class, ' topic/dd ')]"/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dt ')]">
        <fo:block xsl:use-attribute-sets="dlentry.dt__content">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dd ')]">
        <fo:block xsl:use-attribute-sets="dlentry.dd__content">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!--  Map processing  -->
    <xsl:template match="*[contains(@class, ' map/map ')]/*[contains(@class, ' map/reltable ')]">
        <fo:table-and-caption>
            <fo:table-caption>
                <fo:block xsl:use-attribute-sets="reltable__title">
                    <xsl:value-of select="@title"/>
                </fo:block>
            </fo:table-caption>
            <fo:table xsl:use-attribute-sets="reltable">
                <xsl:call-template name="topicrefAttsNoToc"/>
                <xsl:call-template name="selectAtts"/>
                <xsl:call-template name="globalAtts"/>

                <xsl:apply-templates select="relheader"/>

                <fo:table-body>
                    <xsl:apply-templates select="relrow"/>
                </fo:table-body>

            </fo:table>
        </fo:table-and-caption>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' map/relheader ')]">
        <fo:table-header xsl:use-attribute-sets="relheader">
            <xsl:call-template name="globalAtts"/>
            <xsl:apply-templates/>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' map/relcolspec ')]">
        <fo:table-cell xsl:use-attribute-sets="relcolspec">
            <xsl:apply-templates/>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' map/relrow ')]">
        <fo:table-row xsl:use-attribute-sets="relrow">
            <xsl:call-template name="globalAtts"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' map/relcell ')]">
        <fo:table-cell xsl:use-attribute-sets="relcell">
            <xsl:call-template name="globalAtts"/>
            <xsl:call-template name="topicrefAtts"/>

            <xsl:apply-templates/>

        </fo:table-cell>
    </xsl:template>

    <!-- DITA-OT flagging preprocess may add flag info directly into simpletable; account for that and
         skip to next row. Currently known to match ditaval-startprop when flagging used on simpletable
         as well as suitesol:changebar-start when revision bar used on sthead or stentry. -->
    <xsl:template match="*" mode="count-max-simpletable-cells">
      <xsl:param name="maxcount" select="0" as="xs:integer"/>
      <xsl:apply-templates select="following-sibling::*[1]" mode="count-max-simpletable-cells">
        <xsl:with-param name="maxcount" select="$maxcount"/>
      </xsl:apply-templates>
    </xsl:template>
    <!-- SourceForge bug tracker item 2872988:
         Count the max number of cells in any row of a simpletable -->
    <xsl:template match="*[contains(@class, ' topic/sthead ')] | *[contains(@class, ' topic/strow ')]" mode="count-max-simpletable-cells">
      <xsl:param name="maxcount" select="0" as="xs:integer"/>
      <xsl:variable name="newmaxcount" as="xs:integer">
        <xsl:variable name="row-cell-count" select="count(*[contains(@class, ' topic/stentry ')])"/>
        <xsl:choose>
          <xsl:when test="$row-cell-count > $maxcount"><xsl:sequence select="$row-cell-count"/></xsl:when>
          <xsl:otherwise><xsl:sequence select="$maxcount"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="not(following-sibling::*[contains(@class, ' topic/strow ')])">
          <xsl:value-of select="$newmaxcount"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="following-sibling::*[contains(@class, ' topic/strow ')][1]" mode="count-max-simpletable-cells">
            <xsl:with-param name="maxcount" select="$newmaxcount"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!-- SourceForge bug tracker item 2872988:
         Count the number of values in @relcolwidth (to add values if one is missing) -->
    <xsl:template match="*" mode="count-colwidths">
      <xsl:param name="relcolwidth" select="@relcolwidth"/>
      <xsl:param name="count" select="0"/>
      <xsl:choose>
        <xsl:when test="not(contains($relcolwidth,' '))">
          <xsl:value-of select="$count + 1"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." mode="count-colwidths">
            <xsl:with-param name="relcolwidth" select="substring-after($relcolwidth,' ')"/>
            <xsl:with-param name="count" select="$count + 1"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!-- SourceForge bug tracker item 2872988:
         If there are more cells in any row than there are relcolwidth values,
         add 1* for each missing cell, otherwise the FO processor may crash. -->
    <xsl:template match="*" mode="fix-relcolwidth">
      <xsl:param name="update-relcolwidth" select="@relcolwidth"/>
      <xsl:param name="number-cells">
        <xsl:apply-templates select="*[1]" mode="count-max-simpletable-cells"/>
      </xsl:param>
      <xsl:param name="number-relwidths">
        <xsl:apply-templates select="." mode="count-colwidths"/>
      </xsl:param>
      <xsl:choose>
        <xsl:when test="$number-relwidths &lt; $number-cells">
          <xsl:apply-templates select="." mode="fix-relcolwidth">
            <xsl:with-param name="update-relcolwidth" select="concat($update-relcolwidth,' 1*')"/>
            <xsl:with-param name="number-cells" select="$number-cells"/>
            <xsl:with-param name="number-relwidths" select="$number-relwidths+1"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$update-relcolwidth"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!--WARNING: Following templates are imported from default implementation-->
    <xsl:template name="xcalcColumnWidth">
        <!-- see original support comments in the XSL spec, source of this fragment -->
        <xsl:param name="theColwidth">1*</xsl:param>

        <!-- Ok, the theColwidth could have any one of the following forms: -->
        <!--        1*       = proportional width -->
        <!--     1unit       = 1.0 units wide -->
        <!--         1       = 1pt wide -->
        <!--  1*+1unit       = proportional width + some fixed width -->
        <!--      1*+1       = proportional width + some fixed width -->

        <!-- If it has a proportional width, translate it to XSL -->
        <xsl:if test="contains($theColwidth, '*')">
            <xsl:variable name="colfactor" select="substring-before($theColwidth, '*')"/>
            <xsl:text>proportional-column-width(</xsl:text>
            <xsl:choose>
                <xsl:when test="not($colfactor = '')">
                    <xsl:value-of select="$colfactor"/>
                </xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
            <xsl:text>)</xsl:text>
        </xsl:if>

        <!-- Now get the non-proportional part of the specification -->
        <xsl:variable name="width-units">
            <xsl:choose>
                <xsl:when test="contains($theColwidth, '*')">
                    <xsl:value-of
                        select="normalize-space(substring-after($theColwidth, '*'))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space($theColwidth)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Now the width-units could have any one of the following forms: -->
        <!--                 = <empty string> -->
        <!--     1unit       = 1.0 units wide -->
        <!--         1       = 1pt wide -->
        <!-- with an optional leading sign -->

        <!-- Get the width part by blanking out the units part and discarding -->
        <!-- whitespace. -->
        <xsl:variable name="width"
            select="normalize-space(translate($width-units,
                                              '+-0123456789.abcdefghijklmnopqrstuvwxyz',
                                              '+-0123456789.'))"/>

        <!-- Get the units part by blanking out the width part and discarding -->
        <!-- whitespace. -->
        <xsl:variable name="units"
            select="normalize-space(translate($width-units,
                                              'abcdefghijklmnopqrstuvwxyz+-0123456789.',
                                              'abcdefghijklmnopqrstuvwxyz'))"/>

        <!-- Output the width -->
        <xsl:value-of select="$width"/>

        <!-- Output the units, translated appropriately -->
        <xsl:choose>
            <xsl:when test="$units = 'pi'">pc</xsl:when>
            <xsl:when test="$units = '' and $width != ''">pt</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$units"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="attAlign">
        <xsl:if test="string(@align)">
            <xsl:attribute name="text-align">
                <xsl:value-of select="normalize-space(@align)"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template name="univAttrs">
        <xsl:apply-templates select="@platform | @product | @audience | @otherprops | @importance | @rev | @status"/>
    </xsl:template>

    <xsl:function name="opentopic-func:getSortString">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text, '[') and contains($text, ']')">
                <xsl:value-of select="substring-before(substring-after($text, '['),']')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="opentopic-func:fetchValueableText">
        <xsl:param name="node"/>

        <xsl:variable name="res">
            <xsl:apply-templates select="$node" mode="insert-text"/>
        </xsl:variable>

        <xsl:value-of select="$res"/>
    </xsl:function>

    <xsl:template match="*" mode="insert-text">
        <xsl:apply-templates mode="insert-text"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/indexterm ')]" mode="insert-text"/>

    <xsl:template match="text()[contains(., '[') and contains(., ']')][ancestor::*[contains(@class, ' topic/dl ')][contains(@otherprops,'sortable')]]" priority="10">
        <xsl:value-of select="substring-before(.,'[')"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/table ')]">
        <xsl:variable name="scale">
            <xsl:call-template name="getTableScale"/>
        </xsl:variable>

        <fo:block xsl:use-attribute-sets="table">
            <xsl:call-template name="commonattributes"/>
            <xsl:if test="not(@id)">
              <xsl:attribute name="id">
                <xsl:call-template name="get-id"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:if test="not($scale = '')">
                <xsl:attribute name="font-size"><xsl:value-of select="concat($scale, '%')"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/title ')]">
        <fo:block xsl:use-attribute-sets="table.title">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="'Table'"/>
                <xsl:with-param name="theParameters">
                    <number>
                        <xsl:number level="any" count="*[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/title ')]" from="/"/>
                    </number>
                    <title>
                        <xsl:apply-templates/>
                    </title>
                </xsl:with-param>
            </xsl:call-template>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tgroup ')]" name="tgroup">
        <xsl:if test="not(@cols)">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">006</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:if>

        <xsl:variable name="scale">
            <xsl:call-template name="getTableScale"/>
        </xsl:variable>

        <xsl:variable name="table">
            <fo:table xsl:use-attribute-sets="table.tgroup">
                <xsl:call-template name="commonattributes"/>

                <xsl:call-template name="displayAtts">
                    <xsl:with-param name="element" select=".."/>
                </xsl:call-template>

                <xsl:if test="(parent::*/@pgwide) = '1'">
                    <xsl:attribute name="start-indent">0</xsl:attribute>
                    <xsl:attribute name="end-indent">0</xsl:attribute>
                    <xsl:attribute name="width">auto</xsl:attribute>
                </xsl:if>

                <xsl:apply-templates/>
            </fo:table>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="not($scale = '')">
                <xsl:apply-templates select="$table" mode="setTableEntriesScale"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$table"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
  
    <xsl:template match="*[contains(@class, ' topic/tgroup ')][empty(*[contains(@class, ' topic/tbody ')]//*[contains(@class, ' topic/row ')])]" priority="10"/>

    <xsl:template match="*[contains(@class, ' topic/colspec ')]">
        <fo:table-column>
            <xsl:attribute name="column-number">
                <xsl:number count="colspec"/>
            </xsl:attribute>
      <xsl:if test="normalize-space(@colwidth) != ''">
        <xsl:attribute name="column-width">
          <xsl:choose>
            <xsl:when test="not(contains(@colwidth, '*'))">
              <xsl:call-template name="calculateColumnWidth.nonProportional">
                <xsl:with-param name="colwidth" select="@colwidth"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="calculateColumnWidth.Proportional">
                <xsl:with-param name="colwidth" select="@colwidth"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>

      <xsl:call-template name="applyAlignAttrs"/>
        </fo:table-column>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/thead ')]">
        <fo:table-header xsl:use-attribute-sets="tgroup.thead">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tbody ')]">
        <fo:table-body xsl:use-attribute-sets="tgroup.tbody">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-body>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/thead ')]/*[contains(@class, ' topic/row ')]">
        <fo:table-row xsl:use-attribute-sets="thead.row">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tbody ')]/*[contains(@class, ' topic/row ')]">
        <fo:table-row xsl:use-attribute-sets="tbody.row">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/thead ')]/*[contains(@class, ' topic/row ')]/*[contains(@class, ' topic/entry ')]">
        <fo:table-cell xsl:use-attribute-sets="thead.row.entry">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="applySpansAttrs"/>
            <xsl:call-template name="applyAlignAttrs"/>
            <xsl:call-template name="generateTableEntryBorder"/>
            <fo:block xsl:use-attribute-sets="thead.row.entry__content">
                <xsl:call-template name="processEntryContent"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tbody ')]/*[contains(@class, ' topic/row ')]/*[contains(@class, ' topic/entry ')]">
        <xsl:choose>
            <xsl:when test="ancestor::*[contains(@class, ' topic/table ')][1]/@rowheader = 'firstcol'
                        and empty(preceding-sibling::*[contains(@class, ' topic/entry ')])">
                <fo:table-cell xsl:use-attribute-sets="tbody.row.entry__firstcol">
                    <xsl:apply-templates select="." mode="processTableEntry"/>
                </fo:table-cell>
            </xsl:when>
            <xsl:otherwise>
                <fo:table-cell xsl:use-attribute-sets="tbody.row.entry">
                    <xsl:apply-templates select="." mode="processTableEntry"/>
                </fo:table-cell>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="processTableEntry">
        <xsl:call-template name="commonattributes"/>
        <xsl:call-template name="applySpansAttrs"/>
        <xsl:call-template name="applyAlignAttrs"/>
        <xsl:call-template name="generateTableEntryBorder"/>
        <fo:block xsl:use-attribute-sets="tbody.row.entry__content">
            <xsl:call-template name="processEntryContent"/>
        </fo:block>
    </xsl:template>

    <xsl:template name="processEntryContent">
        <xsl:variable name="entryNumber">
            <xsl:call-template name="countEntryNumber"/>
        </xsl:variable>
        <xsl:variable name="char">
            <xsl:choose>
                <xsl:when test="@char">
                    <xsl:value-of select="@char"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][position() = number($entryNumber)]/@char">
                    <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][position() = $entryNumber]/@char"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="charoff">
            <xsl:choose>
                <xsl:when test="@charoff">
                    <xsl:value-of select="@charoff"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][position() = number($entryNumber)]/@charoff">
                    <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][position() = $entryNumber]/@charoff"/>
                </xsl:when>
                <xsl:otherwise>50</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <xsl:choose>
            <xsl:when test="not($char = '')">
                <xsl:call-template name="processCharAlignment">
                    <xsl:with-param name="char" select="$char"/>
                    <xsl:with-param name="charoff" select="$charoff"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processCharAlignment">
        <xsl:param name="char"/>
        <xsl:param name="charoff"/>
        <xsl:choose>
            <xsl:when test="not(descendant::*)">
                <xsl:variable name="text-before" select="substring-before(text(),$char)"/>
                <xsl:variable name="text-after" select="substring-after(text(),$text-before)"/>
                <fo:list-block start-indent="0in"
                    provisional-label-separation="0pt"
                    provisional-distance-between-starts="{concat($charoff,'%')}">
                    <fo:list-item>
                        <fo:list-item-label end-indent="label-end()">
                            <fo:block text-align="right">
                                <xsl:copy-of select="$text-before"/>
                            </fo:block>
                        </fo:list-item-label>
                        <fo:list-item-body start-indent="body-start()">
                            <fo:block text-align="left">
                                <xsl:copy-of select="$text-after"/>
                            </fo:block>
                        </fo:list-item-body>
                    </fo:list-item>
                </fo:list-block>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="countEntryNumber">
        <xsl:choose>
            <xsl:when test="@colname">
                <xsl:variable name="colname" select="@colname"/>
                <xsl:if test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]">
                  <xsl:number select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]"/>
                </xsl:if>
            </xsl:when>
            <xsl:when test="@colnum">
                <xsl:value-of select="@colnum"/>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="calculateColumnWidth.Proportional">
        <xsl:param name="colwidth" >1*</xsl:param>

        <xsl:if test="contains($colwidth, '*')">
            <xsl:text>proportional-column-width(</xsl:text>
            <xsl:choose>
                <xsl:when test="substring-before($colwidth, '*') != ''">
                    <xsl:value-of select="substring-before($colwidth, '*')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>1.00</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>)</xsl:text>
        </xsl:if>

        <xsl:variable name="width-units">
            <xsl:choose>
                <xsl:when test="contains($colwidth, '*')">
                    <xsl:value-of select="normalize-space(substring-after($colwidth, '*'))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space($colwidth)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="width" select="normalize-space(translate($width-units, '+-0123456789.abcdefghijklmnopqrstuvwxyz%', '+-0123456789.'))"/>

        <xsl:if test="$width != ''">
            <xsl:text>proportional-column-width(</xsl:text>
                <xsl:value-of select="$width"/>
            <xsl:text>)</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template name="calculateColumnWidth.nonProportional">
        <xsl:param name="colwidth" >1*</xsl:param>

        <xsl:if test="contains($colwidth, '*')">
            <xsl:text>proportional-column-width(</xsl:text>
            <xsl:choose>
                <xsl:when test="substring-before($colwidth, '*') != ''">
                    <xsl:value-of select="substring-before($colwidth, '*')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>1.00</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>)</xsl:text>
        </xsl:if>

        <xsl:variable name="width-units">
            <xsl:choose>
                <xsl:when test="contains($colwidth, '*')">
                    <xsl:value-of select="normalize-space(substring-after($colwidth, '*'))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space($colwidth)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="width" select="normalize-space(translate($width-units, '+-0123456789.abcdefghijklmnopqrstuvwxyz%', '+-0123456789.'))"/>

        <xsl:variable name="units" select="normalize-space(translate($width-units, 'abcdefghijklmnopqrstuvwxyz%+-0123456789.', 'abcdefghijklmnopqrstuvwxyz%'))"/>

        <xsl:value-of select="$width"/>

        <xsl:choose>
            <xsl:when test="$units = 'pi'">pc</xsl:when>
            <xsl:when test="$units = '' and $width != ''">pt</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$units"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getEntryNumber">
        <xsl:param name="colname"/>
        <xsl:param name="optionalName" select="''"/>

        <xsl:choose>
            <xsl:when test="not(string(number($colname))='NaN')">
                <xsl:value-of select="$colname"/>
            </xsl:when>

            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]">
                <xsl:for-each select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]">
                    <xsl:choose>
                        <xsl:when test="@colnum">
                            <xsl:value-of select="@colnum"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="count(preceding-sibling::*[contains(@class, ' topic/colspec ')])+1"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>

            <xsl:when test="not($optionalName = '') and ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $optionalName]">
                <xsl:for-each select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $optionalName]">
                    <xsl:choose>
                        <xsl:when test="@colnum">
                            <xsl:value-of select="@colnum"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="count(preceding-sibling::*[contains(@class, ' topic/colspec ')])+1"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>

            <xsl:when test="not(string(number(translate($colname,'+-0123456789.abcdefghijklmnopqrstuvwxyz','0123456789')))='NaN')">
                <xsl:value-of select="number(translate($colname,'0123456789.abcdefghijklmnopqrstuvwxyz','0123456789'))"/>
            </xsl:when>

            <xsl:otherwise>
                <xsl:value-of select="'-1'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="applySpansAttrs">
        <xsl:if test="(@morerows) and (number(@morerows) &gt; 0)">
            <xsl:attribute name="number-rows-spanned">
                <xsl:value-of select="number(@morerows)+1"/>
            </xsl:attribute>
        </xsl:if>

        <xsl:if test="(@nameend) and (@namest)">
            <xsl:variable name="startNum">
                <xsl:call-template name="getEntryNumber">
                    <xsl:with-param name="colname" select="@namest"/>
                    <xsl:with-param name="optionalName" select="@colname"/>
                </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="endNum">
                <xsl:call-template name="getEntryNumber">
                    <xsl:with-param name="colname" select="@nameend"/>
                </xsl:call-template>
            </xsl:variable>

            <xsl:if test="($startNum &gt; '-1') and ($endNum &gt; '-1') and ((number($endNum) - number($startNum)) &gt; 0)">
                <xsl:attribute name="number-columns-spanned">
                    <xsl:value-of select="(number($endNum) - number($startNum))+1"/>
                </xsl:attribute>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template name="applyAlignAttrs">
        <xsl:variable name="align">
            <xsl:choose>
                <xsl:when test="@align">
                    <xsl:value-of select="@align"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class, ' topic/tbody ')][1][@align]">
                    <xsl:value-of select="ancestor::*[contains(@class, ' topic/tbody ')][1]/@align"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class, ' topic/thead ')][1][@align]">
                    <xsl:value-of select="ancestor::*[contains(@class, ' topic/tbody ')][1]/@align"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1][@align]">
                    <xsl:value-of select="ancestor::*[contains(@class, ' topic/tbody ')][1]/@align"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="valign">
            <xsl:choose>
                <xsl:when test="@valign">
                    <xsl:value-of select="@valign"/>
                </xsl:when>
                <xsl:when test="parent::*[contains(@class, ' topic/row ')][@valign]">
                    <xsl:value-of select="parent::*[contains(@class, ' topic/row ')]/@valign"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="not($align = '')">
                <xsl:attribute name="text-align">
                    <xsl:value-of select="$align"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="($align='') and contains(@class, ' topic/colspec ')"/>
            <xsl:otherwise>
                <xsl:attribute name="text-align">from-table-column()</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="$valign='top'">
                <xsl:attribute name="display-align">
                    <xsl:value-of select="'before'"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$valign='middle'">
                <xsl:attribute name="display-align">
                    <xsl:value-of select="'center'"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$valign='bottom'">
                <xsl:attribute name="display-align">
                    <xsl:value-of select="'after'"/>
                </xsl:attribute>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="generateTableEntryBorder">
        <xsl:variable name="colsep">
            <xsl:call-template name="getTableColsep"/>
        </xsl:variable>
        <xsl:variable name="rowsep">
            <xsl:call-template name="getTableRowsep"/>
        </xsl:variable>
        <xsl:variable name="frame">
          <xsl:variable name="f" select="ancestor::*[contains(@class, ' topic/table ')][1]/@frame"/>
          <xsl:choose>
            <xsl:when test="$f">
              <xsl:value-of select="$f"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$table.frame-default"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="needTopBorderOnBreak">
            <xsl:choose>
                <xsl:when test="$frame = 'all' or $frame = 'topbot' or $frame = 'top'">
                    <xsl:choose>
                        <xsl:when test="../parent::node()[contains(@class, ' topic/thead ')]">
                            <xsl:value-of select="'true'"/>
                        </xsl:when>
                        <xsl:when test="(../parent::node()[contains(@class, ' topic/tbody ')]) and not(../preceding-sibling::*[contains(@class, ' topic/row ')])">
                            <xsl:value-of select="'true'"/>
                        </xsl:when>
                        <xsl:when test="../parent::node()[contains(@class, ' topic/tbody ')]">
                            <xsl:variable name="entryNum" select="count(preceding-sibling::*[contains(@class, ' topic/entry ')]) + 1"/>
                            <xsl:variable name="prevEntryRowsep">
                                <xsl:for-each select="../preceding-sibling::*[contains(@class, ' topic/row ')][1]/*[contains(@class, ' topic/entry ')][$entryNum]">
                                    <xsl:call-template name="getTableRowsep"/>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:choose>
                                <xsl:when test="number($prevEntryRowsep)">
                                    <xsl:value-of select="'true'"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="'false'"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="'false'"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'false'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="number($rowsep) = 1 and (../parent::node()[contains(@class, ' topic/thead ')])">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'thead__tableframe__bottom'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="number($rowsep) = 1 and ((../following-sibling::*[contains(@class, ' topic/row ')]) or (../parent::node()[contains(@class, ' topic/tbody ')] and ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/tfoot ')]))">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__bottom'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$needTopBorderOnBreak = 'true'">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="number($colsep) = 1 and following-sibling::*[contains(@class, ' topic/entry ')]">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="number($colsep) = 1 and not(following-sibling::*[contains(@class, ' topic/entry ')]) and ((count(preceding-sibling::*)+1) &lt; ancestor::*[contains(@class, ' topic/tgroup ')][1]/@cols)">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="getTableColsep">
        <xsl:variable name="spanname" select="@spanname"/>
        <xsl:variable name="colname" select="@colname"/>
        <xsl:choose>
            <xsl:when test="@colsep">
                <xsl:value-of select="@colsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@colsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@colsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@colsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@colsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/table ')][1]/@colsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/table ')][1]/@colsep"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$table.colsep-default"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getTableRowsep">
        <xsl:variable name="colname" select="@colname"/>
        <xsl:variable name="spanname" select="@spanname"/>
        <xsl:choose>
            <xsl:when test="@rowsep">
                <xsl:value-of select="@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/row ')][1]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/row ')][1]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/table ')][1]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/table ')][1]/@rowsep"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$table.rowsep-default"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="displayAtts">
        <xsl:param name="element"/>
        <xsl:variable name="frame">
          <xsl:choose>
            <xsl:when test="$element/@frame">
              <xsl:value-of select="$element/@frame"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$table.frame-default"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$frame = 'all'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__all'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$frame = 'topbot'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__topbot'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$frame = 'top'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__top'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$frame = 'bottom'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__bottom'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$frame = 'sides'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__sides'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getTableScale">
        <xsl:value-of select="ancestor-or-self::*[contains(@class, ' topic/table ')][1]/@scale"/>
    </xsl:template>

    <xsl:template match="@*" mode="setTableEntriesScale">
        <xsl:choose>
            <xsl:when test="name() = 'font-size'">
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="node() | text()" mode="setTableEntriesScale">
        <xsl:copy>
            <xsl:apply-templates select="node() | @* | text()" mode="setTableEntriesScale"/>
        </xsl:copy>
    </xsl:template>

    <!--  Simpletable processing  -->
    <xsl:template match="*[contains(@class, ' topic/simpletable ')]">
        <xsl:variable name="number-cells">
            <!-- Contains the number of cells in the widest row -->
            <xsl:apply-templates select="*[1]" mode="count-max-simpletable-cells"/>
        </xsl:variable>
        <fo:table xsl:use-attribute-sets="simpletable">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="globalAtts"/>
            <xsl:call-template name="displayAtts">
                <xsl:with-param name="element" select="."/>
            </xsl:call-template>

            <xsl:if test="@relcolwidth">
                <xsl:variable name="fix-relcolwidth">
                    <xsl:apply-templates select="." mode="fix-relcolwidth">
                        <xsl:with-param name="number-cells" select="$number-cells"/>
                    </xsl:apply-templates>
                </xsl:variable>
                <xsl:call-template name="createSimpleTableColumns">
                    <xsl:with-param name="theColumnWidthes" select="$fix-relcolwidth"/>
                </xsl:call-template>
            </xsl:if>

            <!-- Toss processing to another template to process the simpletable
                 heading, and/or create a default table heading row. -->
            <xsl:apply-templates select="." mode="dita2xslfo:simpletable-heading">
                <xsl:with-param name="number-cells" select="$number-cells"/>
            </xsl:apply-templates>

            <fo:table-body xsl:use-attribute-sets="simpletable__body">
                <xsl:apply-templates select="*[contains(@class, ' topic/strow ')]">
                    <xsl:with-param name="number-cells" select="$number-cells"/>
                </xsl:apply-templates>
            </fo:table-body>

        </fo:table>
    </xsl:template>
  
    <xsl:template match="*[contains(@class, ' topic/simpletable ')][empty(*)]" priority="10"/>

    <xsl:template name="createSimpleTableColumns">
        <xsl:param name="theColumnWidthes" select="'1*'"/>

        <xsl:choose>
            <xsl:when test="contains($theColumnWidthes, ' ')">
                <fo:table-column>
                    <xsl:attribute name="column-width">
                        <xsl:call-template name="xcalcColumnWidth">
                            <xsl:with-param name="theColwidth" select="substring-before($theColumnWidthes, ' ')"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </fo:table-column>

                <xsl:call-template name="createSimpleTableColumns">
                    <xsl:with-param name="theColumnWidthes" select="substring-after($theColumnWidthes, ' ')"/>
                </xsl:call-template>

            </xsl:when>
            <xsl:otherwise>
                <fo:table-column>
                    <xsl:attribute name="column-width">
                        <xsl:call-template name="xcalcColumnWidth">
                            <xsl:with-param name="theColwidth" select="$theColumnWidthes"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </fo:table-column>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- SourceForge RFE 2874200:
         Fill in empty cells when one is missing from strow or sthead.
         Context for this call is strow or sthead. -->
    <xsl:template match="*" mode="fillInMissingSimpletableCells">
        <xsl:param name="fill-in-count" select="0"/>
        <xsl:if test="$fill-in-count >
            0">
            <fo:table-cell xsl:use-attribute-sets="strow.stentry">
                <xsl:call-template name="commonattributes"/>
                <xsl:variable name="frame">
                    <xsl:choose>
                        <xsl:when test="../@frame">
                            <xsl:value-of select="../@frame"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$table.frame-default"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="following-sibling::*[contains(@class, ' topic/strow ')]">
                    <xsl:call-template name="generateSimpleTableHorizontalBorders">
                        <xsl:with-param name="frame" select="$frame"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top'">
                    <xsl:call-template name="processAttrSetReflection">
                        <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                        <xsl:with-param name="path" select="$tableAttrs"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="($frame = 'all') or ($frame = 'topbot') or ($frame = 'sides')">
                    <xsl:call-template name="processAttrSetReflection">
                        <xsl:with-param name="attrSet" select="'__tableframe__left'"/>
                        <xsl:with-param name="path" select="$tableAttrs"/>
                    </xsl:call-template>
                    <xsl:call-template name="processAttrSetReflection">
                        <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                        <xsl:with-param name="path" select="$tableAttrs"/>
                    </xsl:call-template>
                </xsl:if>
                <fo:block>
                    <fo:inline>&#160;</fo:inline>
                </fo:block>
                <!-- Non-breaking space --> </fo:table-cell>
            <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
                <xsl:with-param name="fill-in-count" select="$fill-in-count - 1"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- Specialized simpletable elements may override this rule to add
         default headings for the table. By default, the existing sthead
         element is used when specified. -->
    <xsl:template match="*[contains(@class, ' topic/simpletable ')]" mode="dita2xslfo:simpletable-heading">
        <xsl:param name="number-cells">
            <xsl:apply-templates select="*[1]" mode="count-max-simpletable-cells"/>
        </xsl:param>
        <xsl:apply-templates select="*[contains(@class, ' topic/sthead ')]">
            <xsl:with-param name="number-cells" select="$number-cells"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/sthead ')]">
        <xsl:param name="number-cells">
            <xsl:apply-templates select="../*[1]" mode="count-max-simpletable-cells"/>
        </xsl:param>
        <fo:table-header xsl:use-attribute-sets="sthead">
            <xsl:call-template name="commonattributes"/>
            <fo:table-row xsl:use-attribute-sets="sthead__row">
                <xsl:apply-templates/>
                <xsl:variable name="row-cell-count" select="count(*[contains(@class, ' topic/stentry ')])"/>
                <xsl:if test="$row-cell-count &lt; $number-cells">
                    <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
                        <xsl:with-param name="fill-in-count" select="$number-cells - $row-cell-count"/>
                    </xsl:apply-templates>
                </xsl:if>
            </fo:table-row>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/strow ')]">
        <xsl:param name="number-cells">
            <xsl:apply-templates select="../*[1]" mode="count-max-simpletable-cells"/>
        </xsl:param>
        <fo:table-row xsl:use-attribute-sets="strow">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
            <xsl:variable name="row-cell-count" select="count(*[contains(@class, ' topic/stentry ')])"/>
            <xsl:if test="$row-cell-count &lt; $number-cells">
                <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
                    <xsl:with-param name="fill-in-count" select="$number-cells - $row-cell-count"/>
                </xsl:apply-templates>
            </xsl:if>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/sthead ')]/*[contains(@class, ' topic/stentry ')]">
        <fo:table-cell xsl:use-attribute-sets="sthead.stentry">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="entryCol" select="count(preceding-sibling::*[contains(@class, ' topic/stentry ')]) + 1"/>
            <xsl:variable name="frame">
                <xsl:variable name="f" select="ancestor::*[contains(@class, ' topic/simpletable ')][1]/@frame"/>
                <xsl:choose>
                    <xsl:when test="$f">
                        <xsl:value-of select="$f"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$table.frame-default"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:call-template name="generateSimpleTableHorizontalBorders">
                <xsl:with-param name="frame" select="$frame"/>
            </xsl:call-template>
            <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="following-sibling::*[contains(@class, ' topic/stentry ')]">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="number(ancestor::*[contains(@class, ' topic/simpletable ')][1]/@keycol) = $entryCol">
                    <fo:block xsl:use-attribute-sets="sthead.stentry__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="sthead.stentry__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/strow ')]/*[contains(@class, ' topic/stentry ')]">
        <fo:table-cell xsl:use-attribute-sets="strow.stentry">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="entryCol" select="count(preceding-sibling::*[contains(@class, ' topic/stentry ')]) + 1"/>
            <xsl:variable name="frame">
                <xsl:variable name="f" select="ancestor::*[contains(@class, ' topic/simpletable ')][1]/@frame"/>
                <xsl:choose>
                    <xsl:when test="$f">
                        <xsl:value-of select="$f"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$table.frame-default"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:if test="../following-sibling::*[contains(@class, ' topic/strow ')]">
                <xsl:call-template name="generateSimpleTableHorizontalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="following-sibling::*[contains(@class, ' topic/stentry ')]">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="number(ancestor::*[contains(@class, ' topic/simpletable ')][1]/@keycol) = $entryCol">
                    <fo:block xsl:use-attribute-sets="strow.stentry__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="strow.stentry__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <xsl:template name="generateSimpleTableHorizontalBorders">
        <xsl:param name="frame"/>
        <xsl:choose>
            <xsl:when test="($frame = 'all') or ($frame = 'topbot') or ($frame = 'sides') or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__bottom'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="generateSimpleTableVerticalBorders">
        <xsl:param name="frame"/>
        <xsl:choose>
            <xsl:when test="($frame = 'all') or ($frame = 'topbot') or ($frame = 'sides') or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="topicrefAttsNoToc">
      <!--TODO-->
    </xsl:template>

    <xsl:template name="topicrefAtts">
      <!--TODO-->
    </xsl:template>

    <xsl:template name="selectAtts">
      <!--TODO-->
    </xsl:template>

    <xsl:template name="globalAtts">
      <!--TODO-->
    </xsl:template>

</xsl:stylesheet>