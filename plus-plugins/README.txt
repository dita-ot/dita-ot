Plus plugins

This set of plugins provides a framework for extensible HTML-based
transformations.  The transformation types "html+", "xhtml+",
"eclipsehelp+" and "htmlhelp+" produce the same output as the basic
DITA-OT ones, but there are a number of extension points which enable
you to add additional functionality to the transforms.

Extra functionality out of the box includes:
* Rasterization of SVG images referenced in <image> elements.
* Presentation MathML in topics can be rendered to SVG (and then
  optionally rasterized).
* DITA Programming Domain <syntaxdiagram> elements rendered as
  railroad diagrams in SVG (which can be optionally rasterized).
* Tree diagram domain, for representing hierarchical diagrams,
  plus processing to render these as SVG (which can be optionally
  rasterized).
* XHTML transformation is properly in XHTML namespace.
* HTML Help transformation uses the current machine's character
  encoding, resolving most garbage-character incidents.
* HTML Help transformation much more configurable, including
  window size, navigation pane tabs, etc.
* Rudimentary context-sensitive help support for HTML Help
  transformation.
* Breadcrumb ancestor links in topics.

Many of these additions require additional free software.  Requirements
may include:
* Apache Ant 1.7 [ant.jar]
* Apache Batik 1.7, an SVG processor [batik-all.jar]
* Batik Rasterizer Ant task (contributed code to Batik) [rasterizertask.jar]
* JEuclid 3.0.x (not 3.1, which has a different API),
  a MathML renderer [jeuclid-core-3.0.x.jar]
* Saxon 9 Basic, an XSLT 2.0 processor [saxon9.jar, saxon9-dom.jar]
* Mozilla Rhino, a JavaScript interpreter [js.jar]
* Apache Commons External APIs [xml-apis-ext.jar]
* Apache Commons Logging APIs [commons-logging-api-1.1.jar]
Ensure that these are available in the classpath before invoking the
transform.  Each plugin contains a document doc/reference.xml which
describes its prerequisites.

Support for these plugins is available by posting to the dita-users
Yahoo group.

These plugins are distributed under the same terms as DITA Open Toolkit.

Deborah Pickett
Moldflow Pty Ltd
January 2009

