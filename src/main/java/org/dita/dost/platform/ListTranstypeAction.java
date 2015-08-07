/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * List transtypes integration action.
 *
 * @since 1.5.4
 * @author Jarno Elovirta
 */
final class ListTranstypeAction extends ImportAction {

    /**
     * Get result.
     * @return result
     */
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        final String separator = paramTable.containsKey("separator") ? paramTable.get("separator") : "|";
        final List<String> v = new ArrayList<>(valueSet);
        Collections.sort(v);
        final StringBuilder retBuf = new StringBuilder();
        for (final Iterator<String> i = v.iterator(); i.hasNext(); ) {
            retBuf.append(i.next());
            if (i.hasNext()) {
                retBuf.append(separator);
            }
        }
        final char[] ret = retBuf.toString().toCharArray();
        buf.characters(ret, 0, ret.length);
    }
    
    @Override
    public String getResult() {
        throw new UnsupportedOperationException();        
    }

}
