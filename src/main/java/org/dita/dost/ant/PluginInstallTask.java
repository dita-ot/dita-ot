/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Get;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.platform.*;
import org.dita.dost.platform.Registry.Dependency;
import org.dita.dost.util.Configuration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public final class PluginInstallTask extends Task {

    private List<String> registries;

    private File tempDir;
    private final ObjectMapper mapper = new ObjectMapper();
    private List<String> installedPlugins;
    private Path pluginFile;
    private URL pluginUrl;
    private String pluginName;
    private SemVerMatch pluginVersion;
    private boolean force;
    private Integrator integrator;

    @Override
    public void init() {
        registries = Arrays.stream(Configuration.configuration.get("registry").trim().split("\\s+"))
                .map(registry -> registry.endsWith("/") ? registry : (registry + "/"))
                .collect(Collectors.toList());
        try {
            tempDir = Files.createTempDirectory(null).toFile();
        } catch (IOException e) {
            throw new BuildException("Failed to create temporary directory: " + e.getMessage(), e);
        }
        installedPlugins = Plugins.getInstalledPlugins();

        final DITAOTAntLogger logger;
        logger = new DITAOTAntLogger(getProject());
        logger.setTarget(getOwningTarget());
        logger.setTask(this);

        integrator = new Integrator(getProject().getBaseDir());
        integrator.setLogger(logger);
    }

    private void cleanUp() {
        if (tempDir != null) {
            try {
                FileUtils.deleteDirectory(tempDir);
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    @Override
    public void execute() throws BuildException {
        if (pluginFile == null && pluginUrl == null && pluginName == null) {
            throw new BuildException(new IllegalStateException("pluginName argument not set"));
        }

        try {
            final Map<String, File> installs = new HashMap<>();
            if (pluginFile != null && Files.exists(pluginFile)) {
                final File tempPluginDir = unzip(pluginFile.toFile());
                final String name = getPluginName(tempPluginDir);
                installs.put(name, tempPluginDir);
            } else if (pluginUrl != null) {
                final File tempFile = get(pluginUrl, null);
                final File tempPluginDir = unzip(tempFile);
                final String name = getPluginName(tempPluginDir);
                installs.put(name, tempPluginDir);
            } else {
                final Set<Registry> plugins = readRegistry(this.pluginName, pluginVersion);
                for (final Registry plugin : plugins) {
                    final File tempFile = get(plugin.url, plugin.cksum);
                    final File tempPluginDir = unzip(tempFile);
                    final String name = plugin.name;
                    installs.put(name, tempPluginDir);
                }
            }
            for (final Map.Entry<String, File> install : installs.entrySet()) {
                final String name = install.getKey();
                final File tempPluginDir = install.getValue();
                final File pluginDir = getPluginDir(name);
                if (pluginDir.exists()) {
                    if (force) {
                        log("Force install to " + pluginDir, Project.MSG_INFO);
                        integrator.addRemoved(name);
                        FileUtils.deleteDirectory(pluginDir);
                    } else {
                        throw new BuildException(new IllegalStateException(String.format("Plug-in %s already installed: %s", name, pluginDir)));
                    }
                }
                FileUtils.copyDirectory(tempPluginDir, pluginDir);
            }
        } catch (IOException e) {
            throw new BuildException(e.getMessage(), e);
        } finally {
            cleanUp();
        }
        try {
            integrator.execute();
        } catch (final Exception e) {
            throw new BuildException("Integration failed: " + e.getMessage(), e);
        }
    }

    private String getFileHash(final File file) {
        try (DigestInputStream digestInputStream = new DigestInputStream(new BufferedInputStream(
                new FileInputStream(file)), MessageDigest.getInstance("SHA-256"))) {
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

    private String getPluginName(final File pluginDir) {
        final File config = new File(pluginDir, "plugin.xml");
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(config);
            return doc.getDocumentElement().getAttribute("id");
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new BuildException("Failed to read plugin name: " + e.getMessage(), e);
        }
    }

    private File getPluginDir(final String id) {
        return Paths.get(getProject().getProperty("dita.dir"), "plugins", id).toFile();
    }

    private Set<Registry> readRegistry(final String name, final SemVerMatch version) {
        log(String.format("Reading registries for %s@%s", name, version), Project.MSG_INFO);
        Registry res = null;
        for (final String registry : registries) {
            final URI registryUrl = URI.create(registry + name + ".json");
            log(String.format("Read registry %s", registry), Project.MSG_INFO);
            try (BufferedInputStream in = new BufferedInputStream(registryUrl.toURL().openStream())) {
                log("Parse registry", Project.MSG_INFO);
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
                    log(String.format("Plugin found at %s@%s", registryUrl, plugin.vers), Project.MSG_INFO);
                    res = plugin;
                    break;
                }
            } catch (MalformedURLException e) {
                log(String.format("Invalid registry URL %s: %s", registryUrl, e.getMessage()), e, Project.MSG_ERR);
            } catch (FileNotFoundException e) {
                log(String.format("Registry configuration %s not found", registryUrl), e, Project.MSG_INFO);
            } catch (IOException e) {
                log(String.format("Failed to read registry configuration %s: %s", registryUrl, e.getMessage()), e, Project.MSG_ERR);
            }
        }
        if (res == null) {
            throw new BuildException("Unable to find plugin " + pluginFile + " in any configured registry.");
        }

        Set<Registry> results = new HashSet<>();
        results.add(res);
        res.deps.stream()
                .filter(dep -> !installedPlugins.contains(dep.name))
                .flatMap(dep -> readRegistry(dep.name, dep.req).stream())
                .forEach(results::add);

        return results;
    }

    private Collection<Registry> resolveAlias(Alias registry) {
        return readRegistry(registry.alias, null);
    }

    private File get(final URL url, final String expectedChecksum) {
        final File tempPluginFile = new File(tempDir, "plugin.zip");

        final Get get = new Get();
        get.setProject(getProject());
        get.setTaskName("get");
        get.setSrc(url);
        get.setDest(tempPluginFile);
        get.setIgnoreErrors(false);
        get.setVerbose(false);
        get.execute();

        if (expectedChecksum != null) {
            final String checksum = getFileHash(tempPluginFile);
            if (!checksum.equalsIgnoreCase(expectedChecksum)) {
                throw new BuildException(new IllegalArgumentException(String.format("Downloaded plugin file checksum %s does not match expected value %s", checksum, expectedChecksum)));
            }
        }

        return tempPluginFile;
    }

    private File unzip(final File input) {
        final File tempPluginDir = new File(tempDir, "plugin");

        final Expand unzip = new Expand();
        unzip.setProject(getProject());
        unzip.setTaskName("unzip");
        unzip.setSrc(input);
        unzip.setDest(tempPluginDir);
        unzip.execute();

        return findBaseDir(tempPluginDir);
    }

    private File findBaseDir(final File tempPluginDir) {
        final File config = new File(tempPluginDir, "plugin.xml");
        if (config.exists()) {
            return tempPluginDir;
        } else {
            for (final File dir : tempPluginDir.listFiles(File::isDirectory)) {
                final File res = findBaseDir(dir);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
    }

    private Optional<Registry> findPlugin(final Collection<Registry> regs, final SemVerMatch version) {
        if (version == null) {
            return regs.stream()
                    .filter(this::matchingPlatformVersion)
                    .max(Comparator.comparing(o -> o.vers));
        } else {
            return regs.stream()
                    .filter(this::matchingPlatformVersion)
                    .filter(reg -> version.contains(reg.vers))
                    .findFirst();
        }
    }

    @VisibleForTesting
    boolean matchingPlatformVersion(final Registry reg) {
        final Optional<Dependency> platformDependency = reg.deps.stream()
                .filter(dep -> dep.name.equals("org.dita.base"))
                .findFirst();
        if (platformDependency.isPresent()) {
            final SemVer platform = new SemVer(Configuration.configuration.get("otversion"));
            final Dependency dep = platformDependency.get();
            return dep.req.contains(platform);
        } else {
            return true;
        }
    }

    // Setters

    public void setPluginFile(final String pluginFile) {
        try {
            this.pluginFile = Paths.get(pluginFile);
        } catch (InvalidPathException e) {
            // Ignore
        }
        try {
            final URI uri = new URI(pluginFile);
            if (uri.isAbsolute()) {
                this.pluginUrl = uri.toURL();
            }
        } catch (MalformedURLException | URISyntaxException e) {
            // Ignore
        }
        if (pluginFile.contains("@")) {
            final String[] tokens = pluginFile.split("@");
            pluginName = tokens[0];
            pluginVersion = new SemVerMatch(tokens[1]);
        } else {
            pluginName = pluginFile;
            pluginVersion = null;
        }
    }

    public void setForce(final boolean force) {
        this.force = force;
    }
}
