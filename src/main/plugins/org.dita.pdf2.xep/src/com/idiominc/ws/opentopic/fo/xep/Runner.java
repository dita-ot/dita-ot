package com.idiominc.ws.opentopic.fo.xep;

import com.renderx.xep.FOTarget;
import com.renderx.xep.FormatterImpl;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

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
public class Runner {
    private boolean failed;

    private static boolean failOnError = true;

    public static void main(final String[] args) {
        if (args.length < 2) {
            System.out.println("Invalid number of parameters. " +
                    "This class should not be executed outside of publising output build script");
            System.exit(1);
        }

        if (args.length >= 3 && args[2].equals("failOnError=false"))
        {
            failOnError = false;
        }

        final boolean failed = new Runner().run(args);

        if (failed && failOnError) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    public void fail()
    {
        this.failed = true;
    }

    /**
     * @return true if transformation failed
     */
    private boolean run(final String[] args) {
        FormatterImpl xep = null;
        try {
            xep = new FormatterImpl();
        } catch (final Exception e) {
            System.out.println("Could not create XEP formatter: " + e.toString());
            return true;
        }

        String outputFormat = "PDF";
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                outputFormat = args[i].substring(1);
            }
        }

        try {
            final SAXSource source = createSAXSource(args);
            final OutputStream out = createOutputStream(args);
            final Logger logger = createCustomLogger(this);

            try {
                try {
                    xep.render(source, new FOTarget(out, outputFormat), logger);
                } finally {
                    out.close();
                }
            } catch (final Exception e) {
                System.out.println("Rendering failed: " + e.getMessage());
                return true;
            }
        } finally {
            xep.cleanup();
        }

        return this.failed;
    }

    private static SAXSource createSAXSource(final String[] args) {
        SAXSource source = null;
        try {
            final File in = new File(args[0]);
            final InputSource saxsrc = new InputSource(in.toURI().toString());
            source = new SAXSource(saxsrc);
        } catch (final Exception e) {
            System.out.println("Source creation failed: " + e.getMessage());
            System.exit(1);
        }
        return source;
    }

    private static OutputStream createOutputStream(final String[] args) {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(args[1]));
        } catch (final Exception e) {
            System.out.println("Could not open output file: " + e.getMessage());
            System.exit(1);
        }
        return out;
    }

    private static Logger createCustomLogger(final Runner runner) {
        Logger logger = null;
        try {
            logger = new Logger(runner);
        } catch (final Exception e) {
            System.out.println("Log handler creation failed: " + e.getMessage());
            System.exit(1);
        }
        return logger;
    }
}

