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

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class TestDitaValReader {

    private static final QName PLATFORM = QName.valueOf("platform");
    private static final QName PRODUCT = QName.valueOf("product");
    private static final QName AUDIENCE = QName.valueOf("audience");
    private static final QName PROPS = QName.valueOf("props");
    private static final QName KEYWORD = QName.valueOf("keyword");

    private final File resourceDir = TestUtils.getResourceDir(TestDitaValReader.class);

    @Test
    public void testRead() throws DITAOTException{
        final File ditavalFile = new File(resourceDir, "src" + File.separator + "DITAVAL_1.ditaval");
        DitaValReader reader = new DitaValReader();
        reader.read(ditavalFile.toURI());
        final Map<FilterKey, Action> map = reader.getFilterMap();
        assertTrue(map.get(new FilterKey(AUDIENCE, "Cindy")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey(QName.valueOf("produt"), "p1")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(PRODUCT, "ABase_ph")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey(PRODUCT, "AExtra_ph")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey(PRODUCT, "Another_ph")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey(PLATFORM, "Windows")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(PLATFORM, "Linux")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(KEYWORD, "key1")) instanceof FilterUtils.Exclude);
        assertTrue(map.get(new FilterKey(KEYWORD, "key2")) instanceof FilterUtils.Flag);
        assertTrue(map.get(new FilterKey(KEYWORD, "key3")) instanceof FilterUtils.Include);
        assertTrue(map.get(new FilterKey(PROPS, null)) instanceof FilterUtils.Include);
    }

}
