// sequence (usually groupseq)

syntaxdiagram_Dispatch["sequence"] = new Array;
syntaxdiagram_Dispatch["sequence"]["init"] = syntaxdiagram_sequence_init;
syntaxdiagram_Dispatch["sequence"]["place"] = syntaxdiagram_sequence_place;
syntaxdiagram_Dispatch["sequence"]["getHeightAbove"] = syntaxdiagram_sequence_getHeightAbove;
syntaxdiagram_Dispatch["sequence"]["getHeightBelow"] = syntaxdiagram_sequence_getHeightBelow;
syntaxdiagram_Dispatch["sequence"]["getWidth"] = syntaxdiagram_sequence_getWidth;

function syntaxdiagram_sequence_init(g)
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
    syntaxdiagram_Dispatch[childClass]["place"](children[i], runningWidth, 0);
    runningWidth = runningWidth + syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]);
    maxHeightAbove = Math.max(maxHeightAbove, syntaxdiagram_Dispatch[childClass]["getHeightAbove"](children[i]));
    maxHeightBelow = Math.max(maxHeightBelow, syntaxdiagram_Dispatch[childClass]["getHeightBelow"](children[i]));
    
    if (i != children.length - 1)
    {
      var medialLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
      medialLine.setAttribute("class", "arrow");
      medialLine.setAttribute("x1", runningWidth);
      medialLine.setAttribute("y1", 0);
      runningWidth = runningWidth + syntaxdiagram_Constants.sequence_join_length_medial
      medialLine.setAttribute("x2", runningWidth);
      medialLine.setAttribute("y2", 0);
      g.appendChild(medialLine);
      g.appendChild(syntaxdiagram_arrowHead(runningWidth, 0, 0));
    }
  }

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", runningWidth);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", maxHeightAbove);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", maxHeightBelow);

}

function syntaxdiagram_sequence_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + x + "," + y + ")");
}

function syntaxdiagram_sequence_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_sequence_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_sequence_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
