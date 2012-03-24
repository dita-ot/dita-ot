/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;

import org.dita.dost.TestUtils;
import org.dita.dost.module.Content;
import org.dita.dost.reader.ConrefPushReader;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestConrefPushReader {

    private static final File resourceDir = new File(TestUtils.testStub, TestConrefPushReader.class.getSimpleName());
    private static final File srcDir = new File(resourceDir, "src");
    //private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setUp() throws IOException{
        tempDir = TestUtils.createTempDir(TestConrefPushReader.class);
    }

    @Test
    public void testRead(){
        /*
         * the part of content of conrefpush_stup.xml is
         *  <steps>
         * 	 <step conaction="pushbefore"><cmd>before</cmd></step>
         *   <step conref="conrefpush_stub2.xml#X/A" conaction="mark"/>
         *   <step conref="conrefpush_stub2.xml#X/B" conaction="mark"/>
         *	 <step conaction="pushafter"><cmd>after</cmd></step>
         *	 <step conref="conrefpush_stub2.xml#X/C" conaction="pushreplace"><cmd>replace</cmd></step>
         *	</steps>
         */
        final File filename = new File(srcDir, "conrefpush_stub.xml");
        final ConrefPushReader pushReader = new ConrefPushReader();
        pushReader.read(filename.getAbsolutePath());
        final Content content = pushReader.getContent();
        final Set<Map.Entry<String, Hashtable<String,String>>> pushSet =(Set<Map.Entry<String, Hashtable<String,String>>>) content.getCollection();
        final Iterator<Map.Entry<String, Hashtable<String,String>>> it= pushSet.iterator();
        if (it.hasNext()){
            // pushSet has only one entry, so there is no need to iterate it.
            final Hashtable<String, String> table = it.next().getValue();
            assertTrue(table.containsKey("#X/A|pushbefore"));
            assertEquals(table.get("#X/A|pushbefore"), "<step class=\"- topic/li task/step \"><cmd class=\"- topic/ph task/cmd \">before</cmd></step>");
            assertTrue(table.containsKey("#X/B|pushafter"));
            assertEquals(table.get("#X/B|pushafter"), "<step class=\"- topic/li task/step \"><cmd class=\"- topic/ph task/cmd \">after</cmd></step>");
            assertTrue(table.containsKey("#X/C|pushreplace"));
            assertEquals( "<step class=\"- topic/li task/step \" id=\"C\"><cmd class=\"- topic/ph task/cmd \">replace</cmd></step>",table.get("#X/C|pushreplace"));
        }
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
