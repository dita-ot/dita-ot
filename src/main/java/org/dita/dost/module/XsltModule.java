package org.dita.dost.module;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * XSLT processing module.
 * 
 * <p>The module matches Ant's XSLT task with the following exceptions:</p>
 * <ul>
 *   <li>If source and destination directories are same, transformation results are saved to a temporary file
 *   and the original source file is replaced after a successful transformation.</li>
 *   <li>If no {@code extension} attribute is set, the target file extension is the same as the source file extension.</li>
 * </ul>
 *  
 */
public final class XsltModule extends AbstractPipelineModuleImpl {

    private Templates templates;
    private final Map<String, String> params = new HashMap<>();
    private File style;
    private File in;
    private File out;
    private File destDir;
    private File baseDir;
    private Collection<File> includes;
    private String filenameparameter;
    private String filedirparameter;
    private boolean reloadstylesheet;
    private XMLCatalog xmlcatalog;
	private FileNameMapper mapper;
    
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
    	logger.info("Transforming into " + destDir.getAbsolutePath());
        final TransformerFactory tf = TransformerFactory.newInstance();
        tf.setURIResolver(xmlcatalog);
        try {
            templates = tf.newTemplates(new StreamSource(style));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to compile stylesheet '" + style.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        XMLReader parser;
		try {
			parser = XMLUtils.getXMLReader();
		} catch (final SAXException e) {
			throw new RuntimeException("Failed to create XML reader: " + e.getMessage(), e);
		}
        parser.setEntityResolver(xmlcatalog);
        
    	Transformer t = null;
        for (final File include: includes) {
        	if (reloadstylesheet || t == null) {
                logger.info("Loading stylesheet " + style.getAbsolutePath());
	            try {
	                t = templates.newTransformer();
                    if (Configuration.DEBUG) {
                        t.setURIResolver(new XMLUtils.DebugURIResolver(xmlcatalog));
                    }
	            } catch (final TransformerConfigurationException e) {
	                throw new DITAOTException("Failed to create Transformer: " + e.getMessage(), e);
	            }
        	}
            final File in = new File(baseDir, include.getPath());
            File out = new File(destDir, include.getPath());
            if (mapper != null) {
            	final String[] outs = mapper.mapFileName(out.getAbsolutePath());
            	if (outs == null) {
            		continue;
            	}
            	if (outs.length > 1) {
            		throw new RuntimeException("XSLT module only support one to one output mapping");
            	}
            	out = new File(outs[0]);
            }
            final boolean same = in.getAbsolutePath().equals(out.getAbsolutePath());
            final File tmp = same ? new File(out.getAbsolutePath() + ".tmp" + Long.toString(System.currentTimeMillis())) : out; 
            for (Map.Entry<String, String> e: params.entrySet()) {
                logger.debug("Set parameter " + e.getKey() + " to '" + e.getValue() + "'");
                t.setParameter(e.getKey(), e.getValue());
            }
            if (filenameparameter != null) {
                logger.debug("Set parameter " + filenameparameter + " to '" + include.getName() + "'");
                t.setParameter(filenameparameter, include.getName());
            }
            if (filedirparameter != null) {
            	final String v = include.getParent() != null ? include.getParent() : ".";
                logger.debug("Set parameter " + filedirparameter + " to '" + v + "'");
                t.setParameter(filedirparameter, v);
            }
            if (same) {
	            logger.info("Processing " + in.getAbsolutePath());
	            logger.debug("Processing " + in.getAbsolutePath() + " to " + tmp.getAbsolutePath());
            } else {
            	logger.info("Processing " + in.getAbsolutePath() + " to " + tmp.getAbsolutePath());
            }
            final Source source = new SAXSource(parser, new InputSource(in.toURI().toString()));
            try {
            	if (!tmp.getParentFile().exists() && !tmp.getParentFile().mkdirs()) {
                	throw new IOException("Failed to create directory " + tmp.getParent());
                }
                t.transform(source, new StreamResult(tmp));
                if (same) {
                    logger.debug("Moving " + tmp.getAbsolutePath() + " to " + out.getAbsolutePath());
                    if (!out.delete()) {
                        throw new IOException("Failed to to delete input file " + out.getAbsolutePath());
                    }
                    if (!tmp.renameTo(out)) {
                        throw new IOException("Failed to to replace input file " + out.getAbsolutePath());
                    }
                }
            } catch (final Exception e) {
                logger.error("Failed to transform document: " + e.getMessage(), e);
                logger.debug("Remove " + tmp.getAbsolutePath());
                FileUtils.delete(tmp);
            } 
        }
        return null;
    }
    
    public void setStyle(final File style) {
    	this.style = style;
    }

    public void setParam(final String key, final String value) {
        params.put(key, value);
    }

    public void setIncludes(final Collection<File> includes) {
        this.includes = includes;
    }

    public void setDestinationDir(final File destDir) {
        this.destDir = destDir;
    }

    public void setSorceDir(final File baseDir) {
        this.baseDir = baseDir;
    }

    public void setFilenameParam(final String filenameparameter) {
        this.filenameparameter = filenameparameter;
    }
    
    public void setFiledirParam(final String filedirparameter) {
        this.filedirparameter = filedirparameter;
    }
    
    public void setReloadstylesheet(final boolean reloadstylesheet) {
    	this.reloadstylesheet = reloadstylesheet;
    }

	public void setSource(final File in) {
		this.in = in;
	}

	public void setResult(final File out) {
		this.out = out;
	}

	public void setXMLCatalog(final XMLCatalog xmlcatalog) {
		this.xmlcatalog = xmlcatalog;
	}

	public void setMapper(final FileNameMapper mapper) {
		this.mapper = mapper;
	}
    
}
