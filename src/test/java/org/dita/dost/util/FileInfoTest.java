/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.dita.dost.util.Job.FileInfo;
import static org.dita.dost.util.Job.FileInfo.Builder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public final class FileInfoTest {

    @Test
    public void testURIConstructor() throws URISyntaxException {
        final FileInfo f = new FileInfo(new URI("foo" + URI_SEPARATOR + "bar"));
        assertEquals(new File("foo" + File.separator + "bar"), f.file);
        assertEquals(new URI("foo" + URI_SEPARATOR + "bar"), f.uri);
    }
    
    @Test
    public void testFileBuilder() throws URISyntaxException {
        final FileInfo f = new Builder().file(new File("foo" + File.separator + "bar")).build();
        assertEquals(new File("foo" + File.separator + "bar"), f.file);
        assertEquals(new URI("foo" + URI_SEPARATOR + "bar"), f.uri);
    }

    @Test
    public void testURIBuilder() throws URISyntaxException {
        final FileInfo f = new Builder().uri(new URI("foo" + URI_SEPARATOR + "bar")).build();
        assertEquals(new File("foo" + File.separator + "bar"), f.file);
        assertEquals(new URI("foo" + URI_SEPARATOR + "bar"), f.uri);
    }
    
}
