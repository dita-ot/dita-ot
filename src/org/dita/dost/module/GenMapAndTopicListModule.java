/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.PipelineReader;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.PropertiesWriter;


/**
 * Generate map and topic list.
 * 
 * @version 1.0 Nov 25, 2004
 * 
 * @author Wu, Zhi Qiang
 */
public class GenMapAndTopicListModule extends AbstractPipelineModule {
    private PipelineReader reader = null;
    private Set ditaList = null;
    private Set fullTopicList = null;
    private Set fullMapList = null;
    private Set hrefTopicList = null;
    private Set hrefMapList = null;
    private Set conrefList = null;
    private Set imageList = null;
    private List waitList = null;

    public GenMapAndTopicListModule() {
        ditaList = new TreeSet();
        fullTopicList = new TreeSet();
        fullMapList = new TreeSet();
        hrefTopicList = new TreeSet();
        hrefMapList = new TreeSet();
        conrefList = new TreeSet();
        imageList = new TreeSet();
        reader = new PipelineReader();
        waitList = new ArrayList();

        try {
            reader.initXMLReader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param input
     * @return
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) {
        String inputFile = ((PipelineHashIO) input).getAttribute("inputmap");
        String baseDir = ((PipelineHashIO) input).getAttribute("basedir");
        String tempDir = ((PipelineHashIO) input).getAttribute("tempDir");

        // Initiate the waitingList
        addToWaitList(inputFile);

        // Parse all the file in the waitingList
        for (int i = 0; i < waitList.size(); i++) {
            // Get the file name from the waitingList
            String filename = (String) waitList.get(i);

            // Parse the file
            try {
                if (baseDir != null) {
                    reader.parse(baseDir + "|" + filename);
                } else {
                    reader.parse(filename);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Parse the result
            Iterator iter = reader.getResult().iterator();
            while (iter.hasNext()) {
                String item = (String) iter.next();

                addToWaitList(item);

                String lcaseItem = item.toLowerCase();
                if (lcaseItem.endsWith(".jpg") || lcaseItem.endsWith(".gif")
                        || lcaseItem.endsWith(".eps")) {
                    imageList.add(item);
                }
            }

            /*
             * Add filename to the proper list according to the extension file
             * name and parse result
             */
            ditaList.add(filename);
            if (reader.hasConRef()) {
                conrefList.add(filename);
            }

            if (filename.toLowerCase().endsWith(".dita")
                    || filename.toLowerCase().endsWith(".xml")) {
                fullTopicList.add(filename);
                if (reader.hasHref()) {
                    hrefTopicList.add(filename);
                }
            }

            if (filename.toLowerCase().endsWith(".ditamap")) {
                fullMapList.add(filename);
                if (reader.hasHref()) {
                    hrefMapList.add(filename);
                }
            }
        }

        Properties prop = new Properties();
        prop.put("dita.list", StringUtils.assembleString(ditaList, ",")
                .replaceAll("\\\\", "/"));
        prop.put("fullditatopc.list", StringUtils.assembleString(fullTopicList,
                ",").replaceAll("\\\\", "/"));
        prop.put("fullditamap.list", StringUtils.assembleString(fullMapList,
                ",").replaceAll("\\\\", "/"));
        prop.put("hrefditatopc.list", StringUtils.assembleString(hrefTopicList,
                ",").replaceAll("\\\\", "/"));
        prop.put("conref.list", StringUtils.assembleString(conrefList, ",")
                .replaceAll("\\\\", "/"));
        prop.put("imagelist.list", StringUtils.assembleString(imageList, ",")
                .replaceAll("\\\\", "/"));

        PropertiesWriter writer = new PropertiesWriter();
        Content content = new ContentImpl();
        content.setObject(prop);
        writer.setContent(content);
        
        try {
            File dir = new File(tempDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            writer.write(tempDir + File.separator + "dita.list");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addToWaitList(String fileName) {
        if (waitList.contains(fileName)) {
            return;
        }

        if (fileName.endsWith(".dita") || fileName.endsWith(".xml")
                || fileName.endsWith(".ditamap")) {
            waitList.add(fileName);
        }
    }
}