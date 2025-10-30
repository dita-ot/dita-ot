/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Follow OSGi bundle version syntax rules:
 *
 * <pre>
 * version   ::= major( '.' minor ( '.' micro ( '.' qualifier )? )? )?
 * major     ::= number
 * minor     ::= number
 * micro     ::= number
 * qualifier ::= ( alphanum | '_' | '-' )+
 * </pre>
 */
public class SemVer implements Comparable<SemVer> {

  public final int major;
  public final int minor;
  public final int patch;
  public final List<Object> preRelease;

  public SemVer(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.preRelease = Collections.emptyList();
  }

  @JsonCreator
  public SemVer(String version) {
    String value = version;
    String preRel = null;
    final int sep = value.indexOf('-');
    if (sep != -1) {
      preRel = value.substring(sep + 1);
      value = value.substring(0, sep);
    }
    String build = null;
    if (preRel != null) {
      final int buildSep = preRel.indexOf('+');
      if (buildSep != -1) {
        build = preRel.substring(buildSep + 1);
        preRel = preRel.substring(0, buildSep);
      }
    }
    String[] tokens = value.split("\\.");
    major = Integer.parseInt(tokens[0]);
    minor = tokens.length >= 2 ? Integer.parseInt(tokens[1]) : 0;
    patch = tokens.length >= 3 ? Integer.parseInt(tokens[2]) : 0;
    preRelease =
      preRel != null
        ? Stream
          .of(preRel.split("\\."))
          .map(token -> {
            try {
              return Integer.valueOf(token);
            } catch (NumberFormatException e) {
              return token;
            }
          })
          .collect(Collectors.toList())
        : Collections.emptyList();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SemVer semVer = (SemVer) o;
    return major == semVer.major && minor == semVer.minor && patch == semVer.patch;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch);
  }

  @Override
  public int compareTo(SemVer o) {
    if (o.major == this.major && o.minor == this.minor && o.patch == this.patch) {
      return 0;
    }
    if (o.major > this.major) {
      return -1;
    }
    if (o.major < this.major) {
      return 1;
    }
    if (o.minor > this.minor) {
      return -1;
    }
    if (o.minor < this.minor) {
      return 1;
    }
    if (o.patch > this.patch) {
      return -1;
    }
    if (o.patch < this.patch) {
      return 1;
    }
    throw new IllegalStateException();
  }

  @Override
  @JsonValue
  public String toString() {
    return String.format("%d.%d.%d", major, minor, patch);
  }
}
