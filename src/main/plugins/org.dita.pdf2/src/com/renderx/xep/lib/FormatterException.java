// Dummy class to allow compiling against RenderX

package com.renderx.xep.lib;

import org.xml.sax.SAXException;

public class FormatterException extends SAXException {
    public FormatterException() {
        super("");
    }

    public FormatterException(String var1) {
        super(var1);
    }

    public FormatterException(Exception var1) {
        super(var1);
    }

    public FormatterException(String var1, Exception var2) {
        super(var1, var2);
    }
}
