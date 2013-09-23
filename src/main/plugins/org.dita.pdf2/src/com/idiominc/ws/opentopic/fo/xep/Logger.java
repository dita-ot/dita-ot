package com.idiominc.ws.opentopic.fo.xep;

/*
Copyright (c) 2004-2006 by Idiom Technologies, Inc. All rights reserved.
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other
trademarks are the property of their respective owners.

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project.
See the accompanying license.txt file for applicable licenses.
 */
 
import org.dita.dost.log.MessageUtils;
 
public class Logger implements com.renderx.xep.lib.Logger {

    private final MessageUtils messageUtils = MessageUtils.getInstance();
    private final Runner runner;

    public Logger(final Runner runner) {
        this.runner = runner;
    }

    public void openDocument() {
    }

    public void closeDocument() {
    }

    public void event(final String name, final String message) {
    }

    public void openState(final String name) {
    }

    public void closeState(final String name) {
    }

    public void info(final String message) {
    }

    public void warning(final String message) {
        System.err.println(messageUtils.getMessage("XEPJ001W", message).toString());
    }

    public void error(final String message) {
        runner.fail();
        System.err.println(messageUtils.getMessage("XEPJ002E", message).toString());
    }

    public void exception(final String message, final java.lang.Exception except) {
        runner.fail();
        System.err.println(messageUtils.getMessage("XEPJ003E", message).toString());
    }
}

