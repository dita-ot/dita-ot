package org.dita.dost.writer;

import static java.util.Arrays.*;
import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@RunWith(Parameterized.class)
public class GenListModuleFilterTest {
    
    private File tempDir;
    private static final File resourceDir = TestUtils.getResourceDir(GenListModuleFilterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    private GenListModuleFilter listFilter;
    private List<FileInfo> expFileInfos;
    
    public GenListModuleFilterTest(final List<FileInfo> expFileInfos) {
        this.expFileInfos = expFileInfos;
    }
    
    @Parameters
    public static Collection<Object[]> getFiles() {
        final List<List<FileInfo>> files = new ArrayList<List<FileInfo>>();
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("test.ditamap")).format("ditamap").hasLink(false).build(),
                new Builder().file(new File("plain.dita")).format("dita").isTarget(true).isNonConrefTarget(true).build(),
                new Builder().file(new File("keyword-keyref.dita")).format("dita").isTarget(true).isNonConrefTarget(true).build(),
                new Builder().file(new File("topic.dita")).format("dita").isTarget(true).isNonConrefTarget(true).build(),
                new Builder().file(new File("plain-copy-to.dita")).isNonConrefTarget(true).build(),
                new Builder().file(new File("to-content.dita")).format("dita").isTarget(true).isSkipChunk(true).build(),
                new Builder().file(new File("copy-to-to-content.dita")).format("dita").isTarget(true).isSkipChunk(true).build(),
                new Builder().file(new File("to-content-copy-to.dita")).isSkipChunk(true).build()
                }));
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("topic.dita")).format("dita").isTarget(true).isNonConrefTarget(true).hasLink(true).build(),
                new Builder().file(new File("local.dita")).format("dita").isTarget(true).isNonConrefTarget(true).build(),
//                new Builder().file(new File("peer.dita")).format("dita").isTarget(true).isNonConrefTarget(true).build(),
//                new Builder().file(new File("external.dita")).format("dita").isTarget(true).isNonConrefTarget(true).build(),
//                new Builder().file(new File("local.html")).format("html").isTarget(true).isNonConrefTarget(true).build(),
//                new Builder().file(new File("peer.html")).format("html").isTarget(true).isNonConrefTarget(true).build(),
//                new Builder().file(new File("external.html")).format("html").isTarget(true).isNonConrefTarget(true).build()
                }));
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("plain.dita")).format("dita").build()
                }));
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("keyword-keyref.dita")).format("dita").hasKeyref(true).build()
                }));
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("coderef.dita")).format("dita").hasCoderef(true).build(),
                new Builder().file(new File("code.txt")).isSubtarget(true).format("code").build()
                }));
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("conref.dita")).format("dita").hasConref(true).build(),
                new Builder().file(new File("conref-library.dita")).isConrefTarget(true).build()
                }));
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("conaction.dita")).format("dita").isConrefPush(true).hasConref(true).build(),
                new Builder().file(new File("conction-target.dita")).isConrefTarget(true).build()
                }));
        files.add(asList(new FileInfo[] {
                new Builder().file(new File("resource-only.ditamap")).format("ditamap").hasLink(false).build(),
                new Builder().file(new File("normal.dita")).format("dita").isTarget(true).isNonConrefTarget(true).build(),
                new Builder().file(new File("resource-only.dita")).format("dita").isTarget(true).isNonConrefTarget(true).isResourceOnly(true).build(),
                }));
        
        final List<Object[]> params = new ArrayList<Object[]>(files.size());
        for (final List<FileInfo> f : files) {
            params.add(new Object[] { f });
        }
        return params;
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }
    
    @Before
    public void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(GenListModuleFilterTest.class);
//        listFilter = new GenListModuleFilter();
//        listFilter.setLogger(new TestUtils.TestLogger());
//        listFilter.setInputFile(new File(srcDir, "test.ditamap").toURI());
//        listFilter.setInputDir(srcDir.toURI());
//        final OutputUtils outputUtils = new OutputUtils();
//        outputUtils.setInputMapPathName(new File(srcDir, "test.ditamap"));
//        listFilter.setOutputUtils(outputUtils);
//        listFilter.setTranstype("xhtml");
    }

    @Test
    public void testFilter() throws Exception {
        final File srcFile = new File(srcDir, expFileInfos.get(0).file.getPath());
        
        listFilter = new GenListModuleFilter();
        listFilter.setLogger(new TestUtils.TestLogger());
        listFilter.setInputFile(srcFile.toURI());
        listFilter.setInputDir(srcDir.toURI());
        final OutputUtils outputUtils = new OutputUtils();
        outputUtils.setInputMapPathName(srcFile);
        listFilter.setOutputUtils(outputUtils);
//        listFilter.setTranstype("xhtml");
        listFilter.setCurrentDir(new URI(""));
        listFilter.setCurrentFile(srcFile.toURI());
        listFilter.setParent(StringUtils.getXMLReader());
        listFilter.setTempDir(tempDir);
        
        final Source source = new SAXSource(listFilter, new InputSource(srcFile.toURI().toASCIIString()));
        final DOMResult result = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(source, result);
        
        
        final Set<File> resourceOnly = listFilter.getResourceOnlySet();
        final List<FileInfo> actFileInfos = listFilter.getFileInfo();        
        assertEquals(expFileInfos.size(), actFileInfos.size());
        for (final FileInfo actFileInfo: actFileInfos) {
            final FileInfo expFileInfo = findFileInfo(actFileInfo.file);
            assertNotNull(expFileInfo);
            assertEquals(actFileInfo.file + ": ", expFileInfo.format, actFileInfo.format);
            assertEquals(actFileInfo.file + ": ", expFileInfo.hasConref, actFileInfo.hasConref);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isChunked, actFileInfo.isChunked);
            assertEquals(actFileInfo.file + ": ", expFileInfo.hasLink, actFileInfo.hasLink);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isResourceOnly, resourceOnly.contains(actFileInfo.file));
            assertEquals(actFileInfo.file + ": ", expFileInfo.isTarget, actFileInfo.isTarget);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isConrefTarget, actFileInfo.isConrefTarget);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isNonConrefTarget, actFileInfo.isNonConrefTarget);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isConrefPush, actFileInfo.isConrefPush);
            assertEquals(actFileInfo.file + ": ", expFileInfo.hasKeyref, actFileInfo.hasKeyref);
            assertEquals(actFileInfo.file + ": ", expFileInfo.hasCoderef, actFileInfo.hasCoderef);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isSubjectScheme, actFileInfo.isSubjectScheme);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isSkipChunk, actFileInfo.isSkipChunk);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isSubtarget, actFileInfo.isSubtarget);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isFlagImage, actFileInfo.isFlagImage);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isChunkedDitaMap, actFileInfo.isChunkedDitaMap);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isOutDita, actFileInfo.isOutDita);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isCopyToSource, actFileInfo.isCopyToSource);
            assertEquals(actFileInfo.file + ": ", expFileInfo.isActive, actFileInfo.isActive);
        }
        
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document expDoc = documentBuilder.parse(new File(expDir, expFileInfos.get(0).file.getPath()));
        
        assertXMLEqual(expDoc, (Document) result.getNode());
    }

    private FileInfo findFileInfo(final File file) {
        for (final FileInfo f: expFileInfos) {
            if (f.file.equals(file)) {
                return f;
            }
        }
        return null;
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }
    
}
