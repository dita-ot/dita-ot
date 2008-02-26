Welcome to Idiom's FO output for the DITA Open Toolkit
======================================================

Idiom Technologies has donated its production quality PDF output for use with 
the DITA Open Toolkit. This has been integrated into the Toolkit as a plugin. 
Features of this code include:

- Index (with extensions) and table of contents generation
- Font mapping: characters can be remapped to different fonts. This is 
  particularly useful for CJK font support, and is configured per-locale.
- Extensive customization of publishing output by setting variables
  (67 of them) and attribute-sets (391 of them).
- A catalog-based customization framework where you can customize variables, 
  attribute-sets and XSLs without modifying Open Toolkit code: your custom 
  settings live in files in a separate customization hierarchy.
- Bookmap/bookinfo support.

This plugin has only been tested with Sun's JDK 1.4 and may not work
with the IBM JDK due to a conflict with XEP's crimson.jar.

Building and installing Idiom's FO output
=========================================

You will need the following software, which we cannot include with
this plugin due to licensing and redistribution restrictions:

- RenderX XEP: Idiom's FO output currently requires RenderX XEP to
  convert FO to PDF. The personal edition of XEP can be obtained for
  free from RenderX (http://www.renderx.com). XEP comes with Saxon
  6.5.3, which this plugin also needs. But if you are using JDK 1.5 or 
  newer, you will need Saxon 6.5.5 or newer which has fixes for JDK 1.5
  compatibility. Download Saxon 6.5.5 from http://saxon.sourceforge.net.

- ICU4J: You can find this library by clicking on the "Download ICU" link
  at http://icu.sourceforge.net. Look for the ICU4J downloads. ICU4J is
  an optional jar that will give you better collation of index entries.

 To install and use with the Open Toolkit's distribution,

1. Install the DITA Open Toolkit.
2. Unzip this plugin into an Open Toolkit installation's
   demo directory.
3. Install XEP personal edition and license into demo/fo/lib/xep.
4. If you are using JDK 1.5, install saxon.jar in demo/fo/lib/xep/lib.
5. Install the ICU4j jar (optional) as demo/fo/lib/icu4j.jar
6. From the Open Toolkit directory, run "ant -f integrator.xml"

At this point, the FO output is fully integrated into the Open
Toolkit's pipeline. The plugin infrastructure will detect the FO
plugin in step 5 and provide a "pdf2" output: this will invoke this FO
output instead of the previous FOP-based output. Invoke with a command
line like:

java -jar lib/dost.jar /i:doc/DITA-readme.ditamap /transtype:pdf2

If you are using the startcmd.bat/sh script, make sure the classpath in that 
script lists saxon.jar before xalan.jar. 

About the index extensions
==========================

The index generation feature adds extensions to DITA's indexterm that can be
expressed as content of that element. These extensions are expressed in a 
FrameMaker-like syntax in the element's textual content, and provide 
functionality not yet available in the standard indexterm element. They also
help migration from legacy FrameMaker content. For example, the following 
generates a "see also" entry:

	<indexterm>Carp:<$nopage>see also Goldfish</indexterm>
	
As:

	Carp, 34
	   see also Goldfish	
	
The extended syntax consists of:

	: (colon)       Separates levels in an entry
	; (semicolon)   Separates entries in a marker
	[] (brackets)   Specifies a special sort order for the entry
	<$startrange>   Indicates the beginning of a page range
	<$endrange>     Indicates the end of a page range
	<$nopage>       Suppresses the page number in the entry (for example, in
	                a See entry)
	<$singlepage>   In a marker that contains several entries, restores the 
	                page number for an entry that follows a <$nopage>
	                building block

DITA 1.1 indexing elements have been implemented in this plugin that
should make these extensions unnecessary. You can now express see/see
also, sort order and page ranges using standard DITA 1.1 elements.

About /Customization
====================

This directory is where the custom files live that make up customized
versions of the FO publishing outputs.  Idiom's FO publishing output will 
look for certain files here to override the standard ones. Things you can 
currently override include:

  - Custom XSL via fo/xsl/custom.xsl and fo/attrs/custom.xsl
  - Layout overrides via fo/layout-masters.xml
  - Font overrides via fo/font-mappings.xml
  - Per-locale variable overrides via common/vars/[locale].xml
  - I18N configuration via fo/i18n/[locale].xml
  - Index configuration via fo/index/[locale].xml

When customizing any of these areas, modify the relevant file(s) in
/Customization.  Then, to enable the changes in the publishing process, 
you find the corresponding entry for each file you modified in 
/Customization/catalog.xml. It should look like this:

    <!--uri name="cfg:fo/attrs/custom.xsl" uri="fo/attrs/custom.xsl"/-->

Remove the comment markers "!--" and "--" to enable the change:

    <uri name="cfg:fo/attrs/custom.xsl" uri="fo/attrs/custom.xsl"/>

Your customization should now be enabled as part of the publishing process.

Idiom has provided template files that you can start with throughout
this directory structure.  These files end in the suffix ".orig" (for
example, "catalog.xml.orig").  To enable these files, make a copy of
them and remove the ".orig" suffix.  For example, copy
"catalog.xml.orig" to "catalog.xml".  You can then make modifications
to the copy.

Idiom's FO output also provides a general configuration file called
"build.properties" that allows you to control the publishing process.
To modify these settings, copy "build.properties.orig" to
"build.properties" and then modify the relevant options.

History
=======

FO Plugin Release 1.4.2
Available since Feb 26, 2008

- Misc internal fixes.

Sourceforge bug fixes:
- 1710233: summary element in bookmeta not handled properly (15251)
- 1744350: Navtitle not supported in PDF2
- 1807277: FO topicmerge needs to discard reltables

----------------------------------------------------------------------------

FO Plugin Release 1.4.1
Available since Oct 5, 2007

- more flexible index configuration (see common/index/zh_CN.xml for example)

Sourceforge bug fixes:
- 1793307: issue with topicref in bookmap's <notices> (16587)
- 1791403: invalid column-width attribute value (16586)
- 1805389: XSLT errors in FO plugin
- 1694607: Use-by-reference footnotes not rendered correctly (15249), (not quite right).

----------------------------------------------------------------------------

FO Plugin Release 1.4

- Rebased with Idiom's internal code as of August 28, 2007.
- Updated DITA 1.1 support: new bookmap and indexing elements.
- Minor fixes

Sourceforge bug fixes:
- 1686323: Idiom FO plug-in fails during pdf2 build (15348)
- 1729594: Topichead not supported in pdf2 output (15247) 
- 1647267: Single-step tasks numbers the step (15248)

----------------------------------------------------------------------------

FO Plugin Release 1.2.1

- rebased with Idiom's internal code as of Nov 6, 2006.
- updated copyright/licensing notices in files.
- fixed image copying task for customization.

Sourceforge bug fixes:
- 1574115: Using 1.3 and Idiom FO 1.2 plugin fail
- 1523653: pdf2 transform assumes basedir = ditadir (for real, this time!)

----------------------------------------------------------------------------

FO Plugin Release 1.2
Available since Sept 29, 2006

- rebased with Idiom's internal code as of Sept 28, 2006
- removed icu4j requirement: it will be used if found, otherwise the built-in
  Java Collator will be used.
- some preliminary DITA 1.1 support. No point going into details since the 
  standard is still in flux.

Bug fixes (IDs are Idiom's internal tracking numbers):
- 11492: image sometimes indented too far right
- 11625: footnote text's formatting not rendered
- 10955: tables with titles should be numbered sequentially
- 11273: table col span @namest/@nameend not respected
- 11432: <p> tag should be formatted according to enclosing tag
- 10849: nested codeblocks misrendered
- 10755: NullPointerException processing <dita> elements

Sourceforge bug fix:
- 1523653: pdf2 transform assumes basedir = ditadir

----------------------------------------------------------------------------

FO Plugin Release 1.1
Available since June 15, 2006

- rebased with Idiom's internal build 8.0.1.1.7
- improved performance for documents with many xrefs/links, such as the
  DITA Language Reference
- fixed indexterm in topicmeta bug
- fixed topicmerge issues that affected (among other things) chapter 
  division rendering for bookmaps
- misc bug fixes

----------------------------------------------------------------------------

FO Plugin release 1.0
Available since Feb. 23, 2006

- first release of Idiom's open source donation.

============================================================================

Copyright © 2005 by Idiom Technologies, Inc. All rights reserved. 
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other 
trademarks are the property of their respective owners. 

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH 
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF 
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING 
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.
