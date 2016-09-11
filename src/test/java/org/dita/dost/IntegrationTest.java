package org.dita.dost;

import static java.util.Collections.emptyList;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.*;
import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public final class IntegrationTest {

    private static final String TEMP_DIR = "temp_dir";
    private static final String BASEDIR = "basedir";
    private static final String DITA_DIR = "dita_dir";
    private static final String LOG_LEVEL = "log_level";
    private static final String TEST = "test";

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
    private static final File resourceDir = new File(baseDir, "resources");
//    private static final File resultDir = new File(baseDir, "testresult");
    private static DocumentBuilder db;
    private static HtmlDocumentBuilder htmlb;
    private static int level;

    private final File testDir;

    /**
     * Get test cases
     *
     * @return test cases which have comparable expected results
     */
    @Parameters(name = "{1}")
    public static Collection<Object[]> getFiles() {
        final Set<String> testNames = System.getProperty(TEST) != null && !System.getProperty(TEST).isEmpty()
                ? new HashSet<>(Arrays.asList(System.getProperty(TEST).split("[\\s|,]")))
                : null;
        final List<File> cases = Arrays.asList(resourceDir.listFiles(f -> {
            if (testNames != null && !testNames.contains(f.getName())) {
                return false;
            }
            if (!f.isDirectory() || !new File(f, "build.xml").exists()) {
                return false;
            }
            final File exp = new File(f, EXP_DIR);
            if (exp.exists()) {
                for (final String t : exp.list()) {
                    if (canCompare.contains(t)) {
                        return true;
                    }
                }
            }
            return false;
        }));
        Collections.sort(cases, (arg0, arg1) -> arg0.compareTo(arg1));
        final List<Object[]> params = new ArrayList<>(cases.size());
        for (final File f : cases) {
            final Object[] arr = new Object[]{f, f.getName()};
            params.add(arr);
        }
        return params;
    }

    public IntegrationTest(final File testDir, final String name) {
        this.testDir = testDir;
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

    @Test
    public void test() throws Throwable {
        final File expDir = new File(testDir, EXP_DIR);
        final File actDir = new File(baseTempDir, testDir.getName() + File.separator + "testresult" );
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

    private void outputLog(List<TestListener.Message> log) {
        System.err.println("Log start: " + testDir.getName());
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
        System.err.println("Log end: " + testDir.getName());
    }

    private int countMessages(final List<TestListener.Message> messages, final int level) {
        int count = 0;
        for (final TestListener.Message m : messages) {
            if (m.level == level) {
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
        System.out.println(resDir.getAbsolutePath());
        System.out.println(tempDir.getAbsolutePath());
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
            for (final String transtype : transtypes) {
                if (canCompare.contains(transtype)) {
                    project.setUserProperty("run." + transtype, "true");
                    if (transtype.equals("pdf") || transtype.equals("pdf2")) {
                        project.setUserProperty("pdf.formatter", "fop");
                        project.setUserProperty("fop.formatter.output-format", "text/plain");
                    }
                }
            }
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

    private static boolean isWindows() {
        final String osName = System.getProperty("os.name");
        return osName.startsWith("Windows");
    }

    private void compare(final File exp, final File act) throws Throwable {
        for (final File e : exp.listFiles()) {
            final File a = new File(act, e.getName());
            if (a.exists()) {
                if (e.isDirectory()) {
                    compare(e, a);
                } else {
                    final String name = e.getName();
                    try {
                        if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml")
                                || name.endsWith(".hhk")) {
                            TestUtils.resetXMLUnit();
                            XMLUnit.setNormalizeWhitespace(true);
                            XMLUnit.setIgnoreWhitespace(true);
                            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
                            XMLUnit.setIgnoreComments(true);
                            assertXMLEqual(parseHtml(e), parseHtml(a));
                        } else if (name.endsWith(".xml") || name.endsWith(".dita") || name.endsWith(".ditamap")) {
                            TestUtils.resetXMLUnit();
                            XMLUnit.setNormalizeWhitespace(true);
                            XMLUnit.setIgnoreWhitespace(true);
                            XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
                            XMLUnit.setIgnoreComments(true);
                            assertXMLEqual(parseXml(e), parseXml(a));
                        } else if (name.endsWith(".txt")) {
                            //assertEquals(readTextFile(e), readTextFile(a));
                            assertArrayEquals(readTextFile(e), readTextFile(a));
                        }
                    } catch (final RuntimeException ex) {
                        throw ex;
                    } catch (final Throwable ex) {
                        throw new Throwable("Failed comparing " + e.getAbsolutePath() + " and " + a.getAbsolutePath() + ": " + ex.getMessage(), ex);
                    }
                }
            }
        }
    }

    /**
     * Read text file into a string.
     *
     * @param f file to read
     * @return file contents
     * @throws IOException if reading file failed
     */
    private String[] readTextFile(final File f) throws IOException {
        final List<String> buf = new ArrayList<>();
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            String l = null;
            while ((l = r.readLine()) != null) {
                buf.add(l);
            }
        } catch (final IOException e) {
            throw new IOException("Unable to read " + f.getAbsolutePath() + ": " + e.getMessage());
        }
        return buf.toArray(new String[buf.size()]);
    }

    private static final Map<String, Pattern> htmlIdPattern = new HashMap<>();
    private static final Map<String, Pattern> ditaIdPattern = new HashMap<>();

    static {
        final String SAXON_ID = "d\\d+e\\d+";
        htmlIdPattern.put("id", Pattern.compile("(.*__)" + SAXON_ID + "|" + SAXON_ID + "(.*)"));
        htmlIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
        htmlIdPattern.put("headers", Pattern.compile(SAXON_ID + "(.*)"));

        ditaIdPattern.put("id", htmlIdPattern.get("id"));
        ditaIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
    }

    private Document parseHtml(final File f) throws SAXException, IOException {
        Document d = htmlb.parse(f);
        d = removeCopyright(d);
        return rewriteIds(d, htmlIdPattern);
    }

    private Document parseXml(final File f) throws SAXException, IOException {
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
            // remove workdir processing instructions
            removeWorkdirProcessingInstruction(e);
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

    private Document removeCopyright(final Document doc) {
        final NodeList ns = doc.getElementsByTagName("meta");
        for (int i = 0; i < ns.getLength(); i++) {
            final Element e = (Element) ns.item(i);
            final String name = e.getAttribute("name");
            if (name.equals("copyright") || name.equals("DC.rights.owner")) {
                e.removeAttribute("content");
            }
        }
        return doc;
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
                            if (idMap.containsKey(v)) {
                                res.add(idMap.get(v));
                            } else {
                                res.add(v);
                            }
                        }
                        id.setNodeValue(join(res));

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

    private String join(final List<String> vals) {
        final StringBuilder buf = new StringBuilder();
        for (final Iterator<String> i = vals.iterator(); i.hasNext(); ) {
            buf.append(i.next());
            if (i.hasNext()) {
                buf.append(" ");
            }
        }
        return buf.toString();
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


    static class TestListener implements BuildListener {

        private final Pattern fatalPattern = Pattern.compile("\\[\\w+F\\]\\[FATAL\\]");
        private final Pattern errorPattern = Pattern.compile("\\[\\w+E\\]\\[ERROR\\]");
        private final Pattern warnPattern = Pattern.compile("\\[\\w+W\\]\\[WARN\\]");
        private final Pattern infoPattern = Pattern.compile("\\[\\w+I\\]\\[INFO\\]");
        private final Pattern debugPattern = Pattern.compile("\\[\\w+D\\]\\[DEBUG\\]");

        public final List<Message> messages = new ArrayList<>();
        final PrintStream out;
        final PrintStream err;

        public TestListener(final PrintStream out, final PrintStream err) {
            this.out = out;
            this.err = err;
        }

        //@Override
        public void buildStarted(BuildEvent event) {
            messages.add(new Message(-1, "build started: " + event.getMessage()));
        }

        //@Override
        public void buildFinished(BuildEvent event) {
            messages.add(new Message(-1, "build finished: " + event.getMessage()));
        }

        //@Override
        public void targetStarted(BuildEvent event) {
            messages.add(new Message(-1, event.getTarget().getName() + ":"));
        }

        //@Override
        public void targetFinished(BuildEvent event) {
            messages.add(new Message(-1, "target finished: " + event.getTarget().getName()));
        }

        //@Override
        public void taskStarted(BuildEvent event) {
            messages.add(new Message(Project.MSG_DEBUG, "task started: " + event.getTask().getTaskName()));
        }

        //@Override
        public void taskFinished(BuildEvent event) {
            messages.add(new Message(Project.MSG_DEBUG, "task finished: " + event.getTask().getTaskName()));
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
                    err.println(message);
            }

            messages.add(new Message(level, message));
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