/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Job.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.xml.sax.XMLFilter;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.ConkeyrefFilter;
import org.dita.dost.writer.KeyrefPaser;
/**
 * Keyref Module.
 *
 */
final class KeyrefModule extends AbstractPipelineModuleImpl {

    /**
     * Entry point of KeyrefModule.
     * 
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(new Filter() {
            @Override
            public boolean accept(final FileInfo f) {
                //Conref Module will change file's content, it is possible that tags with @keyref are copied in
                //while keyreflist is hard update with xslt.
                return f.hasKeyref || f.hasConref;
            }
        });
        if (!fis.isEmpty()) {
            // TODO: If map merge is done before key processing, this needs to be rewritten to just read the single map and take submap wrappers into consideration.
            // maps of keyname and target
            final Map<String, URI> keymap = new HashMap<String, URI>();
            // store the key name defined in a map(keyed by ditamap file)
            final Hashtable<URI, Set<String>> maps = new Hashtable<URI, Set<String>>();
            final Collection<KeyDef> keydefs = KeyDef.readKeydef(new File(job.tempDir, KEYDEF_LIST_FILE));
            for (final KeyDef keyDef: keydefs) {
                keymap.put(keyDef.keys, keyDef.href);
                // map file which define the keys
                final URI map = keyDef.source;
                // put the keyname into corresponding map which defines it.
                //a map file can define many keys
                if (maps.containsKey(map)) {
                    maps.get(map).add(keyDef.keys);
                } else {
                    final Set<String> set = new HashSet<String>();
                    set.add(keyDef.keys);
                    maps.put(map, set);
                }
            }
            
            final KeyrefReader reader = new KeyrefReader();
            reader.setLogger(logger);
            for(final URI mapFile: maps.keySet()){
                logger.info("Reading " + job.tempDir.toURI().resolve(mapFile).toString());
                reader.setKeys(maps.get(mapFile));
                reader.read(job.tempDir.toURI().resolve(mapFile));
            }
            final Map<String, Element> keyDefinition = reader.getKeyDefinition();
            
            final Set<File> normalProcessingRole = new HashSet<File>();
            for (final FileInfo f: fis) {
                final File file = f.file;
                logger.info("Processing " + new File(job.tempDir, file.getPath()).getAbsolutePath());
                
                final List<XMLFilter> filters = new ArrayList<XMLFilter>();
                
                final ConkeyrefFilter conkeyrefFilter = new ConkeyrefFilter();
                conkeyrefFilter.setLogger(logger);
                conkeyrefFilter.setKeyDefinitions(keydefs);
                conkeyrefFilter.setTempDir(job.tempDir);
                conkeyrefFilter.setCurrentFile(file);
                filters.add(conkeyrefFilter);
                
                final KeyrefPaser parser = new KeyrefPaser();
                parser.setLogger(logger);
                parser.setKeyDefinition(keyDefinition);
                parser.setTempDir(job.tempDir);
                parser.setCurrentFile(file);
                parser.setKeyMap(keymap);
                filters.add(parser);
                
                XMLUtils.transform(new File(job.tempDir, file.getPath()), filters);
                
                // validate resource-only list
                normalProcessingRole.addAll(parser.getNormalProcessingRoleTargets());
            }
            for (final File file: normalProcessingRole) {
                final FileInfo f = job.getFileInfo(file);
                if (f != null) {
                    f.isResourceOnly = false;
                    job.add(f);
                }
            }
    
            try {
                job.write();
            } catch (final IOException e) {
                throw new DITAOTException("Failed to store job state: " + e.getMessage(), e);
            }
        }
        return null;
    }

}
