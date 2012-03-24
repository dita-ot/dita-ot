<?xml version="1.0" encoding="UTF-8" ?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<xsl:param name="JAVAAPICSS" select="'ibmjavaref.css'"/>
<xsl:param name="JAVAAPICSSRTL" select="'ibmjavarefrtl.css'"/>


<xsl:template name="javaGetString">
  <xsl:param name="stringName"/>
  <xsl:call-template name="getString">
    <xsl:with-param name="stringName" select="$stringName"/>
  </xsl:call-template>
</xsl:template>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Suppressed processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- always process definitions in a mode for a specific purpose -->
<!-- Removed with P019897: Generic rule in apiref2xhtml should be used -->
<!--<xsl:template match="*[contains(@class,' apiRef/apiDef ')]"/>-->

<!-- process the package, class, and interface name as part of the
     body processing -->
<xsl:template match="*[contains(@class,' javaPackage/javaPackage ') or
        contains(@class,' javaClass/javaClass ') or
        contains(@class,' javaInterface/javaInterface ')] /
    *[contains(@class,' apiRef/apiName ')]"/>

<!-- process the members as part of the body processing -->
<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
        contains(@class,' javaInterface/javaInterface ')] /
    *[contains(@class,' javaMethod/javaMethod ') or
        contains(@class,' javaField/javaField ')]"/>

<!-- process the parent package for a class or interface
     as part of the body processing -->
<xsl:template match="*[contains(@class,' topic/link ') and
    @type='javaPackage' and @role='parent']" priority="3"/>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Alternatives to base processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[
        contains(@class,' javaPackage/javaPackage '    ) or
        contains(@class,' javaClass/javaClass '        ) or
        contains(@class,' javaInterface/javaInterface ') or
        contains(@class,' javaField/javaField '        ) or
        contains(@class,' javaMethod/javaMethod '      )] /
    *[contains(@class,' apiRef/apiName ')]" mode="default">
  <xsl:apply-imports/>
</xsl:template>

<xsl:template match="/*[
        contains(@class,' javaPackage/javaPackage '    ) or
        contains(@class,' javaClass/javaClass '        ) or
        contains(@class,' javaInterface/javaInterface ')]">
  <xsl:call-template name="javaapi-chapter-setup"/>
</xsl:template>

<xsl:template name="javaapi-chapter-setup">
  <html>
    <xsl:call-template name="setTopicLanguage"/>
    <xsl:value-of select="$newline"/>
    <xsl:call-template name="javaapiChapterHead"/>
    <xsl:call-template name="javaapiChapterBody"/>
  </html>
</xsl:template>

<xsl:template name="javaapiChapterHead">
  <head><xsl:value-of select="$newline"/>
    <!-- initial meta information -->
    <xsl:call-template name="generateCharset"/>   <!-- Set the character set to UTF-8 -->
    <xsl:call-template name="generateDefaultCopyright"/> <!-- Generate a default copyright, if needed -->
    <xsl:call-template name="generateDefaultMeta"/> <!-- Standard meta for security, robots, etc -->
    <xsl:call-template name="getMeta"/>           <!-- Process metadata from topic prolog -->
    <xsl:call-template name="generateCssLinks"/>  <!-- Generate links to defaultCSS files -->
    <xsl:call-template name="javaapiGenerateCssLinks"/>  <!-- Generate links to CSS files -->
    <xsl:call-template name="generateChapterTitle"/> <!-- Generate the <title> element -->
    <xsl:call-template name="javaapi-gen-user-head" />    <!-- include user's XSL HEAD processing here -->
    <xsl:call-template name="javaapi-gen-user-scripts" /> <!-- include user's XSL javascripts here -->
    <xsl:call-template name="javaapi-gen-user-styles" />  <!-- include user's XSL style element and content here -->
    <xsl:call-template name="processHDF"/>        <!-- Add user HDF file, if specified -->
  </head>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template name="javaapiGenerateCssLinks">
  <xsl:variable name="childlang"><xsl:call-template name="getLowerCaseLang"/></xsl:variable>
  <xsl:variable name="urltest">
    <xsl:call-template name="url-string">
      <xsl:with-param name="urltext" select="$CSSPATH"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="($childlang='ar-eg' or $childlang='ar' or $childlang='he' or $childlang='he-il') and ($urltest='url')">
      <link rel="stylesheet" type="text/css" href="{$CSSPATH}{$JAVAAPICSSRTL}" />
    </xsl:when>
    <xsl:when test="($childlang='ar-eg' or $childlang='ar' or $childlang='he' or $childlang='he-il') and ($urltest='')">
      <link rel="stylesheet" type="text/css" href="{$PATH2PROJ}{$CSSPATH}{$JAVAAPICSSRTL}" />
    </xsl:when>
    <xsl:when test="not($childlang='ar-eg' or $childlang='ar' or $childlang='he' or $childlang='he-il') and ($urltest='url')">
      <link rel="stylesheet" type="text/css" href="{$CSSPATH}{$JAVAAPICSS}" />
    </xsl:when>
    <xsl:otherwise>
      <link rel="stylesheet" type="text/css" href="{$PATH2PROJ}{$CSSPATH}{$JAVAAPICSS}" />
    </xsl:otherwise>
  </xsl:choose>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template name="javaapiChapterBody">
  <body>
    <!-- Already put xml:lang on <html>; do not copy to body with commonattributes -->
    <xsl:call-template name="setidaname"/>
    <xsl:value-of select="$newline"/>
    <xsl:call-template name="flagit"/>
    <xsl:call-template name="start-revflag"/>
    <!-- xsl:call-template name="generateBreadcrumbs"/ -->
    <xsl:call-template name="javaapi-gen-user-header"/>  <!-- include user's XSL running header here -->
    <xsl:call-template name="processHDR"/>
    <!-- Include a user's XSL call here to generate a toc based on what's a child of topic -->
    <xsl:call-template name="javaapi-gen-user-sidetoc"/>
    <!-- RDA: for javaClass and javaInterface, the related-links is being processed both here
              and from within the body. Remove the processing from this section. P019420 -->
    <xsl:choose>
      <xsl:when test="contains(@class,' javaClass/javaClass ') or contains(@class,' javaInterface/javaInterface ')">
        <xsl:apply-templates select="*[not(contains(@class,' topic/related-links '))]|comment()|processing-instruction()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/> <!-- this will include all things within topic; therefore, -->
                           <!-- title content will appear here by fall-through -->
                           <!-- followed by prolog (but no fall-through is permitted for it) -->
                           <!-- followed by body content, again by fall-through in document order -->
                           <!-- followed by related links -->
                           <!-- followed by child topics by fall-through -->
      </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="gen-endnotes"/>    <!-- include footnote-endnotes -->
    <xsl:call-template name="javaapi-gen-user-footer"/> <!-- include user's XSL running footer here -->
    <xsl:call-template name="processFTR"/>      <!-- Include XHTML footer, if specified -->
    <xsl:call-template name="end-revflag"/>
  </body>
  <xsl:value-of select="$newline"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Hooks
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template name="javaapi-gen-user-head">
  <!-- by default, execute the default user head -->
  <xsl:call-template name="gen-user-head"/>
</xsl:template>

<xsl:template name="javaapi-gen-user-scripts">
  <!-- by default, execute the default user scripts -->
  <xsl:call-template name="gen-user-scripts"/>
</xsl:template>

<xsl:template name="javaapi-gen-user-styles">
  <!-- by default, execute the default user styles -->
  <xsl:call-template name="gen-user-styles"/>
</xsl:template>

<xsl:template name="javaapi-gen-user-header">
  <!-- by default, execute the default user header -->
  <xsl:call-template name="gen-user-header"/>
</xsl:template>

<xsl:template name="javaapi-gen-user-sidetoc">
  <!-- by default, execute the default user sidetoc -->
  <xsl:call-template name="gen-user-sidetoc"/>
</xsl:template>

<xsl:template name="javaapi-gen-user-footer">
  <!-- by default, execute the default user footer -->
  <xsl:call-template name="gen-user-footer"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Package processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- BASED ON topic/body PROCESSING IN dit2htm -->
<xsl:template match="*[contains(@class,' javaPackage/javaPackageDetail ')]">
<div>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>

  <xsl:apply-templates select=".." mode="nameheader"/>

  <xsl:apply-templates
      select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]"
      mode="outofline"/>

  <xsl:apply-templates select="../*[contains(@class,' topic/related-links ')]"
      mode="javapackage"/>

  <h3 class="packageDescriptionHead">
    <xsl:apply-templates select=".." mode="nametype"/>
    <xsl:text> </xsl:text>
    <xsl:apply-templates
        select="preceding-sibling::*[contains(@class,' apiRef/apiName ')]"
        mode="default"/>
    <xsl:text> </xsl:text>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Description'"/>
    </xsl:call-template>
  </h3>
  <xsl:apply-templates/>

  <xsl:call-template name="end-revflag"/>
</div>
<xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaPackage/javaPackage ')] /
      *[contains(@class,' topic/related-links ')]"/>

<xsl:template match="*[contains(@class,' javaPackage/javaPackage ')] /
      *[contains(@class,' topic/related-links ')]" mode="javapackage">
  <xsl:variable name="subPackageNodes" select="
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/link ') and
          contains(@mapclass,' javaAPIMap/javaPackageRef ') and
          @role='child']"/>
  <!-- 20090825: per RFE 2018376, changed 
       contains(@mapclass,' javaAPIMap/javaInterfaceRef ')
       to
       @type='javaInterface'
       in the following variable. -->
  <xsl:variable name="interfaceNodes" select="
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/link ') and
          @type='javaInterface' and
          @role='child']"/>
  <xsl:variable name="classNodes" select="
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/link ') and
          contains(@mapclass,' javaAPIMap/javaClassRef ') and
          @role='child']"/>
  <xsl:variable name="exceptionNodes" select="
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/link ') and
          contains(@mapclass,' javaAPIMap/javaExceptionClassRef ') and
          @role='child']"/>
  <xsl:variable name="errorNodes" select="
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/linkpool ')] /
      *[contains(@class,' topic/link ') and
          contains(@mapclass,' javaAPIMap/javaErrorClassRef ') and
          @role='child']"/>
  <xsl:if test="$subPackageNodes">
    <table class="packageSubpackageSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaPackageHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$subPackageNodes" mode="packageSummary"/>
    </table>
  </xsl:if>
  <xsl:if test="$interfaceNodes">
    <table class="packageInterfaceSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaInterfaceHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$interfaceNodes" mode="packageSummary"/>
    </table>
  </xsl:if>
  <xsl:if test="$classNodes">
    <table class="packageClassSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaClassHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$classNodes" mode="packageSummary"/>
    </table>
  </xsl:if>
  <xsl:if test="$exceptionNodes">
    <table class="packageClassSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaExceptionHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$exceptionNodes" mode="packageSummary"/>
    </table>
  </xsl:if>
  <xsl:if test="$errorNodes">
    <table class="packageClassSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaErrorHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$errorNodes" mode="packageSummary"/>
    </table>
  </xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/link ') and @role='child']"
      mode="packageSummary">
  <xsl:variable name="file">
    <xsl:call-template name="getTopicFile"/>
  </xsl:variable>
  <xsl:variable name="topicid">
    <xsl:call-template name="getTopicID"/>
  </xsl:variable>
  <xsl:variable name="fileDocument" select="document($file,/)"/>
  <xsl:variable name="topicNode"
    select="($fileDocument[$topicid = '#none#']//*[contains(@class, ' topic/topic ')])[1] |
        $fileDocument[$topicid != '#none#']//*[contains(@class, ' topic/topic ')][@id=$topicid]"/>
  <xsl:variable name="topicTitleNode"
    select="$topicNode/*[contains(@class, ' topic/title ')]"/>
  <xsl:variable name="topicShortDescNode"
    select="$topicNode/*[contains(@class, ' topic/shortdesc ')]"/>
  <xsl:if test="not($topicNode)">
    <xsl:message>
      <xsl:text>$file=</xsl:text>
      <xsl:value-of select="$file"/>
      <xsl:text>, $topicid=</xsl:text>
      <xsl:value-of select="$topicid"/>
      <xsl:text>, $fileDocument is</xsl:text>
      <xsl:value-of select="boolean($fileDocument)"/>
      <xsl:text>, $topicNode is</xsl:text>
      <xsl:value-of select="boolean($topicNode)"/>
      <xsl:text>, $topicTitleNode=</xsl:text>
      <xsl:value-of select="$topicTitleNode"/>
      <xsl:text>, $topicShortDescNode=</xsl:text>
      <xsl:value-of select="$topicShortDescNode"/>
    </xsl:message>
  </xsl:if>
  <tr>
    <td valign="top" class="apiPackageListName">
      <a>
        <xsl:attribute name="href">
          <xsl:call-template name="href"/>
        </xsl:attribute>
	    <xsl:apply-templates select="$topicTitleNode/node()" mode="default"/>
      </a>
    </td>
    <td valign="top" class="apiPackageListDesc">
      <xsl:choose>
      <xsl:when test="$topicShortDescNode">
	    <xsl:apply-templates
            select="$topicShortDescNode/*|$topicShortDescNode/text()"/>
      </xsl:when>
      <xsl:otherwise>
	    <xsl:apply-templates select="$topicTitleNode/node()" mode="default"/>
      </xsl:otherwise>
      </xsl:choose>
    </td>
  </tr>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Class and interface prodcessing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' javaClass/javaClassDetail ') or
      contains(@class,' javaInterface/javaInterfaceDetail ')]">
  <xsl:apply-templates select=".." mode="checkClass">
    <xsl:with-param name="classDetail" select="."/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ')]"
      mode="checkClass">
  <xsl:param name="classDetail"/>
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="isFirstBase" select="true()"/>
  <xsl:variable name="baseRef" select="
        *[contains(@class,' javaClass/javaClassDetail ')] /
        *[contains(@class,' javaClass/javaClassDef ')] /
        *[contains(@class,' javaClass/javaBaseClass ')]"/>
  <xsl:call-template name="getBaseClass">
    <xsl:with-param name="classDetail" select="$classDetail"/>
    <xsl:with-param name="baseClasses" select="$baseClasses"/>
    <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    <xsl:with-param name="baseRef"     select="$baseRef"/>
    <xsl:with-param name="isFirstBase" select="$isFirstBase"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' javaInterface/javaInterface ')]"
      mode="checkClass">
  <xsl:param name="classDetail"/>
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="isFirstBase" select="true()"/>
  <xsl:variable name="baseRef" select="
        *[contains(@class,' javaInterface/javaInterfaceDetail ')] /
        *[contains(@class,' javaInterface/javaInterfaceDef ')] /
        *[contains(@class,' javaInterface/javaBaseInterface ')]"/>
  <xsl:call-template name="getBaseClass">
    <xsl:with-param name="classDetail" select="$classDetail"/>
    <xsl:with-param name="baseClasses" select="$baseClasses"/>
    <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    <xsl:with-param name="baseRef"     select="$baseRef"/>
    <xsl:with-param name="isFirstBase" select="$isFirstBase"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="getBaseClass">
  <xsl:param name="classDetail"/>
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="baseRef"/>
  <xsl:param name="isFirstBase"/>
  <xsl:choose>
    <!-- P018698: add check to make sure the scope is local. -->
    <xsl:when test="$baseRef and $baseRef/@href and 
                    (not($baseRef/@format) or $baseRef/@format='dita') and
                    (not($baseRef/@scope) or $baseRef/@scope='local')">
      <xsl:variable name="href" select="$baseRef/@href"/>
      <xsl:variable name="file">
        <xsl:call-template name="getTopicFile">
          <xsl:with-param name="href" select="$href"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="topicID">
        <xsl:call-template name="getTopicID">
          <xsl:with-param name="href" select="$href"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="baseDocument" select="document($file, /)"/>
      <xsl:variable name="baseClass" select="
             ($baseDocument[$topicID = '#none#'] //
                 *[contains(@class, ' topic/topic ')])[1] |
             $baseDocument[$topicID != '#none#'] //
                 *[contains(@class, ' topic/topic ')][@id=$topicID]"/>
      <xsl:choose>
        <xsl:when test="$baseClass">
          <xsl:choose>
            <xsl:when test="$isFirstBase">
              <xsl:apply-templates select="$baseClass" mode="checkClass">
                <xsl:with-param name="classDetail" select="$classDetail"/>
                <xsl:with-param name="baseClasses" select="$baseClass"/>
                <xsl:with-param name="baseRefs"    select="$baseRef"/>
                <xsl:with-param name="isFirstBase" select="false()"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="$baseClass" mode="checkClass">
                <xsl:with-param name="classDetail" select="$classDetail"/>
                <xsl:with-param name="baseClasses" select="$baseClass|$baseClasses"/>
                <xsl:with-param name="baseRefs"    select="$baseRef|$baseRefs"/>
                <xsl:with-param name="isFirstBase" select="false()"/>
              </xsl:apply-templates>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="formatClass">
            <xsl:with-param name="classDetail" select="$classDetail"/>
            <xsl:with-param name="baseClasses" select="$baseClasses"/>
            <xsl:with-param name="baseRefs"    select="$baseRefs"/>
            <xsl:with-param name="hasBase"     select="not($isFirstBase)"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="formatClass">
        <xsl:with-param name="classDetail" select="$classDetail"/>
        <xsl:with-param name="baseClasses" select="$baseClasses"/>
        <xsl:with-param name="baseRefs"    select="$baseRefs"/>
        <xsl:with-param name="hasBase"     select="not($isFirstBase)"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="formatClass">
  <xsl:param name="classDetail"/>
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="hasBase"/>
  <xsl:apply-templates select="$classDetail" mode="formatClass">
    <xsl:with-param name="baseClasses" select="$baseClasses"/>
    <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    <xsl:with-param name="hasBase"     select="$hasBase"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClassDetail ') or
          contains(@class,' javaInterface/javaInterfaceDetail ')]"
      mode="formatClass">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="hasBase"/>
  <xsl:variable name="constructorNodes" select="
      following-sibling::*[contains(@class,' javaMethod/javaMethod ') and
          *[contains(@class,' javaMethod/javaMethodDetail ') and
              *[contains(@class,' javaMethod/javaConstructorDef ') and
                  *[contains(@class,' javaMethod/javaMethodAccess ') and
                      @value='public'] ] ] ]"/>
  <xsl:variable name="fieldNodes" select="
      following-sibling::*[contains(@class,' javaField/javaField ') and
          *[contains(@class,' javaField/javaFieldDetail ') and
              *[contains(@class,' javaField/javaFieldDef ') and
                  *[contains(@class,' javaField/javaFieldAccess ') and
                      @value='public'] ] ] ]"/>
  <xsl:variable name="methodNodes" select="
      following-sibling::*[contains(@class,' javaMethod/javaMethod ') and
          *[contains(@class,' javaMethod/javaMethodDetail ') and
              *[contains(@class,' javaMethod/javaMethodDef ') and
                  *[contains(@class,' javaMethod/javaMethodAccess ') and
                      @value='public'] ] ] ]"/>

<div>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>

  <xsl:comment>name header</xsl:comment>
  <xsl:apply-templates select=".." mode="nameheader"/>

  <xsl:comment>ancestry header</xsl:comment>
  <xsl:apply-templates select=".." mode="formatAncestry">
    <xsl:with-param name="baseClasses" select="$baseClasses"/>
    <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    <xsl:with-param name="hasBase"     select="$hasBase"/>
  </xsl:apply-templates>

  <hr/>

  <xsl:comment>signature summary</xsl:comment>
  <xsl:apply-templates select=".." mode="signatureSummary"/>

  <xsl:comment>description</xsl:comment>
  <xsl:apply-templates
      select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]"
      mode="outofline"/>
  <xsl:apply-templates/>

  <xsl:comment>see also</xsl:comment>
  <xsl:apply-templates
      select="following-sibling::*[contains(@class,' topic/related-links ')]"/>

  <hr/>

  <xsl:comment>member summary</xsl:comment>

  <xsl:if test="$fieldNodes">
    <table class="fieldSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaFieldHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$fieldNodes" mode="memberSummary"/>
    </table>
  </xsl:if>

  <xsl:if test="$hasBase">
    <xsl:call-template name="listBaseFields">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    </xsl:call-template>
  </xsl:if>

  <xsl:if test="$constructorNodes">
    <table class="constructorSummary">
      <tr>
        <th align="left" valign="top" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaConstructorHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$constructorNodes"
          mode="signatureSummary"/>
    </table>
  </xsl:if>

  <xsl:if test="$methodNodes">
    <table class="methodSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="'javaMethodHead'"/>
          </xsl:call-template>
        </th>
      </tr>
      <xsl:apply-templates select="$methodNodes" mode="memberSummary"/>
    </table>
  </xsl:if>

  <xsl:if test="$hasBase">
    <xsl:call-template name="listBaseMethods">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    </xsl:call-template>
  </xsl:if>

  <xsl:comment>member detail</xsl:comment>

  <xsl:if test="$constructorNodes">
    <p class="javaConstructorDetail">
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaConstructorDetail'"/>
      </xsl:call-template>
    </p>
    <xsl:apply-templates select="$constructorNodes" mode="memberDetail"/>
  </xsl:if>

  <xsl:if test="$methodNodes">
    <p class="javaMethodDetail">
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaMethodDetail'"/>
      </xsl:call-template>
    </p>
    <xsl:apply-templates select="$methodNodes" mode="memberDetail"/>
  </xsl:if>

  <xsl:call-template name="end-revflag"/>
</div>
<xsl:value-of select="$newline"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Base listing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template name="listBaseFields">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="baseCount" select="count($baseClasses)"/>
  <xsl:param name="currBase"  select="$baseCount"/>
  <xsl:if test="$currBase &gt; 0">
    <xsl:apply-templates select="$baseClasses[$currBase]"
        mode="baseFieldListing">
      <xsl:with-param name="baseRef" select="$baseRefs[$currBase]"/>
    </xsl:apply-templates>
    <xsl:call-template name="listBaseFields">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
      <xsl:with-param name="baseCount"   select="$baseCount"/>
      <xsl:with-param name="currBase"    select="$currBase - 1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="listBaseMethods">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="baseCount" select="count($baseClasses)"/>
  <xsl:param name="currBase"  select="$baseCount"/>
  <xsl:if test="$currBase &gt; 0">
    <xsl:apply-templates select="$baseClasses[$currBase]"
        mode="baseMethodListing">
      <xsl:with-param name="baseRef" select="$baseRefs[$currBase]"/>
    </xsl:apply-templates>
    <xsl:call-template name="listBaseMethods">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
      <xsl:with-param name="baseCount"   select="$baseCount"/>
      <xsl:with-param name="currBase"    select="$currBase - 1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')]"
      mode="baseFieldListing">
  <xsl:param name="baseRef"/>
  <xsl:variable name="fieldNodes" select="
      *[contains(@class,' javaField/javaField ') and
          *[contains(@class,' javaField/javaFieldDetail ') and
              *[contains(@class,' javaField/javaFieldDef ') and
                  *[contains(@class,' javaField/javaFieldAccess ') and
                      @value='public'] ] ] ]"/>
  <xsl:apply-templates select="." mode="baseMemberListing">
    <xsl:with-param name="baseRef"    select="$baseRef"/>
    <xsl:with-param name="memberType" select="'javaBaseFieldHead'"/>
    <xsl:with-param name="members"    select="$fieldNodes"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')]"
      mode="baseMethodListing">
  <xsl:param name="baseRef"/>
  <xsl:variable name="methodNodes" select="
      *[contains(@class,' javaMethod/javaMethod ') and
          *[contains(@class,' javaMethod/javaMethodDetail ') and
              *[contains(@class,' javaMethod/javaMethodDef ') and
                  *[contains(@class,' javaMethod/javaMethodAccess ') and
                      @value='public'] ] ] ]"/>
  <xsl:apply-templates select="." mode="baseMemberListing">
    <xsl:with-param name="baseRef"    select="$baseRef"/>
    <xsl:with-param name="memberType" select="'javaBaseMethodHead'"/>
    <xsl:with-param name="members"    select="$methodNodes"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')]"
      mode="baseMemberListing">
  <xsl:param name="baseRef"/>
  <xsl:param name="memberType"/>
  <xsl:param name="members"/>
  <xsl:if test="$members">
    <xsl:variable name="baseFile">
      <xsl:call-template name="getOutputFile">
        <xsl:with-param name="href" select="$baseRef/@href"/>
      </xsl:call-template>
    </xsl:variable>
    <table class="baseSummary">
      <tr>
        <th align="left" valign="top" colspan="2" class="apiSummaryHead">
          <xsl:call-template name="javaGetString">
            <xsl:with-param name="stringName" select="$memberType"/>
          </xsl:call-template>
          <xsl:text> </xsl:text>
          <a class="baseHeader" href="{$baseFile}">
            <xsl:apply-templates
                select="*[contains(@class, ' apiRef/apiName ')]"
                mode="default"/>
          </a>
        </th>
      </tr>
      <tr>
        <td valign="top">
	      <xsl:for-each select="$members">
            <xsl:apply-templates select="." mode="baseListing">
              <xsl:with-param name="baseFile" select="$baseFile"/>
              <xsl:with-param name="topicID"  select="@id"/>
            </xsl:apply-templates>
            <xsl:if test="position()&lt;last()">
              <xsl:text>, </xsl:text>
            </xsl:if>
    	  </xsl:for-each>
        </td>
      </tr>
    </table>
  </xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')]"
      mode="formatAncestry">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="hasBase"/>
  <xsl:variable name="currClass" select="."/>
  <pre>
    <xsl:choose>
    <xsl:when test="$hasBase">
      <xsl:apply-templates select="$baseClasses[last()]" mode="formatTopBase">
        <xsl:with-param name="baseClasses" select="$baseClasses"/>
        <xsl:with-param name="baseRefs"    select="$baseRefs"/>
        <xsl:with-param name="hasBase"     select="$hasBase"/>
        <xsl:with-param name="currClass"   select="$currClass"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="$currClass" mode="formatTopBase">
        <xsl:with-param name="hasBase"     select="$hasBase"/>
        <xsl:with-param name="currClass"   select="$currClass"/>
      </xsl:apply-templates>
    </xsl:otherwise>
    </xsl:choose>
  </pre>
</xsl:template>

<xsl:template match="*[contains(@class,' javaInterface/javaInterface ')]"
      mode="formatTopBase">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="hasBase"/>
  <xsl:param name="currClass"/>
  <xsl:variable name="topBase" select="
          *[contains(@class,' javaInterface/javaInterfaceDetail ')] /
          *[contains(@class,' javaInterface/javaInterfaceDef ')] /
          *[contains(@class,' javaInterface/javaBaseInterface ')]"/>
  <xsl:call-template name="formatTopBase">
    <xsl:with-param name="baseClasses" select="$baseClasses"/>
    <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    <xsl:with-param name="hasBase"     select="$hasBase"/>
    <xsl:with-param name="currClass"   select="$currClass"/>
    <xsl:with-param name="topBase"     select="$topBase"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ')]"
      mode="formatTopBase">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="hasBase"/>
  <xsl:param name="currClass"/>
  <xsl:variable name="topBase" select="
          *[contains(@class,' javaClass/javaClassDetail ')] /
          *[contains(@class,' javaClass/javaClassDef ')] /
          *[contains(@class,' javaClass/javaBaseClass ')]"/>
  <xsl:call-template name="formatTopBase">
    <xsl:with-param name="baseClasses" select="$baseClasses"/>
    <xsl:with-param name="baseRefs"    select="$baseRefs"/>
    <xsl:with-param name="hasBase"     select="$hasBase"/>
    <xsl:with-param name="currClass"   select="$currClass"/>
    <xsl:with-param name="topBase"     select="$topBase"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="formatTopBase">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="hasBase"/>
  <xsl:param name="currClass"/>
  <xsl:param name="topBase"/>
  <xsl:choose>
  <xsl:when test="$topBase">
    <xsl:text>+-- </xsl:text>
    <xsl:choose>
    <xsl:when test="$topBase/@href">
      <a class="ancestrylink" href="{$topBase/@href}">
        <!--<xsl:value-of select="$topBase"/>-->
        <xsl:apply-templates select="$topBase" mode="formatClassLink"/>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <span class="ancestrybase">
        <!--<xsl:value-of select="$topBase"/>-->
        <xsl:apply-templates select="$topBase" mode="formatClassLink"/>
      </span>
    </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="$newline"/>
    <xsl:call-template name="completeBaseSummary">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
      <xsl:with-param name="hasBase"     select="$hasBase"/>
      <xsl:with-param name="currClass"   select="$currClass"/>
      <xsl:with-param name="indent"      select="'      '"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:call-template name="completeBaseSummary">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
      <xsl:with-param name="hasBase"     select="$hasBase"/>
      <xsl:with-param name="currClass"   select="$currClass"/>
    </xsl:call-template>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- P019419: ensure <desc> stays out of the class link -->
<xsl:template match="*" mode="formatClassLink">
  <xsl:apply-templates select="text()|*[not(contains(@class,' topic/desc '))]" mode="text-only"/>
</xsl:template>

<xsl:template name="completeBaseSummary">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="hasBase"/>
  <xsl:param name="currClass"/>
  <xsl:param name="indent" select="''"/>
  <xsl:choose>
  <xsl:when test="$hasBase">
    <xsl:call-template name="baseSummary">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
      <xsl:with-param name="currClass"   select="$currClass"/>
      <xsl:with-param name="indent"      select="$indent"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:call-template name="currClassSummary">
      <xsl:with-param name="currClass" select="$currClass"/>
      <xsl:with-param name="indent"    select="$indent"/>
    </xsl:call-template>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="baseSummary">
  <xsl:param name="baseClasses"/>
  <xsl:param name="baseRefs"/>
  <xsl:param name="currClass"/>
  <xsl:param name="currBase"  select="1"/>
  <xsl:param name="baseCount" select="count($baseClasses)"/>
  <xsl:param name="printItem" select="number($baseCount - $currBase + 1)"/>
  <xsl:param name="indent"    select="''"/>
  <xsl:choose>
  <xsl:when test="$currBase &lt;= $baseCount">
    <xsl:variable name="filename">
      <xsl:call-template name="getOutputFile">
        <xsl:with-param name="href" select="$baseRefs[$printItem]/@href"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="$indent"/>
    <xsl:text>+-- </xsl:text>
    <a class="ancestrylink" href="{$filename}">
      <xsl:value-of select="$baseClasses[$printItem] /
          *[contains(@class,' apiRef/apiName ')]"/>
    </a>
    <xsl:value-of select="$newline"/>
    <xsl:call-template name="baseSummary">
      <xsl:with-param name="baseClasses" select="$baseClasses"/>
      <xsl:with-param name="baseRefs"    select="$baseRefs"/>
      <xsl:with-param name="currClass"   select="$currClass"/>
      <xsl:with-param name="currBase"    select="$currBase+1"/>
      <xsl:with-param name="baseCount"   select="$baseCount"/>
      <xsl:with-param name="indent"      select="concat($indent,'      ')"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:call-template name="currClassSummary">
      <xsl:with-param name="currClass" select="$currClass"/>
      <xsl:with-param name="indent"    select="$indent"/>
    </xsl:call-template>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="currClassSummary">
  <xsl:param name="currClass"/>
  <xsl:param name="indent" select="''"/>
  <xsl:value-of select="$indent"/>
  <xsl:text>+-- </xsl:text>
  <span class="ancestryself">
    <xsl:value-of select="$currClass/*[contains(@class,' apiRef/apiName ')]"/>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' javaPackage/javaPackage ')]"
      mode="nameheader">
  <h2 class="topHead">
    <xsl:apply-templates select="." mode="nametype"/>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="*[contains(@class,' apiRef/apiName ')]"
        mode="default"/>
  </h2>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')]"
      mode="nameheader">
  <xsl:apply-templates select="
          *[contains(@class,' topic/related-links ')] /
          *[contains(@class,' topic/linkpool ')] /
          *[contains(@class,' topic/link ') and
              @type='javaPackage' and @role='parent']"
      mode="header"/>
  <h2 class="topHead">
    <xsl:apply-templates select="." mode="nametype"/>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="*[contains(@class,' apiRef/apiName ')]"
        mode="default"/>
  </h2>
</xsl:template>

<xsl:template match="*[contains(@class,' javaPackage/javaPackage ')]"
      mode="nametype">
  <!--
  Remove by M Alupului 15.12.2006
  <xsl:call-template name="javaGetString">
    <xsl:with-param name="stringName" select="'javaPackage'"/>
  </xsl:call-template>
   -->
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ')]"
      mode="nametype">
  <xsl:call-template name="javaGetString">
    <xsl:with-param name="stringName" select="'javaClass'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' javaInterface/javaInterface ')]"
      mode="nametype">
  <xsl:call-template name="javaGetString">
    <xsl:with-param name="stringName" select="'javaInterface'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' javaField/javaField ') or
          contains(@class,' javaMethod/javaMethod ')]"
      mode="baseListing">
  <xsl:param name="baseFile"/>
  <xsl:param name="topicID"/>
  <a href="{$baseFile}#{@id}" class="baselink">
    <xsl:apply-templates select="*[contains(@class,' apiRef/apiName ')]"
        mode="apiSignature"/>
  </a>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')]"
      mode="baseListing">
  <xsl:param name="baseFile"/>
  <xsl:param name="topicID"/>
  <xsl:param name="isFirstListing"/>
  <xsl:variable name="methodNodes" select="
      *[contains(@class,' javaMethod/javaMethod ') and
          *[contains(@class,' javaMethod/javaMethodDetail ') and
              *[contains(@class,' javaMethod/javaMethodDef ') and
                  *[contains(@class,' javaMethod/javaMethodAccess ') and
                      @value='public'] ] ] ]"/>
  <xsl:choose>
  <xsl:when test="$methodNodes">
      <xsl:call-template name="baseListingRows">
        <xsl:with-param name="baseFile"    select="$baseFile"/>
        <xsl:with-param name="topicID"     select="$topicID"/>
        <xsl:with-param name="methodNodes" select="$methodNodes"/>
      </xsl:call-template>
    <xsl:apply-templates select="." mode="checkBase">
      <xsl:with-param name="isFirstListing" select="false()"/>
    </xsl:apply-templates>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates select="." mode="checkBase">
      <xsl:with-param name="isFirstListing" select="$isFirstListing"/>
    </xsl:apply-templates>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="baseListingRows">
  <xsl:param name="baseFile"/>
  <xsl:param name="topicID"/>
  <xsl:param name="methodNodes"/>
  <tr>
    <th align="left" valign="top" colspan="2" class="apiSummaryHead">
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaBaseHead'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:apply-templates select="*[contains(@class, ' topic/title ')]"
          mode="default"/>
    </th>
  </tr>
  <tr>
    <td valign="top">
	  <xsl:for-each select="$methodNodes">
        <xsl:apply-templates select="." mode="baseListing">
          <xsl:with-param name="baseFile" select="$baseFile"/>
          <xsl:with-param name="topicID"  select="$topicID"/>
        </xsl:apply-templates>
        <xsl:if test="position()&lt;last()">
          <xsl:text>, </xsl:text>
        </xsl:if>
	  </xsl:for-each>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/link ') and
        @type='javaPackage' and @role='parent']" mode="header">
  <p>
    <a class="headPackage">
      <xsl:attribute name="href">
        <xsl:call-template name="href"/>
      </xsl:attribute>
      <xsl:apply-templates select="*[contains(@class,' topic/linktext ')]"/>
    </a>
  </p>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Member summary
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' javaMethod/javaMethodDef ') or
            contains(@class,' javaMethod/javaConstructorDef ')]"
      mode="memberSummary">
  <xsl:variable name="paramNodes"
          select="*[contains(@class,' javaMethod/javaParam ')]"/>
  <xsl:variable name="returnNode"
          select="*[contains(@class,' javaMethod/javaReturn ') and
              *[contains(@class,' apiRef/apiDefNote ')]]"/>
  <xsl:variable name="exceptionNodes"
      select="*[contains(@class,' javaMethod/javaException ')]"/>
  <xsl:if test="$paramNodes">
    <p class="javaMethodDetailHeader">
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaParameterHead'"/>
      </xsl:call-template>
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'ColonSymbol'"/>
      </xsl:call-template>
    </p>
    <table class="methodParameters">
      <xsl:apply-templates select="$paramNodes" mode="memberSummary"/>
    </table>
  </xsl:if>
  <xsl:if test="$returnNode">
    <p class="javaMethodDetailHeader">
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaReturnHead'"/>
      </xsl:call-template>
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'ColonSymbol'"/>
      </xsl:call-template>
    </p>
    <table class="methodReturns">
      <xsl:apply-templates select="$returnNode" mode="memberSummary"/>
    </table>
  </xsl:if>
  <xsl:if test="$exceptionNodes">
    <p class="javaMethodDetailHeader">
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaThrowHead'"/>
      </xsl:call-template>
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'ColonSymbol'"/>
      </xsl:call-template>
    </p>
    <table class="methodExceptions">
      <xsl:apply-templates select="$exceptionNodes" mode="memberSummary"/>
    </table>
  </xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaVoid ')]"
      mode="memberSummary">
  <tr>
    <td valign="top">
      <xsl:apply-templates select="."/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaReturn ')]"
      mode="memberSummary">
  <tr>
    <td valign="top">
      <!-- should supply shortdesc for target if no contextual note -->
      <xsl:apply-templates select="*[contains(@class,' apiRef/apiDefNote ')]"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaException ')]"
      mode="memberSummary">
  <tr>
    <td valign="top" class="exceptionListing">
      <xsl:apply-templates
          select="*[contains(@class,' javaMethod/javaMethodClass ')]"/>
    </td>
    <td valign="top">
      <xsl:apply-templates select="*[contains(@class,' apiRef/apiDefNote ')]">
        <xsl:with-param name="insertText" select="' - '"/>
      </xsl:apply-templates>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaParam ')]"
      mode="memberSummary">
  <tr>
    <td valign="top" class="paramListing">
      <xsl:apply-templates
          select="*[contains(@class,' apiRef/apiItemName ')]"/>
    </td>
    <td valign="top">
      <!-- should supply shortdesc for target if no contextual note -->
      <xsl:apply-templates
          select="*[contains(@class,' apiRef/apiDefNote ')]">
        <xsl:with-param name="insertText" select="' - '"/>
      </xsl:apply-templates>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')] /
      *[contains(@class,' apiRef/apiRef ' )]"
      mode="memberSummary">
  <tr>
    <td valign="top" class="apiDatatype">
      <xsl:apply-templates select="." mode="apiDatatype"/>
    </td>
    <td valign="top">
      <xsl:apply-templates select="." mode="apiSignature"/>
    </td>
  </tr>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Signature
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' javaClass/javaClass ')]"
      mode="signatureSummary">
  <xsl:variable name="baseNode" select="
          *[contains(@class,' javaClass/javaClassDetail ')] /
            *[contains(@class,' javaClass/javaClassDef ')] /
            *[contains(@class,' javaClass/javaBaseClass ')]"/>
  <xsl:variable name="implementedInterfaceNodes" select="
          *[contains(@class,' javaClass/javaClassDetail ')] /
            *[contains(@class,' javaClass/javaClassDef ')] /
            *[contains(@class,' javaClass/javaImplementedInterface ')]"/>
  <xsl:variable name="qualifierNodes" select="
          *[contains(@class,' javaClass/javaClassDetail ')] /
            *[contains(@class,' javaClass/javaClassDef ')] / *[
            contains(@class,' javaClass/javaFinalClass '   ) or
            contains(@class,' javaClass/javaAbstractClass ') or
            contains(@class,' javaClass/javaStaticClass '  ) or
            contains(@class,' javaClass/javaClassAccess '  )]"/>
  <p class="signatureSummary">
    <xsl:if test="$qualifierNodes">
      <xsl:apply-templates select="$qualifierNodes"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:call-template name="javaGetString">
      <xsl:with-param name="stringName" select="'javaClass'"/>
    </xsl:call-template>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="*[contains(@class,' apiRef/apiName ')]"
          mode="apiSignature">
      <xsl:with-param name="spanClass" select="'javaClassName'"/>
    </xsl:apply-templates>
    <xsl:if test="$baseNode">
      <br />
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaBaseClass'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:apply-templates select="$baseNode"/>
    </xsl:if>
    <xsl:if test="$implementedInterfaceNodes">
      <br />
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaImplementedInterface'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:for-each select="$implementedInterfaceNodes">
        <xsl:apply-templates select="."/>
        <xsl:if test="position()!=last()">, </xsl:if>
      </xsl:for-each>
    </xsl:if>
  </p>
</xsl:template>

<xsl:template match="*[contains(@class,' javaInterface/javaInterface ')]"
      mode="signatureSummary">
  <xsl:variable name="baseNode" select="
          *[contains(@class,' javaInterface/javaInterfaceDetail ')] /
            *[contains(@class,' javaInterface/javaInterfaceDef ')] /
            *[contains(@class,' javaInterface/javaBaseInterface ')]"/>
  <xsl:variable name="qualifierNodes" select="
          *[contains(@class,' javaInterface/javaInterfaceDetail ')] /
            *[contains(@class,' javaInterface/javaInterfaceDef ')] /
            *[contains(@class,' javaInterface/javaInterfaceAccess '  )]"/>
  <p class="{@class}">
    <xsl:if test="$qualifierNodes">
      <xsl:apply-templates select="$qualifierNodes"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:call-template name="javaGetString">
      <xsl:with-param name="stringName" select="'javaInterface'"/>
    </xsl:call-template>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="*[contains(@class,' apiRef/apiName ')]"
          mode="apiSignature">
      <xsl:with-param name="spanClass" select="'javaInterfaceName'"/>
    </xsl:apply-templates>
    <xsl:if test="$baseNode">
      <br />
      <xsl:call-template name="javaGetString">
        <xsl:with-param name="stringName" select="'javaBaseClass'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:apply-templates select="$baseNode"/>
    </xsl:if>
  </p>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/title ')]" mode="apiSignature">
  <xsl:param name="spanClass" select="local-name(.)"/>
  <span class="{$spanClass}">
    <xsl:apply-templates mode="default"/>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' javaClass/javaClass ') or
          contains(@class,' javaInterface/javaInterface ')] /
      *[contains(@class,' javaMethod/javaMethod ' )]"
      mode="signatureSummary">
  <tr>
    <td valign="top">
      <xsl:apply-templates select="." mode="apiSignature"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' javaField/javaField ')]"
      mode="apiSignature">
  <a name="{@id}"></a>
  <span class="signatureSummary">
    <xsl:apply-templates select="*[contains(@class,' topic/title ')]"
        mode="apiSignature"/>
  </span>
  <br />
  <xsl:apply-templates select="*[contains(@class,' topic/shortdesc ')]"
      mode="apiSignature"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/shortdesc ')]"
      mode="apiSignature">
  <p class="apiSignature">
    <xsl:apply-templates/>
  </p>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaMethod ')]"
      mode="apiSignature">
  <span class="signatureSummary">
    <a href="#{@id}" class="summarylink">
      <xsl:apply-templates select="*[contains(@class,' topic/title ')]"
          mode="apiSignature"/>
    </a>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates
        select="*[contains(@class,' javaMethod/javaMethodDetail ')] /
            *[contains(@class,' javaMethod/javaMethodDef '       ) or
              contains(@class,' javaMethod/javaConstructorDef '  )] /
            *[contains(@class,' javaMethod/javaParam '           )]"
        mode="apiSignature"/>
    <xsl:text>)</xsl:text>
  </span>
  <br />
  <xsl:apply-templates select="*[contains(@class,' topic/shortdesc ')]"
      mode="apiSignature"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaParam ')]"
      mode="apiSignature">
  <xsl:if test="preceding-sibling::*[1][
      contains(@class,' javaMethod/javaParam ')]">
    <xsl:text>, </xsl:text>
  </xsl:if>
  <xsl:apply-templates select="*[
      contains(@class,' javaMethod/javaMethodClass '     ) or
      contains(@class,' javaMethod/javaMethodInterface ' ) or
      contains(@class,' javaMethod/javaMethodPrimitive ' ) or
      contains(@class,' javaMethod/javaMethodArray '     )]"
      mode="apiSignature"/>
  <xsl:text> </xsl:text>
  <xsl:apply-templates select="*[contains(@class,' apiRef/apiItemName ')]"
      mode="apiSignature"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaMethodClass ' ) or
      contains(@class,' javaMethod/javaMethodInterface ' ) or
      contains(@class,' javaMethod/javaMethodPrimitive ' ) or
      contains(@class,' javaMethod/javaMethodArray '     )]"
      mode="apiSignature">
  <xsl:apply-templates select="."/>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiItemName ' )]"
      mode="apiSignature">
  <xsl:apply-templates select="."/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Datatype
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' javaMethod/javaMethodDef ')]"
      mode="apiDatatype">
  <xsl:apply-templates select="*[contains(@class,' javaMethod/javaReturn ') or
          contains(@class,' javaMethod/javaVoid ' )]"
      mode="apiDatatype"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaReturn ')]"
      mode="apiDatatype">
    <xsl:apply-templates select="*[
        contains(@class,' javaMethod/javaMethodClass '     ) or
        contains(@class,' javaMethod/javaMethodInterface ' ) or
        contains(@class,' javaMethod/javaMethodPrimitive ' ) or
        contains(@class,' javaMethod/javaMethodArray '     )]"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaVoid ')]"
      mode="apiDatatype">
  <xsl:value-of select="@value"/>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiRef ')]"
      mode="apiDatatype">
  <xsl:apply-templates select="*[contains(@class,' apiRef/apiDetail ')]"
      mode="apiDatatype"/>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiDetail ')]"
      mode="apiDatatype">
  <xsl:apply-templates select="*[contains(@class,' apiRef/apiDef ')]"
      mode="apiDatatype"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaField/javaFieldDef ')]"
      mode="apiDatatype">
  <xsl:if test="*[
      contains(@class,' javaField/javaFieldAccess '         ) or
      contains(@class,' javaField/javaFinalField '          ) or
      contains(@class,' javaField/javaStaticField '         ) or
      contains(@class,' javaField/javaTransientField '      ) or
      contains(@class,' javaField/javaVolatileField '       )]">
    <xsl:if test="*[contains(@class,' javaField/javaFieldAccess ')]">
      <xsl:apply-templates select="*[contains(@class,' javaField/javaFieldAccess ')]"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*[contains(@class,' javaField/javaStaticField ')]"/>
    <xsl:apply-templates select="*[contains(@class,' javaField/javaFinalField ')]"/>
    <xsl:apply-templates select="*[contains(@class,' javaField/javaTransientField ')]"/>
    <xsl:apply-templates select="*[contains(@class,' javaField/javaVolatileField ')]"/>
    <xsl:text> </xsl:text>
  </xsl:if>
  <xsl:apply-templates
      select="*[contains(@class,' javaField/javaFieldClass ') or
      contains(@class,' javaField/javaFieldInterface ') or
      contains(@class,' javaField/javaFieldPrimitive ') or
      contains(@class,' javaField/javaFieldArray '    )]"
      mode="apiDatatype"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaField/javaFieldClass ') or
      contains(@class,' javaField/javaFieldInterface ') or
      contains(@class,' javaField/javaFieldPrimitive ') or
      contains(@class,' javaField/javaFieldArray '    )]"
      mode="apiDatatype">
  <xsl:apply-templates select="."/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaConstructorDef ')]"
      mode="apiDatatype"/>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Member listings
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' javaMethod/javaMethodDetail ')]">
  <div>
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setidaname"/>
    <xsl:call-template name="flagit"/>
    <xsl:call-template name="start-revflag"/>
    <xsl:comment>syntax</xsl:comment>
    <xsl:apply-templates select="
          *[contains(@class,' javaMethod/javaMethodDef ') or
            contains(@class,' javaMethod/javaConstructorDef ')]"
        mode="methodSignature"/>
    <xsl:comment>description</xsl:comment>
    <div class="methodDescription">
      <xsl:apply-templates
          select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]"
          mode="outofline"/>
      <xsl:apply-templates/>
    </div>
    <xsl:comment>member summary</xsl:comment>
    <xsl:apply-templates
        select="*[contains(@class,' javaMethod/javaMethodDef ') or
              contains(@class,' javaMethod/javaConstructorDef ')]"
        mode="memberSummary"/>
    <xsl:comment>see also</xsl:comment>
    <xsl:apply-templates select="following-sibling::
        *[contains(@class,' topic/related-links ')]"/>
    <hr/>
    <xsl:call-template name="end-revflag"/>
  </div>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaMethodDef ') or
            contains(@class,' javaMethod/javaConstructorDef ')]"
      mode="methodSignature">
  <xsl:variable name="returnNodes"
        select="(*[contains(@class,' javaMethod/javaReturn ')]/*[
            contains(@class,' javaMethod/javaMethodClass '     ) or
            contains(@class,' javaMethod/javaMethodInterface ' ) or
            contains(@class,' javaMethod/javaMethodPrimitive ' ) or
            contains(@class,' javaMethod/javaMethodArray '     )]) |
        *[contains(@class,' javaMethod/javaVoid '   )]"/>
  <xsl:comment>signature summary</xsl:comment>
  <p class="javaMethodSignature">
    <xsl:if test="*[contains(@class,' javaMethod/javaMethodAccess ')]">
      <xsl:apply-templates select="*[contains(@class,' javaMethod/javaMethodAccess ')]"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*[contains(@class,' javaMethod/javaFinalMethod ')]"/>
    <xsl:apply-templates select="*[contains(@class,' javaMethod/javaAbstractMethod ')]"/>
    <xsl:apply-templates select="*[contains(@class,' javaMethod/javaStaticMethod ')]"/>
    <xsl:apply-templates select="*[contains(@class,' javaMethod/javaNativeMethod ')]"/>
    <xsl:apply-templates select="*[contains(@class,' javaMethod/javaSynchronizedMethod ')]"/>
    <xsl:text> </xsl:text>
    <xsl:if test="$returnNodes">
      <xsl:apply-templates select="$returnNodes"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:apply-templates
        select="ancestor::*[contains(@class,' apiRef/apiRef ')][1] /
            *[contains(@class,' topic/title ')]"
        mode="apiSignature">
      <xsl:with-param name="spanClass" select="'javaMethodName'"/>
    </xsl:apply-templates>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates
        select="*[contains(@class,' javaMethod/javaParam ')]"
        mode="apiSignature"/>
    <xsl:text>)</xsl:text>
  </p>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaParam ')]">
    <xsl:apply-templates select="*[
        contains(@class,' javaMethod/javaMethodClass '     ) or
        contains(@class,' javaMethod/javaMethodInterface ' ) or
        contains(@class,' javaMethod/javaMethodPrimitive ' ) or
        contains(@class,' javaMethod/javaMethodArray '     )]"/>
</xsl:template>

<xsl:template match="*[contains(@class,' javaMethod/javaException ')]">
    <xsl:apply-templates select="*[
        contains(@class,' javaMethod/javaMethodClass '     )]"/>
</xsl:template>

<xsl:template match="*[
      contains(@class,' javaClass/javaFinalClass '          ) or
      contains(@class,' javaClass/javaAbstractClass '       ) or
      contains(@class,' javaClass/javaStaticClass '         ) or
      contains(@class,' javaClass/javaClassAccess '         ) or
      contains(@class,' javaInterface/javaInterfaceAccess ' ) or
      contains(@class,' javaMethod/javaMethodAccess '       ) or
      contains(@class,' javaMethod/javaFinalMethod '        ) or
      contains(@class,' javaMethod/javaAbstractMethod '     ) or
      contains(@class,' javaMethod/javaStaticMethod '       ) or
      contains(@class,' javaMethod/javaNativeMethod '       ) or
      contains(@class,' javaMethod/javaSynchronizedMethod ' ) or
      contains(@class,' javaMethod/javaVoid '               ) or
      contains(@class,' javaMethod/javaMethodPrimitive '    ) or
      contains(@class,' javaField/javaFieldAccess '         ) or
      contains(@class,' javaField/javaFinalField '          ) or
      contains(@class,' javaField/javaStaticField '         ) or
      contains(@class,' javaField/javaTransientField '      ) or
      contains(@class,' javaField/javaVolatileField '       ) or
      contains(@class,' javaField/javaFieldPrimitive '      )]">
  <xsl:param name="spanClass" select="local-name(.)"/>
  <xsl:if test="preceding-sibling::*[
      contains(@class,' javaClass/javaFinalClass '          ) or
      contains(@class,' javaClass/javaAbstractClass '       ) or
      contains(@class,' javaClass/javaStaticClass '         ) or
      contains(@class,' javaClass/javaClassAccess '         ) or
      contains(@class,' javaInterface/javaInterfaceAccess ' ) or
      contains(@class,' javaMethod/javaMethodAccess '       ) or
      contains(@class,' javaMethod/javaFinalMethod '        ) or
      contains(@class,' javaMethod/javaAbstractMethod '     ) or
      contains(@class,' javaMethod/javaStaticMethod '       ) or
      contains(@class,' javaMethod/javaNativeMethod '       ) or
      contains(@class,' javaMethod/javaSynchronizedMethod ' ) or
      contains(@class,' javaField/javaFieldAccess '         ) or
      contains(@class,' javaField/javaFinalField '          ) or
      contains(@class,' javaField/javaStaticField '         ) or
      contains(@class,' javaField/javaTransientField '      ) or
      contains(@class,' javaField/javaVolatileField '       ) or
      contains(@class,' javaField/javaFieldPrimitive '      )][1]">
    <xsl:text> </xsl:text>
  </xsl:if>
  <xsl:apply-templates select="@value" mode="span">
    <xsl:with-param name="spanClass" select="concat($spanClass,'_',@name)"/>
  </xsl:apply-templates>
</xsl:template>

<!-- RDA: copied in gen-topic. The related-links element is processed
          in the topic body, so it should not also be processed by gen-topic. -->
<xsl:template match="*[contains(@class,' javaMethod/javaMethod ')]"
      mode="memberDetail">
  <div class="nested0">
    <!--<xsl:call-template name="gen-topic"/>-->
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setidaname"/>
    <xsl:call-template name="gen-toc-id"/>
    <xsl:call-template name="flagit"/>
    <xsl:call-template name="start-revflag"/>
    <xsl:apply-templates select="*[not(contains(@class,' topic/related-links '))]|comment()|processing-instruction()"/>
    <xsl:call-template name="end-revflag"/>
  </div>
  <xsl:value-of select="$newline"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - General API processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- Removed for P019897 -->
<!--<xsl:template match="*[contains(@class,' apiRef/apiRelation ') and 
      not(@href)]">
  <span class="{@class}">
    <xsl:apply-templates/>
  </span>
</xsl:template>-->

<xsl:template match="*[contains(@class,' topic/xref ') and 
      contains(@class,' javaapi-d/') and 
      not(@href)]">
  <span class="{@class}">
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="*[(contains(@class,' apiRef/apiQualifier ') or
      contains(@class,' apiRef/apiType ')) and not(
	  contains(@class,' javaClass/') or
	  contains(@class,' javaInterface/') or
	  contains(@class,' javaMethod/') or
	  contains(@class,' javaField/'))]">
  <xsl:param name="spanClass" select="local-name(.)"/>
  <span class="{@class}">
    <xsl:apply-templates select="@name" mode="span">
      <xsl:with-param name="spanClass"
          select="concat($spanClass,'_valuename')"/>
    </xsl:apply-templates>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'ColonSymbol'"/>
    </xsl:call-template>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="@value" mode="span">
      <xsl:with-param name="spanClass" select="concat($spanClass,'_',@name)"/>
    </xsl:apply-templates>
  </span>
</xsl:template>

<!-- P019897: added condition for java ancestors -->
<xsl:template match="*[contains(@class,' apiRef/apiArray ')]
                      [ancestor::*[contains(@class,' javaClass/') or contains(@class,' javaInterface/') or contains(@class,' javaField/') or contains(@class,' javaMethod/') or contains(@class,' javaPackage/')]]">
  <xsl:text>[</xsl:text>
    <xsl:apply-templates select="*[contains(@class,' apiRef/apiArraySize ')]"/>
  <xsl:text>]</xsl:text>
  <xsl:apply-templates select="*[contains(@class,' apiRef/apiArray ')]"/>
</xsl:template>

<!-- P019897: added condition for java ancestors -->
<xsl:template match="*[contains(@class,' apiRef/apiDesc ')]
                      [ancestor::*[contains(@class,' javaClass/') or contains(@class,' javaInterface/') or contains(@class,' javaField/') or contains(@class,' javaMethod/') or contains(@class,' javaPackage/')]]">
  <div class="apiDescription">
    <xsl:apply-templates/>
  </div>
</xsl:template>

<!-- P019897: added condition for java ancestors -->
<xsl:template match="*[contains(@class,' apiRef/apiDefNote ')]
                      [ancestor::*[contains(@class,' javaClass/') or contains(@class,' javaInterface/') or contains(@class,' javaField/') or contains(@class,' javaMethod/') or contains(@class,' javaPackage/')]]">
  <xsl:param name="insertText"/>
  <div class="{@class}">
    <xsl:if test="$insertText">
      <xsl:value-of select="$insertText"/>
    </xsl:if>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<!-- Removed for P019897 -->
<!--<xsl:template match="*[contains(@class,' apiRef/apiSyntax ')]">
  <pre class="{@class}">
    <xsl:apply-templates/>
  </pre>
</xsl:template>-->

<!-- P019897: added condition for java ancestors -->
<xsl:template match="*[contains(@class,' apiRef/apiItemName ' )]
                      [ancestor::*[contains(@class,' javaClass/') or contains(@class,' javaInterface/') or contains(@class,' javaField/') or contains(@class,' javaMethod/') or contains(@class,' javaPackage/')]]">
  <xsl:apply-templates select="*|text()"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - General topic rules
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- specifics needed for precedence -->
<xsl:template match="*|*[contains(@class,' topic/title ')]" mode="div">
  <div class="{@class}">
    <xsl:apply-templates mode="default"/>
  </div>
</xsl:template>

<xsl:template match="*|*[contains(@class,' topic/title ')]" mode="span">
  <span class="{@class}">
    <xsl:apply-templates mode="default"/>
  </span>
</xsl:template>

<xsl:template match="@*" mode="span">
  <xsl:param name="spanClass"
      select="concat(local-name(..),'_',local-name(.))"/>
  <span class="{$spanClass}">
    <xsl:value-of select="."/>
  </span>
</xsl:template>

<!--<xsl:template match="node()" mode="default">
  <xsl:apply-imports/>
</xsl:template>-->


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Filename manipulation
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- adapted from topicpull.xsl - should be shared utilities -->
<xsl:template name="getTopicFile">
  <xsl:param name="href" select="@href"/>
  <xsl:choose>
  <xsl:when test="contains($href,'://') and contains($href,'#')">
    <xsl:value-of select="substring-before($href,'#')"/>
  </xsl:when>
  <xsl:when test="contains($href,'://')">
    <xsl:value-of select="$href"/>
  </xsl:when>
  <xsl:when test="contains($href,'#')">
    <xsl:value-of select="$WORKDIR"/>
    <xsl:value-of select="substring-before($href,'#')"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="$WORKDIR"/>
    <xsl:value-of select="$href"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="getTopicID">
  <xsl:param name="href" select="@href"/>
  <xsl:choose>
  <xsl:when test="contains($href,'#') and contains(substring-after($href,'#'),'/')">
    <xsl:value-of select="substring-before(substring-after($href,'#'),'/')"/>
  </xsl:when>
  <xsl:when test="contains($href,'#')">
    <xsl:value-of select="substring-after($href,'#')"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:text>#none#</xsl:text>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="getBaseFile">
  <xsl:param name="file"/>
  <xsl:choose>
  <xsl:when test="not($file)"/>
  <xsl:when test="contains($file,'\')">
    <xsl:call-template name="getBaseFile">
      <xsl:with-param name="file" select="substring-after($file,'\')"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="contains($file,'/')">
    <xsl:call-template name="getBaseFile">
      <xsl:with-param name="file" select="substring-after($file,'/')"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="$file"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="getFileRoot">
  <xsl:param name="fileroot"/>
  <xsl:param name="file"/>
  <xsl:choose>
  <xsl:when test="$file and contains($file,'.')">
    <xsl:variable name="infix" select="substring-before($file,'.')"/>
    <xsl:variable name="newroot">
      <xsl:choose>
      <xsl:when test="$fileroot and $infix">
        <xsl:value-of select="$fileroot"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="$infix"/>
      </xsl:when>
      <xsl:when test="$infix">
        <xsl:value-of select="$infix"/>
      </xsl:when>
      <xsl:when test="$fileroot">
        <xsl:value-of select="$fileroot"/>
      </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="getFileRoot">
      <xsl:with-param name="fileroot" select="$newroot"/>
      <xsl:with-param name="file"     select="substring-after($file,'.')"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="$fileroot">
    <xsl:value-of select="$fileroot"/>
  </xsl:when>
  <xsl:when test="$file">
    <xsl:value-of select="$file"/>
  </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="getBaseFileRoot">
  <xsl:param name="file"/>
  <xsl:variable name="basefile">
    <xsl:call-template name="getBaseFile">
      <xsl:with-param name="file" select="$file"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:call-template name="getFileRoot">
    <xsl:with-param name="file" select="$basefile"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="getOutputFile">
  <xsl:param name="href"/>
  <xsl:choose>
  <xsl:when test="not($href) or string-length($href) &lt; 1">
    <xsl:message>
      <xsl:text>Empty href for </xsl:text>
      <xsl:value-of select="local-name()"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="@id"/>
    </xsl:message>
  </xsl:when>
  <xsl:when test="contains($href,'.dita')">
    <xsl:value-of select="substring-before($href,'.dita')"/>
  </xsl:when>
  <xsl:when test="contains($href,'.xml')">
    <xsl:value-of select="substring-before($href,'.xml')"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:message>
      <xsl:text>Extension not .dita or .xml for </xsl:text>
      <xsl:value-of select="$href"/>
    </xsl:message>
    <xsl:value-of select="$href"/>
  </xsl:otherwise>
  </xsl:choose>
  <xsl:value-of select="$OUTEXT"/>
</xsl:template>

</xsl:stylesheet>
