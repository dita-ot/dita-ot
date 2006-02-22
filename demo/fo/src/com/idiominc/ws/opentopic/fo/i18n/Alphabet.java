package com.idiominc.ws.opentopic.fo.i18n;

import java.util.HashSet;
import java.util.Set;


/**
 * User: Ivan Luzyanin
 * Date: Jan 14, 2004
 * Time: 1:33:05 PM
 */
public class Alphabet {
    private final String name;

    private final Set charset = new HashSet();


    public Alphabet(String theName, Character[] theChars) {
        this.name = theName;
        for (int i = 0; i < theChars.length; i++) {
            Character aChar = theChars[i];
            this.charset.add(aChar);
        }
    }


    public String getName() {
        return this.name;
    }


    public boolean isContain(char theChar) {
        return this.charset.contains(new Character(theChar));
    }


    public Character[] getAllChars() {
        Character[] characters = (Character[]) charset.toArray(new Character[charset.size()]);
        return characters;
    }
}
