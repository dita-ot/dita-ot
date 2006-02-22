<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- XHTML output with XML syntax -->
<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- == TASK UNIQUE SUBSTRUCTURES == -->

<xsl:template match="*[contains(@class,' task/taskbody ')]" name="topic.task.taskbody">
<div>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>
  <!-- here, you can generate a toc based on what's a child of body -->
  <!--xsl:call-template name="gen-sect-ptoc"/--><!-- Works; not always wanted, though; could add a param to enable it.-->

  <!-- get the short descr para -->
  <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]" mode="outofline"/>

<!-- Insert pre-req links here, after shortdesc - unless there is a prereq section about -->
  <xsl:if test="not(*[contains(@class,' task/prereq ')])">
   <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/related-links ')]" mode="prereqs"/>
  </xsl:if>

  <xsl:apply-templates/>
  <xsl:call-template name="end-revflag"/>
</div><xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' task/prereq ')]" name="topic.task.prereq">
<div class="p">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="gen-toc-id"/>
  <xsl:call-template name="setidaname"/>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
     <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <div class="{@rev}"><xsl:apply-templates select="."   mode="prereq-fmt" /></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="."   mode="prereq-fmt" />
   </xsl:otherwise>
 </xsl:choose>
</div><xsl:value-of select="$newline"/>
</xsl:template>
<xsl:template match="*[contains(@class,' task/prereq ')]" mode="prereq-fmt">
  <xsl:call-template name="start-revflag"/>
  <xsl:call-template name="sect-heading">
     <xsl:with-param name="deftitle"></xsl:with-param>
  </xsl:call-template>
  <!-- Title is not allowed now, but if we add it, make sure it is processed as in section -->
  <xsl:apply-templates select="*[not(contains(@class,' topic/title '))] | text() | comment() | processing-instruction()"/>

<!-- Insert pre-req links - after prereq section -->
  <xsl:apply-templates select="../following-sibling::*[contains(@class,' topic/related-links ')]" mode="prereqs"/>

  <xsl:call-template name="end-revflag"/>
  <xsl:if test="$link-top-section='yes'"> <!-- optional return to top - not used -->
    <p align="left"><a href="#TOP">
      <!--xsl:value-of select="$deftxt-linktop"/-->
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Return to Top'"/>
      </xsl:call-template>
    </a></p>
  </xsl:if>
</xsl:template>


<xsl:template match="*[contains(@class,' task/steps ')]" name="topic.task.steps">
 <!-- If there's one of these elements somewhere in a step, expand the whole step list -->
 <xsl:variable name="step_expand"> <!-- set & save step_expand=yes/no for expanding/compacting list items -->
  <xsl:choose>
   <!-- expand the list when one of the steps has any of these: "*/*" = step context -->
   <xsl:when test="*/*[contains(@class,' task/info ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/stepxmp ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/tutorialinfo ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/stepresult ')]">yes</xsl:when>
   <xsl:otherwise>no</xsl:otherwise>
  </xsl:choose>
 </xsl:variable>
 <xsl:call-template name="flagit"/>
 <xsl:call-template name="start-revflag"/>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
     <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <div class="{@rev}"><xsl:apply-templates select="."   mode="steps-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="."   mode="steps-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
   </xsl:otherwise>
 </xsl:choose>
 <xsl:call-template name="end-revflag"/>
</xsl:template>
<xsl:template match="*[contains(@class,' task/steps ')]" mode="steps-fmt">
 <xsl:param name="step_expand"/> 
 <xsl:choose> 
  <xsl:when test="*[contains(@class,' task/step ')][2]">
   <xsl:call-template name="setaname"/>
   <ol>
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setid"/>
    <xsl:apply-templates mode="steps">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
   </ol><xsl:value-of select="$newline"/>
  </xsl:when>
  <xsl:otherwise> <!-- One step -->
    <xsl:apply-templates mode="onestep">
     <xsl:with-param name="step_expand" select="$step_expand"/> <!-- pass the value to subsequent templates -->
    </xsl:apply-templates>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' task/steps-unordered ')]" name="topic.task.steps-unordered">
 <!-- If there's a block element somewhere in the step list, expand the whole list -->
 <xsl:variable name="step_expand"> <!-- set & save step_expand=yes/no for expanding/compacting list items -->
  <xsl:choose>
   <xsl:when test="*/*[contains(@class,' task/info ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/stepxmp ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/tutorialinfo ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/stepresult ')]">yes</xsl:when>
   <xsl:otherwise>no</xsl:otherwise>
  </xsl:choose>
 </xsl:variable>
 <xsl:call-template name="flagit"/>
 <xsl:call-template name="start-revflag"/>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
     <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <div class="{@rev}"><xsl:apply-templates select="."  mode="stepsunord-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="."  mode="stepsunord-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
   </xsl:otherwise>
 </xsl:choose>
 <xsl:call-template name="end-revflag"/>
</xsl:template>
<xsl:template match="*[contains(@class,' task/steps-unordered ')]" mode="stepsunord-fmt">
 <xsl:param name="step_expand"/> 
 <xsl:choose> 
  <xsl:when test="*[contains(@class,' task/step ')][2]">
   <xsl:call-template name="setaname"/>
   <ul>
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setid"/>
    <xsl:apply-templates mode="steps">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
   </ul><xsl:value-of select="$newline"/>
  </xsl:when>
  <xsl:otherwise> <!-- One step -->
    <xsl:apply-templates mode="onestep"/>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- only 1 step - output as a para -->
<xsl:template match="*[contains(@class,' task/step ')]" mode="onestep">
<xsl:param name="step_expand"/>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')">
     <xsl:call-template name="find-active-rev-flag">              
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <div class="{@rev}"><xsl:apply-templates select="."  mode="onestep-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="."  mode="onestep-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>
<xsl:template match="*[contains(@class,' task/step ')]" mode="onestep-fmt">
<xsl:param name="step_expand"/>
<div class="p">
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:if test="@importance='optional'">
    <strong>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Optional'"/>
    </xsl:call-template>
    <xsl:call-template name="getString">
     <xsl:with-param name="stringName" select="'ColonSymbol'"/>
    </xsl:call-template><xsl:text> </xsl:text>
    </strong>
  </xsl:if>
  <xsl:if test="@importance='required'">
    <strong>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Required'"/>
    </xsl:call-template>
    <xsl:call-template name="getString">
     <xsl:with-param name="stringName" select="'ColonSymbol'"/>
    </xsl:call-template><xsl:text> </xsl:text>
    </strong>
  </xsl:if>
 <xsl:apply-templates/>
</div><xsl:value-of select="$newline"/>
<xsl:call-template name="end-revflag"/>
</xsl:template>

<!-- multiple steps - output as list items -->
<xsl:template match="*[contains(@class,' task/step ')]" mode="steps">
<xsl:param name="step_expand"/>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')">
     <xsl:call-template name="find-active-rev-flag">              
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <span class="{@rev}"><xsl:apply-templates select="."  mode="steps-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates></span>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="."  mode="steps-fmt">
     <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>
<xsl:template match="*[contains(@class,' task/step ')]" mode="steps-fmt">
<xsl:param name="step_expand"/>
<li>
  <xsl:if test="$step_expand='yes'">
   <xsl:attribute name="class">stepexpand</xsl:attribute>
  </xsl:if>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>
  <xsl:if test="@importance='optional'">
    <strong>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Optional'"/>
    </xsl:call-template>
    <xsl:call-template name="getString">
     <xsl:with-param name="stringName" select="'ColonSymbol'"/>
    </xsl:call-template><xsl:text> </xsl:text>
    </strong>
  </xsl:if>
  <xsl:if test="@importance='required'">
    <strong>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Required'"/>
    </xsl:call-template>
    <xsl:call-template name="getString">
     <xsl:with-param name="stringName" select="'ColonSymbol'"/>
    </xsl:call-template><xsl:text> </xsl:text>
    </strong>
  </xsl:if>
 <xsl:apply-templates>
  <xsl:with-param name="step_expand" select="$step_expand"/>
 </xsl:apply-templates>
 <xsl:call-template name="end-revflag"/>
</li><xsl:value-of select="$newline"/>
</xsl:template>

<!-- nested steps - 1 level of nesting only -->
<xsl:template match="*[contains(@class,' task/substeps ')]" name="topic.task.substeps">
 <!-- If there's a block element somewhere in the step list, expand the whole list -->
 <xsl:variable name="sub_step_expand"> <!-- set & save sub_step_expand=yes/no for expanding/compacting list items -->
  <xsl:choose>
   <!-- expand the list when one of the substeps has any of these -->
   <xsl:when test="*/*[contains(@class,' task/info ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/stepxmp ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/tutorialinfo ')]">yes</xsl:when>
   <xsl:when test="*/*[contains(@class,' task/stepresult ')]">yes</xsl:when>
   <xsl:otherwise>no</xsl:otherwise>
  </xsl:choose>
 </xsl:variable>
<xsl:call-template name="flagit"/>
<xsl:call-template name="start-revflag"/>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> 
     <xsl:call-template name="find-active-rev-flag">              
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <div class="{@rev}"><xsl:apply-templates select="." mode="substeps-fmt">
     <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
    </xsl:apply-templates></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="." mode="substeps-fmt">
     <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
    </xsl:apply-templates>
   </xsl:otherwise>
 </xsl:choose>
<xsl:call-template name="end-revflag"/>
</xsl:template>
<xsl:template match="*[contains(@class,' task/substeps ')]" mode="substeps-fmt">
<xsl:param name="sub_step_expand"/>
<xsl:call-template name="setaname"/>
<ol>
 <xsl:if test="parent::*/parent::*[contains(@class,' task/steps ')]"> <!-- Is the grandparent an ordered step? -->
  <xsl:attribute name="type">a</xsl:attribute>            <!-- yup, letter these steps -->
 </xsl:if>                                                <!-- otherwise, default to numbered -->
 <xsl:call-template name="commonattributes"/>
 <xsl:call-template name="setid"/>
 <xsl:apply-templates>
  <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
 </xsl:apply-templates>
</ol><xsl:value-of select="$newline"/>
</xsl:template>

<!-- nested step -->
<xsl:template match="*[contains(@class,' task/substep ')]" name="topic.task.substep">
<xsl:param name="sub_step_expand"/>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
     <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <span class="{@rev}"><xsl:apply-templates select="."  mode="substep-fmt">
     <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
    </xsl:apply-templates></span>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="."  mode="substep-fmt">
     <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
    </xsl:apply-templates>
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>
<xsl:template match="*[contains(@class,' task/substep ')]" mode="substep-fmt">
<xsl:param name="sub_step_expand"/>
<li>
  <xsl:if test="$sub_step_expand='yes'">
   <xsl:attribute name="class">substepexpand</xsl:attribute>
  </xsl:if>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>
  <xsl:if test="@importance='optional'">
    <strong>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Optional'"/>
    </xsl:call-template>
    <xsl:call-template name="getString">
     <xsl:with-param name="stringName" select="'ColonSymbol'"/>
    </xsl:call-template><xsl:text> </xsl:text>
    </strong>
  </xsl:if>
  <xsl:if test="@importance='required'">
    <strong>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Required'"/>
    </xsl:call-template>
    <xsl:call-template name="getString">
     <xsl:with-param name="stringName" select="'ColonSymbol'"/>
    </xsl:call-template><xsl:text> </xsl:text>
    </strong>
  </xsl:if>
 <xsl:apply-templates>
  <xsl:with-param name="sub_step_expand"/>
 </xsl:apply-templates>
 <xsl:call-template name="end-revflag"/>
</li><xsl:value-of select="$newline"/>
</xsl:template>

<!-- choices contain choice items -->
<xsl:template match="*[contains(@class,' task/choices ')]" name="topic.task.choices">
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> 
     <xsl:call-template name="find-active-rev-flag">               
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
    <div class="{@rev}"><xsl:apply-templates select="."  mode="choices-fmt" /></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
    <xsl:apply-templates select="."  mode="choices-fmt" />
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>
<xsl:template match="*[contains(@class,' task/choices ')]" mode="choices-fmt">
 <xsl:call-template name="flagit"/>
 <xsl:call-template name="start-revflag"/>
 <xsl:call-template name="setaname"/>
  <ul>
   <xsl:call-template name="commonattributes"/>
   <xsl:call-template name="setid"/>
   <xsl:apply-templates/>
  </ul><xsl:value-of select="$newline"/>
 <xsl:call-template name="end-revflag"/>
</xsl:template>

<!-- task/choice - fall-thru -->

<!-- choice table is like a simpletable - 2 columns, set heading -->
<xsl:template match="*[contains(@class,' task/choicetable ')]" name="topic.task.choicetable">
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> 
     <xsl:call-template name="find-active-rev-flag">               
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
     <div class="{@rev}"><xsl:apply-templates select="."  mode="choicetable-fmt" /></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
     <xsl:apply-templates select="."  mode="choicetable-fmt" />
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>
<xsl:template match="*[contains(@class,' task/choicetable ')]" mode="choicetable-fmt">
 <!-- Find the total number of relative units for the table. If @relcolwidth="1* 2* 2*",
      the variable is set to 5. -->
 <xsl:variable name="totalwidth">
   <xsl:if test="@relcolwidth">
     <xsl:call-template name="find-total-table-width"/>
   </xsl:if>
 </xsl:variable>
 <!-- Find how much of the table each relative unit represents. If @relcolwidth is 1* 2* 2*,
      there are 5 units. So, each unit takes up 100/5, or 20% of the table. Default to 0,
      which the entries will ignore. -->
 <xsl:variable name="width-multiplier">
   <xsl:choose>
     <xsl:when test="@relcolwidth">
       <xsl:value-of select="100 div $totalwidth"/>
     </xsl:when>
     <xsl:otherwise>0</xsl:otherwise>
   </xsl:choose>
 </xsl:variable>
 <xsl:call-template name="flagit"/>
 <xsl:call-template name="start-revflag"/>
 <xsl:call-template name="setaname"/>
 <xsl:value-of select="$newline"/>
 <table border="1" frame="hsides" rules="rows" cellpadding="4" cellspacing="0" summary="" class="choicetableborder">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setid"/><xsl:value-of select="$newline"/>
  <!--If the choicetable has no header - output a default one-->
  <xsl:choose>
  <xsl:when test="not(./*[contains(@class,' task/chhead ')])">
   <thead><tr><th id="{generate-id(.)}-option" valign="bottom">
    <xsl:call-template name="th-align"/>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Option'"/>
    </xsl:call-template>
    </th><xsl:value-of select="$newline"/>
    <th id="{generate-id(.)}-desc" valign="bottom">
    <xsl:call-template name="th-align"/>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Description'"/>
    </xsl:call-template>
    </th></tr></thead><xsl:value-of select="$newline"/>
  </xsl:when>
  <xsl:otherwise>
   <thead><tr><th valign="bottom">     
     <xsl:attribute name="id">
     <xsl:call-template name="th-align"/>
     <xsl:choose>
      <!-- if the option header has an ID, use that -->
      <xsl:when test="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/choptionhd ')]/@id">
       <xsl:value-of select="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/choptionhd ')]/@id"/><xsl:text>-option</xsl:text>
      </xsl:when>
      <xsl:otherwise>  <!-- output a default option header ID -->
       <xsl:value-of select="generate-id(.)"/><xsl:text>-option</xsl:text>
      </xsl:otherwise>
     </xsl:choose>
     </xsl:attribute>
     <xsl:apply-templates select="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/choptionhd ')]" mode="chtabhdr"/>
    </th><xsl:value-of select="$newline"/>
    <th valign="bottom">     
     <xsl:attribute name="id">
     <xsl:call-template name="th-align"/>
     <xsl:choose>
      <!-- if the description header has an ID, use that -->
      <xsl:when test="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/chdeschd ')]/@id">
       <xsl:value-of select="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/chdeschd ')]/@id"/><xsl:text>-desc</xsl:text>
      </xsl:when>
      <xsl:otherwise>  <!-- output a default descr header ID -->
       <xsl:value-of select="generate-id(.)"/><xsl:text>-desc</xsl:text>
      </xsl:otherwise>
     </xsl:choose>
     </xsl:attribute>
     <xsl:apply-templates select="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/chdeschd ')]" mode="chtabhdr"/>
    </th></tr></thead><xsl:value-of select="$newline"/>
  </xsl:otherwise>
  </xsl:choose>
  <tbody>
    <xsl:apply-templates>     <!-- width-multiplier will be used in the first row to set widths. -->
      <xsl:with-param name="width-multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
    </xsl:apply-templates>
  </tbody>
 </table><xsl:value-of select="$newline"/>
 <xsl:call-template name="end-revflag"/>
</xsl:template>

<!-- headers are called above, hide the fall thru -->
<xsl:template match="*[contains(@class,' task/chhead ')]" />
<xsl:template match="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/choptionhd ')]" />
<xsl:template match="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/chdeschd ')]" />

<!-- Option & Description headers -->
<xsl:template match="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/choptionhd ')]" mode="chtabhdr">
 <xsl:apply-templates/>
</xsl:template>
<xsl:template match="*[contains(@class,' task/chhead ')]/*[contains(@class,' task/chdeschd ')]" mode="chtabhdr">
 <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' task/chrow ')]" name="topic.task.chrow">
 <tr><xsl:call-template name="commonattributes"/><xsl:apply-templates/></tr>
 <xsl:value-of select="$newline"/>
</xsl:template>

<!-- specialization of stentry - choption -->
<!-- for specentry - if no text in cell, output specentry attr; otherwise output text -->
<!-- Bold the @keycol column. Get the column's number. When (Nth stentry = the @keycol value) then bold the stentry -->
<xsl:template match="*[contains(@class,' task/choption ')]" name="topic.task.choption">
 <xsl:param name="width-multiplier">0</xsl:param>
  <td valign="top">
   <!-- Add header attr for column header -->
   <xsl:attribute name="headers">
    <xsl:choose>
      <!-- First choice: if there is a user-specified header, and it has an ID -->
      <xsl:when test="ancestor::*[contains(@class,' task/choicetable ')]/*[contains(@class,' task/chhead ')]/*[contains(@class,' task/choptionhd ')]/@id">
        <xsl:value-of select="ancestor::*[contains(@class,' task/choicetable ')]/*[1][contains(@class,' task/chhead ')]/*[contains(@class,' task/choptionhd ')]/@id"/><xsl:text>-option</xsl:text>
      </xsl:when>
      <!-- Second choice: no user-specified header for this column. ID is based on the table's generated ID. -->
      <xsl:otherwise>
        <xsl:value-of select="generate-id(ancestor::*[contains(@class,' task/choicetable ')])"/><xsl:text>-option</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
   </xsl:attribute>
   <!-- Add header attr, column header then row header -->
   <xsl:attribute name="id">
    <!-- If there is a user-specified ID, use it -->
    <xsl:choose>
      <xsl:when test="@id">
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise> <!-- generate one -->
        <xsl:value-of select="generate-id(.)"/>
      </xsl:otherwise>
    </xsl:choose>
   </xsl:attribute>
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
    <xsl:variable name="thiscolnum"><xsl:value-of select="number(count(preceding-sibling::*)+1)"/></xsl:variable>
    <!-- If width-multiplier=0, then either @relcolwidth was not specified, or this is not the first
         row, so do not create a width value. Otherwise, find out the relative width of this column. -->
    <xsl:variable name="widthpercent">
      <xsl:if test="$width-multiplier != 0">
        <xsl:call-template name="get-current-entry-percentage">
          <xsl:with-param name="multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
          <xsl:with-param name="entry-num"><xsl:value-of select="$thiscolnum"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <!-- If we calculated a width, create the width attribute. -->
    <xsl:if test="string-length($widthpercent)>0">
      <xsl:attribute name="width">
        <xsl:value-of select="$widthpercent"/><xsl:text>%</xsl:text>
      </xsl:attribute>
    </xsl:if>
    <xsl:call-template name="flagit"/>
    <xsl:call-template name="start-revflag-parent"/>
    <xsl:call-template name="start-revflag"/>
    <xsl:variable name="revtest">
      <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
        <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
          <xsl:with-param name="allrevs" select="@rev"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="revtest-row">
      <xsl:if test="../@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
        <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
          <xsl:with-param name="allrevs" select="../@rev"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <!-- Does the column match? Is REV on for entry or row? -->
    <xsl:choose>
     <xsl:when test="$thiscolnum=$localkeycol and $revtest-row=1">
      <strong><span class="{../@rev}">
<xsl:call-template name="stentry-templates"/>
      </span></strong>
     </xsl:when>
     <xsl:when test="$thiscolnum=$localkeycol and $revtest=1">
      <strong><span class="{@rev}">
<xsl:call-template name="stentry-templates"/>
      </span></strong>
     </xsl:when>
     <xsl:when test="$thiscolnum=$localkeycol">
      <strong>
<xsl:call-template name="stentry-templates"/>
      </strong>
     </xsl:when>
     <xsl:when test="$revtest-row=1">
      <span class="{../@rev}">
<xsl:call-template name="stentry-templates"/>
      </span>
     </xsl:when>
     <xsl:when test="$revtest=1">
      <span class="{@rev}">
<xsl:call-template name="stentry-templates"/>
      </span>
     </xsl:when>
     <xsl:otherwise>
<xsl:call-template name="stentry-templates"/>
     </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="end-revflag"/>
    <xsl:call-template name="end-revflag-parent"/>
  </td><xsl:value-of select="$newline"/>
</xsl:template>

<!-- specialization of stentry - chdesc -->
<!-- for specentry - if no text in cell, output specentry attr; otherwise output text -->
<!-- Bold the @keycol column. Get the column's number. When (Nth stentry = the @keycol value) then bold the stentry -->
<xsl:template match="*[contains(@class,' task/chdesc ')]" name="topic.task.chdesc">
 <xsl:param name="width-multiplier">0</xsl:param>
  <td valign="top">
   <!-- Add header attr, column header then option header -->
   <xsl:attribute name="headers">
    <xsl:choose>
      <!-- First choice: if there is a user-specified header, and it has an ID-->
      <xsl:when test="ancestor::*[contains(@class,' task/choicetable ')]/*[contains(@class,' task/chhead ')]/*[contains(@class,' task/chdeschd ')]/@id">
       <!-- If there is a user-specified row ID -->
        <xsl:value-of select="ancestor::*[contains(@class,' task/choicetable ')]/*[contains(@class,' task/chhead ')]/*[contains(@class,' task/chdeschd ')]/@id"/><xsl:text>-desc </xsl:text>
        <!-- add CHOption ID -->
        <xsl:choose>
         <xsl:when test="../*[contains(@class,' task/choption ')]/@id">
          <xsl:value-of select="../*[contains(@class,' task/choption ')]/@id"/>
         </xsl:when>
         <xsl:otherwise>
          <xsl:value-of select="generate-id(../*[contains(@class,' task/choption ')])"/>
         </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!-- Second choice: no user-specified header for this column. ID is based on the table's generated ID. -->
      <xsl:otherwise>
        <xsl:value-of select="generate-id(ancestor::*[contains(@class,' task/choicetable ')])"/><xsl:text>-desc </xsl:text>
        <!-- add CHOption ID -->
        <xsl:choose>
         <xsl:when test="../*[contains(@class,' task/choption ')]/@id">
          <xsl:value-of select="../*[contains(@class,' task/choption ')]/@id"/>
         </xsl:when>
         <xsl:otherwise>
          <xsl:value-of select="generate-id(../*[contains(@class,' task/choption ')])"/>
         </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
   </xsl:attribute>
   <!-- If there is a user-specified ID, add it -->
   <xsl:if test="@id">
    <xsl:attribute name="id">
     <xsl:value-of select="@id"/>
    </xsl:attribute>
   </xsl:if>
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
    <xsl:variable name="thiscolnum"><xsl:value-of select="number(count(preceding-sibling::*)+1)"/></xsl:variable>
    <!-- If width-multiplier=0, then either @relcolwidth was not specified, or this is not the first
         row, so do not create a width value. Otherwise, find out the relative width of this column. -->
    <xsl:variable name="widthpercent">
      <xsl:if test="$width-multiplier != 0">
        <xsl:call-template name="get-current-entry-percentage">
          <xsl:with-param name="multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
          <xsl:with-param name="entry-num"><xsl:value-of select="$thiscolnum"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <!-- If we calculated a width, create the width attribute. -->
    <xsl:if test="string-length($widthpercent)>0">
      <xsl:attribute name="width">
        <xsl:value-of select="$widthpercent"/><xsl:text>%</xsl:text>
      </xsl:attribute>
    </xsl:if>
    <xsl:call-template name="flagit"/>
    <xsl:call-template name="start-revflag-parent"/>
    <xsl:call-template name="start-revflag"/>
    <xsl:variable name="revtest">
      <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
        <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
          <xsl:with-param name="allrevs" select="@rev"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="revtest-row">
      <xsl:if test="../@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
        <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
          <xsl:with-param name="allrevs" select="../@rev"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <!-- Does the column match? Is REV on for entry or row? -->
    <xsl:choose>
     <xsl:when test="$thiscolnum=$localkeycol and $revtest-row=1">
      <strong><span class="{../@rev}">
<xsl:call-template name="stentry-templates"/>
      </span></strong>
     </xsl:when>
     <xsl:when test="$thiscolnum=$localkeycol and $revtest=1">
      <strong><span class="{@rev}">
<xsl:call-template name="stentry-templates"/>
      </span></strong>
     </xsl:when>
     <xsl:when test="$thiscolnum=$localkeycol">
      <strong>
<xsl:call-template name="stentry-templates"/>
      </strong>
     </xsl:when>
     <xsl:when test="$revtest-row=1">
      <span class="{../@rev}">
<xsl:call-template name="stentry-templates"/>
      </span>
     </xsl:when>
     <xsl:when test="$revtest=1">
      <span class="{@rev}">
<xsl:call-template name="stentry-templates"/>
      </span>
     </xsl:when>
     <xsl:otherwise>
<xsl:call-template name="stentry-templates"/>
     </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="end-revflag"/>
    <xsl:call-template name="end-revflag-parent"/>
  </td><xsl:value-of select="$newline"/>
</xsl:template>


<!-- these para-like items need a leading space -->
<xsl:template match="*[contains(@class,' task/stepxmp ')]" name="topic.task.stepxmp">
  <xsl:text> </xsl:text><xsl:call-template name="flagcheck"/><xsl:call-template name="revblock"/>
</xsl:template>

<!-- these para-like items need a leading space -->
<xsl:template match="*[contains(@class,' task/stepresult ')]" name="topic.task.stepresult">
  <xsl:text> </xsl:text><xsl:call-template name="flagcheck"/><xsl:call-template name="revblock"/>
</xsl:template>

<xsl:template match="*[contains(@class,' task/info ')]" name="topic.task.info">
  <xsl:text> </xsl:text><xsl:call-template name="flagcheck"/><xsl:call-template name="revblock"/>
</xsl:template>

<xsl:template match="*[contains(@class,' task/tutorialinfo ')]" name="topic.task.tutorialinfo">
  <xsl:text> </xsl:text><xsl:call-template name="flagcheck"/><xsl:call-template name="revblock"/>
</xsl:template>

</xsl:stylesheet>
