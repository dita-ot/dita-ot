// Container for a box and associated note callouts

syntaxdiagram_Dispatch["notecontainer"] = new Array;
syntaxdiagram_Dispatch["notecontainer"]["init"] = syntaxdiagram_notecontainer_init;
syntaxdiagram_Dispatch["notecontainer"]["place"] = syntaxdiagram_notecontainer_place;
syntaxdiagram_Dispatch["notecontainer"]["getHeightAbove"] = syntaxdiagram_notecontainer_getHeightAbove;
syntaxdiagram_Dispatch["notecontainer"]["getHeightBelow"] = syntaxdiagram_notecontainer_getHeightBelow;
syntaxdiagram_Dispatch["notecontainer"]["getWidth"] = syntaxdiagram_notecontainer_getWidth;

function syntaxdiagram_notecontainer_init(g)
{
  var children = syntaxdiagram_getDispatchableChildren(g, null, null);
  
  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    syntaxdiagram_Dispatch[childClass]["init"](children[i]);
  }

  var runningWidth = 0;
  var maxHeightAbove = syntaxdiagram_Constants.arrow_size;
  var maxHeightBelow = syntaxdiagram_Constants.arrow_size;

  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    
    if (childClass == "note")
    {
      var line = document.createElementNS("http://www.w3.org/2000/svg", "line");
      line.setAttribute("class", "arrow");
      line.setAttribute("x1", runningWidth);
      line.setAttribute("y1", 0);
      runningWidth = runningWidth + syntaxdiagram_Constants.note_padding_sides;
      syntaxdiagram_Dispatch[childClass]["place"](children[i], runningWidth, 0);
      runningWidth = runningWidth + syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]) + syntaxdiagram_Constants.note_padding_sides;
      line.setAttribute("x2", runningWidth);
      line.setAttribute("y2", 0);
      g.appendChild(line);
    }
    else
    {
      runningWidth = runningWidth + syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]);
    }

    maxHeightAbove = Math.max(maxHeightAbove, syntaxdiagram_Dispatch[childClass]["getHeightAbove"](children[i]));
    maxHeightBelow = Math.max(maxHeightBelow, syntaxdiagram_Dispatch[childClass]["getHeightBelow"](children[i]));
  }

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", runningWidth);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", maxHeightAbove);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", maxHeightBelow);

}

function syntaxdiagram_notecontainer_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + x + "," + y + ")");
}

function syntaxdiagram_notecontainer_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_notecontainer_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_notecontainer_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
