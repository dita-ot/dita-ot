/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.index;

import static org.dita.dost.util.Constants.*;

/**
 * This class represent the topicref element in ditamap.
 * 
 * @version 1.0 2005-06-03
 * 
 * @author Wu, Zhi Qiang
 */
public final class TopicrefElement {
    /** The href attribute of the topicref element. */
    private String href = null;

    /** The format attribute of the topicref element. */
    private String format = null;
    /**the navtitle attribute of topicref element.*/
    private String navtitle = null;

    /**
     * Default constructor.
     */
    public TopicrefElement() {
    }

    /**
     * Get the format attribute.
     * 
     * @return Returns the format.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Set the format attribute with the given value.
     * 
     * @param formatValue The format to set.
     */
    public void setFormat(final String formatValue) {
        this.format = formatValue;
    }

    /**
     * Get href attribute.
     * 
     * @return Returns the href.
     */
    public String getHref() {
        return href;
    }

    /**
     * Set href attribute with the given value.
     * 
     * @param hrefValue The href to set.
     */
    public void setHref(final String hrefValue) {
        this.href = hrefValue;
    }

    /**
     * To see if need to extract indexterm under this topic element.
     * 
     * @return true if need, false or else
     */
    @Deprecated
    public boolean needExtractTerm() {
        return (href != null && format != null && !ATTRIBUTE_FORMAT_VALUE_DITA
                .equals(format));
    }

    /**
     * Set navtitle attribute with the given value.
     * @param aNavtitle The navtitle to set.
     */
    public void setNavTitle (final String aNavtitle){
        navtitle = aNavtitle;
    }

    /**
     * Get navtitle attribute.
     * 
     * @return Returns the navtitle.
     */
    public String getNavTitle(){
        return navtitle;
    }


}
