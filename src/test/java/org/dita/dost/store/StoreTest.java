/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import static org.junit.Assert.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.dita.dost.TestUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {

    private final File resourceDir = TestUtils.getResourceDir(StoreTest.class);
    private final File srcDir = new File(resourceDir, "src").getAbsoluteFile();
    private final URI srcDirUri = srcDir.toURI();
    
    private Store store;
    
    @Before
    public void setup() {
        store = new StreamStore(srcDir, new XMLUtils());
    }
    
    @Test
    public void testGetPath() {
        // fail("Not yet implemented");
    }

//    @Test
//    public void testGetFile() {
//        assertEquals(new File(srcDir, "root.xml"), store.getFile(new File("root.xml")));
//        assertEquals(new File(srcDir, "folder" + File.separator + "leaf.xml"), store.getFile(new File("folder" + File.separator + "leaf.xml")));
//        assertEquals(new File(srcDir, "root.xml"), store.getFile(new File(srcDir, "root.xml")));
//    }

    @Test
    public void testGetUri() {
        assertEquals(srcDirUri.resolve("root.xml"), store.getUri(toURI("root.xml")));
        assertEquals(srcDirUri.resolve("folder/leaf.xml"), store.getUri(toURI("folder/leaf.xml")));
        assertEquals(srcDirUri.resolve("root.xml"), store.getUri(srcDirUri.resolve("root.xml")));

    }

    @Test
    public void testResolve() throws TransformerException {
        final Source s = store.resolve("root.xml", srcDirUri.toString());
        assertEquals(srcDirUri.resolve("root.xml"), toURI(s.getSystemId()));
        
//        assertNull(store.resolve("root.xml", "file:/dummy/"));
    }

    @Test
    public void testGetSourceFile() {
        // fail("Not yet implemented");
    }

    @Test
    public void testGetSourceURI() {
        // fail("Not yet implemented");
    }

    @Test
    public void testGetResultFile() {
        // fail("Not yet implemented");
    }

    @Test
    public void testGetResultURI() {
        // fail("Not yet implemented");
    }

}
