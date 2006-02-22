<?xml version="1.0" encoding="utf-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="../common/output-message.xsl"/>


<!-- Define the error message prefix identifier -->
<xsl:variable name="msgprefix">DOTX</xsl:variable>

<!--xsl:param name="WORKDIR" select="'./'"/-->
<xsl:param name="PROJDIR" select="'.'"/>
<xsl:param name="DBG" select="no"/>
<xsl:param name="FILEREF">file://</xsl:param>

<xsl:template match="/">
    <xsl:apply-templates>
      <xsl:with-param name="conref-ids" select="' '"/>
    </xsl:apply-templates>
</xsl:template>

<!-- If the target element does not exist, this template will be called to issue an error -->
<xsl:template name="missing-target-error">
  <xsl:call-template name="output-message">    
    <xsl:with-param name="msgnum">010</xsl:with-param>
    <xsl:with-param name="msgsev">E</xsl:with-param>
    <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- If an ID is duplicated, and there are 2 possible targets, issue a warning -->
<xsl:template name="duplicateConrefTarget">
  <xsl:call-template name="output-message">    
    <xsl:with-param name="msgnum">011</xsl:with-param>
    <xsl:with-param name="msgsev">W</xsl:with-param>
    <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- Determine the relative path to a conref'ed file. Start with the path and
     filename. Output each single directory, and chop it off. Keep going until
     only the filename is left. -->
<xsl:template name="find-relative-path">
  <xsl:param name="remainingpath"><xsl:value-of select="substring-before(@conref,'#')"/></xsl:param>
  <xsl:if test="contains($remainingpath,'/')">
    <xsl:value-of select="substring-before($remainingpath,'/')"/>/<xsl:text/>
    <xsl:call-template name="find-relative-path">
      <xsl:with-param name="remainingpath"><xsl:value-of select="substring-after($remainingpath,'/')"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>
    
<xsl:template name="get-source-attribute">
    <xsl:apply-templates select="@*" mode="get-source-attribute"></xsl:apply-templates>
</xsl:template>
    
<xsl:template match="@*" mode="get-source-attribute">
    <xsl:variable name="attribute-value" select="."></xsl:variable>
    <xsl:if test="not($attribute-value='')">
        <xsl:text>-</xsl:text><xsl:value-of select="name()"/><xsl:text>-</xsl:text>
    </xsl:if>
</xsl:template>

<xsl:template match="@xtrc|@xtrf" mode="get-source-attribute" priority="10"/>
<xsl:template match="@conref" mode="get-source-attribute" priority="10"/>
<xsl:template match="*[contains(@class,' topic/image ')]/@href" mode="get-source-attribute" priority="10"/>
<xsl:template match="*[contains(@class,' topic/tgroup ')]/@cols" mode="get-source-attribute" priority="10"/>
<xsl:template match="*[contains(@class,' topic/boolean ')]/@state" mode="get-source-attribute" priority="10"/>
<xsl:template match="*[contains(@class,' topic/state ')]/@name" mode="get-source-attribute" priority="10"/>
<xsl:template match="*[contains(@class,' topic/state ')]/@value" mode="get-source-attribute" priority="10"/>
<xsl:template match="*[contains(@class,' map/topichead ')]/@navtitle" mode="get-source-attribute" priority="10"/>

    
<!-- targetTopic contains the domains value from the topic that this conref points to. 
     The domains value of the target must be equal to, or a subset of, the domains value 
     of the referencing topic. If it is equal, they allow the same elements. If the target
     is a subset, it allows fewer elements, and is valid in the source. If the target is a
     superset, it may contain elements that are not valid in the source, so do not allow. -->
<xsl:template name="compareDomains">
  <xsl:param name="sourceTopic"/>
  <xsl:param name="targetTopic"/>
  <xsl:choose>
    <xsl:when test="$sourceTopic=$targetTopic or contains($sourceTopic,$targetTopic)">match</xsl:when>
    <xsl:when test="string-length($targetTopic)=0">match</xsl:when>
    <xsl:otherwise>
      <xsl:variable name="firstDomain"><xsl:value-of select="normalize-space(substring-before($targetTopic,')'))"/>)</xsl:variable>
      <xsl:variable name="otherDomains"><xsl:value-of select="substring-after($targetTopic,')')"/></xsl:variable>
      <xsl:choose>
        <xsl:when test="contains($sourceTopic,$firstDomain)">
          <xsl:call-template name="compareDomains">
            <xsl:with-param name="sourceTopic" select="$sourceTopic"/>
            <xsl:with-param name="targetTopic" select="normalize-space($otherDomains)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">012</xsl:with-param>
            <xsl:with-param name="msgsev">W</xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="processing-instruction('workdir')" mode="get-work-dir">
  <xsl:value-of select="."/><xsl:text>/</xsl:text>
</xsl:template>

<!--if something has a conref attribute, jump to the target if valid and continue applying templates-->
<xsl:template match="*[@conref][@conref!='']" priority="10">
  <!-- If we have already followed a relative path, pick it up -->
  <xsl:param name="current-relative-path"/>
	<xsl:param name="conref-source-topicid"/>
  <xsl:param name="source-element"/>
  <xsl:param name="conref-ids"/>
  <xsl:param name="WORKDIR">
    <xsl:apply-templates select="/processing-instruction()" mode="get-work-dir"/>
  </xsl:param>
  <xsl:variable name="add-relative-path">
    <xsl:call-template name="find-relative-path"/>
  </xsl:variable>
  <!-- Add this to the list of followed conref IDs -->
  <xsl:variable name="updated-conref-ids" select="concat($conref-ids,' ',generate-id(.),' ')"/>
      
  <!-- Keep the source node in a variable, to pass to the target. It can be used to save 
       attributes that were specified locally. If for some reason somebody passes from
       conref straight to conref, then just save the first one (in source-element) -->
     
  <!--get element local name, parent topic's domains, and then file name, topic id, element id from conref value-->
  <xsl:variable name="element">
    <xsl:value-of select="local-name(.)"/>
  </xsl:variable>
  <xsl:variable name="domains">
      <xsl:value-of select="ancestor-or-self::*[@domains][1]/@domains"/>
  </xsl:variable>
  
  <xsl:variable name="file-prefix">
	<xsl:value-of select="$FILEREF"/><xsl:value-of select="$WORKDIR"/><xsl:value-of select="$current-relative-path"/>
  </xsl:variable>
  
  <xsl:variable name="file">
  <xsl:choose>
     <xsl:when test="contains(@conref,'#')"><xsl:value-of select="$file-prefix"/><xsl:value-of select="substring-before(@conref,'#')"/></xsl:when>
     <xsl:otherwise><xsl:value-of select="$file-prefix"/><xsl:value-of select="@conref"/></xsl:otherwise>
  </xsl:choose>
  </xsl:variable>

    <!--the file name is useful to href when resolveing conref -->
  <xsl:variable name="conref-filename">
      <xsl:value-of select="substring-after(substring-after($file,$file-prefix),$add-relative-path)"/>
  </xsl:variable>
  
  <xsl:variable name="conref-source-topic">
    <xsl:choose>
      <xsl:when test="not($conref-source-topicid='')">
        <xsl:value-of select="$conref-source-topicid"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of 
          select="ancestor-or-self::*[contains(@class, ' topic/topic ')][last()]/@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
    
  <xsl:variable name="topicid">
  <xsl:choose>
     <xsl:when test="contains(@conref,'#') and contains(substring-after(@conref,'#'),'/')"><xsl:value-of select="substring-before(substring-after(@conref,'#'),'/')"/></xsl:when>
     <xsl:when test="contains(@conref,'#')"><xsl:value-of select="substring-after(@conref,'#')"/></xsl:when>
     <xsl:otherwise>#none#</xsl:otherwise>
  </xsl:choose>
  </xsl:variable>

  <xsl:variable name="elemid">
    <xsl:choose>
     <xsl:when test="contains(@conref,'#') and contains(substring-after(@conref,'#'),'/')"><xsl:value-of select="substring-after(substring-after(@conref,'#'),'/')"/></xsl:when>
     <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="topicpos">
  <xsl:choose>
     <xsl:when test="starts-with(@conref,'#')">samefile</xsl:when>
     <xsl:when test="contains(@conref,'#')">otherfile</xsl:when>
     <xsl:otherwise>firstinfile</xsl:otherwise>
  </xsl:choose>
  </xsl:variable>

  <xsl:choose>
    <!-- If this conref has already been followed, stop to prevent an infinite loop -->
    <xsl:when test="contains($conref-ids,concat(' ',generate-id(.),' '))">
      <xsl:call-template name="output-message">
        <xsl:with-param name="msgnum">013</xsl:with-param>
        <xsl:with-param name="msgsev">E</xsl:with-param>
        <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <!--targetting an element inside a topic-->
    <xsl:when test="contains(substring-after(@conref,'#'),'/')">
      <xsl:choose>
        <xsl:when test="$topicpos='samefile'">
          <xsl:choose>
            <xsl:when test="//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]]">
              <xsl:variable name="testDomains">
                <xsl:call-template name="compareDomains">
                    <xsl:with-param name="sourceTopic" select="$domains"/>
                    <xsl:with-param name="targetTopic" select="//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@domains"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:if test="$testDomains='match'">
                  <xsl:choose>
                      <xsl:when test="not($source-element='')">
                           <xsl:apply-templates select="(//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]])[1]" mode="conref-target">
                              <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                              <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                              <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
							                <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                              <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                           </xsl:apply-templates>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:copy>
                              <xsl:apply-templates select="." mode="original-attributes"/>
                              <xsl:apply-templates select="(//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]])[1]" mode="conref-target">
                                  <xsl:with-param name="source-element"><xsl:call-template name="get-source-attribute"/></xsl:with-param>
                                  <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                                  <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>                                  
                								  <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                                  <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                              </xsl:apply-templates>
                          </xsl:copy>
                      </xsl:otherwise>
                  </xsl:choose>
               
                <xsl:if test="(//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]])[2]">
                  <xsl:call-template name="duplicateConrefTarget"/>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="$topicpos='otherfile'">
          <xsl:choose>
            <xsl:when test="document($file,/)//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]]">
              <xsl:variable name="testDomains">
                <xsl:call-template name="compareDomains">
                  <xsl:with-param name="sourceTopic" select="$domains"/>
                  <xsl:with-param name="targetTopic" select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@domains"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:if test="$testDomains='match'">
                  <xsl:choose>
                      <xsl:when test="not($source-element='')">
                           <xsl:apply-templates select="(document($file,/)//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]])[1]" mode="conref-target">
                              <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                              <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                              <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                               <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
              							   <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                               <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                            </xsl:apply-templates>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:copy>
                              <xsl:apply-templates select="." mode="original-attributes"/>
                              <xsl:apply-templates select="(document($file,/)//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]])[1]" mode="conref-target">
                                  <xsl:with-param name="source-element"><xsl:call-template name="get-source-attribute"/></xsl:with-param>
                                  <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                                  <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                                  <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                								  <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                                  <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                              </xsl:apply-templates>
                          </xsl:copy>
                      </xsl:otherwise>
                  </xsl:choose>
                
                <xsl:if test="(document($file,/)//*[local-name()=$element][@id=$elemid][ancestor::*[contains(@class, ' topic/topic ')][1][@id=$topicid]])[2]">
                  <xsl:call-template name="duplicateConrefTarget"/>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise/><!--never happens - only other value is firstinfile, but we know there's a # in the conref so it's either samefile or otherfile-->
      </xsl:choose>
    </xsl:when>

    <!--targetting a topic-->
    <xsl:when test="contains(@class, ' topic/topic ')">
      <xsl:choose>
        <xsl:when test="$topicpos='samefile'">
          <xsl:choose>
            <xsl:when test="//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element]">
              <xsl:variable name="testDomains">
                <xsl:call-template name="compareDomains">
                  <xsl:with-param name="sourceTopic" select="$domains"/>
                  <xsl:with-param name="targetTopic" select="//*[contains(@class, ' topic/topic ')][@id=$topicid][1][local-name()=$element]/@domains"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:if test="$testDomains='match'">
                  <xsl:choose>
                      <xsl:when test="not($source-element='')">
                           <xsl:apply-templates select="(//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                              <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                              <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                              <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
              							  <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                              <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                           </xsl:apply-templates>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:copy>
                              <xsl:apply-templates select="." mode="original-attributes"/>
                              <xsl:apply-templates select="(//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                                  <xsl:with-param name="source-element"><xsl:call-template name="get-source-attribute"/></xsl:with-param>
                                  <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                                  <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                								  <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                                  <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                              </xsl:apply-templates>
                          </xsl:copy>
                      </xsl:otherwise>
                  </xsl:choose>
                <xsl:if test="(//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element])[2]">
                  <xsl:call-template name="duplicateConrefTarget"/>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="$topicpos='otherfile'">
          <xsl:choose>
            <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element]">
              <xsl:variable name="testDomains">
                <xsl:call-template name="compareDomains">
                  <xsl:with-param name="sourceTopic" select="$domains"/>
                  <xsl:with-param name="targetTopic" select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1][local-name()=$element]/@domains"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:if test="$testDomains='match'">
                  <xsl:choose>
                      <xsl:when test="not($source-element='')">
                           <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                              <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                              <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                               <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                               <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
							                 <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                               <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                            </xsl:apply-templates>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:copy>
                              <xsl:apply-templates select="." mode="original-attributes"/>
                              <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                                  <xsl:with-param name="source-element"><xsl:call-template name="get-source-attribute"/></xsl:with-param>
                                  <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                                  <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                                  <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
								                  <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                                  <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                              </xsl:apply-templates>
                          </xsl:copy>
                      </xsl:otherwise>
                  </xsl:choose>
                
                <xsl:if test="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][local-name()=$element])[2]">
                  <xsl:call-template name="duplicateConrefTarget"/>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="$topicpos='firstinfile'">
          <xsl:choose>
            <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1][local-name()=$element]">
              <xsl:variable name="testDomains">
                <xsl:call-template name="compareDomains">
                  <xsl:with-param name="sourceTopic" select="$domains"/>
                  <xsl:with-param name="targetTopic" select="document($file,/)//*[contains(@class, ' topic/topic ')][1][local-name()=$element]/@domains"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:if test="$testDomains='match'">
                  <xsl:choose>
                      <xsl:when test="not($source-element='')">
                           <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')][1][local-name()=$element])[1]" mode="conref-target">
                              <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                              <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                               <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                               <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
							                 <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                               <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                            </xsl:apply-templates>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:copy>
                              <xsl:apply-templates select="." mode="original-attributes"/>
                              <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')][1][local-name()=$element])[1]" mode="conref-target">
                                  <xsl:with-param name="source-element"><xsl:call-template name="get-source-attribute"/></xsl:with-param>
                                  <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                                  <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                                  <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
								                  <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                                  <xsl:with-param name="conref-ids" select="$updated-conref-ids"/>
                              </xsl:apply-templates>
                          </xsl:copy>
                      </xsl:otherwise>
                  </xsl:choose>
                
                <xsl:if test="(document($file,/)//*[contains(@class, ' topic/topic ')][1][local-name()=$element])[2]">
                  <xsl:call-template name="duplicateConrefTarget"/>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise/><!--never happens - only three possible values for topicpos, all are tested-->
      </xsl:choose>
    </xsl:when>
    
    <!--targetting a topicref from within a map-->
    <xsl:when test="contains(@class, ' map/topicref ')">
      <xsl:choose>
        <xsl:when test="$topicpos='samefile'">
          <xsl:choose>
            <xsl:when test="//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element]">
              <xsl:choose>
                <xsl:when test="not($source-element='')">
                  <xsl:apply-templates select="(//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                    <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                    <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                    <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                    <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                  </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:copy>
                    <xsl:apply-templates select="." mode="original-attributes"/>
                    <xsl:apply-templates select="(//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                      <xsl:with-param name="source-element"><xsl:call-template name="get-source-attribute"/></xsl:with-param>
                      <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                      <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                      <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                    </xsl:apply-templates>
                  </xsl:copy>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:if test="(//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element])[2]">
                <xsl:call-template name="duplicateConrefTarget"/>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="$topicpos='otherfile'">
          <xsl:choose>
            <xsl:when test="document($file,/)//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element]">
              <xsl:variable name="testDomains">
                <xsl:call-template name="compareDomains">
                  <xsl:with-param name="sourceTopic" select="$domains"/>
                  <xsl:with-param name="targetTopic" select="document($file,/)//*[contains(@class, ' map/map ')]/@domains"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:if test="$testDomains='match'">
                <xsl:choose>
                  <xsl:when test="not($source-element='')">
                    <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                      <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                      <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                      <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                      <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                      <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                    </xsl:apply-templates>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:copy>
                    <xsl:apply-templates select="." mode="original-attributes"/>
                    <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                      <xsl:with-param name="source-element"><xsl:call-template name="get-source-attribute"/></xsl:with-param>
                      <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                      <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                      <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                      <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                    </xsl:apply-templates>
                    </xsl:copy>
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="(document($file,/)//*[contains(@class, ' map/topicref ')][@id=$topicid][local-name()=$element])[2]">
                  <xsl:call-template name="duplicateConrefTarget"/>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>        
        <xsl:otherwise>
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">014</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    
    <!--targetting anything else within a map (such as reltable)-->
    <xsl:when test="contains(/*/@class, ' map/map ')">
      <xsl:choose>
        <xsl:when test="$topicpos='samefile'">
          <xsl:choose>
            <xsl:when test="//*[@id=$topicid][local-name()=$element]">
              <xsl:choose>
                <xsl:when test="not($source-element='')">
                  <xsl:apply-templates select="(//*[@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                   <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                    <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                    <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                    <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                  </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:copy>
                    <xsl:apply-templates select="." mode="original-attributes"/>
                    <xsl:apply-templates select="(//*[@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                      <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                      <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                      <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                      <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                    </xsl:apply-templates>
                  </xsl:copy>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:if test="(//*[@id=$topicid][local-name()=$element])[2]">
                <xsl:call-template name="duplicateConrefTarget"/>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="$topicpos='otherfile'">
          <xsl:choose>
            <xsl:when test="document($file,/)//*[@id=$topicid][local-name()=$element]">
              <xsl:variable name="testDomains">
                <xsl:call-template name="compareDomains">
                  <xsl:with-param name="sourceTopic" select="$domains"/>
                  <xsl:with-param name="targetTopic" select="document($file,/)//*[contains(@class, ' map/map ')]/@domains"/>
                </xsl:call-template>
              </xsl:variable>
              <xsl:if test="$testDomains='match'">
                <xsl:choose>
                  <xsl:when test="not($source-element='')">
                    <xsl:apply-templates select="(document($file,/)//*[@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                      <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                      <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                      <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                      <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                      <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                    </xsl:apply-templates>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:copy>
                      <xsl:apply-templates select="." mode="original-attributes"/>
                      <xsl:apply-templates select="(document($file,/)//*[@id=$topicid][local-name()=$element])[1]" mode="conref-target">
                        <xsl:with-param name="source-element"><xsl:copy-of select="$source-element"/></xsl:with-param>
                        <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/><xsl:value-of select="$add-relative-path"/></xsl:with-param>
                        <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
                        <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                        <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topic"/></xsl:with-param>
                      </xsl:apply-templates>
                    </xsl:copy>
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="(document($file,/)//*[@id=$topicid][local-name()=$element])[2]">
                  <xsl:call-template name="duplicateConrefTarget"/>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:call-template name="missing-target-error"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">014</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
      
    <xsl:otherwise>
      <xsl:call-template name="output-message">
    	  <xsl:with-param name="msgnum">015</xsl:with-param>
        <xsl:with-param name="msgsev">E</xsl:with-param>
        <xsl:with-param name="msgparams">%1=<xsl:value-of select="@conref"/></xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- When an element is the target of a conref, treat everything the same as any other element EXCEPT the attributes.
     Create the current element, and add all attributes except @id. Then go back to the original element
     (passed in source-element). Process all attributes on that element, except @conref. They will
     replace values in the source. -->
<xsl:template match="*" mode="conref-target">
  <xsl:param name="WORKDIR"/>
  <xsl:param name="conref-source-topicid"/>
  <xsl:param name="conref-ids"/>
  <xsl:param name="source-element"/>
  <xsl:param name="current-relative-path"/> <!-- File system path from original file to here -->
  <xsl:param name="conref-filename"/>
    <xsl:variable name="topicid">
        <xsl:choose>
            <xsl:when test="contains(@class, ' topic/topic ')">
                <xsl:value-of select="@id"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/topic ')][1]/@id"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:variable name="elemid">
        <xsl:choose>
            <xsl:when test="contains(@class, ' topic/topic ')">
                <xsl:text>#none#</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@id"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
  <xsl:choose>
    <!-- If for some bizarre reason you conref to another element that uses @conref, forget the original and continue here. -->
    <xsl:when test="@conref">
      <xsl:apply-templates select=".">
        <xsl:with-param name="source-element"><xsl:value-of select="$source-element"/></xsl:with-param>
		    <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topicid"/></xsl:with-param>
        <xsl:with-param name="conref-ids" select="$conref-ids"/>
        <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/></xsl:with-param>
        <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise>
        <xsl:for-each select="@*">
            <xsl:variable name="attribute-name"><xsl:text>-</xsl:text><xsl:value-of select="name()"/><xsl:text>-</xsl:text></xsl:variable>
            
            <xsl:if test="not(name()='id') and not(contains($source-element,$attribute-name))">
                <xsl:choose>
                    <xsl:when test="name()='href'">
                        <xsl:apply-templates select=".">
                            <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/></xsl:with-param>
                            <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                            <xsl:with-param name="topicid"><xsl:value-of select="$topicid"/></xsl:with-param>
                            <xsl:with-param name="elemid"><xsl:value-of select="$elemid"/></xsl:with-param>
							              <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topicid"/></xsl:with-param>
                            <xsl:with-param name="conref-ids" select="$conref-ids"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select=".">
                            <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/></xsl:with-param>
                            <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topicid"/></xsl:with-param>
                            <xsl:with-param name="conref-ids" select="$conref-ids"/>
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
            
        </xsl:for-each>
        
        <!-- Continue processing this element as any other -->
        <xsl:apply-templates select="*|comment()|processing-instruction()|text()">
          <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/></xsl:with-param>
            <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
            <xsl:with-param name="topicid"><xsl:value-of select="$topicid"/></xsl:with-param>
            <xsl:with-param name="elemid"><xsl:value-of select="$elemid"/></xsl:with-param>
			      <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topicid"/></xsl:with-param>
            <xsl:with-param name="conref-ids" select="$conref-ids"/>
          <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
        </xsl:apply-templates>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Processing a copy of the original element, that used @conref: apply-templates on
     all of the attributes, though some may be filtered out. -->
<xsl:template match="*" mode="original-attributes">
  <xsl:apply-templates select="@*" mode="original-attributes"/>
</xsl:template>
<xsl:template match="@*" mode="original-attributes">    
    <xsl:variable name="attribute-value" select="."></xsl:variable>
    <xsl:if test="not($attribute-value='')">
        <xsl:copy><xsl:value-of select="."/></xsl:copy> 
    </xsl:if>
</xsl:template>

<!-- If an element is required, it must be specified on the original source element to avoid parsing errors.
     Such attributes should NOT be copied from the source. Conref should also not be copied. 
     NOTE: if a new specialized element requires attributes, it should be added here. -->

<xsl:template match="@xtrc|@xtrf" mode="original-attributes" priority="10"/>
<xsl:template match="@conref" mode="original-attributes" priority="10"/>
<xsl:template match="*[contains(@class,' topic/image ')]/@href" mode="original-attributes" priority="10"/>
<xsl:template match="*[contains(@class,' topic/tgroup ')]/@cols" mode="original-attributes" priority="10"/>
<xsl:template match="*[contains(@class,' topic/boolean ')]/@state" mode="original-attributes" priority="10"/>
<xsl:template match="*[contains(@class,' topic/state ')]/@name" mode="original-attributes" priority="10"/>
<xsl:template match="*[contains(@class,' topic/state ')]/@value" mode="original-attributes" priority="10"/>
<!-- topichead is specialized from topicref, and requires @navtitle -->
<xsl:template match="*[contains(@class,' map/topichead ')]/@navtitle" mode="original-attributes" priority="10"/>

<xsl:template match="@href">
  <xsl:param name="current-relative-path"/>
    <xsl:param name="conref-filename"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:param name="conref-source-topicid"/>
    <xsl:param name="conref-ids"/>
  <xsl:attribute name="href">
    <xsl:choose>
      <xsl:when test="../@scope='external'"><xsl:value-of select="."/></xsl:when>
        
        <xsl:when test="starts-with(.,'http://') or starts-with(.,'ftp://')"><xsl:value-of select="."/></xsl:when>
        
        <xsl:when test="starts-with(.,'#')">
            <xsl:choose>
                <xsl:when test="$conref-filename=''">  <!--in the local file -->
                    <xsl:value-of select="."/>
                </xsl:when>
                <xsl:otherwise> <!--not in the local file -->                    
                    <xsl:call-template name="generate-href">
                        <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/></xsl:with-param>
                        <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
                        <xsl:with-param name="topicid"><xsl:value-of select="$topicid"/></xsl:with-param>
                        <xsl:with-param name="elemid"><xsl:value-of select="$elemid"/></xsl:with-param>
						            <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topicid"/></xsl:with-param>
                    </xsl:call-template>     <!--experimental code -->
                </xsl:otherwise>
            </xsl:choose>
            
        </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$current-relative-path"/><xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:template>

<xsl:template match="@id">
    <xsl:param name="current-relative-path"/>
    <xsl:param name="conref-filename"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:attribute name="id">
        <xsl:choose>
            <xsl:when test="not($conref-filename='')">
                <xsl:value-of select="generate-id(..)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:attribute>
</xsl:template>    

<xsl:template name="generate-href">
	<xsl:param name="current-relative-path"/>
	<xsl:param name="conref-filename"/>
	<xsl:param name="topicid"/>
	<xsl:param name="elemid"/>
	<xsl:param name="conref-source-topicid"/>
	<xsl:variable name="conref-topicid">
		<xsl:choose>
			<xsl:when test="$topicid='#none#'">
				<xsl:value-of 
					select="//*[contains(@class, ' topic/topic ')][1]/@id"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$topicid"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="href-topicid">
		<xsl:value-of select="substring-before(substring-after(.,'#'),'/')"/>
	</xsl:variable>
	<xsl:variable name="href-elemid">
		<xsl:value-of select="substring-after(.,'/')"/>
	</xsl:variable>
	<xsl:variable name="conref-gen-id">
		<xsl:choose>
			<xsl:when test="$elemid='#none#' or $elemid=$href-elemid">
				<xsl:value-of 
					select="generate-id(//*[contains(@class, ' topic/topic ')][@id=$conref-topicid]//*[@id=$href-elemid])"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of 
					select="generate-id(//*[contains(@class, ' topic/topic ')][@id=$conref-topicid]//*[@id=$elemid]//*[@id=$href-elemid])"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="href-gen-id">
		<xsl:value-of 
			select="generate-id(//*[contains(@class, ' topic/topic ')][@id=$href-topicid]//*[@id=$href-elemid])"/>
	</xsl:variable>
	<xsl:choose>
		<xsl:when 
			test="($conref-gen-id='') or (not($conref-gen-id=$href-gen-id))">
			<!--href target is not in conref target -->
			<xsl:value-of select="$current-relative-path"/>
			<xsl:value-of select="$conref-filename"/>
			<xsl:value-of select="."/>
		</xsl:when>
		<xsl:when test="$conref-gen-id=$href-gen-id">
			<xsl:text>#</xsl:text>
			<xsl:value-of select="$conref-source-topicid"/>
      <xsl:text>/</xsl:text>
			<xsl:value-of select="$conref-gen-id"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>
  
<!--copy everything else-->
<xsl:template match="*|@*|comment()|processing-instruction()|text()">
  <xsl:param name="current-relative-path"/>
    <xsl:param name="conref-filename"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:param name="WORKDIR">
      <xsl:apply-templates select="/processing-instruction()" mode="get-work-dir"/>
    </xsl:param>
	<xsl:param name="conref-source-topicid"/>
  <xsl:param name="conref-ids"/>
  <xsl:copy>
    <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()">
      <xsl:with-param name="current-relative-path"><xsl:value-of select="$current-relative-path"/></xsl:with-param>
        <xsl:with-param name="conref-filename"><xsl:value-of select="$conref-filename"/></xsl:with-param>
        <xsl:with-param name="topicid"><xsl:value-of select="$topicid"/></xsl:with-param>
        <xsl:with-param name="elemid"><xsl:value-of select="$elemid"/></xsl:with-param>
        <xsl:with-param name="WORKDIR"><xsl:value-of select="$WORKDIR"/></xsl:with-param>
        <xsl:with-param name="conref-source-topicid"><xsl:value-of select="$conref-source-topicid"/></xsl:with-param>
        <xsl:with-param name="conref-ids" select="$conref-ids"/>
    </xsl:apply-templates>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
