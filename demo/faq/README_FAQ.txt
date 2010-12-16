## <!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

FAQ EXAMPLE

This example gives a demonstration of topic specialization by
defining a simple FAQ topic.

The purpose of the FAQ topic is to model a list of frequently asked
questions.  The FAQ topic uses a specialization of the simpletable
element to model the list so multiple types of information can be
tracked for each FAQ item.

The example includes the DTD module, an XSL module for generating HTML
output, and a sample data file.


Drop the demo source file ditafaq.xml onto a Mozilla-class browser to
see the effect of CSS styling on literally rendered DITA XML elements.
Drag/drop the demo file onto IE6 to see the effect of a DITA XML topic
rendered by XSLT directly in the browser.

CAVEAT

The FAQ example is provided only as an example of topic specialization
and not as a standard topic type within the DITA architecture. Other
design approaches are possible.

For instance, you could model a FAQ list as a domain specialization
of the simpletable element.  This approach would let you provide a FAQ
list within topic types such as concept or reference.

Or, you could model a FAQ list with each FAQ item as a separate
topic.  This approach would make it easy to assemble different kinds
of FAQ lists from a pool of FAQ items.

The design for a FAQ information type would need to be validated with
a community.


FAQ EXAMPLE FILES

This directory contains the FAQ example, including:

ditafaq.xml
    - a sample data source file

ditafaq.html
    - a sample data output file

faq.mod
    - topic module to define the FAQ information type

faq_shell.dtd
    - shell DTD to merge base and FAQ information types

faq.css
    - convenience CSS for styling FAQ elements (for instance, in
	  editors such as XMetaL)

faq_shell.css
    - convenience shell CSS to merge base and FAQ styles

faq2html.xsl
    - XSL module to format the FAQ deltas as HTML

faq2html_shell.xsl
    - shell XSL to merge base and FAQ formatting

faq2fo.xsl
    - XSL module to format the FAQ deltas as XSL-FO

faq2fo_shell.xsl
    - shell XSL to merge base and FAQ formatting

faq_strings.xml
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

    java com.icl.saxon.StyleSheet -o ditafaq.html ditafaq.xml faq2html_shell.xsl
    java com.icl.saxon.StyleSheet -o ditafaq.fo ditafaq.xml faq2fo_shell.xsl


FEEDBACK

If you run into problems or want to discuss this example, please feel
free to ask questions on the DITA forum:

    news://news.software.ibm.com:119/ibm.software.developerworks.xml.dita


Have fun exploring,


The DITA Team


Java is a registered trademark of Sun Microsystems, Inc..