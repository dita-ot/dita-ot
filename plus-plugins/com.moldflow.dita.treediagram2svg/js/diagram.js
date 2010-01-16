treediagram_Dispatch["diagram"] = new Array;
treediagram_Dispatch["diagram"]["init"] = treediagram_diagram_init;
treediagram_Dispatch["diagram"]["setRank"] = treediagram_diagram_setRank;
treediagram_Dispatch["diagram"]["setDepth"] = treediagram_diagram_setDepth;
treediagram_Dispatch["diagram"]["place"] = treediagram_diagram_place;
treediagram_Dispatch["diagram"]["getWidth"] = treediagram_diagram_getWidth;
treediagram_Dispatch["diagram"]["getHeight"] = treediagram_diagram_getHeight;

function treediagram_diagram_init(g)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");
    treediagram_Dispatch[childClass]["init"](children[i]);
  }
}

function treediagram_diagram_setRank(g)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

    treediagram_Dispatch[childClass]["setRank"](children[i], 0);
  }
  
}

function treediagram_diagram_setDepth(g)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

    // Discard return value.
    treediagram_Dispatch[childClass]["setDepth"](children[i]);
  }
}

function treediagram_diagram_place(g)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  var x = treediagram_Constants.diagram_margin_sides - 0;
  
  var height = 0;

  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

    treediagram_Dispatch[childClass]["setMiddle"](children[i]);
    var leftExtent = treediagram_Dispatch[childClass]["leftExtent"](children[i]);
    var rightExtent = treediagram_Dispatch[childClass]["rightExtent"](children[i]);
    height = Math.max(height, treediagram_Dispatch[childClass]["place"](children[i], x + leftExtent, treediagram_Constants.diagram_margin_topbottom - 0));
    x += leftExtent + rightExtent + (treediagram_Constants.diagram_margin_forest - 0);
  }
  x += treediagram_Constants.diagram_margin_sides - treediagram_Constants.diagram_margin_forest;
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:width", x);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:height", height + (treediagram_Constants.diagram_margin_topbottom - 0));
}

function treediagram_diagram_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "width");
}

function treediagram_diagram_getHeight(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "height");
}

