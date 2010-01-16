// loop (usually groupseq)

syntaxdiagram_Dispatch["loop"] = new Array;
syntaxdiagram_Dispatch["loop"]["init"] = syntaxdiagram_loop_init;
syntaxdiagram_Dispatch["loop"]["place"] = syntaxdiagram_loop_place;
syntaxdiagram_Dispatch["loop"]["getHeightAbove"] = syntaxdiagram_loop_getHeightAbove;
syntaxdiagram_Dispatch["loop"]["getHeightBelow"] = syntaxdiagram_loop_getHeightBelow;
syntaxdiagram_Dispatch["loop"]["getWidth"] = syntaxdiagram_loop_getWidth;

function syntaxdiagram_loop_init(g)
{
  var repsepList = syntaxdiagram_getDispatchableChildren(g, null, "repsep");
  var repsep;
  var repsepClass;
  var repsepHeightAbove = syntaxdiagram_Constants.arrow_size;
  var repsepHeightBelow = Math.max(syntaxdiagram_Constants.arrow_size, syntaxdiagram_Constants.loop_corner_radius);
  var repsepWidth = 0;
  
  if (repsepList.length == 1)
  {
    repsep = repsepList[0];
    repsepClass = repsep.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    syntaxdiagram_Dispatch[repsepClass]["init"](repsep);
    repsepHeightAbove = Math.max(repsepHeightAbove, syntaxdiagram_Dispatch[repsepClass]["getHeightAbove"](repsep));
    repsepHeightBelow = Math.max(repsepHeightBelow, syntaxdiagram_Dispatch[repsepClass]["getHeightBelow"](repsep));
    repsepWidth = syntaxdiagram_Dispatch[repsepClass]["getWidth"](repsep);
  }
  
  var children = syntaxdiagram_getDispatchableChildren(g, null, "forward");
  var child;
  var childClass;
  var childHeightAbove = syntaxdiagram_Constants.arrow_size + syntaxdiagram_Constants.loop_corner_radius;
  var childHeightBelow = syntaxdiagram_Constants.arrow_size;
  var childWidth = 0;
  
  if (children.length == 1)
  {
    child = syntaxdiagram_getDispatchableChildren(g, null, "forward")[0];
    childClass = child.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    syntaxdiagram_Dispatch[childClass]["init"](child);
    childHeightAbove = Math.max(childHeightAbove, syntaxdiagram_Dispatch[childClass]["getHeightAbove"](child));
    childHeightBelow = Math.max(childHeightBelow, syntaxdiagram_Dispatch[childClass]["getHeightBelow"](child));
    childWidth = syntaxdiagram_Dispatch[childClass]["getWidth"](child);
  }
  
  var maxWidth = Math.max(repsepWidth, childWidth);

  var runningWidth = 0;

  var initialLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
  initialLine.setAttribute("class", "arrow");
  initialLine.setAttribute("x1", 0);
  initialLine.setAttribute("y1", 0);
  runningWidth = runningWidth + syntaxdiagram_Constants.loop_corner_radius + syntaxdiagram_Constants.loop_join_length_initial + (maxWidth/2) - (childWidth/2);
  initialLine.setAttribute("x2", runningWidth);
  initialLine.setAttribute("y2", 0);
  g.appendChild(initialLine);
  g.appendChild(syntaxdiagram_arrowHead(runningWidth, 0, 0));
  
  if (children.length == 1)
  {
    syntaxdiagram_Dispatch[childClass]["place"](child, runningWidth, 0);
  }
  runningWidth = runningWidth + childWidth;
    
  var finalLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
  finalLine.setAttribute("class", "arrow");
  finalLine.setAttribute("x1", runningWidth);
  finalLine.setAttribute("y1", 0);
  runningWidth = runningWidth + syntaxdiagram_Constants.loop_corner_radius + syntaxdiagram_Constants.loop_join_length_final + (maxWidth/2) - (childWidth/2);
  finalLine.setAttribute("x2", runningWidth);
  finalLine.setAttribute("y2", 0);
  g.appendChild(finalLine);
  g.appendChild(syntaxdiagram_arrowHead(runningWidth - syntaxdiagram_Constants.loop_corner_radius, 0, 0));

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", runningWidth);
  
  var returnHeightAbove = childHeightAbove + syntaxdiagram_Constants.loop_row_padding + repsepHeightBelow;
  
  var initialReturnLine = document.createElementNS("http://www.w3.org/2000/svg", "path");
  initialReturnLine.setAttribute("class", "arrow");
  initialReturnLine.setAttribute("d",
      "M" + (runningWidth - syntaxdiagram_Constants.loop_corner_radius) + " " + 0 + " " +
      "Q" + (runningWidth) + " " + 0 + " " + (runningWidth) + " " + (0 - syntaxdiagram_Constants.loop_corner_radius) + " " +
      "L" + (runningWidth) + " " + (0 - returnHeightAbove + syntaxdiagram_Constants.loop_corner_radius) + " " +
      "Q" + (runningWidth) + " " + (0 - returnHeightAbove) + " " + (runningWidth - syntaxdiagram_Constants.loop_corner_radius) + " " + (0 - returnHeightAbove) + " " +
      "L" + (runningWidth - syntaxdiagram_Constants.loop_corner_radius - syntaxdiagram_Constants.loop_join_length_final - maxWidth/2 + repsepWidth/2) + "," + (0 - returnHeightAbove) 
    );
  g.appendChild(initialReturnLine);
  runningWidth = runningWidth - syntaxdiagram_Constants.loop_corner_radius - syntaxdiagram_Constants.loop_join_length_final - maxWidth/2 + repsepWidth/2;
  g.appendChild(syntaxdiagram_arrowHead(runningWidth, 0 - returnHeightAbove, 180));
  
  runningWidth = runningWidth - repsepWidth;
  
  if (repsepList.length == 1)
  {
    syntaxdiagram_Dispatch[repsepClass]["place"](repsep, runningWidth, 0 - returnHeightAbove);
  }

  var finalReturnLine = document.createElementNS("http://www.w3.org/2000/svg", "path");
  finalReturnLine.setAttribute("class", "arrow");
  finalReturnLine.setAttribute("d",
      "M" + (runningWidth) + " " + (0 - returnHeightAbove) + " " +
      "L" + (syntaxdiagram_Constants.loop_corner_radius) + " " + (0 - returnHeightAbove) + " " + 
      "Q" + (0) + " " + (0 - returnHeightAbove) + " " + (0) + " " + (0 - returnHeightAbove + syntaxdiagram_Constants.loop_corner_radius) + " " +
      "L" + (0) + " " + (0 - syntaxdiagram_Constants.loop_corner_radius) + " " +
      "Q" + (0) + " " + (0) + " " + (syntaxdiagram_Constants.loop_corner_radius) + " " + (0)
    );
  g.appendChild(finalReturnLine);

  //var finalReturnLine = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
  //finalReturnLine.setAttribute("class", "arrow");
  //finalReturnLine.setAttribute("points",
  //    (runningWidth) + "," + (0 - returnHeightAbove) + " " + 
  //    (0) + "," + (0 - returnHeightAbove) + " " + 
  //    (0) + "," + (0)
  //  );
  //g.appendChild(finalReturnLine);
  g.appendChild(syntaxdiagram_arrowHead(0, 0 - syntaxdiagram_Constants.loop_corner_radius, 90));
  
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", childHeightAbove + syntaxdiagram_Constants.loop_row_padding + repsepHeightBelow + repsepHeightAbove);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", childHeightBelow);

}

function syntaxdiagram_loop_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + x + "," + y + ")");
}

function syntaxdiagram_loop_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_loop_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_loop_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
