package org.dita.dost;

import org.dita.dost.util.Configuration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DITA-OT processer factory. Not thread-safe, but can be reused.
 */
public final class ProcessorFactory {

    private final File ditaDir;
    private final Map<String, String> args = new HashMap<>();

    private ProcessorFactory(final File ditaDir) {
        this.ditaDir = ditaDir;
    }

    /**
     * Obtain a new instance of a ProcessorFactory.
     *
     * @param ditaDir absolute directory to DITA-OT installation
     * @return new ProcessorFactory instance
     */
    public static ProcessorFactory newInstance(final File ditaDir) {
        if (!ditaDir.isAbsolute()) {
            throw new IllegalArgumentException("DITA-OT directory must be absolute");
        }
        return new ProcessorFactory(ditaDir);
    }

    /**
     * Set base directory for temporary directories.
     *
     * @param tmp absolute directory for temporary directories
     */
    public void setBaseTempDir(final File tmp) {
        if (!tmp.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory must be absolute");
        }
        args.put("base.temp.dir", tmp.getAbsolutePath());
    }

    /**
     * Create new Processor to run DITA-OT
     *
     * @param transtype transtype for the processor
     * @return new Processor instance
     */
    public Processor newProcessor(final String transtype) {
        if (ditaDir == null) {
            throw new IllegalStateException();
        }
        if (!Configuration.transtypes.contains(transtype)) {
            throw new IllegalArgumentException("Transtype " + transtype + " not supported");
        }
        return new Processor(ditaDir, transtype, Collections.unmodifiableMap(args));
    }

}
