/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.TopicRefWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * The chunking module class.
 *
 */
final class ChunkModule implements AbstractPipelineModule {

    private DITAOTLogger logger;

    /**
     *  using to save relative path when do rename action for newly chunked file
     */
    final Map<String,String> relativePath2fix=new HashMap<String,String>();

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
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        final String ditaext = input.getAttribute(ANT_INVOKER_PARAM_DITAEXT) != null ? input.getAttribute(ANT_INVOKER_PARAM_DITAEXT) : ".dita";
        final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);

        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        //change to xml property
        final ChunkMapReader mapReader = new ChunkMapReader();
        mapReader.setLogger(logger);
        mapReader.setup(ditaext, transtype);

        Job job = null;
        try{
            job = new Job(tempDir);
        }catch(final IOException ioe){
            throw new DITAOTException(ioe);
        }
        try{
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            String mapFile = new File(tempDir, job.getProperty(INPUT_DITAMAP)).getAbsolutePath();
            final Document doc = builder.parse(new File(mapFile));
            final Element root = doc.getDocumentElement();
            if(root.getAttribute(ATTRIBUTE_NAME_CLASS).contains(" eclipsemap/plugin ") && transtype.equals(INDEX_TYPE_ECLIPSEHELP)){
                for (final String ditaMap: job.getSet(FULL_DITAMAP_LIST)) {
                    mapFile = new File(tempDir, ditaMap).getAbsolutePath();
                    mapReader.read(mapFile);
                }
            }
            else{
                mapReader.read(mapFile);
            }
        }catch (final Exception e){
            logger.logError(e.getMessage(), e) ;
        }

        final Map<String,String> changeTable = mapReader.getChangeTable();
        if(changeTable != null){
            // update dita.list to include new generated files
            updateList(changeTable, mapReader.getConflicTable(),input);
            // update references in dita files
            updateRefOfDita(changeTable, mapReader.getConflicTable(),input);
        }




        return null;
    }
    //update the href in ditamap and topic files
    private void updateRefOfDita(final Map<String,String> changeTable, final Hashtable<String, String> conflictTable, final AbstractPipelineInput input){
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        Job job = null;
        try{
            job = new Job(tempDir);
        }catch(final IOException io){
            logger.logError(io.getMessage());
        }
        final TopicRefWriter topicRefWriter=new TopicRefWriter();
        topicRefWriter.setLogger(logger);
        topicRefWriter.setChangeTable(changeTable);
        topicRefWriter.setup(conflictTable);
        try{
            for (final String f: job.getSet(FULL_DITAMAP_TOPIC_LIST)) {
                topicRefWriter.write(tempDir.getAbsolutePath(), f, relativePath2fix);
            }
        }catch(final DITAOTException ex){
            logger.logError(ex.getMessage(), ex) ;
        }

    }


    private void updateList(final Map<String, String> changeTable, final Hashtable<String, String> conflictTable, final AbstractPipelineInput input){
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        final File xmlDitalist=new File(tempDir, "dummy.xml");
        Job job = null;
        try{
            job = new Job(tempDir);
        }catch(final IOException ex){
            logger.logError(ex.getMessage(), ex) ;
        }

        final Set<String> hrefTopics = job.getSet(HREF_TOPIC_LIST);
        final Set<String> chunkTopics = job.getSet(CHUNK_TOPIC_LIST);
        for (final String s : chunkTopics) {
            if (!StringUtils.isEmptyString(s) && !s.contains(SHARP)) {
                // This entry does not have an anchor, we assume that this topic will
                // be fully chunked. Thus it should not produce any output.
                final Iterator<String> hrefit = hrefTopics.iterator();
                while(hrefit.hasNext()) {
                    final String ent = hrefit.next();
                    if (FileUtils.resolveFile(tempDir.getAbsolutePath(), ent).equalsIgnoreCase(
                            FileUtils.resolveFile(tempDir.getAbsolutePath(), s)))  {
                        // The entry in hrefTopics points to the same target
                        // as entry in chunkTopics, it should be removed.
                        hrefit.remove();
                    }
                }
            } else if (!StringUtils.isEmptyString(s) && hrefTopics.contains(s)) {
                hrefTopics.remove(s);
            }
        }

        final Set<String> topicList = new LinkedHashSet<String>(INT_128);
        final Set<String> oldTopicList = job.getSet(FULL_DITA_TOPIC_LIST);
        for (String t : hrefTopics) {
            if (t.lastIndexOf(SHARP) != -1) {
                t = t.substring(0, t.lastIndexOf(SHARP));
            }
            t = FileUtils.getRelativePath(xmlDitalist.getAbsolutePath(), FileUtils.resolveFile(tempDir.getAbsolutePath(), t));
            topicList.add(t);
            if (oldTopicList.contains(t)) {
                oldTopicList.remove(t);
            }
        }

        final Set<String> chunkedTopicSet=new LinkedHashSet<String>(INT_128);
        final Set<String> chunkedDitamapSet=new LinkedHashSet<String>(INT_128);
        final Set<String> ditamapList = job.getSet(FULL_DITAMAP_LIST);
        for (final Map.Entry<String, String> entry: changeTable.entrySet()) {
            final String oldFile=entry.getKey();
            if(entry.getValue().equals(oldFile)){
                //newly chunked file
                String newChunkedFile=entry.getValue();
                newChunkedFile=FileUtils.getRelativePath(xmlDitalist.getAbsolutePath(), newChunkedFile);
                final String extName = FileUtils.getExtension(newChunkedFile);
                if(extName!=null && !extName.equalsIgnoreCase("DITAMAP")){
                    chunkedTopicSet.add(newChunkedFile);
                    if (!topicList.contains(newChunkedFile)) {
                        topicList.add(newChunkedFile);
                        if (oldTopicList.contains(newChunkedFile)) {
                            //newly chunked file shouldn't be deleted
                            oldTopicList.remove(newChunkedFile);
                        }
                    }
                }else{
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
        //removed extra topic files
        for (final String s : oldTopicList) {
            if (!StringUtils.isEmptyString(s)) {
                final File f = new File(tempDir, s);
                if(f.exists()) {
                    f.delete();
                }
            }
        }

        //TODO we have refined topic list and removed extra topic files, next we need to clean up
        // conflictTable and try to resolve file name conflicts.
        for (final Map.Entry<String,String> entry: changeTable.entrySet()) {
            final String oldFile = entry.getKey();
            if (entry.getValue().equals(oldFile)) {
                // original topic file
                final String targetPath = conflictTable.get(entry.getKey());
                if (targetPath != null) {
                    final File target = new File(targetPath);
                    if (!FileUtils.fileExists(target.getAbsolutePath())) {
                        // newly chunked file
                        final File from = new File(entry.getValue());
                        String relativePath = FileUtils.getRelativePath(xmlDitalist.getAbsolutePath(), from.getAbsolutePath());
                        final String relativeTargetPath = FileUtils.getRelativePath(xmlDitalist.getAbsolutePath(), target.getAbsolutePath());
                        if (relativeTargetPath.lastIndexOf(SLASH)!=-1){
                            relativePath2fix.put(relativeTargetPath, relativeTargetPath.substring(0, relativeTargetPath.lastIndexOf(SLASH)+1));
                        }
                        //ensure the rename
                        target.delete();
                        //ensure the newly chunked file to the old one
                        from.renameTo(target);
                        if (topicList.contains(relativePath)) {
                            topicList.remove(relativePath);
                        }
                        if (chunkedTopicSet.contains(relativePath)){
                            chunkedTopicSet.remove(relativePath);
                        }
                        relativePath = FileUtils.getRelativePath(xmlDitalist.getAbsolutePath(), target.getAbsolutePath());
                        topicList.add(relativePath);
                        chunkedTopicSet.add(relativePath);
                    } else {
                        conflictTable.remove(entry.getKey());
                    }
                }
            }
        }

        //TODO Remove newly generated files from resource-only list, these new files should not
        //     excluded from the final outputs.
        final Set<String> resourceOnlySet = job.getSet(RESOURCE_ONLY_LIST);
        resourceOnlySet.removeAll(chunkedTopicSet);
        resourceOnlySet.removeAll(chunkedDitamapSet);

        job.setSet(RESOURCE_ONLY_LIST, resourceOnlySet);
        job.setSet(FULL_DITA_TOPIC_LIST, topicList);
        job.setSet(FULL_DITAMAP_LIST, ditamapList);
        topicList.addAll(ditamapList);
        job.setSet(FULL_DITAMAP_TOPIC_LIST, topicList);

        job.setProperty("chunkedditamapfile", CHUNKED_DITAMAP_LIST_FILE);
        job.setProperty("chunkedtopicfile", CHUNKED_TOPIC_LIST_FILE);
        job.setProperty("resourceonlyfile", RESOURCE_ONLY_LIST_FILE);

        job.setSet(CHUNKED_DITAMAP_LIST, chunkedDitamapSet);
        job.setSet(CHUNKED_TOPIC_LIST, chunkedTopicSet);

        try{
            job.write();
        }catch(final IOException ex){
            logger.logError(ex.getMessage(), ex) ;
        }
    }

}
