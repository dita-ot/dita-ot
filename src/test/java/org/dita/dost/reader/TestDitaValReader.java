/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;


public class TestDitaValReader {

    private final File resourceDir = TestUtils.getResourceDir(TestDitaValReader.class);

    @Test
    public void testRead() throws DITAOTException{
        final File ditavalFile = new File(resourceDir, "src" + File.separator + "DITAVAL_1.ditaval");
        DitaValReader reader = new DitaValReader();
        reader.read(ditavalFile.getAbsoluteFile());
        final Map<FilterKey, Action> map = reader.getFilterMap();
        assertEquals(Action.INCLUDE, map.get(new FilterKey("audience", "Cindy")));
        assertEquals(Action.FLAG, map.get(new FilterKey("produt", "p1")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("product", "ABase_ph")));
        assertEquals(Action.INCLUDE, map.get(new FilterKey("product", "AExtra_ph")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("product", "Another_ph")));
        assertEquals(Action.FLAG, map.get(new FilterKey("platform", "Windows")));
        assertEquals(Action.FLAG, map.get(new FilterKey("platform", "Linux")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("keyword", "key1")));
        assertEquals(Action.FLAG, map.get(new FilterKey("keyword", "key2")));
        assertEquals(Action.INCLUDE, map.get(new FilterKey("keyword", "key3")));
    }

}
