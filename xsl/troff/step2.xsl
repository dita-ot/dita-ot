<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- Second step in the DITA to text transform. This takes an intermediate
     format, and converts it to text output. The text style is determined by
     the OUTFORMAT parameter. Currently supported values are plaintext, troff,
     and nroff (troff and nroff match at the moment). 

     The first step creates an intermediate format that uses only a few elements.
     It has a root <dita> element, and everything else fits in to these elements:
      <section> : used for <section> and <example>. This can nest any of the following elements.
      <sectiontitle> : used for the titles of <section> and <example>. This will nest the <text> element.
      <block> : all other block-like elements. The reason section does not use <block> 
                is that it maps well to troff-style sections that use the .SH macro
                for highlighting and indenting. This can nest any number of <block> 
                or <text> elements. Attributes set lead-in text (such as list item numbers 
                that must appear before the list item text), as well as indent values.
                Other attributes are described below.
      <text> : all text nodes and phrases. This can include text or additional <text> elements.

     Text will be wrapped, with the width determined by the LINELENGTH parameter. 
     Formatters such as troff may reflow the text as needed. Line breaks should only
     be forced in pre-formatted text, or between blocks.
     
     -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon"
                >

<!--
   ALL ELEMENTS CAN TAKE @xtrf and @xtrc
   
   Attributes on dita: 
           
   Attributes on section: 

   Attributes on sectiontitle: 
   
   Attributes on block:
      @xml:space="preserve"
      @position="center"
      @indent="digit" - additional indent new for this element
      @compact="yes|no"
      @leadin="" - text that appears once at the start of the element. It does not get the
                   extra indenting specified by @indent.
                   
   Attributes on text:
      @style="bold|italics|underline|tt|sup|sub"
      @href="" [target, if this is a link]
      @format="" copy through @format for a link
      @scope="" copy through @scope for a link
-->

<xsl:import href="../common/output-message.xsl"/>

<xsl:output method="text"
            encoding="UTF-8"
            indent="no"
            omit-xml-declaration = "yes"
/>


<xsl:param name="LINELENGTH">50</xsl:param>

<xsl:param name="OUTFORMAT">troff</xsl:param>
<xsl:param name="FILENAME"></xsl:param>
<xsl:variable name="msgprefix">DOTX</xsl:variable>
<xsl:variable name="OUTEXT">txt</xsl:variable>

<!-- Single newline character. This is used to search for newlines in pre-formatted text, and
     is used for wrapping text (processors may choose to reflow wrapped text). -->
<xsl:variable name="newline"><xsl:text>
</xsl:text></xsl:variable>

<xsl:template name="force-newline">
  <xsl:choose>
    <xsl:when test="$OUTFORMAT='plaintext'"><xsl:value-of select="$newline"/></xsl:when>
    <xsl:when test="$OUTFORMAT='troff' or $OUTFORMAT='nroff'">
      <xsl:value-of select="$newline"/>.sp<xsl:value-of select="$newline"/>
    </xsl:when>
  </xsl:choose>
</xsl:template>
<xsl:template name="force-two-newlines">
  <xsl:choose>
    <xsl:when test="$OUTFORMAT='plaintext'"><xsl:value-of select="$newline"/><xsl:value-of select="$newline"/></xsl:when>
    <xsl:when test="$OUTFORMAT='troff' or $OUTFORMAT='nroff'">
      <xsl:value-of select="$newline"/>.sp 2<xsl:value-of select="$newline"/>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<!-- root rule -->
<xsl:template match="/">
  <xsl:apply-templates select="*[1]"/>
</xsl:template>

<xsl:template match="*">
    <xsl:apply-templates select="*[1]"/>
</xsl:template>

<!-- Find the current indent length. If formatters (such as troff?) do indenting on
     their own, they can always return '' from this function. -->
<xsl:template match="*" mode="find-indent">
  <xsl:choose>
    <xsl:when test="not(@indent)"/>
    <xsl:when test="@indent='1'"><xsl:text> </xsl:text></xsl:when>
    <xsl:when test="@indent='2'"><xsl:text>  </xsl:text></xsl:when>
    <xsl:when test="@indent='3'"><xsl:text>   </xsl:text></xsl:when>
    <xsl:when test="@indent='4'"><xsl:text>    </xsl:text></xsl:when>
    <xsl:when test="@indent='5'"><xsl:text>     </xsl:text></xsl:when>
    <xsl:when test="@indent='6'"><xsl:text>      </xsl:text></xsl:when>
    <xsl:when test="@indent='7'"><xsl:text>       </xsl:text></xsl:when>
    <xsl:when test="@indent='8'"><xsl:text>        </xsl:text></xsl:when>
    <xsl:when test="@indent='9'"><xsl:text>         </xsl:text></xsl:when>
    <xsl:when test="@indent='10'"><xsl:text>          </xsl:text></xsl:when>
    <xsl:when test="@indent='11'"><xsl:text>           </xsl:text></xsl:when>
    <xsl:when test="@indent='12'"><xsl:text>            </xsl:text></xsl:when>
    <xsl:when test="@indent='13'"><xsl:text>             </xsl:text></xsl:when>
    <xsl:when test="@indent='14'"><xsl:text>              </xsl:text></xsl:when>
    <xsl:when test="@indent='15'"><xsl:text>               </xsl:text></xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="getFirstWord">
    <xsl:param name="string"/>
    <xsl:choose>
        <!-- For DBCS text, only take one character, unless English, or followed by punctuation -->
        <xsl:when test="contains($string,' ')"><xsl:value-of select="substring-before($string,' ')"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$string"/></xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- This drops leading spaces... but that's already done if calling with normalize-space.
     The function recursivly processes "string" while counting the line length. It adds a
     newline when needed, and resets the current length. 

     If a formatter wants to control indenting, simply start with the indent command. May want
     to add a parameter that makes it easy to tell if this is the first time "wrap" was called
     in order to enable this. Then update the find-indent template to return '' for that output
     format. -->
<xsl:template name="wrap">
  <xsl:param name="string"/>
  <xsl:param name="curLength">0</xsl:param>
  <xsl:param name="leadin"/>   <!-- Text to use once, before indent -->
  <xsl:param name="addIndent">
    <xsl:choose>
      <!-- If there is lead-in text that does not indent, only get the indent from ancestor blocks -->
      <xsl:when test="string-length(normalize-space($leadin))>0">
        <xsl:apply-templates select="ancestor-or-self::block/ancestor::*[@indent]" mode="find-indent"/>
      </xsl:when>
      <!-- Otherwise, start with the indent on the current element -->
      <xsl:otherwise>
        <xsl:apply-templates select="ancestor-or-self::*[@indent]" mode="find-indent"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:variable name="firstword">
    <xsl:call-template name="getFirstWord">
      <xsl:with-param name="string" select="$string"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="remainder" select="substring-after($string,' ')"/>
  <xsl:choose>
    <!-- If there is leadin text, issue it with the current indent, before
         adding any indent for this block -->
    <xsl:when test="string-length(normalize-space($leadin))>0">
      <xsl:value-of select="$addIndent"/>
      <xsl:value-of select="$leadin"/>
      <xsl:value-of select="$firstword"/>
      <xsl:call-template name="wrap">
        <xsl:with-param name="string" select="$remainder"/>
          <xsl:with-param name="curLength" select="string-length($leadin) + string-length($firstword) + string-length($addIndent)"/>
      </xsl:call-template>
    </xsl:when>
    <!-- End of the string; nothing left to evaluate, so quit -->
    <xsl:when test="string-length($string)=0"/>
    <!-- At the start of the line; add the word, whatever the length -->
    <xsl:when test="$curLength='0'">
      <xsl:value-of select="$addIndent"/>
      <xsl:value-of select="$firstword"/>
      <xsl:call-template name="wrap">
        <xsl:with-param name="string" select="$remainder"/>
        <xsl:with-param name="curLength" select="string-length($firstword) + string-length($addIndent)"/>
        <xsl:with-param name="addIndent" select="$addIndent"/>
      </xsl:call-template>
    </xsl:when>
    <!-- Normal text. This word does not fit on the line. End this line, start the next. -->
    <xsl:when test="string-length($firstword) + 1 + number($curLength) > $LINELENGTH">
      <xsl:value-of select="$newline"/>
      <xsl:value-of select="$addIndent"/>
      <xsl:value-of select="$firstword"/>
      <xsl:call-template name="wrap">
        <xsl:with-param name="string" select="$remainder"/>
        <xsl:with-param name="curLength" select="string-length($firstword) + string-length($addIndent)"/>
        <xsl:with-param name="addIndent" select="$addIndent"/>
      </xsl:call-template>
    </xsl:when>
    <!-- Normal text; this word fits on the line. Add it and continue. --> 
    <xsl:otherwise>
      <xsl:if test="$curLength>0"><xsl:text> </xsl:text></xsl:if>
      <xsl:value-of select="$firstword"/>
      <xsl:call-template name="wrap">
        <xsl:with-param name="string" select="$remainder"/>
        <xsl:with-param name="curLength" select="number($curLength) + 1 + string-length($firstword)"/>
        <xsl:with-param name="addIndent" select="$addIndent"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Process pre-formatted text. Newlines should be preserved, none should be added. -->
<xsl:template name="preserve-space">
    <xsl:param name="string" select="."/>
    <xsl:choose>
        <xsl:when test="string-length($string)=0"/>
        <xsl:when test="contains($string,$newline)">
            <!-- Warn if the line exceeds the limit? -->
            <xsl:value-of select="substring-before($string,$newline)"/>
            <xsl:call-template name="force-newline"/>
            <xsl:call-template name="preserve-space">
                <xsl:with-param name="string" select="substring-after($string,$newline)"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$string"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- Process a block. If there was a block or text immediately before, we need to jump down
     a new line. -->
<xsl:template match="block">
  <xsl:if test="preceding-sibling::*">
    <xsl:choose>
      <xsl:when test="@compact='yes'"><xsl:call-template name="force-newline"/></xsl:when>
      <xsl:otherwise><xsl:call-template name="force-two-newlines"/></xsl:otherwise>
    </xsl:choose>
  </xsl:if>
  <xsl:apply-templates select="*[1]"/>
  <xsl:apply-templates select="following-sibling::*[1]"/>
</xsl:template>

<!-- If the section has a title, TROFF can use the .SH macro to get the title formatting. -->
<xsl:template match="section">
  <xsl:choose>
    <xsl:when test="sectiontitle">
      <xsl:apply-templates select="sectiontitle[1]"/>
      <xsl:apply-templates select="(text|block)[1]"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="preceding-sibling::*">
        <xsl:call-template name="force-two-newlines"/>
      </xsl:if>
      <xsl:apply-templates select="*[1]"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates select="following-sibling::*[1]"/>
</xsl:template>

<!-- Based on step1, section titles should come first in the section. If this is
     a *ROFF format, use the .SH macro to get roff's section-like formatting. -->
<xsl:template match="sectiontitle">
  <xsl:choose>
    <xsl:when test="$OUTFORMAT='plaintext'">
      <xsl:call-template name="force-two-newlines"/>
      <xsl:apply-templates select="*[1]"/>
    </xsl:when>
    <xsl:when test="$OUTFORMAT='troff' or $OUTFORMAT='nroff'">
      <xsl:text>
.SH "</xsl:text><xsl:apply-templates select="*[1]"/><xsl:text>"</xsl:text>
    </xsl:when>
    <xsl:otherwise><xsl:apply-templates select="*[1]"/></xsl:otherwise>
  </xsl:choose>
  <!-- Do not process following siblings: those come through from section -->
</xsl:template>

<!-- Use <block @position="center"> for centering - not yet implemented.
     This template matches pre-formatted blocks like <pre> and <lines>. 

     May be able to update this to use TROFF commands that create an entire
     preformatted section; would need to make sure that nested elements do
     not cause problems with that. -->
<xsl:template match="block[@xml:space='preserve']">
  <xsl:if test="preceding-sibling::*">
    <xsl:call-template name="force-two-newlines"/>
  </xsl:if>
  <xsl:call-template name="preserve-space"/>
  <xsl:apply-templates select="(following-sibling::*)[1]"/>
</xsl:template>

<!-- The following three functions will switch the current style to the
     proper formatting. For plain text, nothing is generated. -->
<xsl:template name="start-bold">
  <xsl:choose>
    <xsl:when test="$OUTFORMAT='plaintext'"/>
    <xsl:when test="$OUTFORMAT='troff' or $OUTFORMAT='nroff'">\fB</xsl:when>
  </xsl:choose>
</xsl:template>
<xsl:template name="start-italics">
  <xsl:choose>
    <xsl:when test="$OUTFORMAT='plaintext'"/>
    <xsl:when test="$OUTFORMAT='troff' or $OUTFORMAT='nroff'">\fI</xsl:when>
  </xsl:choose>
</xsl:template>
<xsl:template name="start-normal">
  <xsl:choose>
    <xsl:when test="$OUTFORMAT='plaintext'"/>
    <xsl:when test="$OUTFORMAT='troff' or $OUTFORMAT='nroff'">\fR</xsl:when>
  </xsl:choose>
</xsl:template>

<!-- This is called to process the contents of <text> elements. It will set
     the correct style if needed, and process children, and then return the
     style to normal. -->
<xsl:template name="format-text">
  <xsl:param name="current-style" select="'normal'"/>
  <xsl:choose>
    <xsl:when test="not(@style)"><xsl:apply-templates/></xsl:when>
    <xsl:when test="@style='bold'">
      <xsl:call-template name="start-bold"/>
      <xsl:apply-templates>
        <xsl:with-param name="current-style" select="'bold'"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:when test="@style='italics'">
      <xsl:call-template name="start-italics"/>
      <xsl:apply-templates>
        <xsl:with-param name="current-style" select="'italics'"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
  </xsl:choose>
  <!-- If there was a style, return to original style -->
  <xsl:if test="@style='bold' or @style='italics'">
    <xsl:choose>
      <xsl:when test="$current-style='bold'"><xsl:call-template name="start-bold"/></xsl:when>
      <xsl:when test="$current-style='italics'"><xsl:call-template name="start-italics"/></xsl:when>
      <xsl:otherwise><xsl:call-template name="start-normal"/></xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<!-- For text within text, do not worry about newlines. Just process the contents. -->
<xsl:template match="text/text">
  <xsl:param name="current-style"/>
  <xsl:call-template name="format-text">
    <xsl:with-param name="current-style" select="$current-style"/>
  </xsl:call-template>
</xsl:template>

<!-- For text that is directly inside a block or section, newlines may be needed. Process
     all consecutive text elements at once. -->
<xsl:template match="text">
  <!-- There should not be a style active, because the parent is a block. However, in the future,
       there could be a reason to set an entire block to bold, italics, etc, so go ahead
       and allow for it. -->
  <xsl:param name="current-style"/>

  <!-- Get all of the text up to the next block. This allows for easy wrapping, and prevents
       us from putting out newlines between text elements. First get the current element, 
       then progress through following elements. -->
  <xsl:variable name="upToBlock">
    <xsl:call-template name="format-text">
      <xsl:with-param name="current-style" select="$current-style"/>
    </xsl:call-template>
    <xsl:apply-templates select="(following-sibling::*)[1]" mode="text-in-block">
      <xsl:with-param name="current-style" select="$current-style"/>
    </xsl:apply-templates>
  </xsl:variable>
   
  <!-- If text comes after </block>, jump to the next line, then leave a blank line. -->
  <xsl:if test="(preceding-sibling::*)[last()][self::block|self::section|self::sectiontitle]">
    <xsl:call-template name="force-two-newlines"/>
  </xsl:if>
  
  <!-- Process all text up to the next block. If the parent block had lead-in text, and
       it has not been used, pass that to the function. -->
  <xsl:call-template name="wrap">
    <xsl:with-param name="string" select="normalize-space($upToBlock)"/>
    <xsl:with-param name="leadin">
      <xsl:if test="../@leadin">
        <xsl:if test="not(preceding-sibling::*)"><xsl:value-of select="../@leadin"/></xsl:if>
      </xsl:if>
    </xsl:with-param>
  </xsl:call-template>
  <xsl:apply-templates select="following-sibling::*[self::block][1]"/>
</xsl:template>

<!-- This matches text when sequentially moving through text blocks. Process
     the contents, and move on to the next element. -->
<xsl:template match="text" mode="text-in-block">
  <xsl:param name="current-style"/>
  <xsl:call-template name="format-text">
    <xsl:with-param name="current-style" select="$current-style"/>
  </xsl:call-template>
  <xsl:apply-templates select="following-sibling::*[1]" mode="text-in-block"/>
</xsl:template>

<!-- When moving through text elements, stop at block or section. -->
<xsl:template match="block|section" mode="text-in-block"/>

</xsl:stylesheet>
