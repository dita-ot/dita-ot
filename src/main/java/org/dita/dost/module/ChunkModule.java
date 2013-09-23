/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.TopicRefWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The chunking module class.
 * 
 */
final public class ChunkModule implements AbstractPipelineModule {

    private DITAOTLogger logger;

    /**
     * using to save relative path when do rename action for newly chunked file
     */
    final Map<String, String> relativePath2fix = new HashMap<String, String>();

    /**
     * Constructor.
     */
    public ChunkModule() {
        super();
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Entry point of chunk module. Starting from map files, it parses and
     * processes chunk attribute, writes out the "chunked" results and finally
     * update references pointing to "chunked" topics in other dita topics.
     * 
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);

        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        // change to xml property
        final ChunkMapReader mapReader = new ChunkMapReader();
        mapReader.setLogger(logger);
        mapReader.setup(transtype);

        Job job = null;
        try {
            job = new Job(tempDir);
        } catch (final IOException ioe) {
            throw new DITAOTException(ioe);
        }
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final String mapFile = new File(tempDir, job.getProperty(INPUT_DITAMAP)).getAbsolutePath();
            final Document doc = builder.parse(new File(mapFile));
            final Element root = doc.getDocumentElement();
            if (root.getAttribute(ATTRIBUTE_NAME_CLASS).contains(" eclipsemap/plugin ")
                    && transtype.equals(INDEX_TYPE_ECLIPSEHELP)) {
                for (final FileInfo f : job.getFileInfo()) {
                    if (f.isActive && ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                        mapReader.read(new File(tempDir, f.file.getPath()).getAbsoluteFile());
                    }
                }
            } else {
                mapReader.read(new File(mapFile));
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }

        final Map<String, String> changeTable = mapReader.getChangeTable();
        if (changeTable != null) {
            // update dita.list to include new generated files
            updateList(changeTable, mapReader.getConflicTable(), input);
            // update references in dita files
            updateRefOfDita(changeTable, mapReader.getConflicTable(), input);
        }

        return null;
    }

    // update the href in ditamap and topic files
    private void updateRefOfDita(final Map<String, String> changeTable, final Hashtable<String, String> conflictTable,
            final AbstractPipelineInput input) {
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        Job job = null;
        try {
            job = new Job(tempDir);
        } catch (final IOException io) {
            logger.logError(io.getMessage());
        }
        final TopicRefWriter topicRefWriter = new TopicRefWriter();
        topicRefWriter.setLogger(logger);
        topicRefWriter.setChangeTable(changeTable);
        topicRefWriter.setup(conflictTable);
        try {
            for (final FileInfo f : job.getFileInfo()) {
                if (f.isActive
                        && (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format))) {
                    topicRefWriter.write(tempDir.getAbsoluteFile(), new File(f.file.getPath()), relativePath2fix);
                }
            }
        } catch (final DITAOTException ex) {
            logger.logError(ex.getMessage(), ex);
        }

    }

    private void updateList(final Map<String, String> changeTable, final Hashtable<String, String> conflictTable,
            final AbstractPipelineInput input) {
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        final File xmlDitalist = new File(tempDir, "dummy.xml");
        Job job = null;
        try {
            job = new Job(tempDir);
        } catch (final IOException ex) {
            logger.logError(ex.getMessage(), ex);
        }

        final Set<String> hrefTopics = new HashSet<String>();
        for (final FileInfo f : job.getFileInfo()) {
            if (f.isNonConrefTarget) {
                hrefTopics.add(f.file.getPath());
            }
        }
        for (final FileInfo f : job.getFileInfo()) {
            if (f.isSkipChunk) {
                final String s = f.file.getPath();
                if (!StringUtils.isEmptyString(s) && getFragment(s) == null) {
                    // This entry does not have an anchor, we assume that this
                    // topic will
                    // be fully chunked. Thus it should not produce any output.
                    final Iterator<String> hrefit = hrefTopics.iterator();
                    while (hrefit.hasNext()) {
                        final String ent = hrefit.next();
                        if (resolveFile(tempDir.getAbsolutePath(), ent).getPath().equals(
                                resolveFile(tempDir.getAbsolutePath(), s).getPath())) {
                            // The entry in hrefTopics points to the same target
                            // as entry in chunkTopics, it should be removed.
                            hrefit.remove();
                        }
                    }
                } else if (!StringUtils.isEmptyString(s) && hrefTopics.contains(s)) {
                    hrefTopics.remove(s);
                }
            }
        }

        final Set<String> topicList = new LinkedHashSet<String>(INT_128);
        final Set<String> oldTopicList = new HashSet<String>();
        for (final FileInfo f : job.getFileInfo()) {
            if (f.isActive && ATTR_FORMAT_VALUE_DITA.equals(f.format)) {
                oldTopicList.add(f.file.getPath());
            }
        }
        for (String t : hrefTopics) {
            t = stripFragment(t);
            t = getRelativePath(xmlDitalist.getAbsolutePath(), resolveFile(tempDir.getAbsolutePath(), t).getPath(), File.separator);
            topicList.add(t);
            if (oldTopicList.contains(t)) {
                oldTopicList.remove(t);
            }
        }
        
        final Set<String> chunkedTopicSet = new LinkedHashSet<String>(INT_128);
        final Set<String> chunkedDitamapSet = new LinkedHashSet<String>(INT_128);
        final Set<String> ditamapList = new HashSet<String>();
        for (final FileInfo f : job.getFileInfo()) {
            if (f.isActive && ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                ditamapList.add(f.file.getPath());
            }
        }
        for (final Map.Entry<String, String> entry : changeTable.entrySet()) {
            final String oldFile = entry.getKey();
            if (entry.getValue().equals(oldFile)) {
                // newly chunked file
                String newChunkedFile = entry.getValue();
                newChunkedFile = getRelativePath(xmlDitalist.getAbsolutePath(), newChunkedFile, File.separator);
                final String extName = getExtension(newChunkedFile);
                if (extName != null && !extName.equalsIgnoreCase("DITAMAP")) {
                    chunkedTopicSet.add(newChunkedFile);
                    if (!topicList.contains(newChunkedFile)) {
                        topicList.add(newChunkedFile);
                        if (oldTopicList.contains(newChunkedFile)) {
                            // newly chunked file shouldn't be deleted
                            oldTopicList.remove(newChunkedFile);
                        }
                    }
                } else {
                    if (!ditamapList.contains(newChunkedFile)) {
                        ditamapList.add(newChunkedFile);
                        if (oldTopicList.contains(newChunkedFile)) {
                            oldTopicList.remove(newChunkedFile);
                        }
                    }
                    chunkedDitamapSet.add(newChunkedFile);
                }

            }
        }
        // removed extra topic files
        for (final String s : oldTopicList) {
            if (!StringUtils.isEmptyString(s)) {
                final File f = new File(tempDir, s);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        // TODO we have refined topic list and removed extra topic files, next
        // we need to clean up
        // conflictTable and try to resolve file name conflicts.
        for (final Map.Entry<String, String> entry : changeTable.entrySet()) {
            final String oldFile = entry.getKey();
            if (entry.getValue().equals(oldFile)) {
                // original topic file
                final String targetPath = conflictTable.get(entry.getKey());
                if (targetPath != null) {
                    final File target = new File(targetPath);
                    if (!fileExists(target.getAbsolutePath())) {
                        // newly chunked file
                        final File from = new File(entry.getValue());
                        String relativePath = getRelativePath(xmlDitalist.getAbsolutePath(), from.getAbsolutePath(), File.separator);
                        final String relativeTargetPath = getRelativePath(xmlDitalist.getAbsolutePath(),
                                target.getAbsolutePath(), File.separator);
                        if (relativeTargetPath.lastIndexOf(SLASH) != -1) {
                            relativePath2fix.put(relativeTargetPath,
                                    relativeTargetPath.substring(0, relativeTargetPath.lastIndexOf(SLASH) + 1));
                        }
                        // ensure the rename
                        target.delete();
                        // ensure the newly chunked file to the old one
                        from.renameTo(target);
                        if (topicList.contains(relativePath)) {
                            topicList.remove(relativePath);
                        }
                        if (chunkedTopicSet.contains(relativePath)) {
                            chunkedTopicSet.remove(relativePath);
                        }
                        relativePath = getRelativePath(xmlDitalist.getAbsolutePath(), target.getAbsolutePath(), File.separator);
                        topicList.add(relativePath);
                        chunkedTopicSet.add(relativePath);
                    } else {
                        conflictTable.remove(entry.getKey());
                    }
                }
            }
        }

        for (final FileInfo f : job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                f.isActive = false;
            }
        }
        for (final String file : topicList) {
            final FileInfo ff = job.getOrCreateFileInfo(file);
            ff.format = ATTR_FORMAT_VALUE_DITA;
            ff.isActive = true;
        }
        for (final String file : ditamapList) {
            final FileInfo ff = job.getOrCreateFileInfo(file);
            ff.format = ATTR_FORMAT_VALUE_DITAMAP;
            ff.isActive = true;
        }

        for (final String file : chunkedDitamapSet) {
            final FileInfo f = job.getOrCreateFileInfo(file);
            f.format = ATTR_FORMAT_VALUE_DITAMAP;
            f.isResourceOnly = false;
            f.isActive = true;
        }
        for (final String file : chunkedTopicSet) {
            final FileInfo f = job.getOrCreateFileInfo(file);
            f.format = ATTR_FORMAT_VALUE_DITA;
            f.isResourceOnly = false;
            f.isActive = true;
        }

        try {
            job.write();
        } catch (final IOException ex) {
            logger.logError(ex.getMessage(), ex);
        }
    }

    /**
     * Factory for chunk filename generator.
     */
    public static class ChunkFilenameGeneratorFactory {

        public static ChunkFilenameGenerator newInstance() {
            final String mode = Configuration.configuration.get("chunk.id-generation-scheme");
            if (mode != null && mode.equals("counter")) {
                return new CounterChunkFilenameGenerator();
            } else {
                return new RandomChunkFilenameGenerator();
            }
        }

    }

    /**
     * Generator fror chunk filenames and identifiers.
     */
    public static interface ChunkFilenameGenerator {

        /**
         * Generate file name
         * 
         * @param prefix file name prefix
         * @param extension file extension
         * @return generated file name
         */
        public String generateFilename(final String prefix, final String extension);

        /**
         * Generate ID.
         * 
         * @return generated ID
         */
        public String generateID();

    }

    public static class RandomChunkFilenameGenerator implements ChunkFilenameGenerator {
        private final Random random = new Random();

        @Override
        public String generateFilename(final String prefix, final String extension) {
            return prefix + random.nextInt(Integer.MAX_VALUE) + extension;
        }

        @Override
        public String generateID() {
            return "unique_" + random.nextInt(Integer.MAX_VALUE);
        }
    }

    public static class CounterChunkFilenameGenerator implements ChunkFilenameGenerator {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public String generateFilename(final String prefix, final String extension) {
            return prefix + counter.getAndIncrement() + extension;
        }

        @Override
        public String generateID() {
            return "unique_" + counter.getAndIncrement();
        }
    }

}
