<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->
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
  xmlns:prodtools="http://www.ibm.com/xmlns/prodtools" xmlns:random="org.dita.dost.util.RandomUtils" exclude-result-prefixes="random"
  version="1.1">
  
  
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/related-links.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-textonly.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/flag.xsl"/>
  <!--
  <xsl:import href="xslodt/flag-old.xsl"/>
  -->
  <xsl:import href="xslodt/dita2odt-utilities.xsl"/>
  <xsl:import href="xslodt/dita2odt-table.xsl"/>
  <xsl:import href="xslodt/dita2odt-lists.xsl"/>
  <xsl:import href="xslodt/dita2odt-img.xsl"/>
  
  <xsl:import href="xslodt/dita2odt-map.xsl"/>
  <xsl:import href="xslodt/dita2odtImpl.xsl"/>
  <xsl:import href="xslodt/dita2odt-concept.xsl"/>
  <xsl:import href="xslodt/dita2odt-gloss.xsl"/>
  <xsl:import href="xslodt/dita2odt-task.xsl"/>
  <xsl:import href="xslodt/dita2odt-reference.xsl"/>
  <xsl:import href="xslodt/hi-d.xsl"/>
  <xsl:import href="xslodt/pr-d.xsl"/>
  <xsl:import href="xslodt/sw-d.xsl"/>
  <xsl:import href="xslodt/ui-d.xsl"/>
  <!--
  <xsl:import href="xslodt/common/vars.xsl"/>
  -->
  <xsl:import href="xslodt/commons.xsl"/>
  <xsl:import href="xslodt/dita2odt-links.xsl"/>

  <xsl:import href="xslodt/dita2odt-flagging.xsl"/>

  <!-- 
  <xsl:include href="xslodt/dita2odt-relinks.xsl"/>
  -->
  
  
<dita:extension id="dita.xsl.odt" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

<!-- =========== DEFAULT VALUES FOR EXTERNALLY MODIFIABLE PARAMETERS =========== -->
<xsl:param name="include.rellinks"/>
<xsl:param name="DRAFT" select="'no'"/>
<xsl:param name="OUTPUTDIR" select="''"/>
<xsl:param name="FILTERFILE"/>
<!-- default "hide index entries" processing parameter ('no' = hide them)-->
<xsl:param name="INDEXSHOW" select="'no'"/><!-- "no" and "yes" are valid values; non-'yes' is ignored -->

<xsl:param name="FILEREF">file:///</xsl:param>
  
<!-- Transform type, such as 'xhtml', 'htmlhelp', or 'eclipsehelp' -->
<xsl:param name="TRANSTYPE" select="'odt'"/>

<!-- for now, disable breadcrumbs pending link group descision -->
<xsl:param name="BREADCRUMBS" select="'no'"/> <!-- "no" and "yes" are valid values; non-'yes' is ignored -->

<!-- the year for the copyright -->
<xsl:param name="YEAR" select="'2010'"/>

<!-- the file name (file name and extension only - no path) of the document being transformed.
  Needed to help with debugging.
  default is 'myfile.xml')-->

<xsl:param name="FILENAME"/>
<xsl:param name="FILEDIR"/>
<xsl:param name="CURRENTFILE">
  <xsl:choose>
    <!-- ditamap -->
    <xsl:when test="contains($FILENAME, '_MERGED')">
      <xsl:value-of select="concat($FILEDIR, '/', substring-before($FILENAME, '_MERGED'), '.ditamap')"/>
    </xsl:when>
    <!-- topic -->
    <xsl:otherwise>
      <xsl:value-of select="concat($FILEDIR, '/', $FILENAME)"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:param>
  

<!-- Debug mode - enables XSL debugging xsl-messages.
  Needed to help with debugging.
  default is 'no')-->
<xsl:param name="DBG" select="'no'"/> <!-- "no" and "yes" are valid values; non-'yes' is ignored -->
<!--embedding images as binary data -->
<xsl:param name="ODTIMGEMBED" select="'yes'"/>

<!-- DITAEXT file extension name of dita topic file -->
<xsl:param name="DITAEXT" select="'.xml'"/>

<!-- Name of the keyref file that contains key definitions -->
<xsl:param name="KEYREF-FILE" select="concat($WORKDIR,$PATH2PROJ,'keydef.xml')"/>

<xsl:param name="BASEDIR"/>

<xsl:param name="TEMPDIR"/>
  
<xsl:param name="includeRelatedLinkRoles" select="concat(' ', normalize-space($include.rellinks), ' ')"/>
  
<xsl:variable name="tempfiledir">
  <xsl:choose>
    <xsl:when test="contains($TEMPDIR, ':\') or contains($TEMPDIR, ':/')">
      <!--xsl:value-of select="concat($FILEREF,'/')"/-->
      <xsl:value-of select="'file:/'"/><xsl:value-of select="concat($TEMPDIR, '/')"/>
    </xsl:when>
    <xsl:when test="starts-with($TEMPDIR, '/')">
      <xsl:value-of select="'file://'"/><xsl:value-of select="concat($TEMPDIR, '/')"/>
    </xsl:when>
    <xsl:when test="starts-with($BASEDIR, '/')">
      <xsl:value-of select="'file://'"/><xsl:value-of select="concat($BASEDIR, '/')"/><xsl:value-of select="concat($TEMPDIR, '/')"/>
    </xsl:when>
    <xsl:otherwise>
      <!--xsl:value-of select="concat($FILEREF,'/')"/-->
      <xsl:value-of select="'file:/'"/><xsl:value-of select="concat($BASEDIR, '/')"/><xsl:value-of select="concat($TEMPDIR, '/')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>
  
<!-- current file with path -->
<xsl:variable name="currentfile" select="concat($tempfiledir, $CURRENTFILE)"/>
<!-- the working directory that contains the document being transformed.
  Needed as a directory prefix for the @conref "document()" function calls.
  default is '../doc/')-->
<xsl:variable name="WORKDIR">
  <xsl:apply-templates select="document($currentfile, /)/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
</xsl:variable>

<!-- the path back to the project. Used for c.gif, delta.gif, css to allow user's to have
  these files in 1 location. -->
<xsl:variable name="PATH2PROJ">
  <xsl:apply-templates select="document($currentfile, /)/processing-instruction('path2project-uri')[1]" mode="get-path2project"/>
</xsl:variable>


<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

  <xsl:attribute-set name="root">
    <xsl:attribute name="office:version">1.1</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:template match="/">
    <office:document-content xsl:use-attribute-sets="root">
      <xsl:call-template name="root"/>
    </office:document-content>
  </xsl:template>
  
  <xsl:template name="root">
    <!--xsl:call-template name="gen-list-table"/-->
    <office:scripts/>
    <office:automatic-styles>
      <xsl:if
        test="//*[contains(@class, ' topic/table ')]|//*[contains(@class, ' topic/simpletable ')]">
          <xsl:call-template name="create_table_cell_styles"/>
      </xsl:if>
      
      <xsl:apply-templates select="//text()|//*[contains(@class, ' topic/state ')]" mode="create_hi_style"/>
      
      <xsl:call-template name="create_flagging_styles"/>
      
    </office:automatic-styles>
    <office:body>
      <office:text>
        <text:sequence-decls>
          <text:sequence-decl text:display-outline-level="0" text:name="Illustration"/>
          <text:sequence-decl text:display-outline-level="0" text:name="Table"/>
          <text:sequence-decl text:display-outline-level="0" text:name="Text"/>
          <text:sequence-decl text:display-outline-level="0" text:name="Drawing"/>
        </text:sequence-decls>
        
        
        
        
        <xsl:choose>
          <!-- bookmap -->
          <xsl:when test="$mapType = 'bookmap'">
            <xsl:call-template name="create_book_title"/>
            
            <xsl:call-template name="create_book_abstract"/>
            
            <xsl:call-template name="create_book_notices"/>
          </xsl:when>
          <!-- normal map -->
          <xsl:when test="$mapType = 'ditamap'">
            <xsl:call-template name="create_map_title"/>
          </xsl:when>
          <!-- topic -->
          <xsl:otherwise>
            <xsl:call-template name="create_topic_title"/>
          </xsl:otherwise>
          
        </xsl:choose>
        
        
        <xsl:call-template name="create_toc"/>
        
        
        <xsl:apply-templates/>
      </office:text>
    </office:body>
  </xsl:template>
  
</xsl:stylesheet>
