<?xml version='1.0'?>

<!-- 20170503 SCH: Add support for troubleshooting elements. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
    exclude-result-prefixes="opentopic opentopic-index dita2xslfo"
    version="2.0">

    <!-- Determines whether to generate titles for task sections. Values are YES and NO. -->
    <xsl:param name="GENERATE-TASK-LABELS">
        <xsl:choose>
            <xsl:when test="$antArgsGenerateTaskLabels='YES'"><xsl:value-of select="$antArgsGenerateTaskLabels"/></xsl:when>
            <xsl:otherwise>NO</xsl:otherwise>
        </xsl:choose>
    </xsl:param>
  
    <xsl:template match="*[contains(@class, ' troubleshooting/troubleshooting ')]" mode="processTopic"
                name="processTroubleshooting">
    <fo:block xsl:use-attribute-sets="task">
      <xsl:apply-templates select="." mode="commonTopicProcessing"/>
    </fo:block>
  </xsl:template>
  
  <!-- Deprecated, retained for backwards compatibility -->
  <xsl:template match="*" mode="processTroubleshooting">
    <xsl:call-template name="processTroubleshooting"/>
  </xsl:template>
  
    <xsl:template match="*[contains(@class, ' troubleshooting/troublebody ')]">
        <fo:block xsl:use-attribute-sets="troublebody">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>
    
    <xsl:template match="*[contains(@class, ' troubleshooting/troubleSolution ')]">
        <fo:block xsl:use-attribute-sets="troubleSolution">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' troubleshooting/condition ')]">
        <fo:block xsl:use-attribute-sets="condition">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="." mode="dita2xslfo:task-heading">
                  <xsl:with-param name="use-label">
                    <xsl:apply-templates select="." mode="dita2xslfo:retrieve-task-heading">
                        <xsl:with-param name="pdf2-string">Troubleshooting Cause</xsl:with-param>
                      <xsl:with-param name="common-string">trouble_condition</xsl:with-param>
                    </xsl:apply-templates>
                </xsl:with-param>
            </xsl:apply-templates>
            <fo:block xsl:use-attribute-sets="condition__content">
              <xsl:apply-templates/>
            </fo:block>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' troubleshooting/cause ')]">
        <fo:block xsl:use-attribute-sets="cause">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="." mode="dita2xslfo:task-heading">
                <xsl:with-param name="use-label">
                    <xsl:apply-templates select="." mode="dita2xslfo:retrieve-task-heading">
                        <xsl:with-param name="pdf2-string">Troubleshooting Cause</xsl:with-param>
                        <xsl:with-param name="common-string">trouble_cause</xsl:with-param>
                    </xsl:apply-templates>
                </xsl:with-param>
            </xsl:apply-templates>
            <fo:block xsl:use-attribute-sets="cause__content">
              <xsl:apply-templates/>
            </fo:block>
        </fo:block>
    </xsl:template>   
    
    <xsl:template match="*[contains(@class, ' troubleshooting/remedy ')]">
        <fo:block xsl:use-attribute-sets="remedy">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="." mode="dita2xslfo:task-heading">
                <xsl:with-param name="use-label">
                    <xsl:apply-templates select="." mode="dita2xslfo:retrieve-task-heading">
                        <xsl:with-param name="pdf2-string">Troubleshooting Remedy</xsl:with-param>
                        <xsl:with-param name="common-string">trouble_remedy</xsl:with-param>
                    </xsl:apply-templates>
                </xsl:with-param>
            </xsl:apply-templates>
            <fo:block xsl:use-attribute-sets="remedy__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:block>
    </xsl:template>
    
    <xsl:template match="*[contains(@class, ' troubleshooting/responsibleParty ')]">
        <fo:block xsl:use-attribute-sets="responsibleParty">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="." mode="dita2xslfo:task-heading">
                <xsl:with-param name="use-label">
                    <xsl:apply-templates select="." mode="dita2xslfo:retrieve-task-heading">
                        <xsl:with-param name="pdf2-string">Troubleshooting responsibleParty</xsl:with-param>
                        <xsl:with-param name="common-string">trouble_responsibleParty</xsl:with-param>
                    </xsl:apply-templates>
                </xsl:with-param>
            </xsl:apply-templates>
            <fo:block xsl:use-attribute-sets="responsibleParty__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/steptroubleshooting ')]">
        <xsl:call-template name="commonattributes"/>
        <xsl:apply-templates select="." mode="dita2xslfo:task-heading">
            <xsl:with-param name="use-label">
                <xsl:apply-templates select="." mode="dita2xslfo:retrieve-task-heading">
                    <xsl:with-param name="pdf2-string">Troubleshooting Step</xsl:with-param>
                    <xsl:with-param name="common-string">trouble_step</xsl:with-param>
                </xsl:apply-templates>
            </xsl:with-param>
        </xsl:apply-templates>
        <fo:block xsl:use-attribute-sets="steptroubleshooting">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/tasktroubleshooting ')]">
        <fo:block xsl:use-attribute-sets="tasktroubleshooting">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="." mode="dita2xslfo:task-heading">
                <xsl:with-param name="use-label">
                    <xsl:apply-templates select="." mode="dita2xslfo:retrieve-task-heading">
                        <xsl:with-param name="pdf2-string">Troubleshooting Task</xsl:with-param>
                        <xsl:with-param name="common-string">trouble_task</xsl:with-param>
                    </xsl:apply-templates>
                </xsl:with-param>
            </xsl:apply-templates>
            <fo:block xsl:use-attribute-sets="tasktroubleshooting__content">
              <xsl:apply-templates/>
            </fo:block>
        </fo:block>
    </xsl:template>

    <!-- If example has a title, process it first; otherwise, create default title (if needed) -->
    <xsl:template match="*[contains(@class, ' troubleshooting/troublebody ')]/*[contains(@class, ' topic/example ')]">
        <fo:block xsl:use-attribute-sets="task.example">
            <xsl:call-template name="commonattributes"/>
            <xsl:choose>
              <xsl:when test="*[contains(@class, ' topic/title ')]">
                <xsl:apply-templates select="*[contains(@class, ' topic/title ')]"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="." mode="dita2xslfo:task-heading">
                  <xsl:with-param name="use-label">
                      <xsl:apply-templates select="." mode="dita2xslfo:retrieve-task-heading">
                        <xsl:with-param name="pdf2-string">Task Example</xsl:with-param>
                        <xsl:with-param name="common-string">task_example</xsl:with-param>
                      </xsl:apply-templates>
                  </xsl:with-param>
                </xsl:apply-templates>
              </xsl:otherwise>
            </xsl:choose>
            <fo:block xsl:use-attribute-sets="task.example__content">
              <xsl:apply-templates select="*[not(contains(@class, ' topic/title '))]|text()|processing-instruction()"/>
            </fo:block>
        </fo:block>
    </xsl:template>

    <xsl:template match="*" mode="dita2xslfo:task-heading">
        <xsl:param name="use-label"/>
        <xsl:if test="$GENERATE-TASK-LABELS='YES'">
            <fo:block xsl:use-attribute-sets="section.title">
                <fo:inline><xsl:copy-of select="$use-label"/></fo:inline>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <!-- Set up to allow string retrieval based on the original PDF2 string;
         if not found, fall back to the common string -->
    <xsl:template match="*" mode="dita2xslfo:retrieve-task-heading">
      <xsl:param name="pdf2-string"/>
      <xsl:param name="common-string"/>
      <xsl:variable name="retrieved-pdf2-string">
        <!-- By default, will return the lookup value -->
        <xsl:call-template name="getVariable">
            <xsl:with-param name="id" select="$pdf2-string"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$retrieved-pdf2-string!=$pdf2-string and $retrieved-pdf2-string!=''">
          <xsl:value-of select="$retrieved-pdf2-string"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="$common-string"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
