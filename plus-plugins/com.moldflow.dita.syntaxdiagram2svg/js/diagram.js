// Diagram

syntaxdiagram_Dispatch["diagram"] = new Array;
syntaxdiagram_Dispatch["diagram"]["init"] = syntaxdiagram_diagram_init;
syntaxdiagram_Dispatch["diagram"]["place"] = syntaxdiagram_diagram_place;
syntaxdiagram_Dispatch["diagram"]["getHeightAbove"] = syntaxdiagram_diagram_getHeightAbove;
syntaxdiagram_Dispatch["diagram"]["getHeightBelow"] = syntaxdiagram_diagram_getHeightBelow;
syntaxdiagram_Dispatch["diagram"]["getWidth"] = syntaxdiagram_diagram_getWidth;

function syntaxdiagram_diagram_init(g)
{
  var children = syntaxdiagram_getDispatchableChildren(g, null, null);
  var heightAbove = new Array;
  var heightBelow = new Array;
  var currentRow = 0;
  var rowUsed = 0;
  var maxHeightAbove = syntaxdiagram_Constants.arrow_size;
  var maxHeightBelow = syntaxdiagram_Constants.arrow_size;
  var runningWidth = syntaxdiagram_Constants.sequence_join_length_initial;
  var maxWidth = syntaxdiagram_Constants.sequence_join_length_initial;

  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");
    syntaxdiagram_Dispatch[childClass]["init"](children[i]);
    var childWidth = syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]);

    var sequenceJoinWidth;
    if (i != children.length - 1)
    {
      sequenceJoinWidth = syntaxdiagram_Constants.sequence_join_length_medial;
    }
    else
    {
      sequenceJoinWidth = syntaxdiagram_Constants.sequence_join_length_final; // plus end bit
    }
    
    if (rowUsed && (childWidth + runningWidth + sequenceJoinWidth > syntaxdiagram_Constants.diagram_wrap_width))
    {
      // Line wrap.
      runningWidth = syntaxdiagram_Constants.diagram_wrap_indent + 2 * syntaxdiagram_Constants.arrow_size;
     
      heightAbove[currentRow] = maxHeightAbove;
      heightBelow[currentRow] = maxHeightBelow;
      currentRow++;
      
      maxHeightAbove = syntaxdiagram_Constants.arrow_size;
      maxHeightBelow = syntaxdiagram_Constants.arrow_size;
      rowUsed = 0;
    }
    
    children[i].setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:row", currentRow);
    // children[i].setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "placement", runningWidth);
    
    runningWidth = runningWidth + childWidth;
    // maxWidth = Math.max(maxWidth, runningWidth);
    maxHeightAbove = Math.max(maxHeightAbove, syntaxdiagram_Dispatch[childClass]["getHeightAbove"](children[i]));
    maxHeightBelow = Math.max(maxHeightBelow, syntaxdiagram_Dispatch[childClass]["getHeightBelow"](children[i]));
    
    if (i != children.length - 1)
    {
      runningWidth = runningWidth + syntaxdiagram_Constants.sequence_join_length_medial
      maxWidth = Math.max(maxWidth, runningWidth);
    }
    rowUsed = 1;
  }
  heightAbove[currentRow] = maxHeightAbove;
  heightBelow[currentRow] = maxHeightBelow;
  maxWidth = Math.max(maxWidth, runningWidth + syntaxdiagram_Constants.sequence_join_length_final + syntaxdiagram_Constants.arrow_size);
  
  var lastRowNumber = 0;
  var overallHeight = heightAbove[0];
  
  var runningWidth = 0;
  
  // Initial arrow.
  g.appendChild(syntaxdiagram_arrowHead(0, overallHeight, 0));
  g.appendChild(syntaxdiagram_arrowHead(syntaxdiagram_Constants.arrow_size, overallHeight, 0));
  var initialLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
  initialLine.setAttribute("class", "arrow");
  initialLine.setAttribute("x1", 0);
  initialLine.setAttribute("y1", overallHeight);
  runningWidth = runningWidth + syntaxdiagram_Constants.sequence_join_length_initial;
  initialLine.setAttribute("x2", runningWidth);
  initialLine.setAttribute("y2", overallHeight);
  g.appendChild(initialLine);
  //g.appendChild(syntaxdiagram_arrowHead(syntaxdiagram_Constants.sequence_join_length_initial, overallHeight, 0));

  // Intervening stuff.
  for (var i = 0; i < children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "dispatch");

    var rowNumber = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "row");
    
    if (rowNumber != lastRowNumber)
    {
      runningWidth = syntaxdiagram_Constants.diagram_wrap_indent;
      overallHeight = overallHeight + heightBelow[lastRowNumber] + heightAbove[rowNumber] + syntaxdiagram_Constants.diagram_row_padding;

      g.appendChild(syntaxdiagram_arrowHead(runningWidth, overallHeight, 0));
      var medialLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
      medialLine.setAttribute("class", "arrow");
      medialLine.setAttribute("x1", runningWidth);
      medialLine.setAttribute("y1", overallHeight);
      runningWidth = runningWidth + syntaxdiagram_Constants.sequence_join_length_medial
      medialLine.setAttribute("x2", runningWidth);
      medialLine.setAttribute("y2", overallHeight);
      g.appendChild(medialLine);

    }
    
    syntaxdiagram_Dispatch[childClass]["place"](children[i], runningWidth, overallHeight);
    runningWidth = runningWidth + syntaxdiagram_Dispatch[childClass]["getWidth"](children[i]);
    
    if (i != children.length - 1)
    {
      var medialLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
      medialLine.setAttribute("class", "arrow");
      medialLine.setAttribute("x1", runningWidth);
      medialLine.setAttribute("y1", overallHeight);
      runningWidth = runningWidth + syntaxdiagram_Constants.sequence_join_length_medial
      medialLine.setAttribute("x2", runningWidth);
      medialLine.setAttribute("y2", overallHeight);
      g.appendChild(medialLine);
      g.appendChild(syntaxdiagram_arrowHead(runningWidth, overallHeight, 0));
    }

    lastRowNumber = rowNumber;
  }
  
  // Final arrow.
  var finalLine = document.createElementNS("http://www.w3.org/2000/svg", "line");
  finalLine.setAttribute("class", "arrow");
  finalLine.setAttribute("x1", runningWidth);
  finalLine.setAttribute("y1", overallHeight);
  runningWidth = runningWidth + syntaxdiagram_Constants.sequence_join_length_final
  finalLine.setAttribute("x2", runningWidth);
  finalLine.setAttribute("y2", overallHeight);
  g.appendChild(finalLine);
  g.appendChild(syntaxdiagram_arrowHead(runningWidth, overallHeight, 0));
  g.appendChild(syntaxdiagram_arrowHead(runningWidth, overallHeight, 180));

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", maxWidth + 2 * syntaxdiagram_Constants.diagram_margin_sides);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", 0 + syntaxdiagram_Constants.diagram_margin_topbottom);
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", overallHeight + heightBelow[lastRowNumber] + syntaxdiagram_Constants.diagram_margin_topbottom);

}

function syntaxdiagram_diagram_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + (x + syntaxdiagram_Constants.diagram_margin_sides) + "," + (y + (g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0)) + ")");
}

function syntaxdiagram_diagram_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_diagram_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_diagram_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
