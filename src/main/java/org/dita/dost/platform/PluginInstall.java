/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.tools.ant.BuildException;
import org.dita.dost.invoker.Main;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.platform.Registry.Dependency;
import org.dita.dost.util.Configuration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public final class PluginInstall {

  private List<String> registries;
  private File tempDir;
  private final ObjectMapper mapper = new ObjectMapper();
  private List<String> installedPlugins;
  private Integrator integrator;

  private DITAOTLogger logger;
  private File ditaDir;

  private Path pluginFile;
  private URI pluginUri;
  private String pluginName;
  private SemVerMatch pluginVersion;
  private boolean force;

  private void init() {
    registries =
      Arrays
        .stream(Configuration.configuration.get("registry").trim().split("\\s+"))
        .map(registry -> registry.endsWith("/") ? registry : (registry + "/"))
        .collect(Collectors.toList());
    try {
      tempDir = Files.createTempDirectory(null).toFile();
    } catch (IOException e) {
      throw new BuildException("Failed to create temporary directory: " + e.getMessage(), e);
    }
    installedPlugins = Plugins.getInstalledPlugins().stream().map(Map.Entry::getKey).toList();

    integrator = new Integrator(ditaDir);
    integrator.setLogger(logger);
  }

  public void execute() throws Exception {
    init();
    if (pluginFile != null || pluginUri != null || pluginName != null) {
      installPlugin();
    }
    try {
      integrator.execute();
    } catch (final Exception e) {
      throw new BuildException("Integration failed: " + e.toString(), e);
    }
  }

  private void installPlugin() throws Exception {
    try {
      final Map<String, Path> installs = new HashMap<>();
      if (pluginFile != null && Files.exists(pluginFile)) {
        final Path tempPluginDir = unzip(pluginFile.toFile());
        final String name = getPluginName(tempPluginDir);
        installs.put(name, tempPluginDir);
      } else if (pluginUri != null) {
        final File tempFile = get(pluginUri, null);
        final Path tempPluginDir = unzip(tempFile);
        final String name = getPluginName(tempPluginDir);
        installs.put(name, tempPluginDir);
      } else {
        final Set<Registry> plugins = readRegistry(this.pluginName, pluginVersion);
        for (final Registry plugin : plugins) {
          final File tempFile = get(plugin.uri(), plugin.cksum());
          final Path tempPluginDir = unzip(tempFile);
          final String name = plugin.name();
          installs.put(name, tempPluginDir);
        }
      }
      for (final Map.Entry<String, Path> install : installs.entrySet()) {
        final String name = install.getKey();
        final Path tempPluginDir = install.getValue();
        final File pluginDir = getPluginDir(name);
        if (pluginDir.exists()) {
          if (force) {
            logger.info("Force install to {0}", pluginDir);
            integrator.addRemoved(name);
            FileUtils.deleteDirectory(pluginDir);
          } else {
            logger.warn(Main.locale.getString("install.error.exists"), name);
            throw new BuildException();
          }
        }
        FileUtils.copyDirectory(tempPluginDir.toFile(), pluginDir);
      }
    } catch (IOException e) {
      throw new BuildException(e.getMessage(), e);
    } finally {
      cleanUp();
    }
  }

  private void cleanUp() {
    if (tempDir != null) {
      try {
        logger.trace("Delete {}", tempDir);
        FileUtils.deleteDirectory(tempDir);
      } catch (IOException e) {
        throw new BuildException(e);
      }
    }
  }

  private String getFileHash(final File file) {
    try (
      DigestInputStream digestInputStream = new DigestInputStream(
        new BufferedInputStream(Files.newInputStream(file.toPath())),
        MessageDigest.getInstance("SHA-256")
      )
    ) {
      IOUtils.copy(digestInputStream, new NullOutputStream());
      final MessageDigest digest = digestInputStream.getMessageDigest();
      final byte[] sha256 = digest.digest();
      return printHexBinary(sha256);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      throw new BuildException("Failed to calculate file checksum: " + e.getMessage(), e);
    }
  }

  private String printHexBinary(final byte[] md5) {
    final StringBuilder sb = new StringBuilder();
    for (byte b : md5) {
      sb.append(String.format("%02X", b));
    }
    return sb.toString().toLowerCase();
  }

  private String getPluginName(final Path pluginDir) {
    final Path config = pluginDir.resolve("plugin.xml");
    try {
      final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(config.toFile());
      return doc.getDocumentElement().getAttribute("id");
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new BuildException("Failed to read plugin name: " + e.getMessage(), e);
    }
  }

  private File getPluginDir(final String id) {
    return Paths.get(ditaDir.getAbsolutePath(), "plugins", id).toFile();
  }

  private Set<Registry> readRegistry(final String name, final SemVerMatch version) {
    logger.trace("Reading registries for {0} {1}", name, Objects.requireNonNullElse(version, ""));
    Registry res = null;
    for (final String registry : registries) {
      final URI registryUrl = URI.create(registry + name + ".json");
      logger.debug("Read registry {0}", registry);
      try (BufferedInputStream in = new BufferedInputStream(registryUrl.toURL().openStream())) {
        logger.trace("Parse registry");
        final JsonFactory factory = mapper.getFactory();
        final JsonParser parser = factory.createParser(in);
        final JsonNode obj = mapper.readTree(parser);
        final Collection<Registry> regs;
        if (obj.isArray()) {
          regs = Arrays.asList(mapper.treeToValue(obj, Registry[].class));
        } else {
          regs = resolveAlias(mapper.treeToValue(obj, Alias.class));
        }
        final Optional<Registry> reg = findPlugin(regs, version);
        if (reg.isPresent()) {
          final Registry plugin = reg.get();
          logger.trace("Plugin found at {0}@{1}", registryUrl, plugin.vers());
          res = plugin;
          break;
        }
      } catch (MalformedURLException e) {
        logger.error("Invalid registry URL {0}: {1}", registryUrl, e.getMessage(), e);
      } catch (FileNotFoundException e) {
        // Ignore
      } catch (IOException e) {
        logger.error("Failed to read registry configuration {0}: {1}", registryUrl, e.getMessage(), e);
      }
    }
    if (res == null) {
      throw new BuildException(Main.locale.getString("install.error.not_found_from_registry").formatted(pluginFile));
    }

    Set<Registry> results = new HashSet<>();
    results.add(res);
    res
      .deps()
      .stream()
      .filter(dep -> !installedPlugins.contains(dep.name()))
      .flatMap(dep -> readRegistry(dep.name(), dep.req()).stream())
      .forEach(results::add);

    return results;
  }

  private Collection<Registry> resolveAlias(Alias registry) {
    return readRegistry(registry.alias(), null);
  }

  private File get(final URI uri, final String expectedChecksum) throws Exception {
    final File tempPluginFile = new File(tempDir, "plugin.zip");

    final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    final HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
    try {
      logger.debug("Download {}", request.uri());
      client.send(request, HttpResponse.BodyHandlers.ofFile(tempPluginFile.toPath()));
    } catch (IOException | InterruptedException e) {
      throw new Exception(Main.locale.getString("install.error.download_failure").formatted(uri), e);
    }

    if (expectedChecksum != null) {
      final String checksum = getFileHash(tempPluginFile);
      if (!checksum.equalsIgnoreCase(expectedChecksum)) {
        throw new BuildException(
          Main.locale.getString("install.error.checksum_mismatch").formatted(checksum, expectedChecksum)
        );
      }
    }

    return tempPluginFile;
  }

  /**
   * @see <a href="https://snyk.io/research/zip-slip-vulnerability">Zip Slip Vulnerability</a>
   */
  private Path unzip(final File input) throws Exception {
    final Path tempPluginDir = new File(tempDir, "plugin").toPath();

    logger.trace("Expanding {0} to {1}", input, tempPluginDir);
    try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(input.toPath()))) {
      for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null;) {
        Path resolvedPath = tempPluginDir.resolve(ze.getName()).normalize();
        if (!resolvedPath.startsWith(tempPluginDir)) {
          throw new Exception("Entry with an illegal path: %s".formatted(ze.getName()));
        }
        if (ze.isDirectory()) {
          Files.createDirectories(resolvedPath);
        } else {
          Files.createDirectories(resolvedPath.getParent());
          logger.trace("Write {0}", resolvedPath);
          Files.copy(zipIn, resolvedPath);
        }
      }
    } catch (IOException e) {
      throw new Exception("Failed to expand %s to %s".formatted(input, tempPluginDir), e);
    }

    return findBaseDir(tempPluginDir);
  }

  private Path findBaseDir(final Path tempPluginDir) throws Exception {
    try {
      return Files
        .find(tempPluginDir, 256, (path, attributes) -> path.getFileName().toString().equals("plugin.xml"))
        .findFirst()
        .orElseThrow(() -> new IOException("plugin.xml not found"))
        .getParent();
    } catch (NoSuchFileException e) {
      throw new Exception(Main.locale.getString("install.error.plugin_xml_not_found"));
    }
  }

  private Optional<Registry> findPlugin(final Collection<Registry> regs, final SemVerMatch version) {
    if (version == null) {
      return regs.stream().filter(this::matchingPlatformVersion).max(Comparator.comparing(o -> o.vers()));
    } else {
      return regs
        .stream()
        .filter(this::matchingPlatformVersion)
        .filter(reg -> version.contains(reg.vers()))
        .findFirst();
    }
  }

  @VisibleForTesting
  boolean matchingPlatformVersion(final Registry reg) {
    final Optional<Dependency> platformDependency = reg
      .deps()
      .stream()
      .filter(dep -> dep.name().equals("org.dita.base"))
      .findFirst();
    if (platformDependency.isPresent()) {
      final SemVer platform = new SemVer(Configuration.configuration.get("otversion"));
      final Dependency dep = platformDependency.get();
      return dep.req().contains(platform);
    } else {
      return true;
    }
  }

  public void setForce(final boolean force) {
    this.force = force;
  }

  public void setLogger(DITAOTLogger logger) {
    this.logger = logger;
  }

  public void setDitaDir(File ditaDir) {
    this.ditaDir = ditaDir;
  }

  public void setPluginFile(Path pluginFile) {
    this.pluginFile = pluginFile;
  }

  public void setPluginUri(URI pluginUri) {
    this.pluginUri = pluginUri;
  }

  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }

  public void setPluginVersion(SemVerMatch pluginVersion) {
    this.pluginVersion = pluginVersion;
  }
}
