<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!DOCTYPE xsl:transform [
<!-- entities for use in the generated output (must produce correctly in FO) -->
  <!ENTITY rbl           "&#160;">
  <!ENTITY quotedblleft  "&#x201C;">
  <!ENTITY quotedblright "&#x201D;">
  <!ENTITY bullet        "&#x2022;"><!--check these two for better assignments -->
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>
 


<!-- =================== start of element rules ====================== -->

<!--==== pre-body subset ====-->
<!-- note that topic titles are handled in the calling stylesheet -->

<!-- renamed titlealts to cause it not to match. Its content is extraneous for PDF output -->

<xsl:template match="*[contains(@class,' topic/xtitlealts ')]">
  <fo:block background-color="#f0f0d0">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="border-width">thin</xsl:attribute>
    <xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/></xsl:attribute>
    <xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/></xsl:attribute>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/navtitle ')]">
  <fo:block>
    <fo:inline font-weight="bold">Navigation title: </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/searchtitle ')]">
  <fo:block>
    <fo:inline font-weight="bold">Search title: </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]" mode="outofline">
  <fo:block xsl:use-attribute-sets="p" start-indent="{$basic-start-indent}">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]">
  <xsl:if test="not(following-sibling::*[contains(@class,' topic/body ')])">
    <xsl:apply-templates select="." mode="outofline"/>
  </xsl:if>
</xsl:template>

<!-- shortdesc is called outside of body thus needs to set up its own indent. also relatedlinks -->
<xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="outofline">
  <fo:block xsl:use-attribute-sets="p" start-indent="{$basic-start-indent}">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<!-- Updated for DITA 1.1 "Shortdesc proposal" -->
<!-- Added for SF 1363055: Shortdesc disappears when optional body is removed -->
<xsl:template match="*[contains(@class,' topic/shortdesc ')]">
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class, ' topic/abstract ')]">
      <xsl:apply-templates select="." mode="outofline"/>
    </xsl:when>
    <xsl:when test="not(following-sibling::*[contains(@class,' topic/body ')])">    
      <xsl:apply-templates select="." mode="outofline"/>
    </xsl:when>
    <xsl:otherwise></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/xshortdesc ')]">
  <fo:block start-indent="{$basic-start-indent}" background-color="#F0C0F0">
    <fo:block background-color="red">The &lt;shortdesc&gt; element is also reflected following the prolog.</fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<!--==== body content subset ===-->

<!-- this is the first body called, which sets the indent and overall organization -->
<xsl:template match="*[contains(@class,' topic/body ')]" name="topbody" priority="3">
  <fo:block start-indent="{$basic-start-indent}">
  <xsl:attribute name="font-size">10pt</xsl:attribute>
    <!-- here, you can generate a toc based on what's a child of body -->
    <!--xsl:call-template  name="gen-sect-ptoc"/-->
    
    <!-- Added for DITA 1.1 "Shortdesc proposal" -->
    <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/abstract ')]" mode="outofline"/>
   
    <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]" mode="outofline"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<!-- this is the fallthrough body for nested topics -->
<xsl:template match="*[contains(@class,' topic/body ')]">
  <fo:block start-indent="{$basic-start-indent}">
   <xsl:attribute name="font-size">10pt</xsl:attribute>
   
   <!-- Added for DITA 1.1 "Shortdesc proposal" -->
   <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/abstract ')]" mode="outofline"/>
   
   <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]" mode="outofline"/>
   <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/section ')]">
  <fo:block line-height="12pt">
 <xsl:attribute name="space-before">0.6em</xsl:attribute>
  <xsl:attribute name="font-size">10pt</xsl:attribute>
    <!-- set id -->
    <xsl:call-template name="gen-toc-id"/>
    <!-- preferentially pull the head here; process in place for now -->
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/example ')]">
  <fo:block line-height="12pt">
  <xsl:attribute name="space-before">0.6em</xsl:attribute>
  <xsl:attribute name="font-size">10pt</xsl:attribute>
    <!-- set id -->
    <xsl:call-template name="gen-toc-id"/>
    <!-- preferentially pull the head here; process in place for now -->
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/p ')]">
  <!-- set id -->
  <xsl:choose>
    <xsl:when test="descendant::*[contains(@class,' topic/pre ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/ul ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/ol ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/lq ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/dl ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/note ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/lines ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/fig ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/table ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:when test="descendant::*[contains(@class,' topic/simpletable ')]">
      <xsl:call-template name="divlikepara"/>
    </xsl:when>
    <xsl:otherwise>
      <fo:block xsl:use-attribute-sets="p">
        <xsl:if test="string(@id)"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute></xsl:if>
        <xsl:apply-templates/>
      </fo:block>
    </xsl:otherwise>
  </xsl:choose>    
</xsl:template>

<xsl:template name="divlikepara">
  <fo:block xsl:use-attribute-sets="divlike.p">
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/note ')]">
  <fo:block>
    <!-- setclass -->
    <!-- set id -->
    <fo:inline border-left-width="0pt" border-right-width="0pt" font-weight="bold">
    <!--[<xsl:value-of select="@type"/>] was wrapper -->
    <xsl:choose>
      <xsl:when test="@type='note'">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Note'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='tip'">
      <xsl:call-template name="proc-ing"/>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Tip'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='fastpath'">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Fastpath'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='restriction'">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Restriction'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='important'">>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Important'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='remember'">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Remember'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='attention'">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Attention'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='caution'">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Caution'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='danger'">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Danger'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="@type='other'">
        <xsl:choose>
          <xsl:when test="@othertype">
          <!-- othertype is a key that should look up external, translateable text. -->
           <xsl:value-of select="@othertype"/>
          </xsl:when>
          <xsl:otherwise>
            [<xsl:value-of select="@type"/>]
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
          <!-- nop --><xsl:text>Note</xsl:text>
      </xsl:otherwise>
    </xsl:choose><xsl:text>: </xsl:text>
    </fo:inline><xsl:text>  </xsl:text>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/desc ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt">
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/lq ')]">
  <fo:block xsl:use-attribute-sets="lq">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates/>
    <xsl:choose>
      <xsl:when test="@href">
        <fo:block text-align="right"><xsl:value-of select="@href"/>,
<xsl:value-of select="@reftitle"/></fo:block>
      </xsl:when>
      <xsl:when test="@reftitle">
        <fo:block text-align="right"><xsl:value-of select="@reftitle"/></fo:block>
      </xsl:when>
      <xsl:otherwise><!--nop--></xsl:otherwise>
    </xsl:choose>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/q ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt">
    &quotedblleft;<xsl:apply-templates />&quotedblright;
  </fo:inline>
</xsl:template>




<!-- figure setup -->

<xsl:template match="*[contains(@class,' topic/fig ')]">
  <fo:block xsl:use-attribute-sets="fig">
    <!-- setclass -->
    <!-- set id -->
    <xsl:if test="@id">
      <xsl:apply-templates select="@id"/>
    </xsl:if>    
    <xsl:call-template name="setframe"/>
    <xsl:if test="@expanse = 'page'">
      <xsl:attribute name="start-indent">-<xsl:value-of select="$basic-start-indent"/></xsl:attribute>
    </xsl:if>
    <!-- this is where the main fig rendering happens -->
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/figgroup ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>

<!-- record end respecting data -->


<xsl:template match="*[contains(@class,' topic/pre ')]">
  <xsl:call-template name="gen-att-label"/>
  <fo:block xsl:use-attribute-sets="pre">
    <!-- setclass -->
    <!-- set id -->
    <xsl:call-template name="setscale"/>
    <xsl:call-template name="setframe"/>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/lines ')]">
  <xsl:call-template name="gen-att-label"/>
  <fo:block xsl:use-attribute-sets="lines">
     <!-- setclass -->
     <!-- set id -->
     <xsl:call-template name="setscale"/>
    <xsl:call-template name="setframe"/>
     <xsl:apply-templates/>
  </fo:block>
</xsl:template>



<!-- phrase elements -->

<xsl:template match="*[contains(@class,' topic/term ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/ph ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt"><!-- color="purple"-->
    <!-- setclass -->

    <!-- set id -->
    <xsl:apply-templates/>
  </fo:inline>
</xsl:template>



<xsl:template match="*[contains(@class,' topic/tm ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates/>
    <fo:inline baseline-shift="super" font-size="75%">
    <xsl:choose>
      <xsl:when test="@tmtype='tm'">(TM)</xsl:when>
      <xsl:when test="@tmtype='reg'">(R)</xsl:when>
      <xsl:when test="@tmtype='service'">(SM)</xsl:when>
      <xsl:otherwise>
        <xsl:text>Error in tm type.</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    </fo:inline>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/boolean ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt" color="green">
    <!-- setclass -->
    <!-- set id -->
    <xsl:value-of select="name()"/><xsl:text>: </xsl:text><xsl:value-of select="@state"/>
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/state ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt" color="red">
    <!-- setclass -->
    <!-- set id -->
    <xsl:value-of select="name()"/><xsl:text>: </xsl:text><xsl:value-of select="@name"/><xsl:text>=</xsl:text><xsl:value-of select="@value"/>
  </fo:inline>
</xsl:template>


<!-- image and object data -->
<xsl:template match="*[contains(@class,' topic/image ')]">
  <!-- build any pre break indicated by style -->
  <xsl:choose>
    <xsl:when test="parent::fig[contains(@frame,'top')]">
      <!-- NOP if there is already a break implied by a parent property -->
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="not(@placement='inline')">
        <!-- generate an FO break here -->
        <fo:block>&rbl;</fo:block>
      </xsl:if>
    </xsl:otherwise>
  </xsl:choose>
  
  <xsl:choose>
    <xsl:when test="@placement='break'">
      <fo:block>
        <xsl:choose>
          <xsl:when test="@align='left'"><xsl:attribute name="text-align">left</xsl:attribute></xsl:when>
          <xsl:when test="@align='right'"><xsl:attribute name="text-align">right</xsl:attribute></xsl:when>
          <xsl:when test="@align='center'"><xsl:attribute name="text-align">center</xsl:attribute></xsl:when>
          <xsl:when test="@align='justify'"><xsl:attribute name="text-align">justify</xsl:attribute></xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
        <!--xsl:call-template name="topic-image"/-->
        <xsl:call-template name="insert-graphic"/>
      </fo:block>
    </xsl:when>
    <xsl:otherwise>
      <!--xsl:call-template name="topic-image"/-->
      <xsl:call-template name="insert-graphic"/>
    </xsl:otherwise>
  </xsl:choose>
  
  <!-- build any post break indicated by style -->
  <xsl:choose>
    <xsl:when test="parent::fig[contains(@frame,'bot')]">
      <!-- NOP if there is already a break implied by a parent property -->
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="not(@placement='inline')">
        <!-- generate an FO break here -->
        <fo:block> </fo:block>
      </xsl:if>
    </xsl:otherwise>
  </xsl:choose>
  <!-- build optional echo of the image name for review -->
  <xsl:if test="$ARTLBL='yes'">
     <fo:block font-weight="bold">[<xsl:value-of select="$IP"/> <xsl:value-of select="@href"/>]</fo:block>
  </xsl:if>
</xsl:template>

<xsl:template name="insert-graphic">
  <fo:external-graphic> <!-- apply scaling attributes here? -->
    <xsl:attribute name="src">url(<xsl:call-template name="get-image-uri"/>)</xsl:attribute>
    <xsl:choose>
      <xsl:when test="@scale"><xsl:attribute name="content-width"><xsl:value-of select="concat(@scale,'%')"/></xsl:attribute></xsl:when>
      <xsl:otherwise><!--xsl:attribute name="content-width">scale-to-fit</xsl:attribute--></xsl:otherwise> <!-- no effect -->
    </xsl:choose>
  </fo:external-graphic>
</xsl:template>

<!-- Get image URI -->
<xsl:template name="get-image-uri">
  <xsl:param name="href" select="@href" />
  <!--hard force the extension to jpg -->
  <!--xsl:choose>
    <xsl:when test="contains($href,'.gif')">
      <xsl:value-of select="concat($img-path, substring-before($href,'.gif'), $dflt-ext)"/>
    </xsl:when>
    <xsl:when test="contains($href,'.jpg')">
      <xsl:value-of select="concat($img-path, substring-before($href,'.jpg'), $dflt-ext)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$href"/>
    </xsl:otherwise-->
  <xsl:value-of select="$href"/>
  <!--</xsl:choose> -->
</xsl:template>
<!-- this is unused for now, but is browser specific; must be converted! -->
<xsl:template name="topic-image">
  <!-- now invoke the actual content and its alt text -->
  <xsl:element name="img">
    <!-- setclass -->
    <xsl:attribute name="src">
     <xsl:value-of select="$IP"/>
       <xsl:choose>
         <xsl:when test="@objname">
           <!--xsl:call-template name="get-objdescinfo"/-->
         </xsl:when>
         <xsl:when test="@href">
           <xsl:value-of select="@href"/>
         </xsl:when>
         <xsl:otherwise>
           <!-- no action -->
         </xsl:otherwise>
       </xsl:choose>
     </xsl:attribute>
    <xsl:if test="@height"><xsl:attribute name="height"><xsl:value-of select="@height"/></xsl:attribute></xsl:if>
    <xsl:if test="@width"><xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute></xsl:if>
    <xsl:choose>
      <xsl:when test="@alt">
        <xsl:attribute name="alt"><xsl:value-of select="@alt"/></xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="alt"><xsl:value-of select="*[contains(@class,' topic/textalt')]"/></xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:element>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/object ')]">
<fo:block>
  <!-- copy through for browsers; unused for FO -->
</fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/param ')]">
<fo:block>
  <!-- copy through for browsers; unused for FO -->
</fo:block>
</xsl:template>




<!-- content usually rendered out of sequence -->

<!-- by adding the [$DRAFT='yes'] predicate to this rule, you can cause
     the content to disappear in the ordinary case. When parameterization
     becomes possible, consider adding yes/no views so that content is not
     "lost" by being invisible!!!! -->

<!-- this template needs to be parametrically controlled by user for visibility -->
<xsl:template match="*[contains(@class,' topic/draft-comment ')]">
  <xsl:if test="$DRAFT='yes'">
    <fo:block background-color="#FF99FF" color="#CC3333">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="border-width">thin</xsl:attribute>
      <fo:block font-weight="bold">
Disposition: <xsl:value-of select="@disposition"/> / 
Status: <xsl:value-of select="@status"/>
      </fo:block> 
      <xsl:apply-templates/>
    </fo:block>
  </xsl:if>
</xsl:template>


<!-- this template needs to be parametrically controlled by user for visibility -->
<xsl:template match="*[contains(@class,' topic/required-cleanup ')]">
  <xsl:if test="$DRAFT='yes'">
    <fo:inline background="yellow" color="#CC3333">  <!-- indents won't apply here; not a block context -->
     <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="border-width">thin</xsl:attribute>
     <!-- set id -->
      <fo:inline font-weight="bold">Required Cleanup <xsl:if test="string(@remap)">(<xsl:value-of select="@remap"/>) </xsl:if><xsl:text>: </xsl:text></fo:inline> 
      <xsl:apply-templates />
    </fo:inline>
  </xsl:if>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/fn ')]">
    <fo:block font-size="8pt" color="purple">
      <xsl:if test="@id">
        <fo:inline font-style="italic">
          <xsl:text>[Footnote: </xsl:text>
          <xsl:value-of select="@id"/>
          <xsl:text>]</xsl:text>
        </fo:inline>
      </xsl:if>
      <xsl:if test="@callout">
        <fo:inline baseline-shift="super" font-size="75%">
           <xsl:value-of select="@callout"/>
           <!--xsl:number level="multiple" count="//fn"  format="1 "/-->
        </fo:inline>
      </xsl:if>
      <xsl:apply-templates/>
    </fo:block>
</xsl:template>

<xsl:template match="footnotex">
<fo:footnote>
  <fo:inline baseline-shift="super" font-size="7pt" font-family="Helvetica">
    <xsl:number count="//fn"    format="1" />
  </fo:inline>
  <fo:footnote-body>
    <!--fo:block space-after="0pt">
      <fo:leader leader-pattern="rule" leader-length="1in" rule-thickness=".5pt" color="black"/>
      <fo:leader leader-pattern="rule" leader-length="30%" rule-thickness="1pt" rule-style="solid"/> 
    </fo:block-->
    <fo:block xsl:use-attribute-sets="footnote">
      <fo:inline baseline-shift="super" font-size="75%">
         <xsl:number level="multiple" count="//fn"  format="1 "/>
      </fo:inline>
      <xsl:apply-templates />
    </fo:block>
  </fo:footnote-body>
</fo:footnote>
</xsl:template>



<!-- other special data -->

<!-- this rule is prolog-specific; can move it into dita-prolog.xsl if desired -->
<xsl:template match="*[contains(@class,' topic/keywords ')]/*[contains(@class,' topic/keyword ')]" priority="2">
  <fo:inline>
    <xsl:text> [</xsl:text><xsl:apply-templates /><xsl:text>] </xsl:text>
  </fo:inline>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/keyword ')]">
  <fo:inline border-left-width="0pt" border-right-width="0pt">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates />
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/cite ')]">
  <fo:inline font-style="italic">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates />
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/indextermref ')]">
  <fo:inline font-style="italic">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates />
  </fo:inline>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/indexterm ')]" priority="3"/>
<xsl:template name="nulled-indexterm">
  <fo:inline margin="1pt" background-color="#ffddff"><!-- border="1pt black solid;"-->
    <!-- setclass -->
    [ <xsl:apply-templates/> ]
  </fo:inline>
</xsl:template>

<!-- Add for "New <data> element (#9)" in DITA 1.1 -->
<xsl:template match="*[contains(@class,' topic/data ')]"/>

<!-- Add for "Support foreign content vocabularies such as 
     MathML and SVG with <unknown> (#35) " in DITA 1.1 -->
<xsl:template match="*[contains(@class,' topic/foreign ') or contains(@class,' topic/unknown ')]"/>

<!-- =================== end of element rules ====================== -->



</xsl:stylesheet>
