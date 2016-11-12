/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import java.util.Locale;
import org.dita.dost.util.DITAOTCollator;
import org.junit.Test;
import static org.junit.Assert.assertNotSame;
public class TestDITAOTCollator {
    @Test
    public void testgetinstance()
    {

        assertNotSame(DITAOTCollator.getInstance(Locale.US),DITAOTCollator.getInstance(Locale.UK));
    }

}
