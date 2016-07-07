package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.CopyToReader;
import org.dita.dost.util.*;
import org.dita.dost.writer.ForceUniqueFilter;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.FileInfo;
import static org.dita.dost.util.Job.Generate;
import static org.dita.dost.util.URLUtils.*;

/**
 * Process copy-to mapping.
 */
public final class CopyToModule extends AbstractPipelineModuleImpl {

    private boolean forceUnique;
    private ForceUniqueFilter forceUniqueFilter;
    private final CopyToReader reader = new CopyToReader();

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        try {
            init(input);

            processMap();
            final Map<URI, URI> copyToMap = getCopyToMap();
            performCopytoTask(copyToMap);

            job.write();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to process copy-to attributes: " + e.getMessage(), e);
        }

        return null;
    }

    private void init(AbstractPipelineInput input) {
        forceUnique = Boolean.valueOf(input.getAttribute(ANT_INVOKER_EXT_PARAN_FORCE_UNIQUE));
    }

    /**
     * Process start map to read copy-to map and write unique topic references.
     */
    private void processMap() throws DITAOTException {
        final URI in = job.tempDir.toURI().resolve(job.getInputMap());

        final List<XMLFilter> pipe = getProcessingPipe(in);

        XMLUtils.transform(in, pipe);
    }

    /**
     * Get processign filters
     */
    private List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        final List<XMLFilter> pipe = new ArrayList<>();

        if (forceUnique) {
            forceUniqueFilter = new ForceUniqueFilter();
            forceUniqueFilter.setLogger(logger);
            forceUniqueFilter.setJob(job);
            forceUniqueFilter.setCurrentFile(fileToParse);
            pipe.add(forceUniqueFilter);
        }

        reader.setJob(job);
        reader.setLogger(logger);
        reader.setCurrentFile(fileToParse);
        pipe.add(reader);

        return pipe;
    }

    /**
     * Get copy-to map based on map processing.
     *
     * @return target to source map of URIs relative to temporary directory
     */
    private Map<URI, URI> getCopyToMap() {
        final Map<URI, URI> copyToMap = new HashMap<>();

        if (forceUnique) {
            copyToMap.putAll(forceUniqueFilter.copyToMap);
        }

        for (final Map.Entry<URI, URI> e: reader.getCopyToMap().entrySet()) {
            final URI target = job.tempDir.toURI().relativize(e.getKey());
            final URI source = job.tempDir.toURI().relativize(e.getValue());
            // Filter copy-to where target is used directly.
            final FileInfo fileInfo = job.getFileInfo(target);
            if (fileInfo != null && fileInfo.src != null) {
                continue;
            }
            copyToMap.put(target, source);
        }

        return copyToMap;
    }

    /**
     * Execute copy-to task, generate copy-to targets base on sources.
     *
     * @param copyToMap target to source map of URIs relative to temporary directory
     */
    private void performCopytoTask(final Map<URI, URI> copyToMap) {
        for (final Map.Entry<URI, URI> entry: copyToMap.entrySet()) {
            final URI copytoTarget = entry.getKey();
            final URI copytoSource = entry.getValue();
            final URI srcFile = job.tempDir.toURI().resolve(copytoSource);
            final URI targetFile = job.tempDir.toURI().resolve(copytoTarget);

            if (new File(targetFile).exists()) {
                logger.warn(MessageUtils.getInstance().getMessage("DOTX064W", copytoTarget.getPath()).toString());
            } else {
                final URI inputMapInTemp = job.tempDir.toURI().resolve(job.getInputMap());
                copyFileWithPIReplaced(srcFile, targetFile, copytoTarget, inputMapInTemp);
                // add new file info into job
                final FileInfo src = job.getFileInfo(copytoSource);
                assert src != null;
                final URI target = job.getInputDir().resolve(copytoTarget);
                final FileInfo.Builder b = new FileInfo.Builder(src).result(target);
                final FileInfo dst = b.uri(copytoTarget).build();
                job.add(dst);
            }
        }
    }


    /**
     * Copy files and replace workdir PI contents.
     * @param src source URI in temporary directory
     * @param target target URI in temporary directory
     * @param copytoTargetFilename target URI relative to temporary directory
     * @param inputMapInTemp
     */
    private void copyFileWithPIReplaced(final URI src, final URI target, final URI copytoTargetFilename, final URI inputMapInTemp) {
        assert src.isAbsolute();
        assert target.isAbsolute();
        assert !copytoTargetFilename.isAbsolute();
        assert inputMapInTemp.isAbsolute();
        final File workdir = new File(target).getParentFile();
        if (!workdir.exists() && ! workdir.mkdirs()) {
            logger.error("Failed to create copy-to target directory " +  workdir.toURI());
            return;
        }
        final File path2project = getPathtoProject(copytoTargetFilename, target, inputMapInTemp, job);
        XMLFilter filter = new CopyToFilter(workdir, path2project);

        logger.info("Processing " + src + " to " + target);
        try {
            XMLUtils.transform(src, target, Collections.singletonList(filter));
        } catch (final DITAOTException e) {
            logger.error("Failed to write copy-to file: " + e.getMessage(), e);
        }
    }

    /**
     * XML filter to rewrite processing instructions to reflect copy-to location. The following processing-instructions are
     * processed:
     *
     * <ul>
     * <li>{@link Constants#PI_WORKDIR_TARGET PI_WORKDIR_TARGET}</li>
     * <li>{@link Constants#PI_WORKDIR_TARGET_URI PI_WORKDIR_TARGET_URI}</li>
     * <li>{@link Constants#PI_PATH2PROJ_TARGET PI_PATH2PROJ_TARGET}</li>
     * <li>{@link Constants#PI_PATH2PROJ_TARGET_URI PI_PATH2PROJ_TARGET_URI}</li>
     * </ul>
     */
    private static final class CopyToFilter extends XMLFilterImpl {

        private final File workdir;
        private final File path2project;

        CopyToFilter(final File workdir, final File path2project) {
            super();
            this.workdir = workdir;
            this.path2project = path2project;
        }

        @Override
        public void processingInstruction(final String target, final String data) throws SAXException {
            String d = data;
            switch (target) {
                case PI_WORKDIR_TARGET:
                    if (workdir != null) {
                        try {
                            if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
                                d = workdir.getCanonicalPath();
                            } else {
                                d = UNIX_SEPARATOR + workdir.getCanonicalPath();
                            }
                        } catch (final IOException e) {
                            throw new RuntimeException("Failed to get canonical path for working directory: " + e.getMessage(), e);
                        }
                    }
                    break;
                case PI_WORKDIR_TARGET_URI:
                    if (workdir != null) {
                        d = workdir.toURI().toString();
                    }
                    break;
                case PI_PATH2PROJ_TARGET:
                    if (path2project != null) {
                        d = path2project.getPath();
                    }
                    break;
                case PI_PATH2PROJ_TARGET_URI:
                    if (path2project != null) {
                        d = toURI(path2project).toString();
                        if (!d.endsWith(URI_SEPARATOR)) {
                            d = d + URI_SEPARATOR;
                        }
                    }
                    break;
            }
            getContentHandler().processingInstruction(target, d);
        }

    }

    /**
     * Get path to base directory
     *
     * @param filename relative input file path from base directory
     * @param traceFilename absolute input file
     * @param inputMap absolute path to start file
     * @return path to base directory, {@code null} if not available
     */
    // TODO return URI
    public static File getPathtoProject(final URI filename, final URI traceFilename, final URI inputMap, final Job job) {
        assert traceFilename.isAbsolute();
        assert inputMap.isAbsolute();
        // FIXME out generation has already been determined, why do it here again?
        if (job.getGeneratecopyouter() != Generate.OLDSOLUTION) {
            if (isOutFile(traceFilename, inputMap)) {
                return toFile(getRelativePathFromOut(traceFilename, job));
            } else {
                return toFile(getRelativePath(traceFilename, inputMap)).getParentFile();
            }
        } else {
            return toFile(URLUtils.getRelativePath(filename));
        }
    }
    /**
     * Just for the overflowing files.
     * @param overflowingFile overflowingFile
     * @return relative system path to out which ends in {@link File#separator File.separator}
     */
    private static String getRelativePathFromOut(final URI overflowingFile, final Job job) {
        final URI relativePath = getRelativePath(job.getInputFile(), overflowingFile);
        final URI outputDir = job.getOutputDir().getAbsoluteFile().toURI();
        final URI outputPathName = outputDir.resolve("index.html");
        final URI finalOutFilePathName = outputDir.resolve(relativePath.getPath());
        final URI finalRelativePathName = getRelativePath(finalOutFilePathName, outputPathName);
        File parentDir = toFile(finalRelativePathName).getParentFile();
        if (parentDir == null || parentDir.getPath().isEmpty()) {
            parentDir = new File(".");
        }
        return parentDir.getPath() + File.separator;
    }

    /**
     * Check if path falls outside start document directory
     *
     * @param filePathName absolute path to test
     * @param inputMap absolute input map path
     * @return {@code true} if outside start directory, otherwise {@code false}
     */
    private static boolean isOutFile(final URI filePathName, final URI inputMap){
        final URI relativePath = URLUtils.getRelativePath(inputMap, filePathName);
        return !(relativePath.getPath().length() == 0 || !relativePath.getPath().startsWith(".."));
    }

}
