// void (for empty straight-through lines in choices)

syntaxdiagram_Dispatch["void"] = new Array;
syntaxdiagram_Dispatch["void"]["init"] = syntaxdiagram_void_init;
syntaxdiagram_Dispatch["void"]["place"] = syntaxdiagram_void_place;
syntaxdiagram_Dispatch["void"]["getHeightAbove"] = syntaxdiagram_void_getHeightAbove;
syntaxdiagram_Dispatch["void"]["getHeightBelow"] = syntaxdiagram_void_getHeightBelow;
syntaxdiagram_Dispatch["void"]["getWidth"] = syntaxdiagram_void_getWidth;

function syntaxdiagram_void_init(g)
{
}

function syntaxdiagram_void_place(g, x, y)
{
}

function syntaxdiagram_void_getHeightAbove(g)
{
  return 0;
}

function syntaxdiagram_void_getHeightBelow(g)
{
  return 0;
}

function syntaxdiagram_void_getWidth(g)
{
  return 0;
}
