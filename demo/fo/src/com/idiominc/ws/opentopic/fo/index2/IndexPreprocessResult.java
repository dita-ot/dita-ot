package com.idiominc.ws.opentopic.fo.index2;

import org.w3c.dom.Document;

/**
 * User: Ivan Luzyanin
 * Date: 30.06.2005
 * Time: 11:27:24
 */
public class IndexPreprocessResult {
	private Document document;
	private IndexEntry[] indexEntries;


	public IndexPreprocessResult(Document theDocument, IndexEntry[] theIndexEntries) {
		document = theDocument;
		indexEntries = theIndexEntries;
	}


	public Document getDocument() {
		return document;
	}


	public IndexEntry[] getIndexEntries() {
		return indexEntries;
	}
}
