package com.idiominc.ws.opentopic.fo.xep;

import com.renderx.xep.FOTarget;
import com.renderx.xep.FormatterImpl;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class Runner {
    private boolean failed;

    private static boolean failOnError = true;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Invalid number of parameters. " +
                    "This class should not be executed outside of publising output build script");
            System.exit(1);
        }

        if (args.length == 3 && args[2].equals("failOnError=false"))
        {
            failOnError = false;
        }

        boolean failed = new Runner().run(args);

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
    private boolean run(String[] args) {
        FormatterImpl xep = null;
        try {
            xep = new FormatterImpl();
        } catch (Exception e) {
            System.out.println("Could not create XEP formatter: " + e.toString());
            return true;
        }

        try {
            SAXSource source = createSAXSource(args);
            OutputStream out = createOutputStream(args);
            Logger logger = createCustomLogger(this);

            try {
                try {
                    xep.render(source, new FOTarget(out, "PDF"), logger);
                } finally {
                    out.close();
                }
            } catch (Exception e) {
                System.out.println("Rendering failed: " + e.getMessage());
                return true;
            }
        } finally {
            xep.cleanup();
        }

        return this.failed;
    }

    private static SAXSource createSAXSource(String[] args) {
        SAXSource source = null;
        try {
            File in = new File(args[0]);
            InputSource saxsrc = new InputSource(in.toURL().toString());
            source = new SAXSource(saxsrc);
        } catch (Exception e) {
            System.out.println("Source creation failed: " + e.getMessage());
            System.exit(1);
        }
        return source;
    }

    private static OutputStream createOutputStream(String[] args) {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(args[1]));
        } catch (Exception e) {
            System.out.println("Could not open output file: " + e.getMessage());
            System.exit(1);
        }
        return out;
    }

    private static Logger createCustomLogger(Runner runner) {
        Logger logger = null;
        try {
            logger = new Logger(runner);
        } catch (Exception e) {
            System.out.println("Log handler creation failed: " + e.getMessage());
            System.exit(1);
        }
        return logger;
    }
}

