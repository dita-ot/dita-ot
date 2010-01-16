treediagram_Dispatch["text"] = new Array;
treediagram_Dispatch["text"]["init"] = treediagram_text_init;
treediagram_Dispatch["text"]["place"] = treediagram_text_place;
treediagram_Dispatch["text"]["rank"] = treediagram_text_rank;
treediagram_Dispatch["text"]["getWidth"] = treediagram_text_getWidth;
treediagram_Dispatch["text"]["getHeightBelow"] = treediagram_text_getHeightBelow;
treediagram_Dispatch["text"]["getHeightAbove"] = treediagram_text_getHeightAbove;

function treediagram_text_init(g)
{
  // To do: handle more than just first child.
  var t = treediagram_getDispatchableChildren(g, "text", null)[0];

  if (t.hasChildNodes())
  {
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:width", t.getBBox().width);
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:heightAbove", 0 - (t.getBBox().y));
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:heightBelow", (t.getBBox().height + (t.getBBox().y - 0)));
    // Tell this text how long it was, in case it is being rendered later with a different renderer.
    t.setAttribute("textLength", t.getBBox().width);
  }
  else
  {
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:width", 0);
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:heightAbove", 0);
    g.setAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "treediagram2svg:heightBelow", 0);
  }
}

function treediagram_text_place(g, x, y)
{
  g.setAttribute("transform",
                 "translate(" + x + "," + y + ")");
}

function treediagram_text_rank(g, y)
{
}

function treediagram_text_getWidth(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "width") - 0;
}

function treediagram_text_getHeightBelow(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "heigntBelow") - 0;
}

function treediagram_text_getHeightAbove(g)
{
  return g.getAttributeNS("http://www.moldflow.com/namespace/2008/treediagram2svg", "heightAbove") - 0;
}
