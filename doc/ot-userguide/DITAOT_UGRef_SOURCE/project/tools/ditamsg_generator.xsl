<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" encoding="ISO-8859-1" indent="yes"
     doctype-public="-//OASIS//DTD DITA Reference//EN" 
     doctype-system="http://docs.oasis-open.org/dita/v1.0.1/dtd/reference.dtd"/>

<!-- MESSAGEAUTODOC.XSL -->
<!-- Stylesheet to convert messages.xml to a DITA reference topic messages.dita -->
<!-- Author: Richard Johnson -->
<!-- Copyright 2006 VR Communications, Inc. All rights reserved. -->
<!-- This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
     See the accompanying license.txt file for applicable licenses. -->


<xsl:template match="//messages">


<reference id="messages">
<title>DITA Open Toolkit Messages</title>
<refbody>

<!-- put all the Ant messages in a simple table -->
<section id="ant">
<title>Ant messages</title>
<p></p>

<simpletable>
<sthead>
<stentry>Message number</stentry>
<stentry>Type</stentry>
<stentry>Message text</stentry>
<stentry>Action</stentry>
</sthead>

<xsl:apply-templates select="message[substring(@id,1,4)='DOTA']" />

</simpletable>
</section>

<!-- put all the Java messages in a simple table -->
<section id="java">
<title>Java messages</title>
<p></p>

<simpletable>
<sthead>
<stentry>Message number</stentry>
<stentry>Type</stentry>
<stentry>Message text</stentry>
<stentry>Action</stentry>
</sthead>

<xsl:apply-templates select="message[substring(@id,1,4)='DOTJ']" />

</simpletable>
</section>

<!-- put all the XSLT messages in a simple table -->
<section id="xslt">
<title>XSLT messages</title>
<p></p>

<simpletable>
<sthead>
<stentry>Message number</stentry>
<stentry>Type</stentry>
<stentry>Message text</stentry>
<stentry>Action</stentry>
</sthead>

<xsl:apply-templates select="message[substring(@id,1,4)='DOTX']" />

</simpletable>
</section>

</refbody>
</reference>

</xsl:template>

<!-- Reformat an individual message -->
<xsl:template match="message">
 
 <strow>
 <stentry>
 <msgnum>
 <xsl:apply-templates select="@id" /></msgnum>
  </stentry>
 <stentry>
 <xsl:apply-templates select="@type" />
  </stentry>
<stentry>
 <msgph>
 <xsl:apply-templates select="reason" /></msgph>
  </stentry>
<stentry>
 <xsl:apply-templates select="response" />
  </stentry>

 </strow>

</xsl:template>


<xsl:template match="description">
      <p>
        <td><xsl:value-of select="."/></td>
      </p>
</xsl:template>

<xsl:template match="link">
 <a>
        <xsl:attribute name="href">
         <xsl:value-of select="."/>
        </xsl:attribute>
       Item link</a>
</xsl:template>

</xsl:stylesheet>
