/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */
package org.dita.dost.platform;

import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.dita.dost.util.FileUtils;
import java.io.File;

/**
 * InsertAntActionRelative inserts the children of the root element of an XML document
 * into a plugin extension point, rewriting relative file references so that they
 * are still correct in their new location.
 *
 * Attributes affected: import/@file
 * 
 * @author Deborah Pickett
 *
 */
public class InsertAntActionRelative extends InsertActionRelative implements
		IAction {

	/**
	 * Constructor.
	 */
	public InsertAntActionRelative() {
		super();
	}
	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(elemLevel != 0){
			int attLen = attributes.getLength();
			retBuf.append(Constants.LINE_SEPARATOR);
			retBuf.append("<"+qName);
			for (int i = 0; i < attLen; i++){
				if ("import".equals(localName) && "file".equals(attributes.getQName(i))
						&& !FileUtils.isAbsolutePath(attributes.getValue(i))) {
					// Rewrite file path to be local to its final resting place.
				    File targetFile = new File(
				    		new File(currentFile).getParentFile(),
				    		attributes.getValue(i));
				    String pastedURI = FileUtils.getRelativePathFromMap(
				    		(String) paramTable.get("template"),
				    		targetFile.toString());
					retBuf.append(" ").append(attributes.getQName(i)).append("=\"");
					retBuf.append(StringUtils.escapeXML(pastedURI)).append("\"");
				}
				else {
					retBuf.append(" ").append(attributes.getQName(i)).append("=\"");
					retBuf.append(StringUtils.escapeXML(attributes.getValue(i))).append("\"");
				}
			}
			retBuf.append(">");
		}
		elemLevel ++;
	}
}
