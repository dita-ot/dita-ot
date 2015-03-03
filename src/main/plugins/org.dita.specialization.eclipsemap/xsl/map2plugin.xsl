<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
  <!--
    | (C) Copyright IBM Corporation 2006. All Rights Reserved.
    *-->
<!-- Need to ensure this comes out with the name "plugin.xml" rather than the default.
     So: use saxon to force the plugin name. -->

<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  
  <xsl:param name="PLUGINFILE" select="'plugin.xml'"/>
  <xsl:param name="DITAMAPEXT" select="'.ditamap'"/>
  <xsl:param name="indexFilename" select="'index.xml'"/>  
  

  <xsl:param name="DEFAULTINDEX" select="''"/>
  <xsl:param name="fragment.country" select="''"/>
  <xsl:param name="fragment.lang"  select="''"/>  
  <xsl:param name="dita.plugin.output" />
  <xsl:param name="plugin"/>

  <xsl:variable name="newline">
<xsl:text>&#10;</xsl:text></xsl:variable>


  
  

<!--  <xsl:output encoding="utf-8" indent="yes" method="xml"/> -->

  <!--<xsl:template match="*[contains(@class,' eclipsemap/plugin ')]//*"/>-->
  
  <xsl:template match="/"> 
    <xsl:call-template name="eclipse.plugin.init"/>    
  </xsl:template>
  
  <xsl:template name="eclipse.plugin.init">
    <xsl:if test="$dita.plugin.output !=''">
      <xsl:choose>
        <!--<xsl:when test="$dita.plugin.output ='dita.eclipse.fragment'">
            <xsl:apply-templates mode="eclipse.fragment"/> 
        </xsl:when>-->
        <xsl:when test="$dita.plugin.output ='dita.eclipse.properties'">
          <xsl:apply-templates mode="eclipse.properties"/>
        </xsl:when>
        <xsl:when test="$dita.plugin.output ='dita.eclipse.manifest'">
          <xsl:apply-templates mode="eclipse.manifest"/>
        </xsl:when>
        <xsl:when test="$dita.plugin.output ='dita.eclipse.plugin'">
          <xsl:apply-templates mode="eclipse.plugin"/>
        </xsl:when>
        <!--  XSLT 2.0 param value used to generate all eclipse plugin related files.-->
        <xsl:when test="$dita.plugin.output ='dita.eclipse.all'">
          
        </xsl:when>
        <!-- Produce the content for the plugin.xml file -->
        <xsl:otherwise>
          <xsl:apply-templates />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    
    <xsl:if test="$dita.plugin.output =''">
        <xsl:apply-templates />
    </xsl:if>

  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/plugin ')]" mode="eclipse.plugin">
      <xsl:value-of select="$newline"/>
      <plugin>
        <!--
         <xsl:apply-templates select="@id"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' eclipsemap/pluginname ')]" mode="plugin"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/providerName ')]" mode="plugin"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' topic/vrmlist ')]" mode="plugin"/>
        -->
          <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/controllingPlugin ')]" mode="plugin"/>
        <xsl:if test="*[contains(@class,' eclipsemap/tocref ')][not(@toc='no')]|*[contains(@class,' eclipsemap/primarytocref ')][not(@toc='no')]">
        
          <xsl:value-of select="$newline"/>
          <extension point="org.eclipse.help.toc">
            <xsl:apply-templates select="*[contains(@class,' eclipsemap/primarytocref ')][not(@toc='no')]"/>
            <xsl:apply-templates select="*[contains(@class,' eclipsemap/tocref ')][not(@toc='no')]"/>
          </extension>
        </xsl:if>
        <xsl:if test="$DEFAULTINDEX!=''">
          <extension point="org.eclipse.help.index">
            <index file="{$DEFAULTINDEX}"/>
          </extension>
        </xsl:if>
        <xsl:call-template name="indexExtension"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/contextExtension ')] | 
                                     *[contains(@class,' eclipsemap/contentExtension ')] |
                                     *[contains(@class,' eclipsemap/extension ')]"/>
      </plugin>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/plugin ')]/@id">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/pluginname ')]" mode="plugin">
    <xsl:attribute name="name"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>
  <xsl:template match="*[contains(@class,' eclipsemap/providerName ')]" mode="plugin">
    <xsl:attribute name="provider-name"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/vrmlist ')]" mode="plugin">
    <xsl:apply-templates select="*[contains(@class,' topic/vrm ')][last()]" mode="plugin"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/vrm ')]" mode="plugin">
    <xsl:attribute name="version"><xsl:value-of select="@version"/><xsl:apply-templates select="@release" mode="eclipse.manifest"/><xsl:apply-templates select="@modification" mode="eclipse.manifest"/><xsl:apply-templates select="../../*[contains(@class,' eclipsemap/pluginQualifier ')]"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/primarytocref ')][@href]">
    <xsl:variable name="tocname">
      <xsl:choose>
        <xsl:when test="@format = 'ditamap'">
          <xsl:call-template name="replace-extension">
            <xsl:with-param name="filename" select="@href"/>
            <xsl:with-param name="extension" select="'.xml'"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$newline"/>
    <toc file="{$tocname}" primary="true">
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/tocrefmeta ')]/*[contains(@class,' eclipsemap/extradir ')]"/>
    </toc>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/tocref ')][@href]">
    <xsl:variable name="tocname">
      <xsl:choose>
        <xsl:when test="@format = 'ditamap'">
          <xsl:call-template name="replace-extension">
            <xsl:with-param name="filename" select="@href"/>
            <xsl:with-param name="extension" select="'.xml'"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$newline"/>
    <toc file="{$tocname}">
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/tocrefmeta ')]/*[contains(@class,' eclipsemap/extradir ')]"/>
    </toc>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extradir ')]">
    <xsl:attribute name="extradir"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>

  <xsl:template  name="indexExtension" match="*[contains(@class,' eclipsemap/indexExtension ')][@href]">
    <xsl:variable name="indexname">
      <xsl:choose>
        <xsl:when test="not(@format) or @format = 'dita' or @format = 'ditamap'">
          <xsl:call-template name="replace-extension">
            <xsl:with-param name="filename" select="@href"/>
            <xsl:with-param name="extension" select="'.xml'"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$newline"/>
    <extension point="org.eclipse.help.index">
      <index file="{$indexFilename}"/>
    </extension>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/contextExtension ')]">
    <xsl:value-of select="$newline"/>
    <extension point="org.eclipse.help.contexts">
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/contextInfo ')]/*[contains(@class,' eclipsemap/extensionName ')]"/>
      <xsl:value-of select="$newline"/>
      <contexts file="{@href}">
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/contextInfo ')]/*[contains(@class,' eclipsemap/contextPlugin ')]"/>
      </contexts>
    </extension>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extensionName ')]">
    <xsl:attribute name="name"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/contextPlugin ')]">
    <xsl:attribute name="plugin"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/contentExtension ')]">
    <xsl:value-of select="$newline"/>
    <extension point="org.eclipse.help.contentProducer">
      <xsl:apply-templates select="*"/>
    </extension>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/contentProducer ')]">
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/extensionName ')]"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/producerClass ')]"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/producerClass ')]">
    <xsl:value-of select="$newline"/>
    <contentProducer producer="{@content}">
      <xsl:apply-templates select="following-sibling::*[contains(@class,' eclipsemap/parameter ')]"/>
    </contentProducer>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/parameter ')]">
    <xsl:value-of select="$newline"/>
    <parameter name="{@name}" value="{@content}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extension ')]">
    <xsl:value-of select="$newline"/>
    <extension>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/extensionMeta ')]/*[contains(@class,' eclipsemap/extensionPoint ')]"/>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/extensionMeta ')]/*[contains(@class,' eclipsemap/extensionName ')]"/>

    </extension>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extensionPoint ')]">
    <xsl:attribute name="point"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/controllingPlugin ')]"  mode="plugin">
    <xsl:value-of select="$newline"/>
    <xsl:element name="extension" >
        <xsl:attribute name="point"><xsl:text>org.eclipse.core.runtime.products</xsl:text></xsl:attribute>
        <xsl:choose>
          <xsl:when test="*[contains(@class,' eclipsemap/productId ')]">
            <xsl:attribute name="id"><xsl:value-of select="*[contains(@class,' eclipsemap/productId ')]" /></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="id"><xsl:text>helpProduct</xsl:text></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:element name="product">
            <xsl:choose>
                <xsl:when test="*[contains(@class,' eclipsemap/productName ')]">
                    <xsl:attribute name="name"><xsl:text>%productName</xsl:text></xsl:attribute>
                    <xsl:attribute name="application"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="name"><xsl:text>%name</xsl:text></xsl:attribute>
                    <xsl:attribute name="application"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    
    </xsl:element>
  </xsl:template>

  <!--  
    ======================================================================
    Eclipse Map Fragment Output
    ======================================================================
  -->
<!--
  <xsl:template match="*[contains(@class,' eclipsemap/plugin ')]" priority="2" mode="eclipse.fragment">  
    
    <xsl:element name="fragment">
      <xsl:call-template name="eclipse.fragment.id"></xsl:call-template>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' eclipsemap/pluginname ')]" mode="eclipse.fragment"/>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/providerName ')]" mode="eclipse.fragment"/>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' topic/vrmlist ')]" mode="eclipse.fragment"/>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/fragmentInfo ')]/*[contains(@class,' eclipsemap/fragmentMatch ')]" mode="eclipse.fragment"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template name="eclipse.fragment.id" mode="eclipse.fragment">
    <xsl:attribute name="plugin-id"><xsl:value-of select="@id"/></xsl:attribute>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/fragmentInfo ')]/*[contains(@class,' eclipsemap/fragmentId ')]">
        <xsl:attribute name="id"><xsl:value-of select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/fragmentInfo ')]/*[contains(@class,' eclipsemap/fragmentId ')]"/></xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="$fragment.lang!=''">
          <xsl:choose>
            <xsl:when test="$fragment.country!=''">
              <xsl:attribute name="id"><xsl:value-of select="@id"/>.<xsl:value-of select="$fragment.lang"/>.<xsl:value-of select="$fragment.country"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="id"><xsl:value-of select="@id"/>.<xsl:value-of select="$fragment.lang"/></xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/fragmentMatch ')]" mode="eclipse.fragment">
    <xsl:attribute name="match">
      <xsl:choose>
        <xsl:when test=".='perfect'">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:when test=".='equivalent'">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:when test=".='greaterOrEqual'">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:when test=".='compatible'">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>compatible</xsl:text>
        </xsl:otherwise>
      </xsl:choose>      
    </xsl:attribute>
  </xsl:template>
   
  <xsl:template match="*[contains(@class,' eclipsemap/pluginname ')]" mode="eclipse.fragment">
    <xsl:attribute name="name">%name</xsl:attribute>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/providerName ')]" mode="eclipse.fragment">
    <xsl:attribute name="provider-name">

      <xsl:text>%providerName</xsl:text>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' topic/vrmlist ')]" mode="eclipse.fragment">
    <xsl:attribute name="plugin-version">
      <xsl:apply-templates select="*[contains(@class,' topic/vrm ')][last()]" mode="eclipse.fragment"/>
    </xsl:attribute>
    <xsl:choose>
      <xsl:when test="../../*[contains(@class,' eclipsemap/fragmentInfo ')]/*[contains(@class,' topic/vrmlist ')]">
        <xsl:attribute name="version">
          <xsl:apply-templates select="../../*[contains(@class,' eclipsemap/fragmentInfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')][last()]" mode="eclipse.fragment"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="version">
          <xsl:apply-templates select="*[contains(@class,' topic/vrm ')][last()]" mode="eclipse.fragment"/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/vrm ')]" mode="eclipse.fragment">
    <xsl:value-of select="@version"/><xsl:apply-templates select="@release" mode="eclipse.manifest"/><xsl:apply-templates select="@modification" mode="eclipse.manifest"/><xsl:apply-templates select="../../*[contains(@class,' eclipsemap/pluginQualifier ')]" mode="eclipse.manifest"/>
  </xsl:template>
  
  -->
  
  <!--  
  ======================================================================
  Eclipse Map OSGI Manifest Output
  ======================================================================
  -->
  
  <xsl:template match= "*[contains(@class,' eclipsemap/plugin ')]" priority="2" mode="eclipse.manifest">
    <xsl:call-template name="eclipse.manifest.init"/>
    
    
  </xsl:template>
  
  <xsl:template name="eclipse.manifest.init">
    <xsl:text>Manifest-Version: 1.0</xsl:text><xsl:value-of select="$newline"/>
    <xsl:text>Bundle-ManifestVersion: 2</xsl:text><xsl:value-of select="$newline"/>
    <xsl:text>Bundle-Localization: plugin</xsl:text><xsl:value-of select="$newline"/>
    <xsl:text>Bundle-Name: %name</xsl:text><xsl:value-of select="$newline"/>
        
    
    <xsl:text>Bundle-Vendor: %providerName</xsl:text><xsl:value-of select="$newline"/>
    <xsl:choose>
      <xsl:when test="$plugin='true'">
        <xsl:text>Eclipse-LazyStart: true</xsl:text><xsl:value-of select="$newline"/>
        <xsl:choose>
          <xsl:when test="@id">
            <xsl:text>Bundle-SymbolicName: </xsl:text><xsl:value-of select="@id"/>;<xsl:text> singleton:=true</xsl:text><xsl:value-of select="$newline"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Bundle-SymbolicName: org.sample.help.doc; singleton:=true</xsl:text><xsl:value-of select="$newline"/>
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">050</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' topic/vrmlist ')]" mode="eclipse.manifest"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/osgiManifest ')]/*[contains(@class,' eclipsemap/manifestMeta ')]" mode="eclipse.manifest"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@id">
            <xsl:if test="$fragment.lang!=''">
            <xsl:text>Fragment-Host: </xsl:text><xsl:value-of select="@id"/>;<xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/fragmentInfo ')]/*[contains(@class,' eclipsemap/fragmentVersionInfo ')]" mode="eclipse.manifest"/>
            <xsl:value-of select="$newline"/>
              
            <xsl:text>Bundle-SymbolicName: </xsl:text><xsl:value-of select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/fragmentInfo ')]/*[contains(@class,' eclipsemap/fragmentName ')]"/><xsl:value-of select="$newline"/>
            <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/fragmentInfo ')]" mode="eclipse.manifest"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            
            <!--  <xsl:text>Bundle-SymbolicName: org.sample.help.doc.</xsl:text> -->
            <xsl:text>Bundle-SymbolicName: org.sample.help.doc</xsl:text>
            <!--   <xsl:choose>
              <xsl:when test="$fragment.lang!=''">
                <xsl:choose>
                  <xsl:when test="$fragment.country!=''">
                    <xsl:value-of select="$fragment.lang"/>.<xsl:value-of select="$fragment.country"/>;<xsl:text/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$fragment.lang"/>;<xsl:text/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>lang; </xsl:text>
              </xsl:otherwise>
            </xsl:choose>  -->
            <xsl:value-of select="$newline"/>
            <xsl:text>Fragment-Host: org.sample.help.doc.</xsl:text>
            <xsl:choose>
              <xsl:when test="$fragment.lang!=''">
                <xsl:choose>
                  <xsl:when test="$fragment.country!=''">
                    <xsl:value-of select="$fragment.lang"/>.<xsl:value-of select="$fragment.country"/>;<xsl:text/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$fragment.lang"/>;<xsl:text/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <!-- We shouldn' t be getting here, but just in case -->
              <xsl:otherwise>
                <xsl:text>lang; </xsl:text>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$newline"/>
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">050</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>                 
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/otherBundleHeaders ')]" mode="eclipse.manifest" >
    <xsl:value-of select="@name"/>: <xsl:value-of select="@content"/>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' eclipsemap/fragmentInfo ')]" mode="eclipse.manifest" priority="2">
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/fragmentVersionInfo ')]/*[contains(@class,' topic/vrm ')][position()=1]" mode="eclipse.manifest"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/fragmentVersionInfo ')]" mode="eclipse.manifest" priority="2">
    <xsl:text> bundle-version="</xsl:text>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionMin ')]" mode="eclipse.manifest"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionGreaterThanMin ')]" mode="eclipse.manifest"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionMax ')]" mode="eclipse.manifest"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionLessThanMax ')]" mode="eclipse.manifest"/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/vrmlist ')]" mode="eclipse.manifest">
    <xsl:apply-templates select="*[contains(@class,' topic/vrm ')][position()=1]" mode="eclipse.manifest"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/vrm ')]" mode="eclipse.manifest">
     <xsl:text>Bundle-Version: </xsl:text><xsl:apply-templates select="@version" mode="eclipse.manifest"/><xsl:apply-templates select="@release" mode="eclipse.manifest"/><xsl:apply-templates select="@modification" mode="eclipse.manifest"/><xsl:apply-templates select="../../*[contains(@class,' eclipsemap/qualifier ')]" mode="eclipse.manifest"/><xsl:value-of select="$newline"/>
  </xsl:template>
  
  <xsl:template match="@version" mode="eclipse.manifest">
    <xsl:choose>
      <xsl:when test="(number(.) &lt; 0) or (string(number(.)) = 'NaN')  or (normalize-space(.)='') ">
          <xsl:text>0</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="floor(.)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="@release" mode="eclipse.manifest">
    <xsl:choose>
      <xsl:when test="(number(.) &lt; 0) or (string(number(.)) = 'NaN')  or (normalize-space(.)='') ">
          <xsl:text>.</xsl:text><xsl:text>0</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>.</xsl:text><xsl:value-of select="floor(.)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="@modification" mode="eclipse.manifest">
    <xsl:choose>
      <xsl:when test="(number(.) &lt; 0) or (string(number(.)) = 'NaN')  or (normalize-space(.)='') ">
          <xsl:text>.</xsl:text><xsl:text>0</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>.</xsl:text><xsl:value-of select="floor(.)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[contains(@class,' eclipsemap/qualifier ')]" mode="eclipse.manifest">
    <xsl:text>.</xsl:text><xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/osgiManifest ')]/*[contains(@class,' eclipsemap/manifestMeta ')]" mode="eclipse.manifest">
    <xsl:if test="*[contains(@class,' eclipsemap/requiresPlugin ')]">
      <xsl:text>Require-Bundle: </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/requiresPlugin ')]" mode="eclipse.manifest"/>
    <xsl:value-of select="$newline"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/otherBundleHeaders ')]" mode="eclipse.manifest"/>
  </xsl:template>
  
  <!--   
    Require-Bundle: org.eclipse.help;bundle-version="[3.2.0,4.0.0)",com.ibm.help.doc;bundle-version="[7.0.0,8.0.0)"
  -->
  <xsl:template match="*[contains(@class,' eclipsemap/requiresPlugin ')]" mode="eclipse.manifest">
    <xsl:if test="position() != 1">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginname ')]" mode="eclipse.manifest"/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/pluginname ')]" mode="eclipse.manifest">
    <xsl:value-of select="."/><xsl:text>; bundle-version="</xsl:text>
    <xsl:apply-templates select="following-sibling::*[contains(@class,' eclipsemap/versionRange ')]"  mode="eclipse.manifest"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/versionRange ')]" mode="eclipse.manifest" priority="2">
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionMin ')]" mode="eclipse.manifest"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionGreaterThanMin ')]" mode="eclipse.manifest"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionMax ')]" mode="eclipse.manifest"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/versionLessThanMax ')]" mode="eclipse.manifest"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/versionMin ')]" mode="eclipse.manifest" priority="2">
    <xsl:if test="following-sibling::*[contains(@class,' eclipsemap/versionMax ')] or following-sibling::*[contains(@class,' eclipsemap/versionLessThanMax ')]">
      <xsl:text>[</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="@version" mode="eclipse.manifest"/><xsl:apply-templates select="@release" mode="eclipse.manifest"/><xsl:apply-templates select="@modification" mode="eclipse.manifest"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/versionGreaterThanMin ')]" mode="eclipse.manifest" priority="2">
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="@version" mode="eclipse.manifest"/><xsl:apply-templates select="@release" mode="eclipse.manifest"/><xsl:apply-templates select="@modification" mode="eclipse.manifest"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/versionMax ')]" mode="eclipse.manifest" priority="2">
    <xsl:text>, </xsl:text>
    <xsl:apply-templates select="@version" mode="eclipse.manifest"/><xsl:apply-templates select="@release" mode="eclipse.manifest"/><xsl:apply-templates select="@modification" mode="eclipse.manifest"/>
    <xsl:text>]</xsl:text>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/versionLessThanMax ')]" mode="eclipse.manifest" priority="2">
    <xsl:text>, </xsl:text>
    <xsl:apply-templates select="@version" mode="eclipse.manifest"/><xsl:apply-templates select="@release" mode="eclipse.manifest"/><xsl:apply-templates select="@modification" mode="eclipse.manifest"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  <!--  
    ======================================================================
    Eclipse Map Plugin Properties Output
    ======================================================================
  -->

  <xsl:template match="*[contains(@class,' eclipsemap/plugin ')]" priority="2" mode="eclipse.properties">
    <xsl:text># NLS_MESSAGEFORMAT_NONE</xsl:text><xsl:value-of select="$newline"/>
    <xsl:text># NLS_ENCODING=UTF-8</xsl:text><xsl:value-of select="$newline"/>

    <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' eclipsemap/pluginname ')]" mode="eclipse.properties"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/providerName ')]" mode="eclipse.properties"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/controllingPlugin ')]/*[contains(@class,' eclipsemap/productName ')]" mode="eclipse.properties"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/pluginProperties ')]" mode="eclipse.properties"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/pluginname ')]" mode="eclipse.properties">
    <xsl:text>name=</xsl:text><xsl:value-of select="normalize-space(.)"/><xsl:value-of select="$newline"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' eclipsemap/providerName ')]" mode="eclipse.properties">
    <xsl:text>providerName=</xsl:text><xsl:value-of select="normalize-space(.)"/><xsl:value-of select="$newline"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' eclipsemap/productName ')]" mode="eclipse.properties">
    <xsl:text>productName=</xsl:text><xsl:value-of select="normalize-space(.)"/><xsl:value-of select="$newline"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/pluginProperties ')]" mode="eclipse.properties">
    <xsl:value-of select="@name"/><xsl:text>=</xsl:text><xsl:value-of select="@content"/><xsl:value-of select="$newline"/>
  </xsl:template>

</xsl:stylesheet>





