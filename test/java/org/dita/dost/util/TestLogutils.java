/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.dita.dost.util.LogUtils;
import org.junit.Test;

public class TestLogutils {

    @Test
    public void testclear()
    {
        LogUtils.clear();
        assertEquals(0,LogUtils.getNumOfFatals());
        assertEquals(0,LogUtils.getNumOfErrors());
        assertEquals(0,LogUtils.getNumOfWarnings());
        assertEquals(0,LogUtils.getNumOfInfo());
    }

    @Test
    public void testhavefatalorerror()
    {
        assertFalse(LogUtils.haveFatalOrError());
        LogUtils.increaseNumOfErrors();
        assertTrue(LogUtils.haveFatalOrError());
        LogUtils.clear();
        LogUtils.increaseNumOfFatals();
        assertTrue(LogUtils.haveFatalOrError());
        LogUtils.clear();
        LogUtils.increaseNumOfErrors();
        LogUtils.increaseNumOfFatals();
        assertTrue(LogUtils.haveFatalOrError());
        LogUtils.clear();
    }

    @Test
    public void testincreaseNumOfExceptionByType()
    {
        LogUtils.increaseNumOfExceptionByType(null);
        assertEquals(1,LogUtils.getNumOfErrors());
        LogUtils.increaseNumOfExceptionByType("FATAL");
        assertEquals(1,LogUtils.getNumOfFatals());
        LogUtils.increaseNumOfExceptionByType("ERROR");
        assertEquals(2,LogUtils.getNumOfErrors());
        LogUtils.increaseNumOfExceptionByType("WARN");
        assertEquals(1,LogUtils.getNumOfWarnings());
        LogUtils.increaseNumOfExceptionByType("INFO");
        assertEquals(1,LogUtils.getNumOfInfo());
    }

}
