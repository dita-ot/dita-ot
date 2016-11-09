package org.dita.dost;

import com.google.common.collect.ImmutableMap;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.apache.tools.ant.*;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.custommonkey.xmlunit.XMLAssert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertArrayEquals;

public final class IntegrationTest {

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
    private static final File resourceDir = new File(baseDir, "resources");
    private static DocumentBuilder db;
    private static HtmlDocumentBuilder htmlb;
    private static int level;

    private enum Transtype {
        PREPROCESS, XHTML;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
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

    @After
    public void cleanUp() {
        // remove temp & output
    }

    @Test
    public void test03() throws Throwable {
        test("03");
    }

    @Test
    public void test1_5_2_M4_BUG3052904() throws Throwable {
        test("1.5.2_M4_BUG3052904");
    }

    @Test
    public void test1_5_2_M4_BUG3052913() throws Throwable {
        test("1.5.2_M4_BUG3052913");
    }

    @Test
    public void test1_5_2_M4_BUG3056939() throws Throwable {
        test("1.5.2_M4_BUG3056939");
    }

    @Test
    public void test1_5_2_M5_BUG3059256() throws Throwable {
        test("1.5.2_M5_BUG3059256");
    }

    @Test
    public void test1_5_3_M2_BUG3157890() throws Throwable {
        test("1.5.3_M2_BUG3157890");
    }

    @Test
    public void test1_5_3_M2_BUG3164866() throws Throwable {
        test("1.5.3_M2_BUG3164866");
    }

    @Test
    public void test1_5_3_M3_BUG3178361() throws Throwable {
        test("1.5.3_M3_BUG3178361");
    }

    @Test
    public void test1_5_3_M3_BUG3191701() throws Throwable {
        test("1.5.3_M3_BUG3191701");
    }

    @Test
    public void test1_5_3_M3_BUG3191704() throws Throwable {
        test("1.5.3_M3_BUG3191704");
    }

    @Test
    public void test22() throws Throwable {
        test("22");
    }

    @Test
    public void test2374525() throws Throwable {
        test("2374525", Transtype.PREPROCESS,
                Paths.get("test.dita"),
                Collections.emptyMap());
    }

    @Test
    public void test3178361() throws Throwable {
        test("3178361", Transtype.PREPROCESS,
                Paths.get("conref-push-test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("dita.ext", ".dita")
                        .build());
    }

    @Test
    public void test3189883() throws Throwable {
        test("3189883", Transtype.PREPROCESS,
                Paths.get("main.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void test3191704() throws Throwable {
        test("3191704", Transtype.PREPROCESS,
                Paths.get("jandrew-test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("dita.ext", ".dita")
                        .build());
    }

    @Test
    public void test3344142() throws Throwable {
        test("3344142", Transtype.PREPROCESS,
                Paths.get("push.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("dita.ext", ".dita")
                        .build(), 2, 0);
    }

    @Test
    public void test3470331() throws Throwable {
        test("3470331", Transtype.PREPROCESS,
                Paths.get("bookmap.ditamap"), emptyMap());
    }

    @Test
    public void testMetadataInheritance() throws Throwable {
        test("MetadataInheritance");
    }

    @Test
    public void testSF1333481() throws Throwable {
        test("SF1333481", Transtype.PREPROCESS,
                Paths.get("main.ditamap"), emptyMap());
//        test("SF1333481", Transtype.XHTML,
//                Paths.get("subdir", "mapref1.ditamap"), emptyMap());
    }

    @Test
    public void testbookmap2() throws Throwable {
        test("bookmap(2)");
    }

    @Test
    public void testcoderef_source() throws Throwable {
        test("coderef_source", Transtype.PREPROCESS,
                Paths.get("mp.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("transtype", "preprocess").put("dita.ext", ".dita").put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testconref() throws Throwable {
        test("conref", Transtype.PREPROCESS, Paths.get("lang-common1.dita"),
                ImmutableMap.<String, Object>builder()
                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushAfter_between_Specialization() throws Throwable {
        test("pushAfter_between_Specialization", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushAfter_with_crossRef() throws Throwable {
        test("pushAfter_with_crossRef", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushAfter_with_InvalidTarget() throws Throwable {
        test("pushAfter_with_InvalidTarget", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushAfter_without_conref() throws Throwable {
        test("pushAfter_without_conref", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testsimple_pushAfter() throws Throwable {
        test("simple_pushAfter", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushBefore_between_Specialization() throws Throwable {
        test("pushBefore_between_Specialization", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushBefore_with_crossRef() throws Throwable {
        test("pushBefore_with_crossRef", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushBefore_with_InvalidTarget() throws Throwable {
        test("pushBefore_with_InvalidTarget", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushBefore_without_conref() throws Throwable {
        test("pushBefore_without_conref", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testsimple_pushBefore() throws Throwable {
        test("simple_pushBefore", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushReplace_between_Specialization() throws Throwable {
        test("pushReplace_between_Specialization", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushReplace_with_crossRef() throws Throwable {
        test("pushReplace_with_crossRef", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushReplace_with_InvalidTarget() throws Throwable {
        test("pushReplace_with_InvalidTarget", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 4);
    }

    @Test
    public void testpushReplace_without_conref() throws Throwable {
        test("pushReplace_without_conref", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testsimple_pushReplace() throws Throwable {
        test("simple_pushReplace", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testconrefbreaksxref() throws Throwable {
        test("conrefbreaksxref");
    }

    @Test
    public void testcontrol_value_file() throws Throwable {
        test("control_value_file");
    }

    @Test
    public void testexportanchors() throws Throwable {
        test("exportanchors", Transtype.PREPROCESS,
                Paths.get("test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("transtype", "eclipsehelp")
                        .build());
    }

    @Test
    public void testimage_scale() throws Throwable {
        test("image-scale");
    }

    @Test
    public void testindex_see() throws Throwable {
        test("index-see");
    }

    @Test
    public void testkeyref() throws Throwable {
        test("keyref", Transtype.PREPROCESS,
                Paths.get("test.ditamap"), emptyMap());
    }

    @Test
    public void testkeyref_All_tags() throws Throwable {
        test("keyref_All_tags");
    }

    @Test
    public void testkeyref_Keyword_links() throws Throwable {
        test("keyref_Keyword_links");
    }

    @Test
    public void testkeyref_Redirect_conref() throws Throwable {
        test("keyref_Redirect_conref");
    }

    @Test
    public void testkeyref_Redirect_link_or_xref() throws Throwable {
        test("keyref_Redirect_link_or_xref");
    }

    @Test
    public void testkeyref_Splitting_combining_targets() throws Throwable {
        test("keyref_Splitting_combining_targets");
    }

    @Test
    public void testkeyref_Swap_out_variable_content() throws Throwable {
        test("keyref_Swap_out_variable_content");
    }

    @Test
    public void testkeyref_modify() throws Throwable {
        test("keyref_modify");
    }

    @Test
    public void testlang() throws Throwable {
        test("lang");
    }

    @Test
    public void testmapref() throws Throwable {
        test("mapref", Transtype.PREPROCESS,
                Paths.get("test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("generate-debug-attributes", "false")
                        .build());
    }

    @Test
    public void testsubjectschema_case() throws Throwable {
        test("subjectschema_case", Transtype.XHTML,
                Paths.get("simplemap.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("args.filter", Paths.get("filter.ditaval"))
                        .put("clean.temp", "no")
                        .build());
    }

    @Test
    public void testuplevels() throws Throwable {
        test("uplevels");
    }

    private void test(final String name, final Transtype transtype, final Path input, final Map<String, Object> args) throws Throwable {
        test(name, transtype, input, args, 0, 0);
    }

    private void test(final String name, final Transtype transtype, final Path input, final Map<String, Object> args,
                      final int warnCount, final int errorCount) throws Throwable {
        final File testDir = Paths.get("src", "test", "resources", name).toFile();
        final File srcDir = new File(testDir, SRC_DIR);
        final File expDir = new File(testDir, EXP_DIR);
        final File outDir = new File(baseTempDir, testDir.getName() + File.separator + "out");
        final File tempDir = new File(baseTempDir, testDir.getName() + File.separator + "temp");

        final ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();
        args.forEach((k, v) -> {
            if (v instanceof Path) {
                builder.put(k, new File(srcDir, v.toString()).getAbsolutePath());
            } else if (v instanceof String) {
                builder.put(k, v.toString());
            } else {
                throw new IllegalArgumentException();
            }
        });
        builder.put("args.input", new File(srcDir, input.toFile().toString()).getAbsolutePath());
        final Map<String, String> params = builder.build();

        List<TestListener.Message> log = null;
        try {
            log = runOt(testDir, transtype, tempDir, outDir, params);
            assertEquals("Warn message count does not match expected",
                    warnCount,
                    countMessages(log, Project.MSG_WARN));
            assertEquals("Error message count does not match expected",
                    errorCount,
                    countMessages(log, Project.MSG_ERR));
            final File actDir = transtype == Transtype.PREPROCESS ? tempDir : outDir;
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

    private void test(final String name) throws Throwable {
        final File testDir = Paths.get("src", "test", "resources", name).toFile();

        final File expDir = new File(testDir, EXP_DIR);
        final File actDir = new File(baseTempDir, testDir.getName() + File.separator + "testresult");
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

    /**
     * Run test conversion
     *
     * @param srcDir    test source directory
     * @param transtype transtype to test
     * @return list of log messages
     * @throws Exception if conversion failed
     */
    private List<TestListener.Message> runOt(final File srcDir, final Transtype transtype, final File tempBaseDir, final File resBaseDir,
                                             final Map<String, String> args) throws Exception {
        final File tempDir = new File(tempBaseDir, transtype.toString());
        final File resDir = new File(resBaseDir, transtype.toString());
        deleteDirectory(resDir);
        deleteDirectory(tempDir);

        final TestListener listener = new TestListener(System.out, System.err);
        final PrintStream savedErr = System.err;
        final PrintStream savedOut = System.out;
        try {
            final File buildFile = new File(ditaDir, "build.xml");
            final Project project = new Project();
            project.addBuildListener(listener);
            System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
            System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
            project.fireBuildStarted();
            project.init();
            project.setUserProperty("transtype", transtype == Transtype.PREPROCESS ? Transtype.XHTML.toString() : transtype.toString());
            if (transtype.equals("pdf") || transtype.equals("pdf2")) {
                project.setUserProperty("pdf.formatter", "fop");
                project.setUserProperty("fop.formatter.output-format", "text/plain");
            }
            project.setUserProperty("generate-debug-attributes", "false");
            project.setUserProperty("preprocess.copy-generated-files.skip", "true");
            project.setUserProperty("ant.file", buildFile.getAbsolutePath());
            project.setUserProperty("ant.file.type", "file");
            project.setUserProperty("dita.dir", ditaDir.getAbsolutePath());
            project.setUserProperty("output.dir", resDir.getAbsolutePath());
            project.setUserProperty("dita.temp.dir", tempDir.getAbsolutePath());
            args.entrySet().forEach(e -> project.setUserProperty(e.getKey(), e.getValue()));

            project.setKeepGoingMode(false);
            ProjectHelper.configureProject(project, buildFile);
            final Vector<String> targets = new Vector<>();
            switch (transtype) {
                case PREPROCESS:
                    targets.addElement("build-init");
                    targets.addElement("preprocess");
                    break;
                default:
                    targets.addElement(project.getDefaultTarget());
            }
            project.executeTargets(targets);

            return listener.messages;
        } finally {
            System.setOut(savedOut);
            System.setErr(savedErr);
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
            String l;
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