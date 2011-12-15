/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

/**
 * Version Utility class, providing method of getting version
 * information to AntVersion.java
 * @author william
 * 
 */
public final class VersionUtil {

    private static final String otversion = "@@OTVERSION@@";


    /**
     * @return the milestone
     * @deprecated use {@link #getOtversion()} instead. To be remove in future releases
     */
    @Deprecated
    public String getMilestone() {
        return "";
    }

    /**
     * @return the otversion
     */
    public String getOtversion() {
        return "DITA Open Toolkit " + otversion;
    }

    public VersionUtil() {

    }

}
