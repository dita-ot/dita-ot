/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.DITAOTAntLogger;

/**
 * This class is for get the first xml:lang value set in ditamap/topic files
 * 
 * @version 1.0 2010-09-30
 * 
 * @author Zhang Di Hua
 */
public final class CheckLang extends Task {

    private String basedir;

    private String tempdir;

    private String outputdir;

    private String inputmap;

    private String message;

    private DITAOTLogger logger;

    /**
     * Executes the Ant task.
     */
    @Override
    public void execute(){
        logger = new DITAOTAntLogger(getProject());
        logger.logInfo(message);

        new Properties();
        //ensure tempdir is absolute
        if (!new File(tempdir).isAbsolute()) {
            tempdir = new File(basedir, tempdir).getAbsolutePath();
        }
        //ensure outdir is absolute
        if (!new File(outputdir).isAbsolute()) {
            outputdir = new File(basedir, outputdir).getAbsolutePath();
        }
        //ensure inputmap is absolute
        if (!new File(inputmap).isAbsolute()) {
            inputmap = new File(tempdir, inputmap).getAbsolutePath();
        }

        Job job = null;
        try{
            job = new Job(new File(tempdir));
        }catch(final IOException e){
            logger.logError(e.getMessage(), e) ;
        }

        final LangParser parser = new LangParser();

        try {

            final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            final SAXParser saxParser = saxParserFactory.newSAXParser();
            //parse the user input file(usually a map)
            saxParser.parse(inputmap, parser);
            String langCode = parser.getLangCode();
            if(!StringUtils.isEmptyString(langCode)){
                setActiveProjectProperty("htmlhelp.locale", langCode);
            }else{
                final Set<String> topicList = job.getSet(FULL_DITA_TOPIC_LIST);
                //parse topic files
                for(final String topicFileName : topicList){
                    final File topicFile = new File(tempdir, topicFileName);
                    if(topicFile.exists()){
                        saxParser.parse(topicFile, parser);
                        langCode = parser.getLangCode();
                        if(!StringUtils.isEmptyString(langCode)){
                            setActiveProjectProperty("htmlhelp.locale", langCode);
                            break;
                        }
                    }
                }
                //no lang is set
                if(StringUtils.isEmptyString(langCode)){
                    //use default lang code
                    setActiveProjectProperty("htmlhelp.locale", "en-us");
                }
            }



        } catch (final Exception e) {
            /* Since an exception is used to stop parsing when the search
             * is successful, catch the exception.
             */
            if (e.getMessage() != null &&
                    e.getMessage().equals("Search finished")) {
                System.out.println("Lang search finished");
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets property in active ant project with name specified inpropertyName,
     * and value specified in propertyValue parameter
     */
    private void setActiveProjectProperty(final String propertyName, final String propertyValue) {
        final Project activeProject = getProject();
        if (activeProject != null) {
            activeProject.setProperty(propertyName, propertyValue);
        }
    }

    public void setBasedir(final String basedir) {
        this.basedir = basedir;
    }

    public void setTempdir(final String tempdir) {
        this.tempdir = tempdir;
    }

    public void setInputmap(final String inputmap) {
        this.inputmap = inputmap;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setOutputdir(final String outputdir) {
        this.outputdir = outputdir;
    }

}
