<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="common/output-message.xsl"/>

<xsl:output indent="yes"/>

<!-- Define the error message prefix identifier -->
<xsl:variable name="msgprefix">IDXS</xsl:variable>

<xsl:param name="WORKDIR" select="'./'"/>
<xsl:param name="OUTEXT" select="'html'"/>
<xsl:param name="DBG" select="no"/>
<xsl:param name="DITAEXT" select="'.xml'"/>

<xsl:template match="*[contains(@class, ' map/map ')]">
  <!-- add NLS processing instruction -->
  <xsl:text>
</xsl:text><xsl:processing-instruction name="NLS"> TYPE="org.eclipse.help.toc"</xsl:processing-instruction><xsl:text>
</xsl:text>
  <toc>
    <xsl:if test="not(@title)">
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">The title attribute is required for Eclipse output. You need to add a title
attribute to your map.</xsl:with-param>
          <xsl:with-param name="msgnum">043</xsl:with-param>
          <xsl:with-param name="msgsev">W</xsl:with-param>
        </xsl:call-template>
    </xsl:if>
    <xsl:apply-templates select="@title|@anchorref"/>
    <!-- Add @topic to map, using the first @href in the map -->
    <xsl:if test="*[contains(@class, ' map/topicref ')][1]/descendant-or-self::*[@href]">
      <xsl:attribute name="topic">
        <xsl:apply-templates select="*[contains(@class, ' map/topicref ')][1]/descendant-or-self::*[@href][1]" mode="format-href"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates/>
  </toc>
</xsl:template>

<xsl:template match="@title">
	<xsl:attribute name="label"><xsl:value-of select="."/></xsl:attribute>
</xsl:template>

<!-- anchorref must use forward slash, not back slash. Allow
     anchorref to a non-ditamap, but warn if the format is still dita. -->
<xsl:template match="@anchorref">
  <xsl:variable name="fix-anchorref">
    <xsl:value-of select="translate(.,
                           '\/=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
                           '//=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
  </xsl:variable>
  <xsl:attribute name="link_to">
    <xsl:choose>
      <xsl:when test="contains($fix-anchorref,'.ditamap')">
	<xsl:value-of select="substring-before($fix-anchorref,'.ditamap')"/>.xml<xsl:value-of select="substring-after($fix-anchorref,'.ditamap')"/>
      </xsl:when>
      <xsl:when test="contains($fix-anchorref,'.xml')"><xsl:value-of select="$fix-anchorref"/></xsl:when>
      <xsl:otherwise> <!-- should be dita, but name does not include .ditamap -->
        <!-- use the for-each so that the message scope is the map element, not the attribute -->
        <xsl:for-each select="parent::*">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msg">The anchorref attribute should either point to another dita map, or
to an Eclipse XML file. The value <xsl:value-of select="@anchorref"/> does not point to either.</xsl:with-param>
            <xsl:with-param name="msgnum">042</xsl:with-param>
            <xsl:with-param name="msgsev">I</xsl:with-param>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:value-of select="$fix-anchorref"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:template>

<!-- Format @href for the title attribute on the map element -->
<xsl:template match="*" mode="format-href">
  <xsl:choose>
    <xsl:when test="@type='external' or (@scope='external' and not(@format)) or not(not(@format) or @format=substring-after($DITAEXT,'.'))"><xsl:value-of select="@href"/></xsl:when> <!-- adding local -->
    <xsl:when test="starts-with(@href,'#')"><xsl:value-of select="@href"/></xsl:when>
    <xsl:when test="contains(@copy-to, $DITAEXT)">
      <xsl:value-of select="substring-before(@copy-to,$DITAEXT)"/>.<xsl:value-of select="$OUTEXT"/>
    </xsl:when>
    <xsl:when test="contains(@href, $DITAEXT)">
      <xsl:value-of select="substring-before(@href, $DITAEXT)"/>.<xsl:value-of select="$OUTEXT"/><xsl:value-of select="substring-after(@href, $DITAEXT)"/>
    </xsl:when>
    <!-- If it is a bad value, there will be a message when doing the real topic link -->
    <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Make the same changes for navref/@mapref that were made for @anchorref. -->
<xsl:template match="*[contains(@class, ' map/navref ')]/@mapref">
  <xsl:variable name="fix-mapref">
    <xsl:value-of select="translate(.,
                           '\/=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
                           '//=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
  </xsl:variable>
  <xsl:attribute name="toc">
    <xsl:choose>
      <xsl:when test="contains($fix-mapref,'.ditamap')"><xsl:value-of select="substring-before($fix-mapref,'.ditamap')"/>.xml</xsl:when>
      <xsl:when test="contains($fix-mapref,'.xml')"><xsl:value-of select="$fix-mapref"/></xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="parent::*">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msg">The navref element should either point to another dita map, or
to an Eclipse XML file. The value <xsl:value-of select="@mapref"/> does not point to either.</xsl:with-param>
            <xsl:with-param name="msgnum">042</xsl:with-param>
            <xsl:with-param name="msgsev">I</xsl:with-param>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:value-of select="$fix-mapref"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:template>

<xsl:template match="*[contains(@class, ' map/navref ')]">
  <xsl:choose>
    <xsl:when test="@mapref">
      <link><xsl:apply-templates select="@mapref"/></link>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">The navref element should either point to another dita map, or
to an Eclipse XML file. Found a navref that does not point to anything.</xsl:with-param>
        <xsl:with-param name="msgnum">046</xsl:with-param>
        <xsl:with-param name="msgsev">I</xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class, ' map/anchor ')]">
	<anchor id="{@id}"/>
</xsl:template>

<!-- If the topicref is a "topicgroup", or some other topicref that does not point
     to a file or have link text, then just move on to children. -->
<xsl:template match="*[contains(@class, ' map/topicref ')][not(@toc='no')]">
  <xsl:choose>
    <xsl:when test="contains(@class, ' mapgroup/topicgroup ')">
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:when test="not(@href) and not(@navtitle) and 
                    not(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')])">
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
	<topic>
		<xsl:attribute name="label">
			<xsl:choose>
				<xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
				<xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"><xsl:value-of select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"/></xsl:when>
				<xsl:otherwise>
                    			<xsl:choose>
		                         <xsl:when test="@type='external' or not(not(@format) or @format=substring-after($DITAEXT,'.'))"><xsl:value-of select="@href"/></xsl:when> <!-- adding local -->
                		         <xsl:when test="starts-with(@href,'#')"><xsl:value-of select="@href"/></xsl:when>
		                         <xsl:when test="contains(@copy-to, $DITAEXT)">
                		              <xsl:value-of select="substring-before(@copy-to, $DITAEXT)"/>.<xsl:value-of select="$OUTEXT"/>
		                         </xsl:when>
		                         <xsl:when test="contains(@href, $DITAEXT)">
                		              <xsl:value-of select="substring-before(@href, $DITAEXT)"/>.<xsl:value-of select="$OUTEXT"/><xsl:value-of select="substring-after(@href, $DITAEXT)"/>
		                         </xsl:when>
                                         <xsl:when test="not(@href) or @href=''"/> <!-- P017000: error generated in prior step -->
                		         <xsl:otherwise><xsl:value-of select="@href"/><xsl:call-template name="output-message">
                       <xsl:with-param name="msg">Unable to find navigation title, using href instead: <xsl:value-of select="@href"/>.
If the topic is not accessible at build time, provide the navigation title in the map, and set the format or scope attributes to indicate why it is not accessible.</xsl:with-param>
                       <xsl:with-param name="msgnum">017</xsl:with-param>
                       <xsl:with-param name="msgsev">E</xsl:with-param>
                     </xsl:call-template></xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
		<xsl:if test="@href and @href!=''">
                  <xsl:attribute name="href">
                    <xsl:choose>
                      <xsl:when test="@type='external' or (@scope='external' and not(@format)) or not(not(@format) or @format=substring-after($DITAEXT,'.'))"><xsl:value-of select="@href"/></xsl:when> <!-- adding local -->
                      <xsl:when test="starts-with(@href,'#')"><xsl:value-of select="@href"/></xsl:when>
                      <xsl:when test="contains(@copy-to, $DITAEXT)">
                        <xsl:value-of select="substring-before(@copy-to, $DITAEXT)"/>.<xsl:value-of select="$OUTEXT"/>
                      </xsl:when>
                      <xsl:when test="contains(@href, $DITAEXT)">
                        <xsl:value-of select="substring-before(@href, $DITAEXT)"/>.<xsl:value-of select="$OUTEXT"/><xsl:value-of select="substring-after(@href, $DITAEXT)"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="@href"/>
                        <xsl:call-template name="output-message">
                          <xsl:with-param name="msg">Unknown file extension in href: <xsl:value-of select="@href"/> 
If this is a link to a non-DITA resource, set the format attribute to match the resource (for example, 'txt', 'pdf', or 'html'). 
If it's a link to a DITA resource, the file extension must be $DITAEXT .</xsl:with-param>
                          <xsl:with-param name="msgnum">015</xsl:with-param>
                          <xsl:with-param name="msgsev">E</xsl:with-param>
                        </xsl:call-template>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:attribute>
		</xsl:if>
		<xsl:apply-templates/>
	</topic>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--makes sure that any literal text in topicmeta does not get output as literal text in the output TOC file, which should only have text in attributes, as pulled in by the topicref template-->
<xsl:template match="text()">
	<xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
