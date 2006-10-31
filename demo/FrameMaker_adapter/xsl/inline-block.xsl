<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Inline v. block: These elements are always inline. -->
    <xsl:template match="*[contains(@class, ' topic/boolean ')
                        or contains(@class, ' topic/cite ')
                        or contains(@class, ' topic/fn ')
                        or contains(@class, ' topic/indextermref ')
                        or contains(@class, ' topic/image ') and (@placement != 'break')
                        or contains(@class, ' topic/itemgroup ')
                        or contains(@class, ' topic/keyword ')
                        or contains(@class, ' topic/ph ')
                        or contains(@class, ' topic/q ')
                        or contains(@class, ' topic/state ')
                        or contains(@class, ' topic/term ')
                        or contains(@class, ' topic/tm ')
                        or contains(@class, ' topic/xref ')]" mode="is-block">
      <xsl:text>n</xsl:text>
    </xsl:template>

    <!-- Inline v. block: These elements have no width. -->
    <xsl:template match="*[contains(@class, ' topic/indexterm ')]" mode="is-block">
      <xsl:text></xsl:text>
    </xsl:template>

    <!-- Inline v. block: Text is inline. -->
    <xsl:template match="text()" mode="is-block">
      <xsl:text>n</xsl:text>
    </xsl:template>

    <!-- Inline v. block: Whitespace inside preformatted elements is sacrosanct. -->
    <xsl:template match="*[contains(@class, ' topic/pre ')
                        or contains(@class, ' topic/lines ')]//text()" mode="is-block" priority="1.0">
      <xsl:text>n</xsl:text>
    </xsl:template>

    <!-- Inline v. block: Empty text might have no width. -->
    <xsl:template match="text()[string-length(normalize-space()) = 0]" mode="is-block">
      <xsl:variable name="parent-is-block">
        <xsl:apply-templates select=".." mode="is-block"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$parent-is-block = 'y'">
          <!-- Parent element is block. -->
          <xsl:variable name="preceding-block">
            <xsl:apply-templates select="preceding-sibling::node()[1]" mode="is-block-chase-preceding"/>
          </xsl:variable>
          <xsl:variable name="following-block">
            <xsl:apply-templates select="following-sibling::node()[1]" mode="is-block-chase-following"/>
          </xsl:variable>
          <!-- Vanish unless preceding element is inline and following element is inline. -->
          <xsl:choose>
            <xsl:when test="starts-with($following-block, 'n') and
                            string-length($preceding-block) &gt; 0 and
                            substring($preceding-block, string-length($preceding-block)) = 'n'">
              <xsl:text>n</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text></xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <!-- Parent element is inline. Always preserve this text. -->
          <xsl:text>n</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
   
    <!-- Inline v. block: Everything else is block. -->
    <xsl:template match="*" mode="is-block">
      <xsl:text>y</xsl:text>
    </xsl:template>

    <!-- Find the first non-vanishing element before this one. -->
    <xsl:template match="*" mode="is-block-chase-preceding">
      <xsl:variable name="is-block">
        <xsl:apply-templates select="." mode="is-block"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$is-block">
          <xsl:value-of select="$is-block"/>
        </xsl:when>
        <xsl:when test="count(preceding-sibling::node()) = 0">
          <xsl:text></xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="preceding-sibling::node()[1]" mode="is-block-chase-preceding"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="text()" mode="is-block-chase-preceding">
      <xsl:choose>
        <xsl:when test="string-length(normalize-space(.)) &gt; 0">
          <xsl:text>n</xsl:text>
        </xsl:when>
        <xsl:when test="count(preceding-sibling::node()) = 0">
          <xsl:text></xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="preceding-sibling::node()[1]" mode="is-block-chase-preceding"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
 
    <!-- Find the first non-vanishing element after this one. -->
    <xsl:template match="*" mode="is-block-chase-following">
      <xsl:variable name="is-block">
        <xsl:apply-templates select="." mode="is-block"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$is-block">
          <xsl:value-of select="$is-block"/>
        </xsl:when>
        <xsl:when test="count(following-sibling::node()) = 0">
          <xsl:text></xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="following-sibling::node()[1]" mode="is-block-chase-following"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="text()" mode="is-block-chase-following">
      <xsl:choose>
        <xsl:when test="string-length(normalize-space(.)) &gt; 0">
          <xsl:text>n</xsl:text>
        </xsl:when>
        <xsl:when test="count(following-sibling::node()) = 0">
          <xsl:text></xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="following-sibling::node()[1]" mode="is-block-chase-following"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
