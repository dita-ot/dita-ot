// boxed (groupcomp, kwd, var, delim, ...)

syntaxdiagram_Dispatch["boxed"] = new Array;
syntaxdiagram_Dispatch["boxed"]["init"] = syntaxdiagram_boxed_init;
syntaxdiagram_Dispatch["boxed"]["place"] = syntaxdiagram_boxed_place;
syntaxdiagram_Dispatch["boxed"]["getHeightAbove"] = syntaxdiagram_boxed_getHeightAbove;
syntaxdiagram_Dispatch["boxed"]["getHeightBelow"] = syntaxdiagram_boxed_getHeightBelow;
syntaxdiagram_Dispatch["boxed"]["getWidth"] = syntaxdiagram_boxed_getWidth;

function syntaxdiagram_boxed_init(g)
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
    syntaxdiagram_Dispatch[childClass]["place"](children[i], runningWidth + syntaxdiagram_Constants.box_padding_horizontal, 0);
    runningWidth = runningWidth + syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]);
    maxHeightAbove = Math.max(maxHeightAbove, syntaxdiagram_Dispatch[childClass]["getHeightAbove"](children[i]));
    maxHeightBelow = Math.max(maxHeightBelow, syntaxdiagram_Dispatch[childClass]["getHeightBelow"](children[i]));
    if (i != children.length - 1)
    {
      runningWidth = runningWidth + syntaxdiagram_Constants.box_medial_padding[g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "element")];
    }
  }

  // Surrounding box.
  var box = document.createElementNS("http://www.w3.org/2000/svg", "rect");
  box.setAttribute("x", 0);
  box.setAttribute("y", 0 - maxHeightAbove - syntaxdiagram_Constants.box_padding_vertical);
  box.setAttribute("width", runningWidth + 2 * syntaxdiagram_Constants.box_padding_horizontal);
  box.setAttribute("height", maxHeightAbove + maxHeightBelow + 2 * syntaxdiagram_Constants.box_padding_vertical);
  box.setAttribute("rx", syntaxdiagram_Constants.box_corner_radius[g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "element")]);
  g.appendChild(box);

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", runningWidth + 2 * syntaxdiagram_Constants.box_padding_horizontal);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", maxHeightAbove + syntaxdiagram_Constants.box_padding_vertical);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", maxHeightBelow + syntaxdiagram_Constants.box_padding_vertical);

}

function syntaxdiagram_boxed_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + x + "," + y + ")");
}

function syntaxdiagram_boxed_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_boxed_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_boxed_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
