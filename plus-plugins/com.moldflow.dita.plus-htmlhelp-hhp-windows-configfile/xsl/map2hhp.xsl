<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:modular="http://www.moldflow.com/namespace/2008/dita/plus-htmlhelp-hhp-modular"
    xmlns:config="http://www.moldflow.com/namespace/2008/dita/plus-htmlhelp-hhp-windows-configfile">

    <xsl:param name="plus-htmlhelp-windows-configfile"/>

    <xsl:template match="/ | node()" mode="modular:windows">
        <xsl:if test="string-length($plus-htmlhelp-windows-configfile) &gt; 0">
            <xsl:apply-templates select="document($plus-htmlhelp-windows-configfile)/*"
                mode="config:windows">
                <xsl:with-param name="map" select="/*[contains(@class, ' map/map ')]"/>
            </xsl:apply-templates>
        </xsl:if>
        <xsl:next-match>
            <xsl:fallback>
                <xsl:message>
                    <xsl:text>Cannot use xsl:next-match in XSLT 1.0.</xsl:text>
                </xsl:message>
            </xsl:fallback>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="windows" mode="config:windows">
        <xsl:param name="map"/>
        <xsl:apply-templates select="*" mode="config:window">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="window" mode="config:window">
        <xsl:param name="map"/>
        <xsl:apply-templates select="." mode="config:arg0"/>
        <xsl:text>=</xsl:text>
        <xsl:apply-templates select="." mode="config:arg1">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg2"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg3"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg4">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg5">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg6">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg7">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg8">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg9">
            <xsl:with-param name="map" select="$map"/>
        </xsl:apply-templates>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg10"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg11"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg12"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg13"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg14"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg15"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg16"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg17"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg18"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg19"/>
        <xsl:text>,</xsl:text>
        <xsl:apply-templates select="." mode="config:arg20"/>
        <xsl:text>
</xsl:text>
    </xsl:template>

    <!-- Argument 0 (before = sign) is window type. Often supplied by programmer who wrote call to HtmlHelp(). -->
    <xsl:template match="window" mode="config:arg0">
        <xsl:value-of select="@type"/>
    </xsl:template>

    <!-- Argument 1 is window title. -->
    <xsl:template match="window" mode="config:arg1">
        <xsl:param name="map"/>
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <!-- Todo: what if title has quotation marks?  What is the escape character? -->
            <xsl:when test="$map/*[contains(@class, ' topic/title ')]">
                <!-- Todo: handle bookmaps with titlealts gracefully (using apply-templates) -->
                <xsl:value-of select="$map/*[contains(@class, ' topic/title ')]"/>
            </xsl:when>
            <xsl:when test="$map/@title">
                <xsl:value-of select="$map/@title"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>Help window</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    <xsl:text>"</xsl:text>
    </xsl:template>
    
    <!-- Argument 2 is ToC (*.hhc) file. -->
    <xsl:template match="window" mode="config:arg2">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="$HHCNAME"/>
        <xsl:text>"</xsl:text>
    </xsl:template>
    
    <!-- Argument 3 is index (*.hhk) file.  -->
    <xsl:template match="window" mode="config:arg3">
        <xsl:text>"</xsl:text>
        <xsl:if test="$USEINDEX = 'yes' and not(@indextab = 'no')">
            <xsl:value-of select="substring-before($HHCNAME, '.hhc')"/>
            <xsl:text>.hhk</xsl:text>
        </xsl:if>
        <xsl:text>"</xsl:text>
    </xsl:template>
    
    <!-- Argument 4 is default (start) topic.  -->
    <xsl:template match="window" mode="config:arg4">
        <xsl:param name="map"/>
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <xsl:when test="contains(@defaulttopic, $DITAEXT)">
                <xsl:value-of select="substring-before(@defaulttopic, $DITAEXT)"/><xsl:value-of select="$OUTEXT"/>
            </xsl:when>
            <xsl:when test="@defaulttopic">
                <xsl:value-of select="@defaulttopic"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="defaulttopic">
                    <xsl:apply-templates select="$map/descendant::*[contains(@class, ' map/topicref ')][@href][contains(@href,$DITAEXT) or contains(@href,'.htm')][1]" mode="defaulttopic"/>
                </xsl:variable>
                <xsl:variable name="const_newline">
                    <xsl:text>
</xsl:text>
                </xsl:variable>
                <xsl:choose>
                    <!-- Argh, template adds a newline.  Bad! -->
                    <xsl:when test="contains($defaulttopic, $const_newline)">
                        <xsl:value-of select="substring-before($defaulttopic, $const_newline)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$defaulttopic"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <!-- Argument 5 is home button destination. -->
    <xsl:template match="window" mode="config:arg5">
        <xsl:param name="map"/>
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <xsl:when test="contains(@hometopic, $DITAEXT)">
                <xsl:value-of select="substring-before(@hometopic, $DITAEXT)"/><xsl:value-of select="$OUTEXT"/>
            </xsl:when>
            <xsl:when test="@hometopic">
                <xsl:value-of select="@hometopic"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="defaulttopic">
                    <xsl:apply-templates select="$map/descendant::*[contains(@class, ' map/topicref ')][@href][contains(@href,$DITAEXT) or contains(@href,'.htm')][1]" mode="defaulttopic"/>
                </xsl:variable>
                <xsl:variable name="const_newline">
                    <xsl:text>
</xsl:text>
                </xsl:variable>
                <xsl:choose>
                    <!-- Argh, template adds a newline.  Bad! -->
                    <xsl:when test="contains($defaulttopic, $const_newline)">
                        <xsl:value-of select="substring-before($defaulttopic, $const_newline)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$defaulttopic"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>"</xsl:text>
    </xsl:template>
    
    <!-- Argument 6 is Jump 1 button destination -->
    <xsl:template match="window" mode="config:arg6">
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <xsl:when test="contains(@jump1topic, $DITAEXT)">
                <xsl:value-of select="substring-before(@jump1topic, $DITAEXT)"/><xsl:value-of select="$OUTEXT"/>
            </xsl:when>
            <xsl:when test="@jump1topic">
                <xsl:value-of select="jump1topic"/>
            </xsl:when>
        </xsl:choose>
        <xsl:text>"</xsl:text>
    </xsl:template>  
    
    <!-- Argument 7 is Jump 1 button text -->
    <xsl:template match="window" mode="config:arg7">
        <!-- Todo: I18N -->
        <xsl:text>"</xsl:text>
        <xsl:value-of select="@jump1text"/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <!-- Argument 8 is Jump 2 button destination -->
    <xsl:template match="window" mode="config:arg8">
        <xsl:text>"</xsl:text>
        <xsl:choose>
            <xsl:when test="contains(@jump2topic, $DITAEXT)">
                <xsl:value-of select="substring-before(@jump2topic, $DITAEXT)"/><xsl:value-of select="$OUTEXT"/>
            </xsl:when>
            <xsl:when test="@jump2topic">
                <xsl:value-of select="jump2topic"/>
            </xsl:when>
        </xsl:choose>
        <xsl:text>"</xsl:text>
    </xsl:template>  
    
    <!-- Argument 9 is Jump 1 button text -->
    <xsl:template match="window" mode="config:arg9">
        <!-- Todo: I18N -->
        <xsl:text>"</xsl:text>
        <xsl:value-of select="@jump2text"/>
        <xsl:text>"</xsl:text>
    </xsl:template>
    
    <!-- Argument 10 is bitfield for navigation pane styles. -->
    <xsl:template match="window" mode="config:arg10">
        <!-- Why do it in hex?  Because XSLT 1.0 has only floats, not integers, and we don't want scientific notation. --> 
        <xsl:text>0x</xsl:text>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="@windowmargin  =  'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="contains(concat(' ', @customtabs, ' '), ' 6 ')"/>
            <xsl:with-param name="bit1" select="contains(concat(' ', @customtabs, ' '), ' 7 ')"/>
            <xsl:with-param name="bit2" select="contains(concat(' ', @customtabs, ' '), ' 8 ')"/>
            <xsl:with-param name="bit3" select="contains(concat(' ', @customtabs, ' '), ' 9 ')"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="contains(concat(' ', @customtabs, ' '), ' 2 ')"/>
            <xsl:with-param name="bit1" select="contains(concat(' ', @customtabs, ' '), ' 3 ')"/>
            <xsl:with-param name="bit2" select="contains(concat(' ', @customtabs, ' '), ' 4 ')"/>
            <xsl:with-param name="bit3" select="contains(concat(' ', @customtabs, ' '), ' 5 ')"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="@msdnmenu = 'yes'"/>
            <xsl:with-param name="bit1" select="@advancedsearch  =  'yes'"/>
            <xsl:with-param name="bit2" select="not(@remembersize  =  'no')"/>
            <xsl:with-param name="bit3" select="contains(concat(' ', @customtabs, ' '), ' 1 ')"/>
        </xsl:call-template> 
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="not(@favoritestab  =  'no')"/>
            <xsl:with-param name="bit1" select="@currentintitlebar  =  'yes'"/>
            <xsl:with-param name="bit2" select="@hidecontentwindow = 'yes'"/>
            <xsl:with-param name="bit3" select="@hidetoolbar = 'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="not(@syncpanes  =  'no')"/>
            <xsl:with-param name="bit1" select="@sendtrackingmessages = 'yes'"/>
            <xsl:with-param name="bit2" select="not(@searchtab  =  'no')"/>
            <xsl:with-param name="bit3" select="@historytab = 'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="@extendedwindowstyles = 'nodefault'"/>
            <xsl:with-param name="bit1" select="not(@tripane  =  'no')"/>
            <xsl:with-param name="bit2" select="@toolbarbuttontext  =  'no'"/>
            <xsl:with-param name="bit3" select="@sendquitmessage = 'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="@hideshow = 'auto'"/>
            <xsl:with-param name="bit1" select="@alwaysontop = 'yes'"/>
            <xsl:with-param name="bit2" select="@titlebar = 'no'"/>
            <xsl:with-param name="bit3" select="@windowstyles = 'nodefault'"/>
        </xsl:call-template>
    </xsl:template>
    
    <!-- Argument 11 is navigation pane width.  -->
    <xsl:template match="window" mode="config:arg11">
        <xsl:choose>
            <xsl:when test="@navwidth">
                <xsl:value-of select="@navwidth"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>200</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Argument 12 is bitfield of toolbar buttons. -->
    <xsl:template match="window" mode="config:arg12">
        <!-- Why do it in hex?  Because XSLT 1.0 has only floats, not integers, and we don't want scientific notation. --> 
        <xsl:text>0x</xsl:text>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="buttons/@font  =  'yes'"/>
            <xsl:with-param name="bit1" select="buttons/@nextcontents  =  'yes'"/>
            <xsl:with-param name="bit2" select="buttons/@prevcontents  =  'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="buttons/@history = 'yes'"/>
            <xsl:with-param name="bit1" select="buttons/@favorites = 'yes'"/>
            <xsl:with-param name="bit2" select="buttons/@jump1 = 'yes' or @jump1topic or @jump1text"/>
            <xsl:with-param name="bit3" select="buttons/@jump2 = 'yes' or @jump2topic or @jump2text"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="not(buttons/@options  =  'no')"/>
            <xsl:with-param name="bit1" select="not(buttons/@print  =  'no')"/>
            <xsl:with-param name="bit2" select="buttons/@index = 'yes'"/>
            <xsl:with-param name="bit3" select="buttons/@search = 'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="buttons/@previous = 'yes'"/>
            <xsl:with-param name="bit1" select="buttons/@notes = 'yes'"/>
            <xsl:with-param name="bit2" select="buttons/@contents = 'yes'"/>
            <xsl:with-param name="bit3" select="buttons/@locate  =  'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit0" select="buttons/@stop  =  'yes'"/>
            <xsl:with-param name="bit1" select="buttons/@refresh  =  'yes'"/>
            <xsl:with-param name="bit2" select="not(buttons/@home  =  'no')"/>
            <xsl:with-param name="bit3" select="buttons/@next = 'yes'"/>
        </xsl:call-template>
        <xsl:call-template name="config:hexdigit">
            <xsl:with-param name="bit1" select="not(buttons/@hideshow  =  'no')"/>
            <xsl:with-param name="bit2" select="not(buttons/@back  =  'no')"/>
            <xsl:with-param name="bit3" select="buttons/@forward  =  'yes'"/>
        </xsl:call-template>
    </xsl:template>
    
    <!-- Argument 13 is geometry of window. -->
    <xsl:template match="window" mode="config:arg13">
        <xsl:text>[</xsl:text>
        <xsl:choose>
            <xsl:when test="@x"><xsl:value-of select="@x"/></xsl:when>
            <xsl:otherwise>100</xsl:otherwise>
        </xsl:choose>
        <xsl:text>,</xsl:text>
        <xsl:choose>
            <xsl:when test="@y"><xsl:value-of select="@y"/></xsl:when>
            <xsl:otherwise>100</xsl:otherwise>
        </xsl:choose>
        <xsl:text>,</xsl:text>
        <xsl:choose>
            <xsl:when test="@x and @width"><xsl:value-of select="@x + @width"/></xsl:when>
            <xsl:when test="@width"><xsl:value-of select="100 + @width"/></xsl:when>
            <xsl:when test="@x"><xsl:value-of select="@x + 300"/></xsl:when>
            <xsl:otherwise>400</xsl:otherwise>
        </xsl:choose>
        <xsl:text>,</xsl:text>
        <xsl:choose>
            <xsl:when test="@y and @height"><xsl:value-of select="@y + @height"/></xsl:when>
            <xsl:when test="@height"><xsl:value-of select="100 + @height"/></xsl:when>
            <xsl:when test="@y"><xsl:value-of select="@y + 200"/></xsl:when>
            <xsl:otherwise>300</xsl:otherwise>
        </xsl:choose>
        <xsl:text>]</xsl:text>
    </xsl:template>
    
    <!-- Argument 14 is window style (http://msdn.microsoft.com/en-us/library/ms632600(VS.85).aspx). -->
    <xsl:template match="window" mode="config:arg14">
        <xsl:choose>
            <xsl:when test="@style">
                <xsl:value-of select="@style"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- A reasonable default. -->
                <xsl:text>0xb0000</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Argument 15 is window extended style (http://msdn.microsoft.com/en-us/library/ms632680(VS.85).aspx). -->
    <xsl:template match="window" mode="config:arg15">
        <xsl:choose>
            <xsl:when test="@exstyle">
                <xsl:value-of select="@exstyle"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- A reasonable default. -->
                <xsl:text>0x0</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Argument 16 is window show state (http://msdn.microsoft.com/en-us/library/ms633548(VS.85).aspx). -->
    <xsl:template match="window" mode="config:arg16">
        <xsl:choose>
            <xsl:when test="@showstate">
                <xsl:value-of select="@showstate"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- A reasonable default. -->
                <xsl:text></xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Argument 17 is navigation pane open or closed. -->
    <xsl:template match="window" mode="config:arg17">
        <xsl:value-of select="number(@navigationstart = 'closed')"/>        
    </xsl:template>
    
    <!-- Argument 18 is navigation pane default. -->
    <xsl:template match="window" mode="config:arg18">
        <xsl:value-of select="
            0 * number(@navigationdefault = 'toc') +
            1 * number(@navigationdefault = 'index') +
            2 * number(@navigationdefault = 'search') +
            3 * number(@navigationdefault = 'favorites') +
            4 * number(@navigationdefault = 'history') +
            5 * number(@navigationdefault = 'author') +
            11 * number(@navigationdefault = 'custom1') +
            12 * number(@navigationdefault = 'custom2') +
            13 * number(@navigationdefault = 'custom3') +
            14 * number(@navigationdefault = 'custom4') +
            15 * number(@navigationdefault = 'custom5') +
            16 * number(@navigationdefault = 'custom6') +
            17 * number(@navigationdefault = 'custom7') +
            18 * number(@navigationdefault = 'custom8') +
            19 * number(@navigationdefault = 'custom9')
            "/>
    </xsl:template>
    
    <!-- Argument 19 is position of navigation panes. -->
    <xsl:template match="window" mode="config:arg19">
        <xsl:value-of select="
            0 * number(@tabposition = 'top') +
            1 * number(@tabposition = 'left') +
            2 * number(@tabposition = 'bottom')
            "></xsl:value-of>
    </xsl:template>
    
    <!--  Argument 20 is notification ID. -->
    <xsl:template match="window" mode="config:arg20">
        <xsl:value-of select="@notifyid"/>
    </xsl:template>
    
    <xsl:template name="config:hexdigit">
        <xsl:param name="bit0" select="0"/>
        <xsl:param name="bit1" select="0"/>
        <xsl:param name="bit2" select="0"/>
        <xsl:param name="bit3" select="0"/>
        <xsl:variable name="nibble" select="8 * number($bit3) + 4 * number($bit2) + 2 * number($bit1) + 1 * number($bit0)"/>
        <xsl:value-of select="substring('0123456789abcdef', $nibble+1, 1)"/>
    </xsl:template>
</xsl:stylesheet>
