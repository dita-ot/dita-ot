// This little kludge to get around Firefox's rendering order not
// being strictly bottom-up; courtesy of bjoern_ on #svg on freenode.

function syntaxdiagram_stretch(svgNode)
{
      //var svgNode = document.getElementsByTagNameNS("http://www.w3.org/2000/svg", "svg").item(0);
      var diagramNode = svgNode.getElementsByTagNameNS("http://www.w3.org/2000/svg", "g").item(0);
      var diagramClass = diagramNode.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
      syntaxdiagram_Dispatch[diagramClass]["init"](diagramNode);
      syntaxdiagram_Dispatch[diagramClass]["place"](diagramNode, 0, 0);
      var width = syntaxdiagram_Dispatch[diagramClass]["getWidth"](diagramNode) - 0;
      var height = (syntaxdiagram_Dispatch[diagramClass]["getHeightAbove"](diagramNode) - 0) + (syntaxdiagram_Dispatch[diagramClass]["getHeightBelow"](diagramNode) - 0);
      var scale = 1;
      var xscale = width / syntaxdiagram_Constants.diagram_max_width;
      var yscale = height / syntaxdiagram_Constants.diagram_max_height;
      if (xscale > scale) { scale = xscale; }
      if (yscale > scale) { scale = yscale; }
      svgNode.setAttribute("width", width / scale * syntaxdiagram_Constants.diagram_scale + syntaxdiagram_Constants.diagram_unit);
      svgNode.setAttribute("height", height / scale * syntaxdiagram_Constants.diagram_scale + syntaxdiagram_Constants.diagram_unit);
      svgNode.setAttribute("viewBox", "0 0 " + width + " " + height);
}

function syntaxdiagram_onloadSvgRoot(evt)
{
  // Detect browser.
  // Thanks to http://www.quirksmode.org/js/detect.html
  var usetimeoutkludge = false;
  
  try {
    if (navigator &&
        navigator.userAgent &&
        (navigator.userAgent.toLowerCase().indexOf("firefox") != -1
        || navigator.userAgent.toLowerCase().indexOf("applewebkit") != -1))
    {
      // Firefox and WebKit need an initial rendering before bounding boxes' sizes are known.
      usetimeoutkludge = true;
    }
  }
  catch (e)
  {
    usetimeoutkludge = false;
  }
      
  if (!usetimeoutkludge)
  {
      syntaxdiagram_stretch(evt.target);
  }
  else
  {
    setTimeout(function(){
        syntaxdiagram_stretch(evt.target);
      },1);
  }
}

// This kludge for IE Node constants courtesy of 
// http://www-128.ibm.com/developerworks/xml/library/x-matters41.html
if (!window['Node']) {
    window.Node = new Object();
    Node.ELEMENT_NODE = 1;
    Node.ATTRIBUTE_NODE = 2;
    Node.TEXT_NODE = 3;
    Node.CDATA_SECTION_NODE = 4;
    Node.ENTITY_REFERENCE_NODE = 5;
    Node.ENTITY_NODE = 6;
    Node.PROCESSING_INSTRUCTION_NODE = 7;
    Node.COMMENT_NODE = 8;
    Node.DOCUMENT_NODE = 9;
    Node.DOCUMENT_TYPE_NODE = 10;
    Node.DOCUMENT_FRAGMENT_NODE = 11;
    Node.NOTATION_NODE = 12;
}

function syntaxdiagram_getDispatchableChildren(g, elementName, className)
{
  var result = new Array;
  var child = g.firstChild;
  while (child != null)
  {
    if (child.nodeType == Node.ELEMENT_NODE)
    {
      if (elementName == null || elementName == child.localName && "http://www.w3.org/2000/svg" == child.namespaceURI)
      {
        if (className == null ||
             (child.hasAttributes() &&
              child.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "role") == className))
        {
          result.push(child);
        }
      }
    }
    child = child.nextSibling; 
  }
  return result;
}

function syntaxdiagram_arrowHead(x, y, angle)
{
  var g = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
  g.setAttribute("class", "arrowhead");
  g.setAttribute("points",
      (x) + "," + (y) + " " + 
      (x - syntaxdiagram_Constants.arrow_size) + "," + (y + syntaxdiagram_Constants.arrow_size / 2) + " " + 
      (x - syntaxdiagram_Constants.arrow_size) + "," + (y - syntaxdiagram_Constants.arrow_size / 2)
    );
  g.setAttribute("transform", "rotate(" + angle + "," + x + "," + y + ")");
  return g;
}
