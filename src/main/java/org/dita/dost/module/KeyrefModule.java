/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.GenMapAndTopicListModule.KeyDef;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.util.Job;
import org.dita.dost.writer.KeyrefPaser;
/**
 * Keyref Module.
 *
 */
final class KeyrefModule implements AbstractPipelineModule {

    private DITAOTLogger logger;

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Entry point of KeyrefModule.
     * 
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));

        if (!tempDir.isAbsolute()){
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }

        //Added by Alan Date:2009-08-04 --begin
        final String extName = input.getAttribute(ANT_INVOKER_PARAM_DITAEXT);
        //Added by Alan Date:2009-08-04 --end

        Job job = null;
        try{
            job = new Job(tempDir);
        }catch(final Exception e){
            logger.logException(e);
        }

        // maps of keyname and target
        final Map<String, String> keymap =new HashMap<String, String>();
        // store the key name defined in a map(keyed by ditamap file)
        final Hashtable<String, Set<String>> maps = new Hashtable<String, Set<String>>();

        for (final KeyDef keyDef: GenMapAndTopicListModule.readKeydef(new File(tempDir, KEYDEF_LIST_FILE))) {
            keymap.put(keyDef.keys, keyDef.href);
            // map file which define the keys
            final String map = keyDef.source;
            // put the keyname into corresponding map which defines it.
            //a map file can define many keys
            if(maps.containsKey(map)){
                maps.get(map).add(keyDef.keys);
            }else{
                final Set<String> set = new HashSet<String>();
                set.add(keyDef.keys);
                maps.put(map, set);
            }
        }
        final KeyrefReader reader = new KeyrefReader();
        reader.setLogger(logger);
        reader.setTempDir(tempDir.getAbsolutePath());
        for(final String mapFile: maps.keySet()){
            logger.logInfo("Reading " + new File(tempDir, mapFile).getAbsolutePath());
            reader.setKeys(maps.get(mapFile));
            reader.read(mapFile);
        }
        final Map<String, Element> keyDefinition = reader.getKeyDefinition();
        //get files which have keyref attr
        final Set<String> parseList = job.getSet(KEYREF_LIST);
        //Conref Module will change file's content, it is possible that tags with @keyref are copied in
        //while keyreflist is hard update with xslt.
        //bug:3056939
        final Set<String> resourceOnlyList = job.getSet(RESOURCE_ONLY_LIST);
        final Set<String> conrefList = job.getSet(CONREF_LIST);
        parseList.addAll(conrefList);
        for(final String file: parseList){
            logger.logInfo("Processing " + new File(tempDir, file).getAbsolutePath());
            final KeyrefPaser parser = new KeyrefPaser();
            parser.setLogger(logger);
            parser.setKeyDefinition(keyDefinition);
            parser.setTempDir(tempDir.getAbsolutePath());
            parser.setKeyMap(keymap);
            //Added by Alan Date:2009-08-04
            parser.setExtName(extName);
            parser.write(file);
            // validate resource-only list
            for (final String t: parser.getNormalProcessingRoleTargets()) {
                if (resourceOnlyList.contains(t)) {
                    resourceOnlyList.remove(t);
                }
            }
        }
        job.setSet(RESOURCE_ONLY_LIST, resourceOnlyList);
        try {
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to store job state: " + e.getMessage(), e);
        }
        return null;
    }

}
