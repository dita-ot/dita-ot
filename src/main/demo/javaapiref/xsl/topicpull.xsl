<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:topicpull="http://dita-ot.sourceforge.net/ns/200704/topicpull"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
                exclude-result-prefixes="topicpull ditamsg">

  <!-- Some elements based on xref and link are allowed to not have href.  Suppress the warning and
       substitute the contained text.  This should be re-evaluated when DITA-OT supports keyref. -->

  <xsl:template match="*[contains(@class, ' javaInterface/javaBaseInterface ')][not(@href)]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' javaClass/javaBaseClass ')][not(@href)]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' javaClass/javaImplemetedInterface ')][not(@href)]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' javaField/javaFieldClass ')][not(@href)]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' javaField/javaFieldInterface ')][not(@href)]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' javaMethod/javaMethodClass ')][not(@href)]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' javaMethod/javaMethodInterface ')][not(@href)]">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
