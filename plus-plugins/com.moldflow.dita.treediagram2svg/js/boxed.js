treediagram_Dispatch["boxed"] = new Array;
treediagram_Dispatch["boxed"]["init"] = treediagram_boxed_init;
treediagram_Dispatch["boxed"]["place"] = treediagram_boxed_place;
treediagram_Dispatch["boxed"]["getHeightAbove"] = treediagram_boxed_getHeightAbove;
treediagram_Dispatch["boxed"]["getHeightBelow"] = treediagram_boxed_getHeightBelow;

function treediagram_boxed_init(g)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");
    treediagram_Dispatch[childClass]["init"](children[i]);
  }
  
  var runningWidth = 0;
  var maxHeightAbove = 0;
  var maxHeightBelow = 0;
  
  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");
    treediagram_Dispatch[childClass]["place"](children[i], runningWidth + treediagram_Constants.box_padding_horizontal, 0);
    runningWidth = runningWidth + treediagram_Dispatch[childClass]["getWidth"](children[i]);
    maxHeightAbove = Math.max(maxHeightAbove, treediagram_Dispatch[childClass]["getHeightAbove"](children[i]));
    maxHeightBelow = Math.max(maxHeightBelow, treediagram_Dispatch[childClass]["getHeightBelow"](children[i]));
  }

  // Surrounding box.
  var box = document.createElementNS("http://www.w3.org/2000/svg", "rect");
  box.setAttribute("x", 0);
  box.setAttribute("y", 0 - maxHeightAbove - treediagram_Constants.box_padding_vertical);
  box.setAttribute("width", runningWidth + 2 * treediagram_Constants.box_padding_horizontal);
  box.setAttribute("height", maxHeightAbove + maxHeightBelow + 2 * treediagram_Constants.box_padding_vertical);
  box.setAttribute("rx", treediagram_Constants.box_corner_radius);
  g.appendChild(box);

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:width", runningWidth + 2 * treediagram_Constants.box_padding_horizontal);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:heightAbove", maxHeightAbove + treediagram_Constants.box_padding_vertical);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:heightBelow", maxHeightBelow + treediagram_Constants.box_padding_vertical);

  //treediagram_boxed_place(g, 10, 10);
}

function treediagram_boxed_place(g, x, y)
{
  g.setAttribute("transform", "translate(" + x + "," + (y + (g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "heightAbove")-0)) + ")");
}

function treediagram_boxed_getHeightAbove(g)
{
  return   g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "heightAbove") - 0;
}

function treediagram_boxed_getHeightBelow(g)
{
  return   g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "heightBelow") - 0;
}
