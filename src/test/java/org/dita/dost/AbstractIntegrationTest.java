/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.apache.tools.ant.*;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.Store;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.Executable;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public abstract class AbstractIntegrationTest {

  /**
   * Message codes where duplicates are ignored in message count.
   */
  private static final String[] ignoreDuplicates = new String[] { "DOTJ037W" };
  private final Collection<Executable> compareResults = new ArrayList<>();

  enum Transtype {
    PREPROCESS("xhtml", true, "preprocess", "build-init", "preprocess"),
    XHTML("xhtml", false, "xhtml", "dita2xhtml"),
    HTML5("html5", false, "html5", "dita2html5"),
    PDF("pdf", true, Set.of("fo"), "pdf", "dita2pdf2"),
    ECLIPSEHELP("eclipsehelp", false, "eclipsehelp", "dita2eclipsehelp"),
    HTMLHELP("htmlhelp", false, "htmlhelp", "dita2htmlhelp"),
    PREPROCESS2("xhtml", true, "preprocess", "build-init", "preprocess2"),
    XHTML_WITH_PREPROCESS2(
      "xhtml",
      false,
      "xhtml",
      "dita2xhtml.init",
      "build-init",
      "preprocess2",
      "xhtml.topics",
      "dita.map.xhtml",
      "copy-css"
    );

    final String name;
    final boolean compareTemp;
    final Set<String> compareable;
    public final String exp;
    final String[] targets;

    Transtype(String name, boolean compareTemp, String exp, String... targets) {
      this(
        name,
        compareTemp,
        Set.of("html", "htm", "xhtml", "hhk", "xml", "dita", "ditamap", "fo", "txt"),
        exp,
        targets
      );
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

  // Builder

  private Path name;
  private Transtype transtype;
  private String[] targets;
  private Path input;
  private final Map<String, Object> args = new HashMap<>();
  private int warnCount = 0;
  private int errorCount = 0;

  public AbstractIntegrationTest name(String name) {
    this.name = Paths.get(name);
    return this;
  }

  public AbstractIntegrationTest name(Path name) {
    this.name = name;
    return this;
  }

  public AbstractIntegrationTest transtype(Transtype transtype) {
    this.transtype = getTranstype(transtype);
    return this;
  }

  abstract Transtype getTranstype(Transtype transtype);

  public AbstractIntegrationTest targets(String... targets) {
    this.targets = targets;
    return this;
  }

  public AbstractIntegrationTest input(Path input) {
    this.input = input;
    return this;
  }

  public AbstractIntegrationTest put(String key, Object value) {
    this.args.put(key, value);
    return this;
  }

  public AbstractIntegrationTest warnCount(int warnCount) {
    this.warnCount = warnCount;
    return this;
  }

  public AbstractIntegrationTest errorCount(int errorCount) {
    this.errorCount = errorCount;
    return this;
  }

  // Runner

  private List<TestListener.Message> log;
  private File actDir;

  protected static final Map<String, Pattern> htmlIdPattern = new HashMap<>();
  protected static final Map<String, Pattern> ditaIdPattern = new HashMap<>();
  private static final String TEMP_DIR = "temp_dir";
  private static final String BASEDIR = "basedir";
  private static final String DITA_DIR = "dita_dir";
  private static final String LOG_LEVEL = "log_level";
  private static final String TEST = "test";
  private static final String SRC_DIR = "src";
  private static final String EXP_DIR = "exp";
  private static final Collection<String> canCompare = Arrays.asList(
    "html5",
    "xhtml",
    "eclipsehelp",
    "htmlhelp",
    "preprocess",
    "pdf"
  );
  private static final File ditaDir = new File(
    System.getProperty(DITA_DIR) != null ? System.getProperty(DITA_DIR) : "src" + File.separator + "main"
  );
  private static final File baseDir = new File(
    System.getProperty(BASEDIR) != null ? System.getProperty(BASEDIR) : "src" + File.separator + "test"
  );
  private static final File baseTempDir = new File(
    System.getProperty(TEMP_DIR) != null
      ? System.getProperty(TEMP_DIR)
      : "build" + File.separator + "tmp" + File.separator + "integrationTest"
  );
  static final File resourceDir = new File(baseDir, "resources");
  private static DocumentBuilder db;
  private static HtmlDocumentBuilder htmlb;
  private static int level;

  static {
    final String SAXON_ID = "d\\d+e\\d+";
    htmlIdPattern.put("id", Pattern.compile("(.*__)" + SAXON_ID + "|" + SAXON_ID + "(.*)"));
    htmlIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
    htmlIdPattern.put("headers", Pattern.compile(SAXON_ID + "(.*)"));

    ditaIdPattern.put("id", htmlIdPattern.get("id"));
    ditaIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
    ditaIdPattern.put(
      "internal-destination",
      Pattern.compile("_OPENTOPIC_TOC_PROCESSING_" + SAXON_ID + "|" + "fn" + SAXON_ID)
    );
  }

  @BeforeAll
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

  @AfterEach
  public void cleanUp() {
    // remove temp & output
  }

  protected File test() throws Throwable {
    final File actDir = run();
    compare();
    assertAll(compareResults);
    return actDir;
  }

  protected File run() throws Throwable {
    final File testDir = Paths.get("src", "test", "resources").resolve(name).toFile();
    final File srcDir = new File(testDir, SRC_DIR);
    final File outDir = new File(baseTempDir, name + File.separator + "out");
    final File tempDir = new File(baseTempDir, name + File.separator + "temp");

    final Map<String, String> builder = new HashMap<>();
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
    final Map<String, String> params = Collections.unmodifiableMap(builder);

    try {
      this.log = runOt(testDir, transtype, tempDir, outDir, params, targets);
      final List<TestListener.Message> warnings = getMessages(log, Project.MSG_WARN);
      final List<TestListener.Message> errors = getMessages(log, Project.MSG_ERR);
      assertAll(
        () -> assertEquals(warnCount, warnings.size(), "warnCount mismatch: " + warnings + "\n"),
        () -> assertEquals(errorCount, errors.size(), "errorCount mismatch: " + errors + "\n")
      );
      this.actDir = transtype.compareTemp ? tempDir : outDir;
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Throwable e) {
      if (log != null && level >= 0) {
        outputLog(log);
      }
      throw new Throwable("Case " + testDir.getName() + " failed: " + e.getMessage(), e);
    }
    return new File(actDir, transtype.name);
  }

  protected AbstractIntegrationTest compare() throws Throwable {
    File exp = Paths
      .get("src", "test", "resources")
      .resolve(name)
      .resolve(Paths.get(EXP_DIR, transtype.toString()))
      .toFile();
    if (!exp.exists()) {
      exp = Paths.get("src", "test", "resources").resolve(name).resolve(Paths.get(EXP_DIR, transtype.exp)).toFile();
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

  private List<TestListener.Message> getMessages(final List<TestListener.Message> messages, final int level) {
    List<TestListener.Message> filteredMessages = new ArrayList<>();
    final Set<String> duplicates = new HashSet<>();
    messages:for (final TestListener.Message m : messages) {
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
        filteredMessages.add(m);
      }
    }
    return filteredMessages;
  }

  /**
   * Run test conversion
   *
   * @param srcDir    test source directory
   * @param transtype transtype to test
   * @return list of log messages
   * @throws Exception if conversion failed
   */
  private List<TestListener.Message> runOt(
    final File srcDir,
    final Transtype transtype,
    final File tempBaseDir,
    final File resBaseDir,
    final Map<String, String> args,
    final String[] targets
  ) throws Exception {
    //        System.out.println(transtype);
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
      project.setUserProperty("transtype", transtype.name);
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
      project.setUserProperty("clean.temp", "no");
      args.forEach(project::setUserProperty);

      project.setKeepGoingMode(false);
      ProjectHelper.configureProject(project, buildFile);
      final Vector<String> ts = new Vector<>();
      if (targets != null) {
        ts.addAll(Arrays.asList(targets));
      } else {
        ts.addAll(Arrays.asList(transtype.targets));
      }
      project.executeTargets(ts);

      final Store store = project.getReference(ANT_REFERENCE_STORE);

      if (store instanceof CacheStore) {
        final Job job = new Job(tempDir.getAbsoluteFile(), store);
        for (Job.FileInfo fileInfo : job.getFileInfo()) {
          if (fileInfo.uri != null) {
            final URI f = job.tempDirURI.resolve(fileInfo.uri);
            if (store.exists(f)) {
              final Path dir = Paths.get(f).getParent();
              if (!Files.exists(dir)) {
                Files.createDirectories(dir);
              }
              try (final InputStream inputStream = store.getInputStream(f)) {
                Files.copy(inputStream, Paths.get(f));
              } catch (NoSuchFileException | FileNotFoundException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }

      return listener.messages;
    } finally {
      System.setOut(savedOut);
      System.setErr(savedErr);
    }
  }

  private void compare(final File expDir, final File actDir) throws Throwable {
    final Collection<String> files = getFiles(expDir, actDir);
    for (final String name : files) {
      final File exp = new File(expDir, name);
      final File act = new File(actDir, name);
      if (exp.isDirectory() || (!exp.exists() && act.isDirectory())) {
        compare(exp, act);
      } else {
        if (exp.exists() && act.exists()) {
          compareFileContents(exp, act);
        } else {
          reportMissingFile(exp, act);
        }
      }
    }
  }

  private void reportMissingFile(File exp, File act) {
    String errorMessage = "Missing file: " + (!exp.exists() ? exp.getAbsolutePath() : act.getAbsolutePath()) + "\n";
    System.out.print(errorMessage);
    compareResults.add(() -> fail(errorMessage));
  }

  private void compareFileContents(File exp, File act) {
    String message = "Failed comparing " + exp.getAbsolutePath() + " and " + act.getAbsolutePath() + ": ";
    final String ext = FileUtils.getExtension(exp.getName());
    if (ext != null) {
      // prettier-ignore
      switch (ext) {
        case "html", "htm", "xhtml", "hhk" ->
          compareResults.add(() -> assertXMLEqual(parseHtml(exp), parseHtml(act), message));
        case "xml", "dita", "ditamap", "fo" ->
          compareResults.add(() -> assertXMLEqual(parseXml(exp), parseXml(act), message));
        case "txt" ->
          compareResults.add(() -> assertArrayEquals(readTextFile(exp), readTextFile(act), message));
      }
    }
  }

  final Set<String> ignorable = Set.of("keydef.xml", "subrelation.xml", ".job.xml", "stage2.fo");

  private Collection<String> getFiles(File expDir, File actDir) {
    final FileFilter filter = f ->
      f.isDirectory() ||
      (this.transtype.compareable.contains(FileUtils.getExtension(f.getName())) && !ignorable.contains(f.getName()));
    final Set<String> buf = new HashSet<>();
    final File[] exp = expDir.listFiles(filter);
    if (exp != null) {
      buf.addAll(Arrays.stream(exp).map(File::getName).toList());
    }
    final File[] act = actDir.listFiles(filter);
    if (act != null) {
      buf.addAll(Arrays.stream(act).map(File::getName).toList());
    }
    return buf;
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
    try (final BufferedReader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
      String l;
      while ((l = r.readLine()) != null) {
        buf.add(l);
      }
    } catch (final IOException e) {
      throw new IOException("Unable to read " + f.getAbsolutePath() + ": " + e.getMessage());
    }
    return buf.toArray(new String[0]);
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
      e.removeAttribute(ATTRIBUTE_NAME_SPECIALIZATIONS);
      e.removeAttributeNS(DITA_OT_NS, "submap-specializations");
      e.removeAttributeNS(DITA_OT_NS, "imagerefuri");
      e.removeAttributeNS(DITA_OT_NS, "original-imageref");
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
      if (
        n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE &&
        (n.getNodeName().equals(PI_WORKDIR_TARGET) || n.getNodeName().equals(PI_WORKDIR_TARGET_URI))
      ) {
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
          if (p.getKey().equals("headers")) { // split value
            final List<String> res = new ArrayList<>();
            for (final String v : id.getValue().trim().split("\\s+")) {
              rewriteId(v, idMap, counter, p.getValue());
              res.add(idMap.getOrDefault(v, v));
            }
            id.setNodeValue(String.join(" ", res));
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
  private void rewriteId(
    final String id,
    final Map<String, String> idMap,
    final AtomicInteger counter,
    final Pattern pattern
  ) {
    final Matcher m = pattern.matcher(id);
    if (m.matches()) {
      if (!idMap.containsKey(id)) {
        final int i = counter.addAndGet(1);
        idMap.put(id, "gen-id-" + i);
      }
    }
  }

  static class TestListener implements BuildListener {

    private final Pattern fatalPattern = Pattern.compile("\\[\\w+F\\]");
    private final Pattern errorPattern = Pattern.compile("\\[\\w+E\\]");
    private final Pattern warnPattern = Pattern.compile("\\[\\w+W\\]");
    private final Pattern infoPattern = Pattern.compile("\\[\\w+I\\]");
    private final Pattern debugPattern = Pattern.compile("\\[\\w+D\\]");

    public final List<Message> messages = new ArrayList<>();
    final PrintStream out;
    final PrintStream err;

    public TestListener(final PrintStream out, final PrintStream err) {
      this.out = out;
      this.err = err;
    }

    @Override
    public void buildStarted(BuildEvent event) {
      messages.add(new Message(-1, "build started: " + event.getMessage()));
    }

    @Override
    public void buildFinished(BuildEvent event) {
      messages.add(new Message(-1, "build finished: " + event.getMessage()));
    }

    @Override
    public void targetStarted(BuildEvent event) {
      messages.add(new Message(-1, event.getTarget().getName() + ":"));
    }

    @Override
    public void targetFinished(BuildEvent event) {
      messages.add(new Message(-1, "target finished: " + event.getTarget().getName()));
    }

    @Override
    public void taskStarted(BuildEvent event) {
      messages.add(new Message(Project.MSG_DEBUG, "task started: " + event.getTask().getTaskName()));
    }

    @Override
    public void taskFinished(BuildEvent event) {
      messages.add(new Message(Project.MSG_DEBUG, "task finished: " + event.getTask().getTaskName()));
    }

    @Override
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

      messages.add(new Message(level, message));
    }

    record Message(int level, String message) {}
  }
}
