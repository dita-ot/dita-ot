<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
	xmlns:rtf="rtf_namespace" 
	exclude-result-prefixes="rtf">

<!--
Defining the most important components of a valid RTF file.

NOTE: 
* The RTF prolog must be at the very beginning of the document, 
otherwise the RTF format won't be recognised.
* RTF ignores line breaks but usually not whitespace!
 -->	
<xsl:variable name="rtf:prolog">
{\rtf1 \ansi \deff0 
</xsl:variable>

<xsl:variable name="rtf:font_table">
{\fonttbl 
{\f0 \froman Times New Roman;} 
{\f1 \fswiss Arial;} 
{\f2 \fmodern Courier New;}
}
</xsl:variable>

<xsl:variable name="rtf:color_table">
{\colortbl 
; 
\red0\green0\blue0; 
\red0\green0\blue255; 
\red128\green128\blue128; 
\red255\green0\blue0; 
\red0\green255\blue0; 
}
</xsl:variable>

<xsl:variable name="rtf:stylesheet">
{\stylesheet
{\*\cs0 Default Paragraph Font;}
{\s0 \f0\fs24 Normal;}
{\s1 \f1\fs48\b\sb120\sa240 Heading 1;}
{\s2 \f1\fs36\b\sb90\sa180 Heading 2;}
{\s3 \f1\fs24\b\sa120 Heading 3;}
{\s4 \f1\fs20\b\sa100 Heading 4;}
{\s5 \f1\fs18\b\sa90 Heading 5;}
{\s6 \f1\fs16\b\sa80 Heading 6;}
{\s7 \f1\fs24\b Table Header;}
{\s8 \f0\fs24\ul\cf2 Link;}
{\s9 \f1\fs24\b Table Title;}
}
</xsl:variable>

<xsl:variable name="rtf:preliminaries">
\deflang1033 \plain \fs24 \ql \fi0 \li0 \widowctrl \hyphauto \ftnbj
</xsl:variable>

</xsl:stylesheet>