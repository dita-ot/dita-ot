// Dummy class to allow compiling against RenderX

package com.renderx.util;

public interface ErrorHandler {
    void info(String var1);

    void warning(String var1);

    void error(String var1);

    void exception(String var1, Exception var2);
}
