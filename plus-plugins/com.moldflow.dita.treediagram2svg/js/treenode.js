treediagram_Dispatch["treenode"] = new Array;
treediagram_Dispatch["treenode"]["init"] = treediagram_treenode_init;
treediagram_Dispatch["treenode"]["setRank"] = treediagram_treenode_setRank;
treediagram_Dispatch["treenode"]["setDepth"] = treediagram_treenode_setDepth;
treediagram_Dispatch["treenode"]["setMiddle"] = treediagram_treenode_setMiddle;
treediagram_Dispatch["treenode"]["place"] = treediagram_treenode_place;
treediagram_Dispatch["treenode"]["leftExtent"] = treediagram_treenode_leftExtent;
treediagram_Dispatch["treenode"]["rightExtent"] = treediagram_treenode_rightExtent;

function treediagram_treenode_init(g)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");
    
    treediagram_Dispatch[childClass]["init"](children[i]);
    
    // Compute left/right envelope of this node.
    if (childClass == "boxed")
    {
      g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:envLeft",
        children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "width") / 2);
      g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:envRight",
        children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "width") / 2);
    }
  }
  
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:offset", 0);
}

function treediagram_treenode_setRank(g, y)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

    if (childClass == "treenode")
    {
      treediagram_Dispatch[childClass]["setRank"](children[i], y + 1);
    }
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:rank", y);
  }
}

function treediagram_treenode_setDepth(g)
{
  // g.setAttribute("transform", "translate(0," + y + ")");
  
  var children = treediagram_getDispatchableChildren(g, "g", null);
  
  var maxdepth = 0;
  
  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

    if (childClass == "treenode")
    {
      maxdepth = Math.max(maxdepth, treediagram_Dispatch[childClass]["setDepth"](children[i]) + 1);
    }
  }
  
  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:depth", maxdepth);
  
  return maxdepth;
}

function getRightEnvelope(g)
{
  return getEnvelope(g, -1, "envRight");
}

function getLeftEnvelope(g)
{
  return getEnvelope(g, 1, "envLeft");
}

function getEnvelope(g, iDirection, side)
{
  var result = new Array;
  result.push(g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", side) - 0);
  
  if (g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "depth") - 0 > 0)
  {
    var depth = 0;
    var children = treediagram_getDispatchableChildren(g, "g", null);
    var i = (children.length - 1) * (1 - iDirection) / 2;
    
    while (i >= 0 && i < children.length)
    {
      var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

      if (childClass != "treenode")
      {
        i += iDirection;
        continue;
      }
      
      if (children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "depth") - 0 < depth)
      {
        i += iDirection;
        continue;
      }
      
      // i is now the index of a deeper child.
      var childEnv = getEnvelope(children[i], iDirection, side);
      
      // Copy deeper envelopes.
      for (var j = depth; j < childEnv.length; j++)
      {
        result.push(iDirection * g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "middle") -
          iDirection * children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "offset") + childEnv[j]);
      }
      
      depth = j;
    }
  }
  
  return result;
}

function treediagram_treenode_setMiddle(g)
{
  var children = treediagram_getDispatchableChildren(g, "g", null);
  var nodeChildren = new Array;

  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

    if (childClass == "treenode")
    {
      treediagram_Dispatch[childClass]["setMiddle"](children[i]);
      nodeChildren.push(children[i]);
    }
  }

  g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:middle", 0);
  
  if (nodeChildren.length > 1)
  {
    nodeChildren[0].setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:offset", 0);
    
    var separation;
    
    for (var straddle = 1; straddle < nodeChildren.length; straddle++)
    {
      for (var L = 0, R = straddle; R < nodeChildren.length; L++, R++)
      {
        var Loffset = nodeChildren[L].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "offset") - 0;
        separation = nodeChildren[R].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "offset") - Loffset;
        var RofL = getRightEnvelope(nodeChildren[L]);
        var LofR = getLeftEnvelope(nodeChildren[R]);
        
        for (var depth = 0; depth < RofL.length && depth < LofR.length; depth++)
        {
          var gap;
          if (depth >= treediagram_Constants.x_spacing.length)
          {
            gap = treediagram_Constants.x_spacing_default - 0;
          }
          else
          {
            gap = treediagram_Constants.x_spacing[depth] - 0;
          }
          var separationNeeded = RofL[depth] + gap + LofR[depth];
          if (separationNeeded > separation)
          {
            separation = separationNeeded;
            var Roffset = Loffset + separation;
            nodeChildren[R].setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:offset", Roffset);
            for (var between = L + 1; between < R; between++)
            {
              nodeChildren[between].setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:offset", ((between-L)*Roffset+(R-between)*Loffset)/(R-L));
            }
          }
        }
      }
    }
    
    // separation is for leftmost and rightmost children.
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:middle", separation/2);
  }
}

function treediagram_treenode_place(g, xOffset, yOffset)
{
  var middle = g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "middle") - 0;
  var children = treediagram_getDispatchableChildren(g, "g", null);
  var boxHeight = 0;
  var maxHeight = yOffset;

  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");

    if (childClass == "boxed")
    {
      boxHeight = treediagram_Dispatch[childClass]["getHeightAbove"](children[i]) + treediagram_Dispatch[childClass]["getHeightBelow"](children[i]);
    }
  } 

  maxHeight = maxHeight + boxHeight;

  for (var i=0; i<children.length; i++)
  {
    var childClass = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "dispatch");
    // var rank = g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "rank")  - 0;

    if (childClass == "treenode")
    {
      var x = children[i].getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "offset") - 0;
      maxHeight = Math.max(maxHeight, treediagram_Dispatch[childClass]["place"](children[i], xOffset - middle + x, yOffset + boxHeight + treediagram_Constants.y_spacing));
      
      // Now insert line joining parent to this child node.
      var line = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
      line.setAttribute("points",
          xOffset + " " + (yOffset + boxHeight) + "," +
          xOffset + " " + (yOffset + treediagram_Constants.y_spacing/2 + boxHeight) + "," +
          (xOffset - middle + x) + " " + (yOffset + treediagram_Constants.y_spacing/2 + boxHeight) + "," +
          (xOffset - middle + x) + " " + (yOffset + (treediagram_Constants.y_spacing - 0) + boxHeight)
        );
      children[i].appendChild(line);
    }
    else
    {
      treediagram_Dispatch[childClass]["place"](children[i], xOffset - g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "envLeft"), yOffset);
    }
  } 

  return maxHeight;
}

function treediagram_treenode_leftExtent(g)
{
  var extent = 0;
  var envelope = getLeftEnvelope(g);
  
  for (var i = 0; i < envelope.length; i++)
  {
    extent = Math.max(extent, envelope[i]);
  }
  return extent;
}

function treediagram_treenode_rightExtent(g)
{
  var extent = 0;
  var envelope = getRightEnvelope(g);
  
  for (var i = 0; i < envelope.length; i++)
  {
    extent = Math.max(extent, envelope[i]);
  }
  return extent;
}
