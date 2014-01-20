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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.TopicRefWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The chunking module class.
 * 
 */
final public class ChunkModule extends AbstractPipelineModuleImpl {

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
        final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
        // change to xml property
        final ChunkMapReader mapReader = new ChunkMapReader();
        mapReader.setLogger(logger);
        mapReader.setJob(job);
        mapReader.setup(transtype);

        try {
            final File mapFile = new File(job.tempDir, job.getProperty(INPUT_DITAMAP)).getAbsoluteFile();
            if (transtype.equals(INDEX_TYPE_ECLIPSEHELP) && isEclipseMap(mapFile)) {
                for (final FileInfo f : job.getFileInfo()) {
                    if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                        mapReader.read(new File(job.tempDir, f.file.getPath()).getAbsoluteFile());
                    }
                }
            } else {
                mapReader.read(mapFile);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        final Map<String, String> changeTable = mapReader.getChangeTable();
        if (!changeTable.isEmpty()) {
            // update dita.list to include new generated files
            updateList(changeTable, mapReader.getConflicTable());
            // update references in dita files
            updateRefOfDita(changeTable, mapReader.getConflicTable());
        }

        return null;
    }
    
    private boolean isEclipseMap(final File mapFile) throws DITAOTException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc;
        try {
            doc = builder.parse(mapFile);
        } catch (final SAXException e) {
            throw new DITAOTException("Failed to parse input map: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DITAOTException("Failed to parse input map: " + e.getMessage(), e);
        }
        final Element root = doc.getDocumentElement();
        return root.getAttribute(ATTRIBUTE_NAME_CLASS).contains(" eclipsemap/plugin ");
    }

    // update the href in ditamap and topic files
    private void updateRefOfDita(final Map<String, String> changeTable, final Map<String, String> conflictTable) {
        final TopicRefWriter topicRefWriter = new TopicRefWriter();
        topicRefWriter.setLogger(logger);
        topicRefWriter.setChangeTable(changeTable);
        topicRefWriter.setup(conflictTable);
        try {
            for (final FileInfo f : job.getFileInfo()) {
                if (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                    topicRefWriter.write(job.tempDir.getAbsoluteFile(), f.file, relativePath2fix);
                }
            }
        } catch (final DITAOTException ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    private void updateList(final Map<String, String> changeTable, final Map<String, String> conflictTable) {
        final File xmlDitalist = new File(job.tempDir, "dummy.xml");

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
                        if (resolve(job.tempDir.getAbsolutePath(), ent).getPath().equals(
                                resolve(job.tempDir.getAbsolutePath(), s).getPath())) {
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

        final Set<String> topicList = new LinkedHashSet<String>(128);
        final Set<String> oldTopicList = new HashSet<String>();
        for (final FileInfo f : job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITA.equals(f.format)) {
                oldTopicList.add(f.file.getPath());
            }
        }
        for (final String hrefTopic : hrefTopics) {
            final String t = getRelativePath(xmlDitalist.getAbsolutePath(), resolve(job.tempDir.getAbsolutePath(), stripFragment(hrefTopic)).getPath(), File.separator);
            topicList.add(t);
            if (oldTopicList.contains(t)) {
                oldTopicList.remove(t);
            }
        }
        
        final Set<String> chunkedTopicSet = new LinkedHashSet<String>(128);
        final Set<String> chunkedDitamapSet = new LinkedHashSet<String>(128);
        final Set<String> ditamapList = new HashSet<String>();
        for (final FileInfo f : job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
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
                if (extName != null && !extName.equalsIgnoreCase(ATTR_FORMAT_VALUE_DITAMAP)) {
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
                final File f = new File(job.tempDir, s);
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
                        // ensure the newly chunked file to the old one
                        try {
                            FileUtils.moveFile(from, target);
                        } catch (final IOException e) {
                            logger.error("Failed to replace chunk topic: " + e.getMessage(), e);

                        }
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

        final Set<String> all = new HashSet<String>();
        all.addAll(topicList);
        all.addAll(ditamapList);
        all.addAll(chunkedDitamapSet);
        all.addAll(chunkedTopicSet);
        
        // remove redundant topic information
        for (final String file: oldTopicList) {
            if (!all.contains(file)) {
                job.remove(job.getOrCreateFileInfo(file));
            }
        }
        
        for (final String file : topicList) {
            final FileInfo ff = job.getOrCreateFileInfo(file);
            ff.format = ATTR_FORMAT_VALUE_DITA;
        }
        for (final String file : ditamapList) {
            final FileInfo ff = job.getOrCreateFileInfo(file);
            ff.format = ATTR_FORMAT_VALUE_DITAMAP;
        }

        for (final String file : chunkedDitamapSet) {
            final FileInfo f = job.getOrCreateFileInfo(file);
            f.format = ATTR_FORMAT_VALUE_DITAMAP;
            f.isResourceOnly = false;
        }
        for (final String file : chunkedTopicSet) {
            final FileInfo f = job.getOrCreateFileInfo(file);
            f.format = ATTR_FORMAT_VALUE_DITA;
            f.isResourceOnly = false;
        }

        try {
            job.write();
        } catch (final IOException ex) {
            logger.error(ex.getMessage(), ex);
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
