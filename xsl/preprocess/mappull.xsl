<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="../common/output-message.xsl"/>

<!-- Define the error message prefix identifier -->
<xsl:variable name="msgprefix">IDXS</xsl:variable>


<!-- The directory where the map resides, starting with root -->
<xsl:param name="WORKDIR" select="'./'"/>
<xsl:param name="FILEREF" select="'file://'"/>
<xsl:param name="DITAEXT" select="'.xml'"/>

<!-- If converting to PDF, never try to pull info from targets with print="no" -->
<xsl:param name="FINALOUTPUTTYPE" select="''"/>

<xsl:template match="*[contains(@class, ' map/topicref ')]">

  <!--copy self-->
  <xsl:copy>

    <!--copy existing explicit attributes-->
    <xsl:apply-templates select="@*[not(self::navtitle)]"/>

    <!--copy inheritable attributes that aren't already explicitly defined-->
    <!--@type|@importance|@linking|@toc|@print|@search|@format|@scope-->

    <!--need to create type variable regardless, for passing as a parameter to getstuff template-->
    <xsl:variable name="type">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">type</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@type)">
      <xsl:choose>
        <xsl:when test="$type='#none#'"><!-- do nothing - will attempt to grab type from target in get-stuff template--></xsl:when>
        <xsl:otherwise><xsl:attribute name="type"><xsl:value-of select="$type"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <!--create importance variable only if importance attribute not already copied-->
    <xsl:if test="not(@importance)">
      <xsl:variable name="importance">
        <xsl:call-template name="inherit">
          <xsl:with-param name="attrib">importance</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$importance='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="importance"><xsl:value-of select="$importance"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="not(@linking)">
      <xsl:variable name="linking">
        <xsl:call-template name="inherit">
          <xsl:with-param name="attrib">linking</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$linking='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="linking"><xsl:value-of select="$linking"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="not(@toc)">
      <xsl:variable name="toc">
        <xsl:call-template name="inherit">
          <xsl:with-param name="attrib">toc</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$toc='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="toc"><xsl:value-of select="$toc"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:variable name="print">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">print</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@print)">
      <xsl:choose>
        <xsl:when test="$print='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="print"><xsl:value-of select="$print"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="not(@search)">
      <xsl:variable name="search">
        <xsl:call-template name="inherit">
          <xsl:with-param name="attrib">search</xsl:with-param>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$search='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="search"><xsl:value-of select="$search"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:variable name="format">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">format</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@format)">
      <xsl:choose>
        <xsl:when test="$format='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="format"><xsl:value-of select="$format"/></xsl:attribute>
          <!-- warn if non-dita format was inherited, and this is dita -->
          <xsl:if test="$format!='dita' and contains(@href,$DITAEXT)">
            <xsl:call-template name="output-message">
             <xsl:with-param name="msg">An href value appears to point to a DITA file, but the format
               attribute inherited a value of "<xsl:value-of select="$format"/>". If the target
               <xsl:value-of select="@href"/> is a DITA file, set the format attribute to "dita".
               If it does not point to a DITA file, set the format attribute locally.</xsl:with-param>
             <xsl:with-param name="msgnum">047</xsl:with-param>
             <xsl:with-param name="msgsev">W</xsl:with-param>
            </xsl:call-template>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:variable name="scope">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">scope</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@scope)">
      <xsl:choose>
        <xsl:when test="$scope='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="scope"><xsl:value-of select="$scope"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:variable name="audience">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">audience</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@audience)">
      <xsl:choose>
        <xsl:when test="$audience='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="audience"><xsl:value-of select="$audience"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:variable name="platform">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">platform</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@platform)">
      <xsl:choose>
        <xsl:when test="$platform='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="platform"><xsl:value-of select="$platform"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:variable name="product">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">product</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@product)">
      <xsl:choose>
        <xsl:when test="$product='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="product"><xsl:value-of select="$product"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:variable name="rev">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">rev</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@rev)">
      <xsl:choose>
        <xsl:when test="$rev='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="rev"><xsl:value-of select="$rev"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>
	
    <xsl:variable name="otherprops">
      <xsl:call-template name="inherit">
        <xsl:with-param name="attrib">otherprops</xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="not(@otherprops)">
      <xsl:choose>
        <xsl:when test="$otherprops='#none#'"><!-- do nothing - no attribute--></xsl:when>
        <xsl:otherwise><xsl:attribute name="otherprops"><xsl:value-of select="$otherprops"/></xsl:attribute></xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <!--grab type, text and metadata, as long there's an href to grab from, and it's not inaccessible-->
    <xsl:choose>
      <xsl:when test="@href=''">
        <xsl:call-template name="output-message">
          <xsl:with-param name="msg">Found a topic reference with an empty HREF attribute. The attribute
            should point to a file or other valid address.</xsl:with-param>
          <xsl:with-param name="msgnum">041</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$print='no' and ($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD')">
      </xsl:when>
      <xsl:when test="@href">
        <xsl:call-template name="get-stuff">
          <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
          <xsl:with-param name="scope"><xsl:value-of select="$scope"/></xsl:with-param>
          <xsl:with-param name="format"><xsl:value-of select="$format"/></xsl:with-param>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>

    <!--apply templates to children-->
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template name="inherit">
<!--@importance|@linking|@toc|@print|@search|@format|@scope-->
  <xsl:param name="attrib"/>
  <xsl:choose>
    <xsl:when test="ancestor::*[contains(@class, ' map/reltable ')]">
      <xsl:variable name="position"><xsl:value-of select="1+count(ancestor::*[contains(@class, ' map/relcell ')]/preceding-sibling::*)"/></xsl:variable>
      <xsl:choose>
        <!--check for relcell ancestor, or topicref ancestor within the current cell-->
        <xsl:when test="ancestor-or-self::*[not(contains(@class, ' map/reltable '))][not(contains(@class, ' map/map '))]/@*[local-name()=$attrib]">
          <xsl:value-of select="(ancestor-or-self::*[not(contains(@class, ' map/reltable '))][not(contains(@class, ' map/map '))]/@*[local-name()=$attrib])[last()]"/>
        </xsl:when>
        <!--check for relcolspec 'ancestor' -->
        <xsl:when test="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]/@*[local-name()=$attrib]">
          <xsl:value-of select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position ]/@*[local-name()=$attrib]"/>
        </xsl:when>

        <!--expand check to catch any ancestor (at this point, shd be reltable or map only-->
        <xsl:when test="ancestor-or-self::*/@*[local-name()=$attrib]">
          <xsl:value-of select="(ancestor-or-self::*/@*[local-name()=$attrib])[last()]"/>
        </xsl:when>
        <xsl:otherwise>#none#</xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:when test="ancestor-or-self::*/@*[local-name()=$attrib]">
      <xsl:value-of select="(ancestor-or-self::*/@*[local-name()=$attrib])[last()]"/>
    </xsl:when>
    <xsl:otherwise>#none#</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="processing-instruction('workdir')" mode="get-work-dir">
  <xsl:value-of select="."/><xsl:text>/</xsl:text>
</xsl:template>

<xsl:template name="verify-type-value">
  <xsl:param name="type"/>         <!-- Specified type on the topicref -->
  <xsl:param name="actual-class"/> <!-- Class value on the target element -->
  <xsl:param name="actual-name"/>  <!-- Name of the target element -->
  <xsl:param name="WORKDIR">
    <xsl:apply-templates select="/processing-instruction()" mode="get-work-dir"/>
  </xsl:param>
  <xsl:choose>
    <!-- The type is correct; concept typed as concept, newtype defined as newtype -->
    <xsl:when test="$type=$actual-name"/>
    <!-- If the actual class contains the specified type; reference can be called topic,
         specializedReference can be called reference -->
    <xsl:when test="contains($actual-class,concat(' ',$type,'/',$type,' '))">
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">The type attribute on a topicref element does not match the target topic.
          The type attribute was set to <xsl:value-of select="$type"/>, but the topicref points to a more
          specific <xsl:value-of select="$actual-name"/> topic. This may cause your links to sort incorrectly
          in the output. Note that the type attribute is inherited in maps, so the
          value '<xsl:value-of select="$type"/>' may come from an ancestor topicref.</xsl:with-param>
        <xsl:with-param name="msgnum">044</xsl:with-param>
         <xsl:with-param name="msgsev">I</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <!-- Otherwise: incorrect type is specified -->
    <xsl:otherwise>
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">The type attribute on a topicref element does not match the target topic.
          The type attribute was set to <xsl:value-of select="$type"/>, but the topicref points to
          a <xsl:value-of select="$actual-name"/>. This may cause your links to sort incorrectly in the
          output. Note that the type attribute is inherited in maps, so the value 
          '<xsl:value-of select="$type"/>' may come from an ancestor topicref.</xsl:with-param>
        <xsl:with-param name="msgnum">045</xsl:with-param>
        <xsl:with-param name="msgsev">W</xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="get-stuff">
  <xsl:param name="type">#none#</xsl:param>
  <xsl:param name="scope">#none#</xsl:param>
  <xsl:param name="format">#none#</xsl:param>
  <xsl:param name="WORKDIR">
    <xsl:apply-templates select="/processing-instruction()" mode="get-work-dir"/>
  </xsl:param>  

  <xsl:variable name="locktitle">
    <xsl:call-template name="inherit">
      <xsl:with-param name="attrib">locktitle</xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  
  <!--figure out what portion of the href is the path to the file-->
  <xsl:variable name="file">
    <xsl:choose>
       <!--an absolute path using a scheme, eg http, plus a fragment identifier - grab the part before the fragment-->
       <xsl:when test="contains(@href,'://') and contains(@href,'#')"><xsl:value-of select="substring-before(@href,'#')"/></xsl:when>
       <!--an absolute path using a scheme, with no fragment - grab the whole url-->
       <xsl:when test="contains(@href,'://')"><xsl:value-of select="@href"/></xsl:when>
       <!--a relative path including a fragment identifier - add the working directory, plus the part before the fragment-->	
       <xsl:when test="contains(@href,'#')"><xsl:value-of select="$FILEREF"/><xsl:value-of select="$WORKDIR"/><xsl:value-of select="substring-before(@href,'#')"/></xsl:when>
       <!--otherwise a relative path with no fragment, add the working directory plus the url-->
       <xsl:otherwise><xsl:value-of select="$FILEREF"/><xsl:value-of select="$WORKDIR"/><xsl:value-of select="@href"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="topicpos">
    <xsl:choose>
       <xsl:when test="contains(@href,'#')">otherfile</xsl:when>
       <xsl:otherwise>firstinfile</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="topicid">
    <xsl:choose>
      <xsl:when test="contains(@href,'#')"><xsl:value-of select="substring-after(@href,'#')"/></xsl:when>
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="topictype">
    <!--need to map #none# to topic for classval variable - keep using type variable for all other purposes-->
    <xsl:choose>
      <xsl:when test="$type='#none#'">topic</xsl:when>
      <xsl:otherwise><xsl:value-of select="$type"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="classval"><xsl:text> </xsl:text><xsl:value-of select="$topictype"/>/<xsl:value-of select="$topictype"/><xsl:text> </xsl:text></xsl:variable>

<!--type-->
<!--grab type from the target if it's not defined locally-->
  <xsl:choose>
    <xsl:when test="$type='#none#'">
      <xsl:choose>
        <xsl:when test="@href=''"/>
        <xsl:when test="$scope='external' or $scope='peer' or $topictype='external' or not($format='#none#' or $format='dita' or $format='DITA')">
          <!-- do nothing - type is unavailable-->
        </xsl:when>
        <xsl:when test="not(contains($file,$DITAEXT))">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msg">Unknown file extension in href: <xsl:value-of select="@href"/> 
              If this is a link to a non-DITA resource, set the format attribute to
              match the resource (for example, 'txt', 'pdf', or 'html'). 
              If it's a link to a DITA resource, the file extension must be .dita or .xml.</xsl:with-param>
            <xsl:with-param name="msgnum">015</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:when>

        <!--finding type based on name of the target element in a particular topic in another file-->
        <xsl:when test="$topicpos='otherfile'">
          <xsl:choose>
            <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]">
              <xsl:attribute name="type"><xsl:value-of select="local-name(document($file,/)//*[contains(@class, $classval)][@id=$topicid])"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise><!-- do nothing - omit attribute--></xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <!--finding type based on name of the target element in the first topic in another file-->
        <xsl:when test="$topicpos='firstinfile'">
          <xsl:choose>
            <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1]">
              <xsl:attribute name="type"><xsl:value-of select="local-name(document($file,/)//*[contains(@class, $classval)][1])"/></xsl:attribute>
            </xsl:when>
            <xsl:otherwise><!-- do nothing - omit attribute--></xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <xsl:otherwise><!--never happens - both values for topicpos are tested--></xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <!-- Type is set locally for a dita topic; warn if it is not correct. -->
    <xsl:when test="contains($file,$DITAEXT) and $scope!='external' and $scope!='peer' and ($format='#none#' or $format='dita' or $format='DITA')">
      <xsl:choose>
        <!--finding type based on name of the target element in a particular topic in another file-->
        <xsl:when test="$topicpos='otherfile' and document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]">
          <xsl:call-template name="verify-type-value">
            <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
            <xsl:with-param name="actual-class"><xsl:value-of select="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@class"/></xsl:with-param>
            <xsl:with-param name="actual-name"><xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid][1])"/></xsl:with-param>
          </xsl:call-template>  
        </xsl:when>

        <!--finding type based on name of the target element in the first topic in another file-->
        <xsl:when test="$topicpos='firstinfile' and document($file,/)//*[contains(@class, ' topic/topic ')]">
          <xsl:call-template name="verify-type-value">
            <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
            <xsl:with-param name="actual-class"><xsl:value-of select="document($file,/)//*[contains(@class, ' topic/topic ')][1]/@class"/></xsl:with-param>
            <xsl:with-param name="actual-name"><xsl:value-of select="local-name(document($file,/)//*[contains(@class, ' topic/topic ')][1])"/></xsl:with-param>
          </xsl:call-template>  
        </xsl:when>
      </xsl:choose>
    </xsl:when>
  </xsl:choose>

<!--navtitle-->
  <xsl:if test="not(@navtitle) or not($locktitle='yes')">
    <xsl:variable name="navtitle-not-normalized">
      <xsl:choose>
        <!--if it's external and not dita, use the href as fallback-->
        <xsl:when test="($scope='external' and not($format='dita' or $format='DITA')) or $type='external'">
          <xsl:choose>
            <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
            <xsl:when test="*/*[contains(@class,' map/linktext ')]">
              <xsl:value-of select="*/*[contains(@class,' map/linktext ')]"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <!--if it's external and dita, leave it undefined as fallback, so the file extension can be processed in the final output stage-->
        <xsl:when test="$scope='external'">
          <xsl:choose>
            <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
            <xsl:when test="*/*[contains(@class,' map/linktext ')]">
              <xsl:value-of select="*/*[contains(@class,' map/linktext ')]"/>
            </xsl:when>
            <xsl:otherwise>#none#</xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <xsl:when test="$scope='peer'">
          <xsl:choose>
            <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
            <xsl:otherwise>
              <xsl:text>#none#</xsl:text>
              <xsl:call-template name="output-message">
                <xsl:with-param name="msg">Missing navtitle attribute for: <xsl:value-of select="@href"/> 
                  When you set scope="peer" you must provide a local navigation title,
                  since the target is not accessible at build time.</xsl:with-param>
                <xsl:with-param name="msgnum">023</xsl:with-param>
                <xsl:with-param name="msgsev">E</xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="not($format='#none#' or $format='dita' or $format='DITA')">
          <xsl:choose>
            <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@href"/>
              <xsl:call-template name="output-message">
                <xsl:with-param name="msg">Missing navtitle attribute for: <xsl:value-of select="@href"/> 
                  When you set format to a non-DITA resource type, you must provide a
                  local navigation title, since the target is not accessible to
                  DITA-aware transforms.</xsl:with-param>
                <xsl:with-param name="msgnum">024</xsl:with-param>
                <xsl:with-param name="msgsev">E</xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <xsl:when test="@href=''"/>
        <xsl:when test="not(contains($file,$DITAEXT))">
          <xsl:value-of select="@href"/>
          <xsl:call-template name="output-message">
            <xsl:with-param name="msg">Unknown file extension in href: <xsl:value-of select="@href"/> 
              If this is a link to a non-DITA resource, set the format attribute to
              match the resource (for example, 'txt', 'pdf', or 'html'). 
              If it's a link to a DITA resource, the file extension must be .dita or .xml.</xsl:with-param>
            <xsl:with-param name="msgnum">015</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:when>

	<!--grabbing text from a particular topic in another file-->
	<xsl:when test="$topicpos='otherfile'">
	  <xsl:choose>
            <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/titlealts ')]/*[contains(@class, ' topic/navtitle ')]">              
              <xsl:apply-templates select="(document($file,/)//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/titlealts ')]/*[contains(@class, ' topic/navtitle ')]" mode="text-only"/>
            </xsl:when>
            <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/title ')]">
              <xsl:apply-templates select="(document($file,/)//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:when>
            <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class, ' topic/title ')]">
              <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
            </xsl:when>
	    <xsl:otherwise><xsl:call-template name="navtitle-fallback"/></xsl:otherwise>
          </xsl:choose>
        </xsl:when>

	<!--grabbing text from the first topic in another file-->
        <xsl:when test="$topicpos='firstinfile'">
            <xsl:choose>
               <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/titlealts ')]/*[contains(@class, ' topic/navtitle ')]">                 
                 <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/titlealts ')]/*[contains(@class, ' topic/navtitle ')]" mode="text-only"/>
               </xsl:when>
               <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]">               
                 <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
               </xsl:when>
              <xsl:otherwise><xsl:call-template name="navtitle-fallback"/></xsl:otherwise>
            </xsl:choose>
        </xsl:when>
	<xsl:otherwise><!--both topicpos values have been tested - no way to fire this--><xsl:value-of select="@href"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="navtitle">
      <xsl:value-of select="normalize-space($navtitle-not-normalized)"/>
    </xsl:variable>

    <xsl:if test="not($navtitle='#none#')">
        <xsl:attribute name="navtitle"><xsl:value-of select="$navtitle"/></xsl:attribute>
        <!--not using normal fallback of @href when format=dita and file-extension=dita; defer to rel-links to decide what to do with it, since the file extension will need to be massaged eg to .htm or .html, and that's an 
        output-specific choice that mappull shouldn't be dictating-->
    </xsl:if>
   </xsl:if>
   <xsl:choose>
     <xsl:when test="*[contains(@class,' map/topicmeta ')]">
        <xsl:for-each select="*[contains(@class,' map/topicmeta ')]">
          <xsl:copy>
            <xsl:copy-of select="@class"/>
            <xsl:for-each select="parent::*">
              <xsl:call-template name="getmetadata">
                <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
                <xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
                <xsl:with-param name="topicpos"><xsl:value-of select="$topicpos"/></xsl:with-param>
                <xsl:with-param name="topicid"><xsl:value-of select="$topicid"/></xsl:with-param>
                <xsl:with-param name="classval"><xsl:value-of select="$classval"/></xsl:with-param>
                <xsl:with-param name="scope"><xsl:value-of select="$scope"/></xsl:with-param>
                <xsl:with-param name="format"><xsl:value-of select="$format"/></xsl:with-param>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:copy>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <topicmeta class="- map/topicmeta ">
          <xsl:call-template name="getmetadata">
            <xsl:with-param name="type"><xsl:value-of select="$type"/></xsl:with-param>
            <xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
            <xsl:with-param name="topicpos"><xsl:value-of select="$topicpos"/></xsl:with-param>
            <xsl:with-param name="topicid"><xsl:value-of select="$topicid"/></xsl:with-param>
            <xsl:with-param name="classval"><xsl:value-of select="$classval"/></xsl:with-param>
            <xsl:with-param name="scope"><xsl:value-of select="$scope"/></xsl:with-param>
            <xsl:with-param name="format"><xsl:value-of select="$format"/></xsl:with-param>
          </xsl:call-template>
        </topicmeta>
      </xsl:otherwise>
   </xsl:choose>

</xsl:template>

<xsl:template name="navtitle-fallback">
  <xsl:choose>
    <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
    <xsl:when test="*/*[contains(@class,' map/linktext ')]">
      <xsl:value-of select="*/*[contains(@class,' map/linktext ')]"/>
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">Unable to retrieve navtitle from target: <xsl:value-of select="@href"/>.
          Make sure thetopicref type matches the target, and that the file name
          and topic id are correct. Using linktext instead.</xsl:with-param>
        <xsl:with-param name="msgnum">025</xsl:with-param>
        <xsl:with-param name="msgsev">W</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>#none#
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">Unable to retrieve navtitle from target: <xsl:value-of select="@href"/>.
          Make sure the topicref type matches the target, and that the file name
          and topic id are correct.</xsl:with-param>
        <xsl:with-param name="msgnum">026</xsl:with-param>
        <xsl:with-param name="msgsev">W</xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="getmetadata">
  <xsl:param name="type"/>
  <xsl:param name="scope">#none#</xsl:param>
  <xsl:param name="format">#none#</xsl:param>
  <xsl:param name="file"/>
  <xsl:param name="topicpos"/>
  <xsl:param name="topicid"/>
  <xsl:param name="classval"/>

<!--linktext-->
  <xsl:choose>
    <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"><xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]"/></xsl:when>
    <xsl:otherwise>
      <xsl:variable name="linktext">
        <xsl:choose>
          <!--if it's external and not dita, use the href as fallback-->
          <xsl:when test="($scope='external' and not($format='dita' or $format='DITA')) or $type='external'">
            <xsl:choose>
              <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!--if it's external and dita, leave empty as fallback, so that the final output process can handle file extension-->
          <xsl:when test="$scope='external'">
            <xsl:choose>
              <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
              <xsl:otherwise>#none#</xsl:otherwise>
            </xsl:choose>
          </xsl:when>

          <xsl:when test="$scope='peer'">
            <xsl:choose>
              <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
              <xsl:otherwise>#none#<xsl:call-template name="output-message">
                  <xsl:with-param name="msg">Missing linktext and navtitle for: <xsl:value-of select="@href"/> 
                    When you set scope="peer" you must use the navtitle attribute, since
                    the target is not accessible at build time.</xsl:with-param>
                  <xsl:with-param name="msgnum">027</xsl:with-param>
                  <xsl:with-param name="msgsev">E</xsl:with-param>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="not($format='#none#' or $format='dita' or $format='DITA')">
            <xsl:choose>
              <xsl:when test="@navtitle"><xsl:value-of select="@navtitle"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="@href"/>
                <xsl:call-template name="output-message">
                  <xsl:with-param name="msg">Missing linktext and navtitle attribute for: <xsl:value-of select="@href"/> 
                    When you set format to a non-DITA resource type, you must provide a
                    navtitle attribute, since the target is not accessible to DITA-aware
                    transforms.</xsl:with-param>
                  <xsl:with-param name="msgnum">028</xsl:with-param>
                  <xsl:with-param name="msgsev">E</xsl:with-param>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@href=''">#none#</xsl:when>
          <xsl:when test="not(contains($file,$DITAEXT))">#none#<xsl:call-template name="output-message">
              <xsl:with-param name="msg">Unknown file extension in href: <xsl:value-of select="@href"/> 
                If this is a link to a non-DITA resource, set the format attribute to
                match the resource (for example, 'txt', 'pdf', or 'html'). 
                If it's a link to a DITA resource, the file extension must be .dita or .xml .</xsl:with-param>
              <xsl:with-param name="msgnum">015</xsl:with-param>
              <xsl:with-param name="msgsev">E</xsl:with-param>
            </xsl:call-template>
          </xsl:when>

          <!--grabbing text from a particular topic in another file-->
          <xsl:when test="$topicpos='otherfile'">
            <xsl:choose>
              <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/title ')]">
                <xsl:variable name="grabbed-value">
                  <xsl:apply-templates select="(document($file,/)//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
                </xsl:variable>                
                <xsl:value-of select="normalize-space($grabbed-value)"/>
              </xsl:when>
              <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class, ' topic/title ')]">
                <xsl:variable name="grabbed-value">
                  <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
                </xsl:variable>
                <xsl:value-of select="normalize-space($grabbed-value)"/>
              </xsl:when>
              <xsl:otherwise><xsl:call-template name="linktext-fallback"/></xsl:otherwise>
            </xsl:choose>
          </xsl:when>

          <!--grabbing text from the first topic in another file-->
          <xsl:when test="$topicpos='firstinfile'">
            <xsl:choose>
              <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]">
                <xsl:variable name="grabbed-value">
                  <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
                </xsl:variable>                
                <xsl:value-of select="normalize-space($grabbed-value)"/>
              </xsl:when>
              <xsl:otherwise><xsl:call-template name="linktext-fallback"/></xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>#none#<!--never happens - both possible values for topicpos are tested--></xsl:otherwise> 
        </xsl:choose>
      </xsl:variable>
      <xsl:if test="not($linktext='#none#')">
        <linktext class="- map/linktext "><xsl:value-of select="$linktext"/></linktext>
      </xsl:if>
    </xsl:otherwise>
  </xsl:choose>

  <!--shortdesc-->
  <xsl:choose>
    <xsl:when test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]"><xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]"/></xsl:when>
    <xsl:when test="$scope='external' or $scope='peer' or $type='external' or not($format='#none#' or $format='dita' or $format='DITA')">
      <!-- do nothing - shortdesc is unavailable-->
    </xsl:when>
    <!--try retrieving from a particular topic in another file-->
    <xsl:when test="$topicpos='otherfile'">
      <xsl:choose>
        <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]/*"/>
        <xsl:when test="document($file,/)//*[contains(@class, $classval)][@id=$topicid]/*[contains(@class, ' topic/shortdesc ')]">
          <xsl:variable name="shortdesc-value">
            <xsl:apply-templates select="(document($file,/)//*[contains(@class, $classval)][@id=$topicid])[1]/*[contains(@class, ' topic/shortdesc ')]" mode="text-only"/>
          </xsl:variable>          
          <shortdesc class="- map/shortdesc "><xsl:value-of select="$shortdesc-value"/></shortdesc>
        </xsl:when>
        <xsl:otherwise><!--no shortdesc - optional element, so no warning-->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <!--try retrieving from the first topic in another file-->
    <xsl:when test="$topicpos='firstinfile'">
      <xsl:choose>
        <xsl:when test="document($file,/)//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/shortdesc ')]">
          <xsl:variable name="shortdesc-value">
            <xsl:apply-templates select="(document($file,/)//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/shortdesc ')]" mode="text-only"/>
          </xsl:variable>          
          <shortdesc class="- map/shortdesc "><xsl:value-of select="$shortdesc-value"/></shortdesc>
        </xsl:when>
        <xsl:otherwise><!--no shortdesc - since optional, no warning-->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise><!--shortdesc optional - no warning if absent-->
    </xsl:otherwise>
  </xsl:choose>

  <!--metadata to be written - if we add logic at some point to pull metadata from topics into the map-->
  <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[not(contains(@class, ' map/linktext '))][not(contains(@class, ' map/shortdesc '))]"/>

</xsl:template>

<xsl:template name="linktext-fallback">
  <xsl:choose>
    <xsl:when test="@navtitle">
      <xsl:value-of select="@navtitle"/>
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">Unable to retrieve linktext from target: <xsl:value-of select="@href"/>
          Using navtitle instead. Make sure the topicref type matches the target,
          and that the file name and topic id are correct.</xsl:with-param>
        <xsl:with-param name="msgnum">029</xsl:with-param>
        <xsl:with-param name="msgsev">W</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>#none#<xsl:call-template name="output-message">
        <xsl:with-param name="msg">Unable to retrieve linktext from target: <xsl:value-of select="@href"/>
          Make sure the topicref type matches the target, and that the file name
          and topic id are correct.</xsl:with-param>
        <xsl:with-param name="msgnum">031</xsl:with-param>
        <xsl:with-param name="msgsev">W</xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/image ')]" mode="text-only">
  <xsl:choose>
    <xsl:when test="*[contains(@class,' topic/alt ')]"><xsl:apply-templates mode="text-only"/></xsl:when>
    <xsl:when test="@alt"><xsl:value-of select="@alt"/></xsl:when>
  </xsl:choose>
</xsl:template>
<xsl:template match="*[contains(@class,' topic/boolean ')]" mode="text-only">
  <xsl:value-of select="name()"/><xsl:text>: </xsl:text><xsl:value-of select="@state"/>
</xsl:template>
<xsl:template match="*[contains(@class,' topic/state ')]" mode="text-only">
  <xsl:value-of select="name()"/><xsl:text>: </xsl:text><xsl:value-of select="@name"/><xsl:text>=</xsl:text><xsl:value-of select="@value"/>
</xsl:template>
<xsl:template match="*[contains(@class,' topic/indexterm ')]" mode="text-only"/>

<xsl:template match="*[contains(@class, ' ui-d/menucascade ')]" mode="text-only">
  <xsl:apply-templates select="*" mode="text-only"/>
</xsl:template>

<xsl:template match="*[contains(@class, ' ui-d/uicontrol ')]" mode="text-only">
  
  <xsl:if test="parent::*[contains(@class,' ui-d/menucascade ')] and preceding-sibling::*[contains(@class, ' ui-d/uicontrol ')]">
    <xsl:text> > </xsl:text>
  </xsl:if>
  <xsl:apply-templates select="*|text()" mode="text-only"/>
</xsl:template>


<xsl:template match="*" mode="text-only">
  <xsl:apply-templates select="text()|*" mode="text-only"/>
</xsl:template>

<xsl:template match="*|@*|comment()|processing-instruction()|text()">
  <xsl:copy>
    <xsl:apply-templates 
     select="*|@*|comment()|processing-instruction()|text()"/>
  </xsl:copy>
</xsl:template>

<!--template is here to make sure topicmeta gets copied in cases where the topicref has no href (and therefore the getstuff template isn't called-->
<xsl:template match="*[contains(@class, ' map/topicref ')]/*[contains(@class, ' map/topicmeta ')]">
	<xsl:variable name="format"><xsl:for-each select="parent::*">
		<xsl:call-template name="inherit"><xsl:with-param name="attrib">format</xsl:with-param></xsl:call-template>
	</xsl:for-each></xsl:variable>
	<xsl:variable name="scope"><xsl:for-each select="parent::*">
		<xsl:call-template name="inherit"><xsl:with-param name="attrib">scope</xsl:with-param></xsl:call-template>
	</xsl:for-each></xsl:variable>
	<xsl:variable name="type"><xsl:for-each select="parent::*">
		<xsl:call-template name="inherit"><xsl:with-param name="attrib">type</xsl:with-param></xsl:call-template>
	</xsl:for-each></xsl:variable>

	<xsl:if test="not(parent::*/@href)">
	    <xsl:copy>
		    <xsl:apply-templates select="@*|*"/>
	    </xsl:copy>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
