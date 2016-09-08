/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;

import org.junit.Before;

import org.dita.dost.util.LogUtils;
import org.junit.Test;

public class TestLogutils {

    @Before
    public void setUp() {
        LogUtils.clear();
    }
    
    @Test
    public void testclear()
    {
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

    @After
    public void tearDown() {
        LogUtils.clear();
    }
}
