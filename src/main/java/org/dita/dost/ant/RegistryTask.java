/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.platform.Registry;
import org.dita.dost.platform.Registry.Dependency;
import org.dita.dost.platform.SemVer;
import org.dita.dost.util.Configuration;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;

public final class RegistryTask extends Task {

    private List<String> registries;

    private String name;
    private SemVer version;
    private String property;
    private String checksumProperty;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        registries = Arrays.asList(Configuration.configuration.get("registry").trim().split("\\s+"));
    }

    @Override
    public void execute() throws BuildException {
        for (final String registry : registries) {
            final URI registryUrl = URI.create(registry + name + ".json");
            log(String.format("Read registry %s", registry), Project.MSG_DEBUG);
            try (BufferedInputStream in = new BufferedInputStream(registryUrl.toURL().openStream())) {
                log("Parse registry", Project.MSG_DEBUG);
                final List<Registry> regs = Arrays.asList(mapper.readValue(in, Registry[].class));
                final Optional<Registry> reg = getRegistry(regs);
                if (reg.isPresent()) {
                    final Registry plugin = reg.get();
                    log(String.format("Plugin found at %s@%s", registryUrl, plugin.vers), Project.MSG_INFO);
                    getProject().setProperty(property, plugin.url);
                    getProject().setProperty(checksumProperty, plugin.cksum);
                    return;
                }
            } catch (MalformedURLException e) {
                log(String.format("Invalid registry URL %s: %s", registryUrl, e.getMessage()), e, Project.MSG_ERR);
            } catch (FileNotFoundException e) {
                log(String.format("Registry configuration %s not found", registryUrl), e, Project.MSG_DEBUG);
            } catch (IOException e) {
                log(String.format("Failed to read registry configuration %s: %s", registryUrl, e.getMessage()), e, Project.MSG_ERR);
            }
        }
        throw new BuildException("Unable to find matching plugin");
    }

    private Optional<Registry> getRegistry(List<Registry> regs) {
        if (version == null) {
            return regs.stream()
                    .filter(this::matchingPlatformVersion)
                    .max(Comparator.comparing(o -> o.vers));
        } else {
            return regs.stream()
                    .filter(this::matchingPlatformVersion)
                    .filter(reg -> reg.vers.equals(version))
                    .findFirst();
        }
    }

    @VisibleForTesting
    boolean matchingPlatformVersion(Registry reg) {
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

    public void setName(final String name) {
        if (name.contains("@")) {
            final String[] tokens = name.split("@");
            this.name = tokens[0];
            this.version = new SemVer(tokens[1]);
        } else {
            this.name = name;
            this.version = null;
        }
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    public void setChecksum(final String checksumProperty) {
        this.checksumProperty = checksumProperty;
    }

}
