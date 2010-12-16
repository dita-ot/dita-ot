// note

syntaxdiagram_Dispatch["note"] = new Array;
syntaxdiagram_Dispatch["note"]["init"] = syntaxdiagram_note_init;
syntaxdiagram_Dispatch["note"]["place"] = syntaxdiagram_note_place;
syntaxdiagram_Dispatch["note"]["getHeightAbove"] = syntaxdiagram_note_getHeightAbove;
syntaxdiagram_Dispatch["note"]["getHeightBelow"] = syntaxdiagram_note_getHeightBelow;
syntaxdiagram_Dispatch["note"]["getWidth"] = syntaxdiagram_note_getWidth;

function syntaxdiagram_note_init(g)
{
  // To do: handle more than just first child.
  var t = syntaxdiagram_getDispatchableChildren(g, "text", null)[0];
  
  g.setAttribute("transform", "translate(0," + (0 - syntaxdiagram_Constants.note_baseline_shift) + ")");
  //t.setAttribute("font-family", syntaxdiagram_Constants.font_face);
  //t.setAttribute("font-size", syntaxdiagram_Constants.font_size);
  
  if (t.hasChildNodes())
  {
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", t.getBBox().width);
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", 0 - (t.getBBox().y) + syntaxdiagram_Constants.note_baseline_shift);
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", (t.getBBox().height + (t.getBBox().y - 0) - syntaxdiagram_Constants.note_baseline_shift));
  }
  else
  {
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:width", 0);
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightAbove", 0);
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "syntaxdiagram2svg:heightBelow", 0);
  }
}

function syntaxdiagram_note_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + x + "," + (y - syntaxdiagram_Constants.note_baseline_shift)+ ")");
}

function syntaxdiagram_note_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightAbove") - 0;
}

function syntaxdiagram_note_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "heightBelow") - 0;
}

function syntaxdiagram_note_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/syntaxdiagram2svg", "width") - 0;
}
