<?xml version="1.0" encoding="utf-8"?><!-- This file is part of the DITA Open Toolkit project hosted on      Sourceforge.net. See the accompanying license.txt file in the     main toolkit package for applicable licenses.--><!-- Copyright IBM Corporation 2010. All Rights Reserved. --><!-- Originally created 2006 by Robert D. Anderson -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="UTF-8" method="html"/>

  <xsl:variable name="hasGenre">
    <xsl:choose>
      <xsl:when test="/*/*/*/*/*[contains(@class,' songCollection/genre ')]">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="hasRating">
    <xsl:choose>
      <xsl:when test="/*/*/*/*/*[contains(@class,' songCollection/rating ')]">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="hasCount">
    <xsl:choose>
      <xsl:when test="/*/*/*/*/*[contains(@class,' songCollection/count ')]">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="hasDate">
    <xsl:choose>
      <xsl:when test="/*/*/*/*/*[contains(@class,' songCollection/playdate ')]">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>



  <xsl:template match="/">
    <html>
      <head><meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
        <title><xsl:value-of select="/*/*[contains(@class,' topic/title ')]"/></title>
        <style> body {margin-left: 2em; font-family: sans-serif, serif; font-size: small}
                table {border:0}
                td {padding: 0em .5em; border:0;}
                th {padding: 0em .5em; border:0; background-color: #ddeeee;}
                table tr.odd,
                table tr.white {background:#fff;}
                table tr.even,
                table tr.gray {background:#ddd;}</style>
      </head>
      <body>
        <xsl:apply-templates select="*"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/title ')]">
    <h1><xsl:apply-templates/></h1>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/body ')]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' songCollection/songList ')]">
    <table border="1">
      <tr>
        <th>Song</th>
        <th>Album</th>
        <th>Artist</th>
        <xsl:if test="$hasGenre='yes'"><th>Genre</th></xsl:if>
        <xsl:if test="$hasRating='yes'"><th>Rating</th></xsl:if>
        <xsl:if test="$hasCount='yes'"><th>Play count</th></xsl:if>
        <xsl:if test="$hasDate='yes'"><th>Last played</th></xsl:if>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="*[contains(@class,' songCollection/songRow ')]">
    <tr>
      <xsl:choose>
        <xsl:when test="count(preceding-sibling::*) mod 2 = 0"><xsl:attribute name="class">odd</xsl:attribute></xsl:when>
        <xsl:otherwise><xsl:attribute name="class">even</xsl:attribute></xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="*[contains(@class,' songCollection/song ')]"/>
      <xsl:apply-templates select="*[contains(@class,' songCollection/album ')]"/>
      <xsl:apply-templates select="*[contains(@class,' songCollection/artist ')]"/>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' songCollection/genre ')]">
          <xsl:apply-templates select="*[contains(@class,' songCollection/genre ')]"/>
        </xsl:when>
        <xsl:when test="$hasGenre='yes'"><td>&#x00A0;</td></xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' songCollection/rating ')]">
          <xsl:apply-templates select="*[contains(@class,' songCollection/rating ')]"/>
        </xsl:when>
        <xsl:when test="$hasRating='yes'"><td>&#x00A0;</td></xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' songCollection/count ')]">
          <xsl:apply-templates select="*[contains(@class,' songCollection/count ')]"/>
        </xsl:when>
        <xsl:when test="$hasCount='yes'"><td>&#x00A0;</td></xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' songCollection/playdate ')]">
          <xsl:apply-templates select="*[contains(@class,' songCollection/playdate ')]"/>
        </xsl:when>
        <xsl:when test="$hasDate='yes'"><td>&#x00A0;</td></xsl:when>
      </xsl:choose>
    </tr>
  </xsl:template>

  <xsl:template match="*[contains(@class,' songCollection/songRow ')]/*">
    <td><xsl:apply-templates/></td>
  </xsl:template>

</xsl:stylesheet>
