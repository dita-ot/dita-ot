/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dita.dost.util.StringUtils;

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
    public String getResult() {
        final String separator = paramTable.containsKey("separator") ? paramTable.get("separator") : "|";
        final List<String> v = new ArrayList<String>(valueSet);
        Collections.sort(v);
        final StringBuilder retBuf = new StringBuilder();
        for (final Iterator<String> i = v.iterator(); i.hasNext(); ) {
            retBuf.append(StringUtils.escapeXML(i.next()));
            if (i.hasNext()) {
                retBuf.append(separator);
            }
        }
        return retBuf.toString();
    }

}
