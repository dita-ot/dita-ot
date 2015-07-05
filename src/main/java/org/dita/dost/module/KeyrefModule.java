/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.URLUtils.*;

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

import org.dita.dost.util.DelayConrefUtils;
import org.w3c.dom.Element;
import org.xml.sax.XMLFilter;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.KeyrefReader;
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

    /** Delayed conref utils. */
    private DelayConrefUtils delayConrefUtils;
    private String transtype;

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
            final Map<String, KeyDef> keymap = new HashMap<String, KeyDef>();
            final Collection<KeyDef> keydefs = KeyDef.readKeydef(new File(job.tempDir, KEYDEF_LIST_FILE));
            for (final KeyDef keyDef: keydefs) {
                keymap.put(keyDef.keys, keyDef);
            }
            
            final KeyrefReader reader = new KeyrefReader();
            reader.setLogger(logger);
            final URI mapFile = job.getInputMap();
            logger.info("Reading " + job.tempDir.toURI().resolve(mapFile).toString());
            reader.read(job.tempDir.toURI().resolve(mapFile));

            final Map<String, Element> keyDefinition = reader.getKeyDefinition();
            transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
            delayConrefUtils = transtype.equals(INDEX_TYPE_ECLIPSEHELP) ? new DelayConrefUtils() : null;
            
            final Set<URI> normalProcessingRole = new HashSet<URI>();
            for (final FileInfo f: fis) {
                final File file = f.file;
                logger.info("Processing " + new File(job.tempDir, file.getPath()).getAbsolutePath());
                
                final List<XMLFilter> filters = new ArrayList<XMLFilter>();
                
                final ConkeyrefFilter conkeyrefFilter = new ConkeyrefFilter();
                conkeyrefFilter.setLogger(logger);
                conkeyrefFilter.setJob(job);
                conkeyrefFilter.setKeyDefinitions(keymap);
                conkeyrefFilter.setCurrentFile(file);
                conkeyrefFilter.setDelayConrefUtils(delayConrefUtils);
                filters.add(conkeyrefFilter);
                
                final KeyrefPaser parser = new KeyrefPaser();
                parser.setLogger(logger);
                parser.setJob(job);
                parser.setKeyDefinition(keyDefinition);
                parser.setCurrentFile(file);
                parser.setKeyMap(keymap);
                filters.add(parser);

                try {
                    XMLUtils.transform(new File(job.tempDir, file.getPath()), filters);
                    // validate resource-only list
                    normalProcessingRole.addAll(parser.getNormalProcessingRoleTargets());
                } catch (final DITAOTException e) {
                    logger.error("Failed to process key references: " + e.getMessage(), e);
                }
            }
            for (final URI file: normalProcessingRole) {
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
