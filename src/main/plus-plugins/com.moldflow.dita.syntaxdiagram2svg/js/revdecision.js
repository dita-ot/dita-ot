// revdecision (usually groupchoice)

syntaxdiagram_Dispatch["revdecision"] = new Array;
syntaxdiagram_Dispatch["revdecision"]["init"] = syntaxdiagram_revdecision_init;
syntaxdiagram_Dispatch["revdecision"]["place"] = syntaxdiagram_revdecision_place;
syntaxdiagram_Dispatch["revdecision"]["getHeightAbove"] = syntaxdiagram_revdecision_getHeightAbove;
syntaxdiagram_Dispatch["revdecision"]["getHeightBelow"] = syntaxdiagram_revdecision_getHeightBelow;
syntaxdiagram_Dispatch["revdecision"]["getWidth"] = syntaxdiagram_revdecision_getWidth;

function syntaxdiagram_revdecision_init(g)
{
  var maxWidth = 0;
  
  var children = syntaxdiagram_getDispatchableChildren(g, null, null);
  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    syntaxdiagram_Dispatch[childClass]["init"](children[i]);
    maxWidth = Math.max(maxWidth, syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]));
  }

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", 4 * syntaxdiagram_Constants.decision_corner_radius + syntaxdiagram_Constants.decision_join_length_initial + syntaxdiagram_Constants.decision_join_length_final + maxWidth);
 
  
  // Straight-through line.
  var straightList = syntaxdiagram_getDispatchableChildren(g, null, "straight");
  var straightHeightAbove = 0 - syntaxdiagram_Constants.decision_row_padding / 2;
  var straightHeightBelow = 0 - syntaxdiagram_Constants.decision_row_padding / 2;
  
  if (straightList.length == 1)
  {
    var straight = straightList[0];
    var straightClass = straight.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");;
    straightHeightAbove = Math.max(syntaxdiagram_Constants.arrow_size, syntaxdiagram_Dispatch[straightClass]["getHeightAbove"](straight));
    straightHeightBelow = Math.max(syntaxdiagram_Constants.arrow_size, syntaxdiagram_Dispatch[straightClass]["getHeightBelow"](straight));
    var straightWidth = syntaxdiagram_Dispatch[straightClass]["getWidth"](straight);
    var runningWidth = 0;
    var initialLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
    initialLine.setAttribute("class", "arrow");
    initialLine.setAttribute("x1", 0);
    initialLine.setAttribute("y1", 0);
    runningWidth = runningWidth - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) + (straightWidth/2);
    initialLine.setAttribute("x2", runningWidth);
    initialLine.setAttribute("y2", 0);
    g.appendChild(initialLine);
    if (straightClass != 'void')
    {
      g.appendChild(syntaxdiagram_arrowHead(runningWidth, 0, 180));
    }
    runningWidth = runningWidth - straightWidth;
    syntaxdiagram_Dispatch[straightClass]["place"](straight, runningWidth, 0);
    var finalLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
    finalLine.setAttribute("class", "arrow");
    finalLine.setAttribute("x1", runningWidth);
    finalLine.setAttribute("y1", 0);
    runningWidth = runningWidth - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - (maxWidth/2) + (straightWidth/2);
    finalLine.setAttribute("x2", runningWidth);
    finalLine.setAttribute("y2", 0);
    g.appendChild(finalLine);
    g.appendChild(syntaxdiagram_arrowHead(runningWidth + syntaxdiagram_Constants.decision_corner_radius, 0, 180));
  }

  // Lines above centre.
  var upwardList = syntaxdiagram_getDispatchableChildren(g, null, "upward");
  var maxHeightAbove = straightHeightAbove;
  
  if (upwardList.length > 0)
  {
    var runningHeightAbove = Math.max(syntaxdiagram_Constants.decision_corner_radius + syntaxdiagram_Constants.arrow_size, maxHeightAbove + syntaxdiagram_Constants.decision_row_padding); 
    
    for (var i = 0; i < upwardList.length; i++)
    {
      var upwardClass = upwardList[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
      var upwardHeightAbove = Math.max(syntaxdiagram_Constants.arrow_size, syntaxdiagram_Dispatch[upwardClass]["getHeightAbove"](upwardList[i]));
      var upwardHeightBelow = Math.max(syntaxdiagram_Constants.arrow_size, syntaxdiagram_Dispatch[upwardClass]["getHeightBelow"](upwardList[i]));
      var upwardWidth = syntaxdiagram_Dispatch[upwardClass]["getWidth"](upwardList[i]);

      var newRunningHeightAbove;
      newRunningHeightAbove = Math.max(
        runningHeightAbove + syntaxdiagram_Constants.decision_corner_radius,
        maxHeightAbove + syntaxdiagram_Constants.decision_row_padding + upwardHeightBelow);
      
      var initialLine = document.createElementNS("http://www.w3.org/2000/svg", "path");
      initialLine.setAttribute("class", "arrow");
      initialLine.setAttribute("d",
          "M" + (0) + " " + (0) + " " + 
          "Q" + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (0) + " " + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " +
          "L" + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (0 - newRunningHeightAbove + syntaxdiagram_Constants.decision_corner_radius) + " " + 
          "Q" + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (0 - newRunningHeightAbove) + " " + (0 - syntaxdiagram_Constants.decision_corner_radius * 2) + " "+ (0 - newRunningHeightAbove) + " " + 
          "L" + (0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) + (upwardWidth/2)) + " " + (0 - newRunningHeightAbove)
        );
      g.appendChild(initialLine);    
      g.appendChild(syntaxdiagram_arrowHead(0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) + (upwardWidth/2), 0 - newRunningHeightAbove, 180));
      
      syntaxdiagram_Dispatch[upwardClass]["place"](upwardList[i], 0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) - (upwardWidth/2), 0 - newRunningHeightAbove);
      
      var finalLine = document.createElementNS("http://www.w3.org/2000/svg", "path");
      finalLine.setAttribute("class", "arrow");
      finalLine.setAttribute("d",
          "M" + (0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) - (upwardWidth/2)) + " " + (0 - newRunningHeightAbove) + " " + 
          "L" + (0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (0 - newRunningHeightAbove) + " " +
          "Q" + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (0 - newRunningHeightAbove) + " " + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (0 - newRunningHeightAbove + syntaxdiagram_Constants.decision_corner_radius) + " " + 
          "L" + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + 
          "Q" + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (0) + " " + (0 - 4 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " "+ (0)
        );
      g.appendChild(finalLine);    
      g.appendChild(syntaxdiagram_arrowHead(0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth, 0 - syntaxdiagram_Constants.decision_corner_radius, 90));
      
      runningHeightAbove = newRunningHeightAbove;
      maxHeightAbove = runningHeightAbove + upwardHeightAbove;
    }
  }  

  // Lines Above centre.
  var downwardList = syntaxdiagram_getDispatchableChildren(g, null, "downward");
  var maxHeightBelow = straightHeightBelow;
  
  if (downwardList.length > 0)
  {
    var runningHeightBelow = Math.max(syntaxdiagram_Constants.decision_corner_radius + syntaxdiagram_Constants.arrow_size, maxHeightBelow + syntaxdiagram_Constants.decision_row_padding); 
    
    for (var i = 0; i < downwardList.length; i++)
    {
      var downwardClass = downwardList[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
      var downwardHeightBelow = Math.max(syntaxdiagram_Constants.arrow_size, syntaxdiagram_Dispatch[downwardClass]["getHeightBelow"](downwardList[i]));
      var downwardHeightAbove = Math.max(syntaxdiagram_Constants.arrow_size, syntaxdiagram_Dispatch[downwardClass]["getHeightAbove"](downwardList[i]));
      var downwardWidth = syntaxdiagram_Dispatch[downwardClass]["getWidth"](downwardList[i]);

      var newRunningHeightBelow;
      newRunningHeightBelow = Math.max(
        runningHeightBelow + syntaxdiagram_Constants.decision_corner_radius,
        maxHeightBelow + syntaxdiagram_Constants.decision_row_padding + downwardHeightAbove);
      
      var initialLine = document.createElementNS("http://www.w3.org/2000/svg", "path");
      initialLine.setAttribute("class", "arrow");
      initialLine.setAttribute("d",
          "M" + (0) + " " + (0) + " " + 
          "Q" + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (0) + " " + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (syntaxdiagram_Constants.decision_corner_radius) + " " +
          "L" + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (newRunningHeightBelow - syntaxdiagram_Constants.decision_corner_radius) + " " + 
          "Q" + (0 - syntaxdiagram_Constants.decision_corner_radius) + " " + (newRunningHeightBelow) + " " + (0 - syntaxdiagram_Constants.decision_corner_radius * 2) + " "+ (newRunningHeightBelow) + " " + 
          "L" + (0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) + (downwardWidth/2)) + " " + (newRunningHeightBelow)
        );
      g.appendChild(initialLine);    
      g.appendChild(syntaxdiagram_arrowHead(0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) + (downwardWidth/2), newRunningHeightBelow, 180));
      
      syntaxdiagram_Dispatch[downwardClass]["place"](downwardList[i], 0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) - (downwardWidth/2), newRunningHeightBelow);
      
      var finalLine = document.createElementNS("http://www.w3.org/2000/svg", "path");
      finalLine.setAttribute("class", "arrow");
      finalLine.setAttribute("d",
          "M" + (0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_initial - (maxWidth/2) - (downwardWidth/2)) + " " + (newRunningHeightBelow) + " " + 
          "L" + (0 - 2 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (newRunningHeightBelow) + " " +
          "Q" + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (newRunningHeightBelow) + " " + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (newRunningHeightBelow - syntaxdiagram_Constants.decision_corner_radius) + " " + 
          "L" + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (syntaxdiagram_Constants.decision_corner_radius) + " " + 
          "Q" + (0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " " + (0) + " " + (0 - 4 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth) + " "+ (0)
        );
      g.appendChild(finalLine);    
      g.appendChild(syntaxdiagram_arrowHead(0 - 3 * syntaxdiagram_Constants.decision_corner_radius - syntaxdiagram_Constants.decision_join_length_final - syntaxdiagram_Constants.decision_join_length_initial - maxWidth, syntaxdiagram_Constants.decision_corner_radius, -90));
      
      runningHeightBelow = newRunningHeightBelow;
      maxHeightBelow = runningHeightBelow + downwardHeightBelow;
    }
  }  

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", maxHeightAbove);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", maxHeightBelow);

}

function syntaxdiagram_revdecision_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + (x + syntaxdiagram_Dispatch["revdecision"]["getWidth"](g)) + "," + y + ")");
}

function syntaxdiagram_revdecision_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_revdecision_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_revdecision_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
