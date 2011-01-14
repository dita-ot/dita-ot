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
 * InsertCatalogActionRelative inserts the children of the root element of an XML document
 * into a plugin extension point, rewriting relative file references so that they
 * are still correct in their new location.
 * 
 * Attributes affected: (public|system|uri)/@uri
 *   (nextCatalog|delegateURI|delegateSystem|delegatePublic)/@catalog
 *   (rewriteSystem|rewriteURI)/@rewritePrefix
 * To do: Handle xml:base.
 * 
 * @author Deborah Pickett
 *
 */
public class InsertCatalogActionRelative extends InsertActionRelative implements
		IAction {

	/**
	 * Constructor.
	 */
	public InsertCatalogActionRelative() {
		super();
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes attributes) throws SAXException {
		if(elemLevel != 0){
			final int attLen = attributes.getLength();
			retBuf.append(Constants.LINE_SEPARATOR);
			retBuf.append("<").append(qName);
			for (int i = 0; i < attLen; i++){
				if ((("public".equals(localName) ||
						"system".equals(localName) ||
						"uri".equals(localName)) && "uri".equals(attributes.getQName(i)) ||
				    ("nextCatalog".equals(localName) ||
						"delegateURI".equals(localName) ||
						"delegateSystem".equals(localName) ||
						"delegatePublic".equals(localName)) && "catalog".equals(attributes.getQName(i)) ||
				    ("rewriteSystem".equals(localName) ||
						"rewriteURI".equals(localName)) && "rewritePrefix".equals(attributes.getQName(i)))
						&& attributes.getValue(i).indexOf(Constants.COLON) == -1) {
					// Rewrite URI to be local to its final resting place.
				    final File targetFile = new File(
				    		new File(currentFile).getParentFile(),
				    		attributes.getValue(i));
				    final String pastedURI = FileUtils.getRelativePathFromMap(
				    		paramTable.get(FileGenerator.PARAM_TEMPLATE),
				    		targetFile.toString());
					retBuf.append(" ").append(attributes.getQName(i)).append("=\"");
					retBuf.append(StringUtils.escapeXML(pastedURI)).append("\"");
				}
				else {
					retBuf.append(" ").append(attributes.getQName(i)).append("=\"");
					retBuf.append(StringUtils.escapeXML(attributes.getValue(i))).append("\"");
				}
			}
			//Added by William on 2010-03-23 for bug:2974667 start
			if(("public".equals(localName) ||
					"system".equals(localName) ||
					"uri".equals(localName))){
				retBuf.append("/>");
			}
			//Added by William on 2010-03-23 for bug:2974667 end
			else{
				retBuf.append(">");
			}
			
		}
		elemLevel ++;
	}
}
