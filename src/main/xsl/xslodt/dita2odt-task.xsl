<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->
<!-- 20090904 RDA: Add support for stepsection; combine duplicated logic
                   for main steps and steps-unordered templates. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
  xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
  xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
  xmlns:prodtools="http://www.ibm.com/xmlns/prodtools"
  xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
  xmlns:dita2html="http://dita-ot.sourceforge.net/ns/200801/dita2html"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg" version="1.0"
  exclude-result-prefixes="related-links dita2html ditamsg">

  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>

  <!-- Determines whether to generate titles for task sections. Values are YES and NO. -->
  <xsl:param name="GENERATE-TASK-LABELS" select="'NO'"/>

  <!-- == TASK UNIQUE SUBSTRUCTURES == -->

  <xsl:template match="*[contains(@class,' task/taskbody ')]" name="topic.task.taskbody">
    <!-- Added for DITA 1.1 "Shortdesc proposal" -->
    <!-- get the abstract para -->
    <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/abstract ')]"
      mode="outofline"/>

    <!-- get the short descr para -->
    <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]"
      mode="outofline"/>

    <!-- Insert pre-req links here, after shortdesc - unless there is a prereq section about -->
    <xsl:if test="not(*[contains(@class,' task/prereq ')])">
      <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/related-links ')]"
        mode="prereqs"/>
    </xsl:if>
    <xsl:apply-templates/>

  </xsl:template>

  <xsl:template match="*[contains(@class,' task/prereq ')]" mode="get-output-class">p</xsl:template>

  <xsl:template match="*[contains(@class,' task/prereq ')]" name="topic.task.prereq">
    
    <xsl:apply-templates select="." mode="prereq-fmt"/>

  </xsl:template>

  <xsl:template match="*[contains(@class,' task/prereq ')]" mode="prereq-fmt">
    
    <!-- Title is not allowed now, but if we add it, make sure it is processed as in section -->
    <xsl:element name="text:p">
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        
        <xsl:apply-templates
          select="*[not(contains(@class,' topic/title '))] | text() | comment() | processing-instruction()"
        />
        
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:element>
    <!-- Insert pre-req links - after prereq section -->
    <xsl:apply-templates select="../following-sibling::*[contains(@class,' topic/related-links ')]"
    mode="prereqs"/>
    
  </xsl:template>

  <xsl:template match="*" mode="make-steps-compact">
    <xsl:choose>
      <!-- expand the list when one of the steps has any of these: "*/*" = step context -->
      <xsl:when test="*/*[contains(@class,' task/info ')]">yes</xsl:when>
      <xsl:when test="*/*[contains(@class,' task/stepxmp ')]">yes</xsl:when>
      <xsl:when test="*/*[contains(@class,' task/tutorialinfo ')]">yes</xsl:when>
      <xsl:when test="*/*[contains(@class,' task/stepresult ')]">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/steps ')]" name="topic.task.steps">
    <!-- If there's one of these elements somewhere in a step, expand the whole step list -->
    <xsl:variable name="step_expand">
      <!-- set & save step_expand=yes/no for expanding/compacting list items -->
      <xsl:apply-templates select="." mode="make-steps-compact"/>
    </xsl:variable>
    
    <!-- start flagging -->
    <xsl:apply-templates select="." mode="start-add-odt-flags">
      <xsl:with-param name="family" select="'_list'"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="." mode="steps-fmt">
      <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
    <!-- end flagging -->
    <xsl:apply-templates select="." mode="end-add-odt-flags">
      <xsl:with-param name="family" select="'_list'"/>
    </xsl:apply-templates>
    
  </xsl:template>

  <xsl:template
    match="*[contains(@class,' task/steps ') or contains(@class,' task/steps-unordered ')]"
    mode="common-processing-within-steps">
    <xsl:param name="step_expand"/>
    <xsl:param name="list-type">
      <xsl:choose>
        <!-- ordered list -->
        <xsl:when test="contains(@class,' task/steps ')">ordered_list_style</xsl:when>
        <!-- unordered list -->
        <xsl:otherwise>list_style</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'task_procedure'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
    <xsl:choose>
      <xsl:when
        test="*[contains(@class,' task/step ')] and not(*[contains(@class,' task/step ')][2])">
        <!-- Single step. Process any stepsection before the step (cannot appear after). -->
        <xsl:apply-templates select="*[contains(@class,' task/stepsection ')]"/>
        <xsl:apply-templates select="*[contains(@class,' task/step ')]" mode="onestep">
          <xsl:with-param name="step_expand" select="$step_expand"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="not(*[contains(@class,' task/stepsection ')])">
        <xsl:apply-templates select="." mode="step-elements-with-no-stepsection">
          <xsl:with-param name="step_expand" select="$step_expand"/>
          <xsl:with-param name="list-type" select="$list-type"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when
        test="*[1][contains(@class,' task/stepsection ')] and not(*[contains(@class,' task/stepsection ')][2])">
        <!-- Stepsection is first, no other appearances -->
        <xsl:apply-templates select="*[contains(@class,' task/stepsection ')]"/>
        <xsl:apply-templates select="." mode="step-elements-with-no-stepsection">
          <xsl:with-param name="step_expand" select="$step_expand"/>
          <xsl:with-param name="list-type" select="$list-type"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <!-- Stepsection elements mixed in with steps -->
        <xsl:apply-templates select="." mode="step-elements-with-stepsection">
          <xsl:with-param name="step_expand" select="$step_expand"/>
          <xsl:with-param name="list-type" select="$list-type"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- step-elements-with-no-stepsection -->
  <xsl:template match="*" mode="step-elements-with-no-stepsection">
    <xsl:param name="step_expand"/>
    <xsl:param name="list-type"/>
    <!-- 
    <xsl:call-template name="setaname"/>
    -->
    <xsl:element name="text:list">
      <xsl:attribute name="text:style-name">
        <xsl:value-of select="$list-type"/>
      </xsl:attribute>
      <!-- 
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="gen-style"/>
      <xsl:call-template name="setid"/>
      -->
      <xsl:apply-templates select="*[contains(@class,' task/step ')]" mode="steps">
        <xsl:with-param name="step_expand" select="$step_expand"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>
  
  <!-- step-elements-with-stepsection -->
  <xsl:template match="*" mode="step-elements-with-stepsection">
    <xsl:param name="step_expand"/>
    <xsl:param name="list-type"/>
    <xsl:for-each select="*">
      <xsl:choose>
        <xsl:when test="contains(@class,' task/stepsection ')">
          <xsl:apply-templates select="."/>
        </xsl:when>
        <xsl:when
          test="contains(@class,' task/step ') and preceding-sibling::*[1][contains(@class,' task/step ')]">
          <!-- Do nothing, was pulled in through recursion -->
        </xsl:when>
        <xsl:otherwise>
          <!-- First step in a series of steps -->
          <xsl:element name="text:list">
            <xsl:attribute name="text:style-name">
              <xsl:value-of select="$list-type"/>
            </xsl:attribute>
            <xsl:variable name="start-value">
            <xsl:if test="$list-type='ordered_list_style' and preceding-sibling::*[contains(@class,' task/step ')]">
              <!-- Restart numbering for ordered steps that were interrupted by stepsection.
                 The start attribute is valid in XHTML 1.0 Transitional, but not for XHTML 1.0 Strict.
                 It is possible (preferable) to keep stepsection within an <li> and use CSS to
                 fix numbering, but with testing in March of 2009, this does not work in IE. 
                 It is possible in Firefox 3. -->
                <xsl:value-of select="count(preceding-sibling::*[contains(@class,' task/step ')])+1"/>
            </xsl:if>
            </xsl:variable>
            <xsl:apply-templates select="." mode="steps">
              <xsl:with-param name="step_expand" select="$step_expand"/>
              <xsl:with-param name="start-value" select="$start-value"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="following-sibling::*[1][contains(@class,' task/step ')]"
              mode="sequence-of-steps">
              <xsl:with-param name="step_expand" select="$step_expand"/>
            </xsl:apply-templates>
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="*" mode="sequence-of-steps">
    <xsl:param name="step_expand"/>
    <xsl:apply-templates select="." mode="steps">
      <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="following-sibling::*[1][contains(@class,' task/step ')]"
      mode="sequence-of-steps">
      <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/stepsection ')]">
    
    <xsl:element name="text:span">
      <!-- start add flagging styles -->
      <xsl:apply-templates select="." mode="start-add-odt-flags"/>
      <xsl:apply-templates/>
      <!-- end add flagging styles -->
      <xsl:apply-templates select="." mode="end-add-odt-flags"/>
    </xsl:element>
      
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/steps-unordered ')]"
    name="topic.task.steps-unordered">
    <!-- If there's a block element somewhere in the step list, expand the whole list -->
    <xsl:variable name="step_expand">
      <!-- set & save step_expand=yes/no for expanding/compacting list items -->
      <xsl:apply-templates select="." mode="make-steps-compact"/>
    </xsl:variable>
    
    <!-- start flagging -->
    <xsl:apply-templates select="." mode="start-add-odt-flags">
      <xsl:with-param name="family" select="'_list'"/>
    </xsl:apply-templates>
    <!-- render list -->
    <xsl:apply-templates select="." mode="stepsunord-fmt">
      <xsl:with-param name="step_expand" select="$step_expand"/>
    </xsl:apply-templates>
    <!-- end flagging -->
    <xsl:apply-templates select="." mode="end-add-odt-flags">
      <xsl:with-param name="family" select="'_list'"/>
    </xsl:apply-templates>
     
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/steps ')]" mode="steps-fmt">
    <xsl:param name="step_expand"/>
    <xsl:apply-templates select="." mode="common-processing-within-steps">
      <xsl:with-param name="step_expand" select="$step_expand"/>
      <xsl:with-param name="list-type" select="'ordered_list_style'"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/steps-unordered ')]" mode="stepsunord-fmt">
    <xsl:param name="step_expand"/>
    <xsl:apply-templates select="." mode="common-processing-within-steps">
      <xsl:with-param name="step_expand" select="$step_expand"/>
      <xsl:with-param name="list-type" select="'list_style'"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- only 1 step - output as a para -->
  <xsl:template match="*[contains(@class,' task/step ')]" mode="onestep">
    <xsl:param name="step_expand"/>
      <xsl:apply-templates select="." mode="onestep-fmt">
        <xsl:with-param name="step_expand" select="$step_expand"/>
      </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/step ')]" mode="onestep-fmt">
    <xsl:param name="step_expand"/>
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <xsl:element name="text:list">
      <xsl:attribute name="text:style-name">list_style</xsl:attribute>
      <xsl:element name="text:list-item">
          <xsl:if test="@importance='optional'">
            <xsl:element name="text:p">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'Optional'"/>
                  </xsl:call-template>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                  </xsl:call-template>
                  <xsl:text> </xsl:text>
              </xsl:element>
            </xsl:element>
          </xsl:if>
          
          <xsl:if test="@importance='required'">
            <xsl:element name="text:p">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'Required'"/>
                </xsl:call-template>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
              </xsl:element>
            </xsl:element>
          </xsl:if>
        <xsl:apply-templates mode="create_list_item"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- multiple steps - output as list items -->
  <xsl:template match="*[contains(@class,' task/step ')]" mode="steps">
    <xsl:param name="step_expand"/>
    <xsl:param name="start-value">0</xsl:param>
    <xsl:variable name="revtest">
      <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')">
        <xsl:call-template name="find-active-rev-flag">
          <xsl:with-param name="allrevs" select="@rev"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="$revtest=1">
        <!-- Rev is active - add the DIV -->
          <xsl:apply-templates select="." mode="steps-fmt">
            <xsl:with-param name="step_expand" select="$step_expand"/>
          </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <!-- Rev wasn't active - process normally -->
        <xsl:apply-templates select="." mode="steps-fmt">
          <xsl:with-param name="step_expand" select="$step_expand"/>
          <xsl:with-param name="start-value" select="$start-value"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/step ')]" mode="steps-fmt">
    <xsl:param name="step_expand"/>
    <xsl:param name="start-value">0</xsl:param>
    
    <xsl:element name="text:list-item">
      <xsl:if test="$start-value &gt; 0">
        <xsl:attribute name="text:start-value">
          <xsl:value-of select="$start-value"/>
        </xsl:attribute>
      </xsl:if>
      
        <xsl:if test="@importance='optional'">
          <xsl:element name="text:p">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'Optional'"/>
                  </xsl:call-template>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                  </xsl:call-template>
                  <xsl:text> </xsl:text>
              </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="@importance='required'">
          <xsl:element name="text:p">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'Required'"/>
                </xsl:call-template>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
            </xsl:element>
          </xsl:element>
        </xsl:if>
      
        <xsl:apply-templates mode="create_list_item"/>
      </xsl:element>
  </xsl:template>

  <!-- nested steps - 1 level of nesting only -->
  <xsl:template match="*[contains(@class,' task/substeps ')]" name="topic.task.substeps">
    <!-- If there's a block element somewhere in the step list, expand the whole list -->
    <xsl:variable name="sub_step_expand">
      <!-- set & save sub_step_expand=yes/no for expanding/compacting list items -->
      <xsl:apply-templates select="." mode="make-steps-compact"/>
    </xsl:variable>
    
    <xsl:apply-templates select="." mode="substeps-fmt">
      <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
    </xsl:apply-templates>
    
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/substeps ')]" mode="substeps-fmt">
    <xsl:param name="sub_step_expand"/>
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <!-- 
    <xsl:call-template name="setaname"/>
    -->
    <xsl:element name="text:list">
      <xsl:attribute name="text:style-name">ordered_list_style</xsl:attribute>
      <xsl:if test="parent::*/parent::*[contains(@class,' task/steps ')]">
        <!-- Is the grandparent an ordered step? -->
        <!-- 
        <xsl:attribute name="type">a</xsl:attribute>
        -->
        <!-- yup, letter these steps -->
      </xsl:if>
      <!-- otherwise, default to numbered -->
      <xsl:apply-templates>
        <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <!-- nested step -->
  <xsl:template match="*[contains(@class,' task/substep ')]" name="topic.task.substep">
    <xsl:param name="sub_step_expand"/>
    <xsl:variable name="revtest">
      <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')">
        <!-- revision? -->
        <xsl:call-template name="find-active-rev-flag">
          <!-- active? (revtest will be 1 when active)-->
          <xsl:with-param name="allrevs" select="@rev"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$revtest=1">
        <!-- Rev is active - add the DIV -->
        <!-- 
        <span class="{@rev}">
        -->
          <xsl:apply-templates select="." mode="substep-fmt">
            <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
          </xsl:apply-templates>
        <!-- 
        </span>
        -->
      </xsl:when>
      <xsl:otherwise>
        <!-- Rev wasn't active - process normally -->
        <xsl:apply-templates select="." mode="substep-fmt">
          <xsl:with-param name="sub_step_expand" select="$sub_step_expand"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/substep ')]" mode="substep-fmt">
    <xsl:param name="sub_step_expand"/>
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>

    <xsl:element name="text:list-item">
      <xsl:if test="$sub_step_expand='yes'">
        <!-- 
        <xsl:attribute name="class">substepexpand</xsl:attribute>
        -->
      </xsl:if>
      <!-- 
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="gen-style">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      <xsl:call-template name="setidaname"/>
      <xsl:call-template name="start-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      <xsl:call-template name="start-revflag">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      -->
     
        <xsl:if test="@importance='optional'">
          <xsl:element name="text:p">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Optional'"/>
              </xsl:call-template>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'ColonSymbol'"/>
              </xsl:call-template>
              <xsl:text> </xsl:text> 
            </xsl:element>
          </xsl:element>
        </xsl:if>
        <xsl:if test="@importance='required'">
          <xsl:element name="text:p">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'Required'"/>
                </xsl:call-template>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
            </xsl:element>
          </xsl:element>
        </xsl:if>
        <xsl:apply-templates mode="create_list_item"/>
      </xsl:element>
      
      <!-- 
      <xsl:call-template name="end-revflag">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      <xsl:call-template name="end-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      -->
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
        <xsl:apply-templates select="."  mode="choices-fmt" />
      </xsl:when>
      <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
        <xsl:apply-templates select="."  mode="choices-fmt" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/choices ')]" mode="choices-fmt">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    
    <xsl:element name="text:list">
      <xsl:attribute name="text:style-name">list_style</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
    
  </xsl:template>

  <!-- task choice table -->
  <xsl:template match="*[contains(@class, ' task/choicetable ')]">

    <xsl:choose>
      <!-- if the table is under p(direct child) -->
      <xsl:when test="parent::*[contains(@class, ' topic/p ')]">
        <!-- break p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        
        <xsl:apply-templates/>
        <!-- start p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
      </xsl:when>
      <!-- nested by list -->
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        
        <!-- caculate list depth -->
        <xsl:variable name="depth">
          <xsl:call-template name="calculate_list_depth"/>
        </xsl:variable>
        <!-- caculate span tag depth -->
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/li '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break first p tag if there are span tags -->
        <xsl:if test="$span_depth &gt;= 0">
          <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        </xsl:if>
        <!-- break list tag -->
        <xsl:call-template name="create_items_for_list">
          <xsl:with-param name="depth" select="$depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- normal process -->
        <!-- start render table -->
        <xsl:variable name="tablenameId" select="generate-id(.)"/>
        <xsl:choose>
          <xsl:when test="not(./*[contains(@class,' task/chhead ')])">
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
              <xsl:call-template name="create_head_for_choicetable"/>
              <xsl:apply-templates/>
            </xsl:element>
            <!-- end flagging -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="family" select="'_table'"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="create_simpletable"/>
          </xsl:otherwise>
        </xsl:choose>
        
        <!-- start list tag again -->
        <xsl:call-template name="create_items_for_list">
          <xsl:with-param name="depth" select="$depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
        <!-- start p tag again if there are span tags -->
        <xsl:if test="$span_depth &gt;= 0">
          <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        </xsl:if>
        <!--  span tags span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- create choicetable header. -->
  <xsl:template name="create_head_for_choicetable">
    <xsl:element name="table:table-header-rows">
      <xsl:element name="table:table-row">
        <xsl:element name="table:table-cell">
          <xsl:attribute name="office:value-type">string</xsl:attribute>
          <!-- Option is always 1nd column in the 1st row -->
          <xsl:attribute name="table:style-name">cell_style_1_task</xsl:attribute>
          <xsl:element name="text:p">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Option'"/>
              </xsl:call-template>
            </xsl:element>
          </xsl:element>
        </xsl:element>
        <xsl:element name="table:table-cell">
          <xsl:attribute name="office:value-type">string</xsl:attribute>
          <!-- Description is always 1nd column in the 1st row -->
          <xsl:attribute name="table:style-name">cell_style_2_task</xsl:attribute>
          <xsl:element name="text:p">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Description'"/>
              </xsl:call-template>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/chrow ')]" priority="2">
    <xsl:param name="width-multiplier">0</xsl:param>
    
    <xsl:element name="table:table-row">
      <xsl:apply-templates mode="emit-cell-style"/>
    </xsl:element>
  </xsl:template>

  <!-- for choption in choice table. -->
  <xsl:template match="*[contains(@class, ' task/choption ')]" mode="emit-cell-style">
    <xsl:element name="table:table-cell">
      <xsl:attribute name="office:value-type">string</xsl:attribute>
      <xsl:call-template name="create_style_stable"/>

      <xsl:element name="text:p">
        <!-- choption should always bolded -->
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">bold</xsl:attribute>
          <xsl:apply-templates select="text()" mode="txt_for_choicetable"/>
        </xsl:element>
      </xsl:element>
      <xsl:apply-templates select="*[@class]"/>
    </xsl:element>
  </xsl:template>
  
<xsl:template match="text()" mode="txt_for_choicetable">
  
   <xsl:call-template name="gen_txt_content"/>
    
</xsl:template>
  
  <xsl:template match="*[contains(@class, ' task/cmd ')]">
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' task/stepresult ')]">
    
    
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        
        <xsl:apply-templates/>
        
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/info ')]" name="topic.task.info">
    
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        
        <xsl:apply-templates/>
        
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/tutorialinfo ')]" name="topic.task.tutorialinfo">
    
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
       <xsl:element name="text:span">
         <!-- start add rev flagging styles -->
         <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
         
          <xsl:apply-templates/>
         
         <!-- end add rev flagging styles -->
         <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
       </xsl:element>
    </xsl:element>
  </xsl:template>
  
  
  
  <!-- these para-like items need a leading space -->
  <xsl:template match="*[contains(@class,' task/stepxmp ')]" name="topic.task.stepxmp">
    
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        
        <xsl:apply-templates/>
        
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/context ')]">
    
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'task_context'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
    
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        
        <xsl:apply-templates/>
        
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/result ')]">
    
    
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'task_results'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
    
    <xsl:element name="text:p">
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        
        <xsl:apply-templates/>
        
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/postreq ')]">
    
    
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'task_postreq'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
    
    <xsl:element name="text:p">
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
      
      <xsl:apply-templates/>
      
      <!-- end add flagging styles -->
      <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
        
      </xsl:element>
    </xsl:element>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/taskbody ')]/*[contains(@class,' topic/example ')][not(*[contains(@class,' topic/title ')])]">
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'task_example'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
    
    <xsl:element name="text:p">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  
  <xsl:template match="*" mode="generate-task-label">
    <xsl:param name="use-label"/>
    <xsl:if test="$GENERATE-TASK-LABELS='YES'">
      <xsl:variable name="headLevel">
          <xsl:value-of select="count(ancestor::*[contains(@class,' topic/topic ')])+1"/>
      </xsl:variable>
      <xsl:element name="text:p">
        <xsl:attribute name="text:style-name"><xsl:value-of select="concat('Heading_20_', $headLevel)"/></xsl:attribute>
        <xsl:value-of select="$use-label"/>
      </xsl:element>
      <!-- 
      <div class="tasklabel">
        <xsl:element name="{$headLevel}">
          <xsl:attribute name="class">sectiontitle tasklabel</xsl:attribute>
          <xsl:value-of select="$use-label"/>
        </xsl:element>
      </div>
      -->
    </xsl:if>
  </xsl:template>
  
  <!-- Related links -->
  <!-- Tasks have their own group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='task']"
    mode="related-links:get-group" name="related-links:group.task">
    <xsl:text>task</xsl:text>
  </xsl:template>

  <!-- Priority of task group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='task']"
    mode="related-links:get-group-priority" name="related-links:group-priority.task">
    <xsl:value-of select="2"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/link ')][@type='task']"
    mode="related-links:result-group" name="related-links:result.task">
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
          <xsl:with-param name="stringName" select="'Related tasks'"/>
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

</xsl:stylesheet>
