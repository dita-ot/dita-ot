<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  
 | DITA domains support for the demo set; extend as needed
 |
 *-->


<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:variable name="eNoteStringFile" select="document('enote_strings.xml')"/>

<xsl:template name="get-recipients_list">
  <xsl:param name="recipients"/>
    <xsl:choose>
      <xsl:when test="count($recipients)=1">
        <xsl:value-of select="$recipients"/>
      </xsl:when>
      <xsl:when test="count($recipients)>1">
        <select name="recipients">
          <xsl:for-each select="$recipients">
            <option value="position()"><xsl:value-of select="."/></option>
          </xsl:for-each>
        </select>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
</xsl:template>

<xsl:template name="get-recipients_href">
  <xsl:param name="recipients"/>
  <xsl:for-each select="$recipients">
    <xsl:value-of select="@href"/>
      <xsl:choose>
        <xsl:when test="position() = last()"/>
        <xsl:otherwise>,</xsl:otherwise> 
      </xsl:choose>
  </xsl:for-each>
</xsl:template>

<xsl:template name="get-recipients_names">
  <xsl:param name="recipients"/>
  <xsl:for-each select="$recipients">
    <xsl:value-of select="."/>
      <xsl:choose>
        <xsl:when test="position() = last()"/>
        <xsl:otherwise>, </xsl:otherwise> 
      </xsl:choose>
  </xsl:for-each>
</xsl:template>

<xsl:variable name="fromField">
  <xsl:call-template name="get-recipients_href">
    <xsl:with-param name="recipients" select="//*[contains(@class,' enote/From ')]/recipient" />
  </xsl:call-template>
</xsl:variable>

<xsl:variable name="toField">
  <xsl:call-template name="get-recipients_href">
    <xsl:with-param name="recipients" select="//*[contains(@class,' enote/To ')]/recipient" />
  </xsl:call-template>
</xsl:variable>

<xsl:variable name ="ccField">
  <xsl:choose>
    <xsl:when test="//*[contains(@class,' enote/Cc ')]">
      <xsl:call-template name="get-recipients_href">
        <xsl:with-param name="recipients" select="//*[contains(@class,' enote/Cc ')]/recipient" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise/>
  </xsl:choose>
</xsl:variable>

<xsl:variable name ="bccField">
  <xsl:choose>
    <xsl:when test="//*[contains(@class,' enote/Bcc ')]">
      <xsl:call-template name="get-recipients_href">
        <xsl:with-param name="recipients" select="//*[contains(@class,' enote/Bcc ')]/recipient" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise/>
  </xsl:choose>
</xsl:variable>

<xsl:variable name="fromField_names">
  <xsl:call-template name="get-recipients_names">
    <xsl:with-param name="recipients" select="//*[contains(@class,' enote/From ')]/recipient" />
  </xsl:call-template>
</xsl:variable>

<xsl:variable name="toField_names">
  <xsl:call-template name="get-recipients_names">
    <xsl:with-param name="recipients" select="//*[contains(@class,' enote/To ')]/recipient" />
  </xsl:call-template>
</xsl:variable>

<xsl:variable name ="ccField_names">
  <xsl:choose>
    <xsl:when test="//*[contains(@class,' enote/Cc ')]">
      <xsl:call-template name="get-recipients_names">
        <xsl:with-param name="recipients" select="//*[contains(@class,' enote/Cc ')]/recipient" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise/>
  </xsl:choose>
</xsl:variable>

<xsl:variable name ="bccField_names">
  <xsl:choose>
    <xsl:when test="//*[contains(@class,' enote/Bcc ')]">
      <xsl:call-template name="get-recipients_names">
        <xsl:with-param name="recipients" select="//*[contains(@class,' enote/Bcc ')]/recipient" />
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise/>
  </xsl:choose>
</xsl:variable>

<xsl:variable name="subjectField" select="//*[contains(@class,' enote/subject ')]"/>

<xsl:variable name="bodyField">
  &amp;body=<xsl:value-of select="//*[contains(@class,' enote/notebody ')]"/>
</xsl:variable>

<xsl:variable name="mailto_href">
  <xsl:value-of select="concat('mailto:',$toField,'$subject=',$subjectField)"/>
</xsl:variable>

<xsl:template match="*[contains(@class,' enote/noteheader ')]"><!-- originally ul -->
<script type="text/javascript" language="JavaScript"><![CDATA[
function show_enote_info() {
  info_txt = 'Subject: ' + document.noteheader.Subject.value + '\n';
  info_txt += 'From: ' + document.noteheader.From.value + '\n';
  info_txt += 'To: ' + document.noteheader.To.value + '\n';
  info_txt += 'Cc: ' + document.noteheader.Cc.value + '\n';
  info_txt += 'Bcc: ' + document.noteheader.Bcc.value + '\n';
  info_txt += 'Date: ' + document.noteheader.Date.value + '\n';
  info_txt += 'Importance: ' + document.noteheader.Importance.value + '\n';
  info_txt += 'Return Receipt: ' + document.noteheader.ReturnReceipt.checked + '\n';
  alert(info_txt);
}
]]></script>
<div space-before="12pt">
  <form name="noteheader" action="#" onsubmit="javascript:return(false);">
    <table border="0">
      <xsl:call-template name="gen-subject"/>
      <xsl:apply-templates/>
    </table>
    <button type="button" onclick="show_enote_info()">Show info</button>
  </form>
</div>
<hr/>
</xsl:template>

<xsl:template name="gen-subject">
  <tr>
    <td align="right">
      <b>
        <xsl:text>Subject: </xsl:text>
      </b>
    </td>
    <td background="#fafafa">
      <input name="Subject" type="text" size="24" maxlength="24">
        <xsl:attribute name="value">
          <xsl:value-of select="//*[contains(@class,' enote/subject ')]"/>
		</xsl:attribute>
      </input>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' enote/From ')]|*[contains(@class,' enote/To ')]|*[contains(@class,' enote/Cc ')]|*[contains(@class,' enote/Bcc ')]">
  <tr>
    <td align="right">
      <b>
        <xsl:value-of select="name()"/>
        <xsl:text>:</xsl:text>
      </b>
      <xsl:text> </xsl:text>
    </td>
    <td background="#fafafa">
      <input name="{name()}" type="text" size="24" maxlength="24">
        <xsl:attribute name="value">
          <xsl:for-each select="recipient">
            <xsl:value-of select="."/>
            <xsl:choose>
              <xsl:when test="position() = last()"/>
              <xsl:otherwise>, </xsl:otherwise> 
            </xsl:choose>
          </xsl:for-each>
		</xsl:attribute>
      </input>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' enote/Date ')]">
  <tr>
    <td align="right">
      <b>
        <xsl:value-of select="name()"/>
        <xsl:text>:</xsl:text>
      </b>
      <xsl:text> </xsl:text>
    </td>
    <td background="#fafafa">
      <input name="{name()}" type="text" size="24" maxlength="24">
        <xsl:attribute name="value">
            <xsl:value-of select="."/>
		</xsl:attribute>
      </input>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' enote/delivery ')]">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' enote/Priority ')]">
  <tr>
    <td align="right">
      <b>
        <xsl:value-of select="name()"/>
        <xsl:text>:</xsl:text>
      </b>
      <xsl:text> </xsl:text>
    </td>
    <td background="#fafafa">
      <select name="{name()}"	size="3">
        <option value="normal">
          <xsl:if test="@value = 'normal'">
            <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
		  normal
		</option>
        <option value="non-urgent">
          <xsl:if test="@value = 'non-urgent'">
            <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
		  non-urgent
		</option>
        <option value="urgent">
          <xsl:if test="@value = 'urgent'">
            <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
		  urgent
		</option>
      </select>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' enote/Importance ')]">
  <tr>
    <td align="right">
      <b>
        <xsl:value-of select="name()"/>
        <xsl:text>:</xsl:text>
      </b>
      <xsl:text> </xsl:text>
    </td>
    <td background="#fafafa">
      <select name="{name()}"	size="3">
        <option value="normal">
          <xsl:if test="@value = 'normal'">
            <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
		  normal
		</option>
        <option value="low">
          <xsl:if test="@value = 'low'">
            <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
		  low
		</option>
        <option value="high">
          <xsl:if test="@value = 'high'">
            <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
		  high
		</option>
      </select>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' enote/ReturnReceipt ')]|*[contains(@class,' enote/Encrypt ')]">
  <tr>
    <td align="right">
      <b>
        <xsl:value-of select="name()"/>
        <xsl:text>:</xsl:text>
      </b>
      <xsl:text> </xsl:text>
    </td>
    <td background="#fafafa">
      <input type="checkbox" name="{name()}" value="true">
        <xsl:if test="@state = 'yes'">
          <xsl:attribute name="checked">true</xsl:attribute>
        </xsl:if>
      </input>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' enote/references ')]" />

<xsl:template match="*[contains(@class,' enote/recipient ')]" />


</xsl:stylesheet>
