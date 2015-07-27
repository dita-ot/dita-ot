// Dummy class to allow compiling against RenderX

package com.renderx.xep.lib;

import com.renderx.util.ErrorHandler;

public interface Logger extends ErrorHandler {
    void openDocument();

    void closeDocument();

    void event(String var1, String var2);

    void openState(String var1);

    void closeState(String var1);

    void info(String var1);

    void warning(String var1);

    void error(String var1);

    void exception(String var1, Exception var2);
}
