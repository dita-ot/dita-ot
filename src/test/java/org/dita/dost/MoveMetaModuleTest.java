/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import com.google.common.collect.ImmutableSet;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.apache.tools.ant.*;
import org.dita.dost.util.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertEquals;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.dita.dost.MoveMetaModuleTest.Transtype.PREPROCESS;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.*;

@RunWith(Parameterized.class)
public class MoveMetaModuleTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"MatadataInheritance_foreign"},
                {"MatadataInheritance_keywords"},
                {"MatadataInheritance_linktext"},
                {"MatadataInheritance_othermeta"},
                {"MatadataInheritance_pemissions"},
                {"MatadataInheritance_pemissions_replace"},
                {"MatadataInheritance_prodinfo"},
                {"MatadataInheritance_publisher"},
                {"MatadataInheritance_resourceid"},
                {"MatadataInheritance_searchtitle"},
                {"MatadataInheritance_shortdesc"},
                {"MatadataInheritance_source"},
                {"MatadataInheritance_source_replace"},
                {"MatadataInheritance_unknown"},
                {"MetadataInheritance_audience"},
                {"MetadataInheritance_author"},
                {"MetadataInheritance_category"},
                {"MetadataInheritance_copyright"},
                {"MetadataInheritance_critdates"},
                {"MetadataInheritance_critdates_replace"},
                {"MetadataInheritance_data"},
                {"MetadataInheritance_dataabout"}
        });
    }

    protected static final Map<String, Pattern> htmlIdPattern = new HashMap<>();
    protected static final Map<String, Pattern> ditaIdPattern = new HashMap<>();
    /**
     * Message codes where duplicates are ignored in message count.
     */
    private static final String[] ignoreDuplicates = new String[]{"DOTJ037W"};
    private static final String TEMP_DIR = "temp_dir";
    private static final String BASEDIR = "basedir";
    private static final String DITA_DIR = "dita_dir";
    private static final String LOG_LEVEL = "log_level";
    private static final String TEST = "test";
    private static final String SRC_DIR = "src";
    private static final String EXP_DIR = "exp";
    private static final Collection<String> canCompare = Arrays.asList("html5", "xhtml", "eclipsehelp", "htmlhelp", "preprocess", "pdf");
    private static final File ditaDir = new File(System.getProperty(DITA_DIR) != null
            ? System.getProperty(DITA_DIR)
            : "src" + File.separator + "main");
    private static final File baseDir = new File(System.getProperty(BASEDIR) != null
            ? System.getProperty(BASEDIR)
            : "src" + File.separator + "test");
    private static final File baseTempDir = new File(System.getProperty(TEMP_DIR) != null
            ? System.getProperty(TEMP_DIR)
            : "build" + File.separator + "tmp" + File.separator + "integrationTest");
    private static DocumentBuilder db;
    private static HtmlDocumentBuilder htmlb;
    private static int level;
    private static final File resourceDir = new File(baseDir, "resources");

    static {
        final String SAXON_ID = "d\\d+e\\d+";
        htmlIdPattern.put("id", Pattern.compile("(.*__)" + SAXON_ID + "|" + SAXON_ID + "(.*)"));
        htmlIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
        htmlIdPattern.put("headers", Pattern.compile(SAXON_ID + "(.*)"));

        ditaIdPattern.put("id", htmlIdPattern.get("id"));
        ditaIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
    }

    final Set<String> ignorable = ImmutableSet.of("keydef.xml", "subrelation.xml", ".job.xml", "stage2.fo");
    private Transtype transtype;
    private Map<String, Object> args = new HashMap<>();
    private int warnCount = 0;
    private int errorCount = 0;
    private List<TestListener.Message> log;
    private File actDir;
    private String testName;

    public MoveMetaModuleTest(String testName) {
        this.testName = testName;
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();
        htmlb = new HtmlDocumentBuilder();
        final String l = System.getProperty(LOG_LEVEL);
        level = l != null ? Integer.parseInt(l) : -2;
    }

    private static boolean isWindows() {
        final String osName = System.getProperty("os.name");
        return osName.startsWith("Windows");
    }

    @Test
    public void testMetadataInheritance() throws Throwable {
        this.transtype = PREPROCESS;
        final File testDir = Paths.get("src", "test", "resources", "MetadataInheritance").toFile();

        final File expDir = Paths.get(testDir.getAbsolutePath(), EXP_DIR, "preprocess", testName).toFile();
        final File actDir = Paths.get(baseTempDir.getAbsolutePath(), testDir.getName(), "temp", "preprocess", testName).toFile();
        List<TestListener.Message> log = null;
        try {
            log = run(testDir, expDir.list(), actDir);
            compare(expDir, actDir);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Throwable e) {
            if (log != null && level >= 0) {
                outputLog(log);
            }
            throw new Throwable("Case " + testDir.getName() + " failed: " + e.getMessage(), e);
        }
    }

    @After
    public void cleanUp() {
        // remove temp & output
    }

    protected MoveMetaModuleTest compare() throws Throwable {
        File exp = Paths.get("src", "test", "resources", "MetadataInheritance", EXP_DIR, transtype.toString()).toFile();
        if (!exp.exists()) {
            exp = Paths.get("src", "test", "resources", "MetadataInheritance", EXP_DIR, transtype.exp).toFile();
        }
        if (!exp.exists()) {
            throw new RuntimeException("Unable to find expected output directory");
        }
        final File act = actDir.toPath().resolve(transtype.toString()).toFile();

        compare(exp, act);
        return this;
    }

    private void outputLog(List<TestListener.Message> log) {
        System.err.println("Log start");
        for (final TestListener.Message m : log) {
            if (m.level <= level) {
                switch (m.level) {
                    case -1:
                        break;
                    case Project.MSG_ERR:
                        System.err.print("ERROR: ");
                        break;
                    case Project.MSG_WARN:
                        System.err.print("WARN:  ");
                        break;
                    case Project.MSG_INFO:
                        System.err.print("INFO:  ");
                        break;
                    case Project.MSG_VERBOSE:
                        System.err.print("VERBO: ");
                        break;
                    case Project.MSG_DEBUG:
                        System.err.print("DEBUG: ");
                        break;
                }
                System.err.println(m.message);
            }
        }
        System.err.println("Log end");
    }

    private int countMessages(final List<TestListener.Message> messages, final int level) {
        int count = 0;
        final Set<String> duplicates = new HashSet<>();
        messages:
        for (final TestListener.Message m : messages) {
            if (m.level == level) {
                for (final String code : ignoreDuplicates) {
                    if (m.message.contains(code)) {
                        if (duplicates.contains(code)) {
                            continue messages;
                        } else {
                            duplicates.add(code);
                        }
                    }
                }
                count++;
            }
        }
        return count;
    }

    /**
     * Run test conversion
     *
     * @param d          test source directory
     * @param transtypes list of transtypes to test
     * @return list of log messages
     * @throws Exception if conversion failed
     */
    private List<TestListener.Message> run(final File d, final String[] transtypes, final File resDir) throws Exception {
        if (transtypes.length == 0) {
            return emptyList();
        }

        final File tempDir = new File(baseTempDir, d.getName() + File.separator + "temp");
        deleteDirectory(resDir);
        deleteDirectory(tempDir);

        final TestListener listener = new TestListener(System.out, System.err);
        final PrintStream savedErr = System.err;
        final PrintStream savedOut = System.out;
        try {
            final File buildFile = new File(d, "build.xml");
            final Project project = new Project();
            project.addBuildListener(listener);
            System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
            System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
            project.fireBuildStarted();
            project.init();
            project.setUserProperty("test-name", testName);
            project.setUserProperty("generate-debug-attributes", "false");
            project.setUserProperty("preprocess.copy-generated-files.skip", "true");
            project.setUserProperty("ant.file", buildFile.getAbsolutePath());
            project.setUserProperty("ant.file.type", "file");
            project.setUserProperty("dita.dir", ditaDir.getAbsolutePath());
            project.setUserProperty("result.dir", resDir.getAbsolutePath());
            project.setUserProperty("temp.dir", tempDir.getAbsolutePath());
            project.setKeepGoingMode(false);
            ProjectHelper.configureProject(project, buildFile);
            final Vector<String> targets = new Vector<>();
            targets.addElement(project.getDefaultTarget());
            project.executeTargets(targets);

            assertEquals("Warn message count does not match expected",
                    getMessageCount(project, "warn"),
                    countMessages(listener.messages, Project.MSG_WARN));
            assertEquals("Error message count does not match expected",
                    getMessageCount(project, "error"),
                    countMessages(listener.messages, Project.MSG_ERR));
        } finally {
            System.setOut(savedOut);
            System.setErr(savedErr);
            return listener.messages;
        }
    }

    private int getMessageCount(final Project project, final String type) {
        int errorCount = 0;
        if (isWindows() && project.getProperty("exp.message-count." + type + ".windows") != null) {
            errorCount = Integer.parseInt(project.getProperty("exp.message-count." + type + ".windows"));
        } else if (project.getProperty("exp.message-count." + type) != null) {
            errorCount = Integer.parseInt(project.getProperty("exp.message-count." + type));
        }
        return errorCount;
    }

    private void compare(final File expDir, final File actDir) throws Throwable {
        final Collection<String> files = getFiles(expDir, actDir);
        for (final String name : files) {
            final File exp = new File(expDir, name);
            final File act = new File(actDir, name);
            if (exp.isDirectory()) {
                compare(exp, act);
            } else {
                final String ext = FileUtils.getExtension(name);
                try {
                    if (ext == null) {
                    } else if (ext.equals("xml") || ext.equals("dita") || ext.equals("ditamap")
                            || ext.equals("fo")) {
                        assertXMLEqual(parseXml(exp), parseXml(act));
                    }
                } catch (final RuntimeException ex) {
                    throw ex;
                } catch (final Throwable ex) {
                    throw new Throwable("Failed comparing " + exp.getAbsolutePath() + " and " + act.getAbsolutePath() + ": " + ex.getMessage(), ex);
                }
            }
        }
    }

    private Collection<String> getFiles(File expDir, File actDir) {
        final FileFilter filter = f -> f.isDirectory()
                || (this.transtype.compareable.contains(FileUtils.getExtension(f.getName()))
                && !ignorable.contains(f.getName()));
        final Set<String> buf = new HashSet<>();
        final File[] exp = expDir.listFiles(filter);
        if (exp != null) {
            buf.addAll(Arrays.asList(exp).stream().map(File::getName).collect(Collectors.toList()));
        }
        final File[] act = actDir.listFiles(filter);
        if (act != null) {
            buf.addAll(Arrays.asList(act).stream().map(File::getName).collect(Collectors.toList()));
        }
        return buf;
    }

    private Document parseXml(final File f) throws SAXException, IOException {
        if (!f.exists()) {
            throw new AssertionError(new FileNotFoundException(f.toString()));
        }
        final Document d = db.parse(f);
        final NodeList elems = d.getElementsByTagName("*");
        for (int i = 0; i < elems.getLength(); i++) {
            final Element e = (Element) elems.item(i);
            // remove debug attributes
            e.removeAttribute(ATTRIBUTE_NAME_XTRF);
            e.removeAttribute(ATTRIBUTE_NAME_XTRC);
            // remove DITA version and domains attributes
            e.removeAttributeNS(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION);
            e.removeAttribute(ATTRIBUTE_NAME_DOMAINS);
            e.removeAttribute(ATTRIBUTE_NAME_SPECIALIZATIONS);
            e.removeAttributeNS(DITA_OT_NS, "submap-specializations");
            // remove workdir processing instructions
            removeWorkdirProcessingInstruction(e);
        }
        final NodeList images = d.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "external-graphic");
        for (int i = 0; i < images.getLength(); i++) {
            final Element e = (Element) images.item(i);
            e.removeAttribute("src");
        }
        // rewrite IDs
        return rewriteIds(d, ditaIdPattern);
    }

    private void removeWorkdirProcessingInstruction(final Element e) {
        Node n = e.getFirstChild();
        while (n != null) {
            final Node next = n.getNextSibling();
            if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE &&
                    (n.getNodeName().equals(PI_WORKDIR_TARGET) || n.getNodeName().equals(PI_WORKDIR_TARGET_URI))) {
                e.removeChild(n);
            }
            n = next;
        }
    }

    private Document rewriteIds(final Document doc, final Map<String, Pattern> patterns) {
        final Map<String, String> idMap = new HashMap<>();
        AtomicInteger counter = new AtomicInteger();
        final NodeList ns = doc.getElementsByTagName("*");
        for (int i = 0; i < ns.getLength(); i++) {
            final Element e = (Element) ns.item(i);
            for (Map.Entry<String, Pattern> p : patterns.entrySet()) {
                final Attr id = e.getAttributeNode(p.getKey());
                if (id != null) {
                    if (p.getKey().equals("headers")) {// split value
                        final List<String> res = new ArrayList<>();
                        for (final String v : id.getValue().trim().split("\\s+")) {
                            rewriteId(v, idMap, counter, p.getValue());
                            res.add(idMap.getOrDefault(v, v));
                        }
                        id.setNodeValue(res.stream().collect(Collectors.joining(" ")));

                    } else {
                        final String v = id.getValue();
                        rewriteId(v, idMap, counter, p.getValue());
                        if (idMap.containsKey(v)) {
                            id.setNodeValue(idMap.get(v));
                        }
                    }
                }
            }
        }
        return doc;
    }

    /**
     * @param id      old ID value
     * @param idMap   ID map
     * @param counter counter
     * @param pattern pattern to test
     */
    private void rewriteId(final String id, final Map<String, String> idMap, final AtomicInteger counter, final Pattern pattern) {
        final Matcher m = pattern.matcher(id);
        if (m.matches()) {
            if (!idMap.containsKey(id)) {
                final int i = counter.addAndGet(1);
                idMap.put(id, "gen-id-" + Integer.toString(i));
            }
        }
    }

    enum Transtype {
        PREPROCESS("xhtml", true, "preprocess",
                "build-init", "preprocess");

        final String name;
        final boolean compareTemp;
        final Set<String> compareable;
        public final String exp;
        final String[] targets;

        Transtype(String name, boolean compareTemp, String exp, String... targets) {
            this(name, compareTemp, ImmutableSet.of("html", "htm", "xhtml", "hhk", "xml", "dita", "ditamap", "fo", "txt"), exp, targets);
        }

        Transtype(String name, boolean compareTemp, Set<String> compareable, String exp, String... targets) {
            this.name = name;
            this.compareTemp = compareTemp;
            this.compareable = compareable;
            this.exp = exp;
            this.targets = targets;
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    static class TestListener implements BuildListener {

        private final Pattern fatalPattern = Pattern.compile("\\[\\w+F\\]\\[FATAL\\]");
        private final Pattern errorPattern = Pattern.compile("\\[\\w+E\\]\\[ERROR\\]");
        private final Pattern warnPattern = Pattern.compile("\\[\\w+W\\]\\[WARN\\]");
        private final Pattern infoPattern = Pattern.compile("\\[\\w+I\\]\\[INFO\\]");
        private final Pattern debugPattern = Pattern.compile("\\[\\w+D\\]\\[DEBUG\\]");

        public final List<TestListener.Message> messages = new ArrayList<>();
        final PrintStream out;
        final PrintStream err;

        public TestListener(final PrintStream out, final PrintStream err) {
            this.out = out;
            this.err = err;
        }

        //@Override
        public void buildStarted(BuildEvent event) {
            messages.add(new TestListener.Message(-1, "build started: " + event.getMessage()));
        }

        //@Override
        public void buildFinished(BuildEvent event) {
            messages.add(new TestListener.Message(-1, "build finished: " + event.getMessage()));
        }

        //@Override
        public void targetStarted(BuildEvent event) {
            messages.add(new TestListener.Message(-1, event.getTarget().getName() + ":"));
        }

        //@Override
        public void targetFinished(BuildEvent event) {
            messages.add(new TestListener.Message(-1, "target finished: " + event.getTarget().getName()));
        }

        //@Override
        public void taskStarted(BuildEvent event) {
            messages.add(new TestListener.Message(Project.MSG_DEBUG, "task started: " + event.getTask().getTaskName()));
        }

        //@Override
        public void taskFinished(BuildEvent event) {
            messages.add(new TestListener.Message(Project.MSG_DEBUG, "task finished: " + event.getTask().getTaskName()));
        }

        //@Override
        public void messageLogged(BuildEvent event) {
            final String message = event.getMessage();
            int level;
            if (fatalPattern.matcher(message).find()) {
                level = Project.MSG_ERR;
            } else if (errorPattern.matcher(message).find()) {
                level = Project.MSG_ERR;
            } else if (warnPattern.matcher(message).find()) {
                level = Project.MSG_WARN;
            } else if (infoPattern.matcher(message).find()) {
                level = Project.MSG_INFO;
            } else if (debugPattern.matcher(message).find()) {
                level = Project.MSG_DEBUG;
            } else {
                level = event.getPriority();
            }

            switch (level) {
                case Project.MSG_DEBUG:
                case Project.MSG_VERBOSE:
                    break;
                case Project.MSG_INFO:
                    // out.println(event.getMessage());
                    break;
                default:
//                    err.println(message);
            }

            messages.add(new TestListener.Message(level, message));
        }

        static class Message {

            public final int level;
            public final String message;

            public Message(final int level, final String message) {
                this.level = level;
                this.message = message;
            }

        }

    }
}
