/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dita.dost.TestUtils;
import org.dita.dost.module.Content;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.junit.Test;

public class TestKeyrefReader {

    private static final File resourceDir = new File("test-stub", TestKeyrefReader.class.getSimpleName());
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    @Test
    public void testKeyrefReader() throws IOException
    {
        final String path=System.getProperty("user.dir");
        DitaURIResolverFactory.setPath(path);
        final File filename = new File(srcDir, "keyrefreader.xml");

        final Set <String> set=new HashSet<String> ();
        set.add("blatview");
        set.add("blatfeference");
        set.add("blatintro");
        final KeyrefReader keyrefreader = new KeyrefReader();
        keyrefreader.setKeys(set);
        keyrefreader.read(filename.getAbsolutePath());
        final Content content = keyrefreader.getContent();
        //keyrefreader.getContent();
        final String string1 = content.getValue().toString();

        assertEquals(TestUtils.readFileToString(new File(expDir, "keyrefreaderCompare.xml")),
                string1);

    }



}
