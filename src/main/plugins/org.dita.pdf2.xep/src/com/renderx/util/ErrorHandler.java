/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
// Dummy class to allow compiling against RenderX

package com.renderx.util;

public interface ErrorHandler {
    void info(String var1);

    void warning(String var1);

    void error(String var1);

    void exception(String var1, Exception var2);
}
