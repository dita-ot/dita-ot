## <!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

ELEMENTREF EXAMPLE

This example gives a demonstration of topic specialization by
defining a simple elementref topic.

The purpose of the elementref topic is to model a reference description
of an XML element and its attributes.  The elementref topic uses a
specialization of the simpletable element to model the attribute list so
multiple attributes or element properties can be described for each
elementref element.

The example includes the DTD module, an XSL module for generating HTML
output, and a sample data file.


Drop the demo source file map.xml or topicref.xml onto a Mozilla-class
browser to see the effect of CSS styling on literally rendered DITA XML
elements. Drag/drop the demo file onto IE6 to see the effect of a DITA
XML topic rendered by XSLT directly in the browser.

CAVEAT

The elementref example is provided only as an example of topic specialization
and not as a standard topic type within the DITA architecture. Other
design approaches are possible.

For instance, you could model semantically-important phrases like
<element>topic</element> and <attr>class</attr> by deriving them from
apiname, which itself derives from keyword.  A "markup domain" is under
consideration but has not been defined yet.  Your ideas are welcome!


elementref EXAMPLE FILES

This directory contains the elementref examples, including:

topicref.xml, map.xml, commonlrdefs.xml
    - sample data source files

topicref.html, map.html
    - sample data output files

elementref.mod
    - topic module to define the elementref information type

elementref_shell.dtd
    - shell DTD to merge base and elementref information types

elementref.css
    - convenience CSS for styling elementref elements (for instance, in
	  editors such as XMetaL)

elementref_shell.css
    - convenience shell CSS to merge base and elementref styles

elementref2html.xsl
    - XSL module to format the elementref deltas as HTML

elementref2html_shell.xsl
    - shell XSL to merge base and elementref formatting

elementref2fo.xsl
    - XSL module to format the elementref deltas as XSL-FO

elementref2fo_shell.xsl
    - shell XSL to merge base and elementref formatting

elementref_strings.xml
    - example internationalization file


GENERATING OUTPUT FROM THE EXAMPLE

To generate HTML output, you need to install an XSLT processor.

There are many options for XSLT processor.  For one example, you can
install a Java�  runtime (JRE) and an XSLT processor such as Saxon.

JRE
    - Get a JRE (or a JDK bundling a JRE) such as the Sun J2SE plaform:

          http://java.sun.com/j2se/downloads.html

      Java�  comes with an install program.

Saxon
    - http://saxon.sourceforge.net/

      To install Saxon, you unzip the package and add the saxon.jar 
      file to the CLASSPATH environment variable.

Using Saxon, you can generate HTML or XSL formatting objects from the sample data with

    java com.icl.saxon.StyleSheet -o topicref.html topicref.xml elementref2html_shell.xsl
    java com.icl.saxon.StyleSheet -o topicref.fo topicref.xml elementref2fo_shell.xsl


FEEDBACK

If you run into problems or want to discuss this example, please feel
free to ask questions on the DITA forum:

    news://news.software.ibm.com:119/ibm.software.developerworks.xml.dita


Have fun exploring,


The DITA Team


Java is a registered trademark of Sun Microsystems, Inc..