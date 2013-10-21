<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:rtf="rtf_namespace" 
	exclude-result-prefixes="rtf">

<!--
Defining the most important components of a valid RTF file.
NOTE: 
The RTF prolog must be at the very beginning of the document, 
otherwise the RTF format won't be recognised.
RTF ignores line breaks but usually not white space!
 -->	
<xsl:variable name="rtf:prolog">{\rtf1\ansi\ansicpg<xsl:value-of select="$code-page"/>\deff0\deflang1033
{\fonttbl
{\f0\fswiss MS Sans Serif;}
{\f1\froman\fcharset2 Symbol;}
{\f2\froman Times New Roman;}
{\f3\froman Times New Roman;}
{\f4\fswiss Arial;}
{\f5\fmono Courier New;}
}
{\colortbl\red0\green0\blue0;\red0\green0\blue255;\red128\green128\blue128;\red255\green0\blue0;\red0\green255\blue0;}
{\stylesheet{\s0 \f2\fs24 Normal;}
{\s1 \f4\fs48\b heading 1;}
{\s2 \f4\fs36\b heading 2;}
{\s3 \f4\fs24\b heading 3;}
{\s4 \f4\fs20\b heading 4;}
{\s5 \f4\fs18\b heading 5;}
{\s6 \f4\fs16\b heading 6;}
{\s7 \f4\fs24\b table header;}
{\s8 \f2\fs24 link;}
{\s9 \f4\fs24\b table title;}
}
</xsl:variable>

<xsl:variable name="rtf:xml_declaration"><xsl:text disable-output-escaping="yes">&lt;?xml version="1.0" encoding="UTF-8"?&gt;</xsl:text>
</xsl:variable>

<xsl:variable name="rtf:line_break">
\line 
</xsl:variable>

<xsl:variable name="rtf:closing_bracket">}</xsl:variable>

</xsl:stylesheet>

