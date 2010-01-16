function treediagram_onloadSvgRoot(evt)
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
      treediagram_stretch(evt.target);
  }
  else
  {
    setTimeout(function(){
        treediagram_stretch(evt.target);
      },1);
  }

  function treediagram_stretch(svgNode)
  {
      var diagramNode = svgNode.getElementsByTagNameNS("http://www.w3.org/2000/svg", "g").item(0);
      var diagramClass = diagramNode.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");
      treediagram_Dispatch[diagramClass]["init"](diagramNode);
      treediagram_Dispatch[diagramClass]["setRank"](diagramNode);
      treediagram_Dispatch[diagramClass]["setDepth"](diagramNode);
      treediagram_Dispatch[diagramClass]["place"](diagramNode);
      var width=treediagram_Dispatch[diagramClass]["getWidth"](diagramNode) - 0;
      var height=treediagram_Dispatch[diagramClass]["getHeight"](diagramNode) - 0;
      var scale = 1;
      var xscale = width / treediagram_Constants.diagram_max_width;
      var yscale = height / treediagram_Constants.diagram_max_height;
      if (xscale > scale) { scale = xscale; }
      if (yscale > scale) { scale = yscale; }
      svgNode.setAttribute("width", width / scale * treediagram_Constants.diagram_scale + treediagram_Constants.diagram_unit);
      svgNode.setAttribute("height", height / scale * treediagram_Constants.diagram_scale + treediagram_Constants.diagram_unit);
      svgNode.setAttribute("viewBox", "0 0 " + width + " " + height);
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

function treediagram_getDispatchableChildren(g, elementName, className)
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
              child.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "role") == className))
        {
          result.push(child);
        }
      }
    }
    child = child.nextSibling; 
  }
  return result;
}
