/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class TestDitaValReader {

    private final File resourceDir = TestUtils.getResourceDir(TestDitaValReader.class);

    @Test
    public void testRead() throws DITAOTException{
        final File ditavalFile = new File(resourceDir, "src" + File.separator + "DITAVAL_1.ditaval");
        DitaValReader reader = new DitaValReader();
        reader.read(ditavalFile.toURI());
        final Map<FilterKey, Action> map = reader.getFilterMap();
        assertTrue(map.get(new FilterKey("audience", "Cindy")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey("produt", "p1")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey("product", "ABase_ph")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey("product", "AExtra_ph")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey("product", "Another_ph")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey("platform", "Windows")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey("platform", "Linux")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey("keyword", "key1")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey("keyword", "key2")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey("keyword", "key3")) instanceof FilterUtils.Include);
    }

}
