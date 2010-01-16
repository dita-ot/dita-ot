// unboxed (groupcomp members)

syntaxdiagram_Dispatch["unboxed"] = new Array;
syntaxdiagram_Dispatch["unboxed"]["init"] = syntaxdiagram_unboxed_init;
syntaxdiagram_Dispatch["unboxed"]["place"] = syntaxdiagram_unboxed_place;
syntaxdiagram_Dispatch["unboxed"]["getHeightAbove"] = syntaxdiagram_unboxed_getHeightAbove;
syntaxdiagram_Dispatch["unboxed"]["getHeightBelow"] = syntaxdiagram_unboxed_getHeightBelow;
syntaxdiagram_Dispatch["unboxed"]["getWidth"] = syntaxdiagram_unboxed_getWidth;

function syntaxdiagram_unboxed_init(g)
{
  var children = syntaxdiagram_getDispatchableChildren(g, null, null);
  
  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    syntaxdiagram_Dispatch[childClass]["init"](children[i]);
  }

  var runningWidth = 0;
  var maxHeightAbove = 0;
  var maxHeightBelow = 0;
  
  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    syntaxdiagram_Dispatch[childClass]["place"](children[i], runningWidth, 0);
    runningWidth = runningWidth + syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]);
    maxHeightAbove = Math.max(maxHeightAbove, syntaxdiagram_Dispatch[childClass]["getHeightAbove"](children[i]));
    maxHeightBelow = Math.max(maxHeightBelow, syntaxdiagram_Dispatch[childClass]["getHeightBelow"](children[i]));
  }

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", runningWidth);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", maxHeightAbove);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", maxHeightBelow);

}

function syntaxdiagram_unboxed_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + x + "," + y + ")");
}

function syntaxdiagram_unboxed_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_unboxed_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_unboxed_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
