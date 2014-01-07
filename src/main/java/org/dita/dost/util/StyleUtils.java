/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/**
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Style utility to get and set style names. Used from ODT transtype's XSLT stylesheets.
 * 
 * @author Zhang Di Hua
 *
 */
public final class StyleUtils {


    //list for style name in xslt for hi-d tags
    private static List<String> hiStyleNameList = new ArrayList<String>();

    //map for storing flagging style names.
    private static Map<String, String> flagStyleNameMap = new HashMap<String, String>();

    /**
     * Default Constructor
     *
     */
    private StyleUtils(){
    }

    //check hi-d style name
    public static String insertHiStyleName(final String styleName){
        for(final String item : hiStyleNameList){
            if(item.length() == styleName.length()){
                //sort strings to remove order effect
                final String cp1 = sort(item);
                final String cp2 = sort(styleName);

                if(cp1.equals(cp2)){
                    return "true";
                }else{
                    continue;
                }
            }else{
                continue;
            }
        }
        //It is a new style name and add it to a list.
        hiStyleNameList.add(styleName);

        return "false";
    }
    //get hi-d style name
    public static String getHiStyleName(final String styleName){
        for(final String item : hiStyleNameList){

            if(item.length() == styleName.length()){
                //sort strings to remove order effect
                final String cp1 = sort(item);
                final String cp2 = sort(styleName);

                if(cp1.equals(cp2)){
                    return item;
                }else{
                    continue;
                }
            }else{
                continue;
            }
        }
        //It is a new style name and add it to a list.
        hiStyleNameList.add(styleName);

        return styleName;
    }
    //sort string for compare.
    private static String sort(final String item) {

        final char[] chars = item.toCharArray();
        for(int i = 1; i < chars.length ; i++){
            for(int j = 0; j < chars.length - 1 ; j++){
                if(chars[j] > chars[j+1]){
                    final char temp = chars[j];
                    chars[j] = chars[j+1];
                    chars[j+1] = temp;
                }
            }
        }
        return String.valueOf(chars);


    }
    //store flagging style name
    public static String insertFlagStyleName(final String styleName){
        flagStyleNameMap.put(styleName, styleName);

        return styleName;
    }
    //get flagging style name
    public static String getFlagStyleName(final String flagStyleName){

        if(flagStyleNameMap.containsKey(flagStyleName)){
            return flagStyleNameMap.get(flagStyleName);
        }else{
            return "";
        }
    }

    //method for get css colors.
    //keyword colors:
    //aqua, black, blue, fuchsia, gray, green, lime, maroon,
    //navy, olive, purple, red, silver, teal, white, and yellow
    public static String getColor(final String colorName){

        final Map<String, String> colorMap = new HashMap<String, String>();
        //Store all css colors
        colorMap.put("aliceblue", "f0f8ff");
        colorMap.put("antiquewhite",  "#faebd7");;
        colorMap.put("aqua",  "#00ffff");
        colorMap.put("aquamarine",  "#7fffd4");
        colorMap.put("azure",   "#f0ffff");
        colorMap.put("beige",   "#f5f5dc");
        colorMap.put("bisque",  "#ffe4c4");
        colorMap.put("black",   "#000000");
        colorMap.put("blanchedalmond",  "#ffebcd");
        colorMap.put("blue",  "#0000ff");
        colorMap.put("blueviolet",  "#8a2be2");
        colorMap.put("brown",   "#a52a2a");
        colorMap.put("burlywood",   "#deb887");
        colorMap.put("cadetblue",  "#5f9ea0");
        colorMap.put("chartreuse",  "#7fff00");
        colorMap.put("chocolate",   "#d2691e");
        colorMap.put("coral",   "#ff7f50");
        colorMap.put("cornflowerblue", "#6495ed");
        colorMap.put("cornsilk",   "#fff8dc");
        colorMap.put("crimson",   "#dc143c");
        colorMap.put("cyan",   "#00ffff");
        colorMap.put("darkblue",   "#00008b");
        colorMap.put("darkcyan",   "#008b8b");
        colorMap.put("darkgoldenrod",  "#b8860b");
        colorMap.put("darkgray",   "#a9a9a9");
        colorMap.put("darkgreen",   "#006400");
        colorMap.put("darkkhaki",   "#bdb76b");
        colorMap.put("darkmagenta",   "#8b008b");
        colorMap.put("darkolivegreen", "#556b2f");
        colorMap.put("darkorange",   "#ff8c00");
        colorMap.put("darkorchid",   "#9932cc");
        colorMap.put("darkred",   "#8b0000");
        colorMap.put("darksalmon",   "#e9967a");
        colorMap.put("darkseagreen",  "#8fbc8f");
        colorMap.put("darkslateblue",  "#483d8b");
        colorMap.put("darkslategray",  "#2f4f4f");
        colorMap.put("darkturquoise",  "#00ced1");
        colorMap.put("darkviolet",   "#9400d3");
        colorMap.put("deeppink",   "#ff1493");
        colorMap.put("deepskyblue",   "#00bfff");
        colorMap.put("dimgray",   "#696969");
        colorMap.put("dodgerblue",   "#1e90ff");
        colorMap.put("firebrick",   "#b22222");
        colorMap.put("floralwhite",   "#fffaf0");
        colorMap.put("forestgreen",   "#228b22");
        colorMap.put("fuchsia",   "#ff00ff");
        colorMap.put("gainsboro",   "#dcdcdc");
        colorMap.put("ghostwhite",   "#f8f8ff");
        colorMap.put("gold",   "#ffd700");
        colorMap.put("goldenrod",   "#daa520");
        colorMap.put("gray",   "#808080");
        colorMap.put("green",   "#008000");
        colorMap.put("greenyellow",   "#adff2f");
        colorMap.put("honeydew",   "#f0fff0");
        colorMap.put("hotpink",   "#ff69b4");
        colorMap.put("indianred",    "#cd5c5c");
        colorMap.put("indigo",    "#4b0082");
        colorMap.put("ivory",   "#fffff0");
        colorMap.put("khaki",   "#f0e68c");
        colorMap.put("lavender",   "#e6e6fa");
        colorMap.put("lavenderblush",  "#fff0f5");
        colorMap.put("lawngreen",   "#7cfc00");
        colorMap.put("lemonchiffon",  "#fffacd");
        colorMap.put("lightblue",   "#add8e6");
        colorMap.put("lightcoral",   "#f08080");
        colorMap.put("lightcyan",   "#e0ffff");
        colorMap.put("lightgoldenrodyellow", "#fafad2");
        colorMap.put("lightgrey",   "#d3d3d3");
        colorMap.put("lightgreen",   "#90ee90");
        colorMap.put("lightpink",   "#ffb6c1");
        colorMap.put("lightsalmon",   "#ffa07a");
        colorMap.put("lightseagreen",  "#20b2aa");
        colorMap.put("lightskyblue",  "#87cefa");
        colorMap.put("lightslategray", "#778899");
        colorMap.put("lightsteelblue", "#b0c4de");
        colorMap.put("lightyellow",   "#ffffe0");
        colorMap.put("lime",   "#00ff00");
        colorMap.put("limegreen",   "#32cd32");
        colorMap.put("linen",   "#faf0e6");
        colorMap.put("magenta",   "#ff00ff");
        colorMap.put("maroon",   "#800000");
        colorMap.put("mediumaquamarine", "#66cdaa");
        colorMap.put("mediumblue",   "#0000cd");
        colorMap.put("mediumorchid",  "#ba55d3");
        colorMap.put("mediumpurple",  "#9370d8");
        colorMap.put("mediumseagreen",  "#3cb371");
        colorMap.put("mediumslateblue",  "#7b68ee");
        colorMap.put("mediumspringgreen",  "#00fa9a");
        colorMap.put("mediumturquoise",  "#48d1cc");
        colorMap.put("mediumvioletred",  "#c71585");
        colorMap.put("midnightblue",  "#191970");
        colorMap.put("mintcream",   "#f5fffa");
        colorMap.put("mistyrose",   "#ffe4e1");
        colorMap.put("moccasin",   "#ffe4b5");
        colorMap.put("navajowhite",   "#ffdead");
        colorMap.put("navy",   "#000080");
        colorMap.put("oldlace",   "#fdf5e6");
        colorMap.put("olive",   "#808000");
        colorMap.put("olivedrab",   "#6b8e23");
        colorMap.put("orange",   "#ffa500");
        colorMap.put("orangered",   "#ff4500");
        colorMap.put("orchid",   "#da70d6");
        colorMap.put("palegoldenrod",  "#eee8aa");
        colorMap.put("palegreen",   "#98fb98");
        colorMap.put("paleturquoise",  "#afeeee");
        colorMap.put("palevioletred",  "#d87093");
        colorMap.put("papayawhip",   "#ffefd5");
        colorMap.put("peachpuff",   "#ffdab9");
        colorMap.put("peru",   "#cd853f");
        colorMap.put("pink",   "#ffc0cb");
        colorMap.put("plum",   "#dda0dd");
        colorMap.put("powderblue",   "#b0e0e6");
        colorMap.put("purple",   "#800080");
        colorMap.put("red",    "#ff0000");
        colorMap.put("rosybrown",   "#bc8f8f");
        colorMap.put("royalblue",   "#4169e1");
        colorMap.put("saddlebrown",   "#8b4513");
        colorMap.put("salmon",   "#fa8072");
        colorMap.put("sandybrown",   "#f4a460");
        colorMap.put("seagreen",   "#2e8b57");
        colorMap.put("seashell",   "#fff5ee");
        colorMap.put("sienna",   "#a0522d");
        colorMap.put("silver",   "#c0c0c0");
        colorMap.put("skyblue",   "#87ceeb");
        colorMap.put("slateblue",   "#6a5acd");
        colorMap.put("slategray",   "#708090");
        colorMap.put("snow",   "#fffafa");
        colorMap.put("springgreen",   "#00ff7f");
        colorMap.put("steelblue",   "#4682b4");
        colorMap.put("tan",    "#d2b48c");
        colorMap.put("teal",   "#008080");
        colorMap.put("thistle",   "#d8bfd8");
        colorMap.put("tomato",   "#ff6347");
        colorMap.put("turquoise",   "#40e0d0");
        colorMap.put("violet",   "#ee82ee");
        colorMap.put("wheat",   "#f5deb3");
        colorMap.put("white",   "#ffffff");
        colorMap.put("whitesmoke",   "#f5f5f5");
        colorMap.put("yellow",   "#ffff00");
        colorMap.put("yellowgreen",  "#9acd32");

        final String key = colorName.toLowerCase();
        if(colorMap.containsKey(key)){
            return colorMap.get(key);
        }else if(key.startsWith(SHARP)){
            //#rrggbb format
            return key;
        }else{
            return "";
        }

    }

}
