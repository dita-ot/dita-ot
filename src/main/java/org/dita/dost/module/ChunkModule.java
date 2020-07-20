/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.TopicRefWriter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.sf.saxon.s9api.streams.Steps.attribute;
import static net.sf.saxon.s9api.streams.Steps.child;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getExtension;
import static org.dita.dost.util.URLUtils.getRelativePath;
import static org.dita.dost.util.URLUtils.stripFragment;

/**
 * The chunking module class.
 * <p>
 * Starting from map files, it parses and processes chunk attribute, writes out the chunked
 * results and finally updates reference pointing to chunked topics in other topics.
 */
final public class ChunkModule extends AbstractPipelineModuleImpl {

    private static final DitaClass ECLIPSEMAP_PLUGIN = new DitaClass("- map/map eclipsemap/plugin ");
    private static final String ROOT_CHUNK_OVERRIDE = "root-chunk-override";

    /**
     * using to save relative path when do rename action for newly chunked file
     */
    private final Map<URI, String> relativePath2fix = new HashMap<>();

    /**
     * Entry point of chunk module.
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
        mapReader.supportToNavigation(INDEX_TYPE_ECLIPSEHELP.equals(transtype));
        if (input.getAttribute(ROOT_CHUNK_OVERRIDE) != null) {
            mapReader.setRootChunkOverride(input.getAttribute(ROOT_CHUNK_OVERRIDE));
        }

        try {
            final Job.FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
            final File mapFile = new File(job.tempDirURI.resolve(in.uri));
            if (transtype.equals(INDEX_TYPE_ECLIPSEHELP) && isEclipseMap(mapFile.toURI())) {
                for (final FileInfo f : job.getFileInfo()) {
                    if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                        mapReader.read(new File(job.tempDir, f.file.getPath()).getAbsoluteFile());
                    }
                }
            } else {
                mapReader.read(mapFile);
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        final Map<URI, URI> changeTable = mapReader.getChangeTable();
        if (hasChanges(changeTable)) {
            final Map<URI, URI> conflicTable = mapReader.getConflicTable();
            updateList(changeTable, conflicTable, mapReader);
            updateRefOfDita(changeTable, conflicTable);
        }

        return null;
    }

    /**
     * Test whether there are changes that require topic rewriting.
     */
    private boolean hasChanges(final Map<URI, URI> changeTable) {
        if (changeTable.isEmpty()) {
            return false;
        }
        for (Map.Entry<URI, URI> e : changeTable.entrySet()) {
            if (!e.getKey().equals(e.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether ditamap is an Eclipse specialization.
     *
     * @param mapFile ditamap file to test
     * @return {@code true} if Eclipse specialization, otherwise {@code false}
     * @throws DITAOTException if reading ditamap fails
     */
    private boolean isEclipseMap(final URI mapFile) throws DITAOTException {
        XdmNode doc;
        try {
            doc = job.getStore().getImmutableNode(mapFile);
        } catch (final IOException e) {
            throw new DITAOTException("Failed to parse input map: " + e.getMessage(), e);
        }
        return doc
                .select(child().first().then(attribute(ATTRIBUTE_NAME_CLASS)))
                .anyMatch(xdmItems -> ECLIPSEMAP_PLUGIN.matches(xdmItems.getStringValue()));
    }

    /**
     * Update href attributes in ditamap and topic files.
     */
    private void updateRefOfDita(final Map<URI, URI> changeTable, final Map<URI, URI> conflictTable) {
        final TopicRefWriter topicRefWriter = new TopicRefWriter();
        topicRefWriter.setLogger(logger);
        topicRefWriter.setJob(job);
        topicRefWriter.setChangeTable(changeTable);
        topicRefWriter.setup(conflictTable);
        try {
            for (final FileInfo f : job.getFileInfo()) {
                if (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                    topicRefWriter.setFixpath(relativePath2fix.get(f.uri));
                    final File tmp = new File(job.tempDirURI.resolve(f.uri));
                    topicRefWriter.write(tmp);
                }
            }
        } catch (final DITAOTException ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    /**
     * Update Job configuration to include new generated files
     */
    private void updateList(final Map<URI, URI> changeTable, final Map<URI, URI> conflictTable, final ChunkMapReader mapReader) {
        final URI xmlDitalist = job.tempDirURI.resolve("dummy.xml");

        final Set<URI> hrefTopics = new HashSet<>();
        final Set<URI> chunkTopicSet = mapReader.getChunkTopicSet();
        for (final FileInfo f : job.getFileInfo()) {
            final URI abs = job.tempDirURI.resolve(f.uri);
            if (f.isTarget && !chunkTopicSet.contains(abs)) {
                hrefTopics.add(f.uri);
            }
        }
        for (final FileInfo f : job.getFileInfo()) {
            final URI abs = job.tempDirURI.resolve(f.uri);
            if (chunkTopicSet.contains(abs)) {
                final URI s = f.uri;
                if (s.getFragment() == null) {
                    // This entry does not have an anchor, we assume that this
                    // topic will
                    // be fully chunked. Thus it should not produce any output.
                    // The entry in hrefTopics points to the same target
                    // as entry in chunkTopics, it should be removed.
                    hrefTopics.removeIf(ent -> job.tempDirURI.resolve(ent).equals(
                            job.tempDirURI.resolve(s)));
                } else {
                    hrefTopics.remove(s);
                }
            }
        }

        // relative temporary files
        final Set<URI> topicList = new LinkedHashSet<>(128);
        final Set<URI> oldTopicList = new HashSet<>();
        for (final FileInfo f : job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITA.equals(f.format)) {
                oldTopicList.add(f.uri);
            }
        }
        for (final URI t : hrefTopics) {
            topicList.add(t);
            oldTopicList.remove(t);
        }

        final Set<URI> chunkedTopicSet = new LinkedHashSet<>(128);
        final Set<URI> chunkedDitamapSet = new LinkedHashSet<>(128);
        final Set<URI> ditamapList = new HashSet<>();
        for (final FileInfo f : job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                ditamapList.add(f.uri);
            }
        }
        for (final Map.Entry<URI, URI> entry : changeTable.entrySet()) {
            final URI oldFile = entry.getKey();
            final URI newFile = entry.getValue();
            if (newFile.equals(oldFile)) {
                // newly chunked file
                final URI newChunkedFile = getRelativePath(xmlDitalist, newFile);
                final String extName = getExtension(newChunkedFile.getPath());
                if (extName != null && !extName.equalsIgnoreCase(ATTR_FORMAT_VALUE_DITAMAP)) {
                    chunkedTopicSet.add(newChunkedFile);
                    if (!topicList.contains(newChunkedFile)) {
                        topicList.add(newChunkedFile);
                        // newly chunked file shouldn't be deleted
                        oldTopicList.remove(newChunkedFile);
                    }
                } else {
                    if (!ditamapList.contains(newChunkedFile)) {
                        ditamapList.add(newChunkedFile);
                        oldTopicList.remove(newChunkedFile);
                    }
                    chunkedDitamapSet.add(newChunkedFile);
                }

            }
        }
        // removed extra topic files
        for (final URI s : oldTopicList) {
            final URI f = job.tempDirURI.resolve(s);
            if (job.getStore().exists(f)) {
                logger.debug("Delete " + f);
                try {
                    job.getStore().delete(f);
                } catch (IOException e) {
                    logger.error("Failed to delete " + f);
                }
            }
        }

        // TODO we have refined topic list and removed extra topic files, next
        // we need to clean up
        // conflictTable and try to resolve file name conflicts.
        for (final Map.Entry<URI, URI> entry : changeTable.entrySet()) {
            final URI oldFile = entry.getKey();
            final URI from = entry.getValue();
            if (from.equals(oldFile)) {
                // original topic file
                // FIXME
                final URI targetPath = conflictTable.get(oldFile);
                if (targetPath != null) {
                    final URI target = targetPath;
                    if (!job.getStore().exists(target)) {
                        // newly chunked file
                        URI relativePath = getRelativePath(xmlDitalist, from);
                        final URI relativeTargetPath = getRelativePath(xmlDitalist, target);
                        if (relativeTargetPath.getPath().contains(URI_SEPARATOR)) {
                            relativePath2fix.put(relativeTargetPath,
                                    relativeTargetPath.getPath().substring(0, relativeTargetPath.getPath().lastIndexOf(URI_SEPARATOR) + 1));
                        }
                        // ensure the newly chunked file to the old one
                        try {
                            if (job.getStore().exists(target)) {
                                logger.debug("Delete " + target);
                                job.getStore().delete(target);
                            }
                            logger.debug("Move " + from + " to " + target);
                            job.getStore().move(from, target);
                            final FileInfo fi = job.getFileInfo(from);
                            if (fi != null) {
                                job.remove(fi);
                            }
                        } catch (final IOException e) {
                            logger.error("Failed to replace chunk topic: " + e.getMessage(), e);

                        }
                        topicList.remove(relativePath);
                        chunkedTopicSet.remove(relativePath);
                        relativePath = getRelativePath(xmlDitalist, target);
                        topicList.add(relativePath);
                        chunkedTopicSet.add(relativePath);
                    } else {
                        conflictTable.remove(oldFile);
                    }
                }
            }
        }

        final Set<URI> all = new HashSet<>();
        all.addAll(topicList);
        all.addAll(ditamapList);
        all.addAll(chunkedDitamapSet);
        all.addAll(chunkedTopicSet);

        // remove redundant topic information
        for (final URI file : oldTopicList) {
            if (!all.contains(file)) {
                final FileInfo fi = job.getFileInfo(file);
                if (fi != null) {
                    job.remove(fi);
                }
            }
        }

        for (final URI file : topicList) {
            // FIXME
            final FileInfo ff = job.getOrCreateFileInfo(stripFragment(file));
            ff.format = ATTR_FORMAT_VALUE_DITA;
        }
        for (final URI file : ditamapList) {
            final FileInfo ff = job.getOrCreateFileInfo(file);
            ff.format = ATTR_FORMAT_VALUE_DITAMAP;
        }

        for (final URI file : chunkedDitamapSet) {
            final FileInfo f = job.getOrCreateFileInfo(file);
            f.format = ATTR_FORMAT_VALUE_DITAMAP;
            f.isResourceOnly = false;
        }
        for (final URI file : chunkedTopicSet) {
            // FIXME
            final FileInfo f = job.getOrCreateFileInfo(stripFragment(file));
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
    public interface ChunkFilenameGenerator {

        /**
         * Generate file name
         *
         * @param prefix    file name prefix
         * @param extension file extension
         * @return generated file name
         */
        String generateFilename(final String prefix, final String extension);

        /**
         * Generate ID.
         *
         * @return generated ID
         */
        String generateID();

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
