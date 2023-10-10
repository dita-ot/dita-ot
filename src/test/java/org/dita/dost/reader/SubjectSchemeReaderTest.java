/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.xml.namespace.QName;
import org.dita.dost.TestUtils;
import org.dita.dost.module.filter.SubjectScheme;
import org.dita.dost.module.filter.SubjectScheme.SubjectDefinition;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SubjectSchemeReaderTest {

  private final XMLUtils xmlUtils = new XMLUtils();

  private SubjectSchemeReader reader;

  @TempDir
  File tempDir;

  @BeforeEach
  void setup() throws IOException {
    Job job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
    reader = new SubjectSchemeReader();
    reader.setJob(job);
    var logger = new TestUtils.CachingLogger();
    reader.setLogger(logger);
  }

  private Path init(String file) {
    final Path src = tempDir.toPath().resolve(file);
    try {
      Files.copy(
        Paths.get(
          Objects
            .requireNonNull(getClass().getResource("/org/dita/dost/reader.SubjectSchemeReaderTest.src/" + file))
            .toURI()
        ),
        src
      );
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return src;
  }

  @ParameterizedTest
  @ValueSource(
    strings = { "example-subjectScheme-filtering.ditamap", "example-subjectScheme-filtering-inline.ditamap" }
  )
  void loadSubjectScheme(String file) {
    final Path src = init(file);

    reader.loadSubjectScheme(src.toFile());

    var act = reader.getSubjectSchemeMap();
    assertFalse(act.isEmpty());
    assertSubjectSchemeEquals(
      act,
      new SubjectScheme(
        Map.of(
          QName.valueOf("platform"),
          Map.of(
            "*",
            Set.of(
              createSubjectDef(
                "os",
                createSubjectDef("linux", createSubjectDef("redhat"), createSubjectDef("suse")),
                createSubjectDef("windows"),
                createSubjectDef("zos")
              )
            )
          )
        )
      )
    );
    assertEquals(
      Map.of(QName.valueOf("platform"), Map.of("*", Set.of("suse", "linux", "windows", "redhat", "zos"))),
      reader.getValidValuesMap()
    );
    assertEquals(Map.of(), reader.getDefaultValueMap());
  }

  @Test
  void loadSubjectScheme_defaultSubject() {
    final Path src = init("defaultSubject.ditamap");

    reader.loadSubjectScheme(src.toFile());

    var act = reader.getSubjectSchemeMap();
    assertFalse(act.isEmpty());
    assertSubjectSchemeEquals(
      act,
      new SubjectScheme(
        Map.of(
          QName.valueOf("platform"),
          Map.of(
            "*",
            Set.of(
              createSubjectDef(
                "os",
                createSubjectDef("linux", createSubjectDef("redhat"), createSubjectDef("suse")),
                createSubjectDef("windows"),
                createSubjectDef("zos")
              )
            )
          )
        )
      )
    );
    assertEquals(
      Map.of(QName.valueOf("platform"), Map.of("*", Set.of("suse", "linux", "windows", "redhat", "zos"))),
      reader.getValidValuesMap()
    );
    assertEquals(Map.of(QName.valueOf("platform"), Map.of("*", "linux")), reader.getDefaultValueMap());
  }

  @Test
  void loadSubjectScheme_multipleKeyValues() {
    final Path src = init("multiple-key-values.ditamap");

    reader.loadSubjectScheme(src.toFile());

    var act = reader.getSubjectSchemeMap();
    assertFalse(act.isEmpty());
    assertSubjectSchemeEquals(
      act,
      new SubjectScheme(
        Map.of(
          QName.valueOf("platform"),
          Map.of("*", Set.of(createSubjectDef("os", createSubjectDef("linux redhat suse windows zos"))))
        )
      )
    );
    assertEquals(
      Map.of(QName.valueOf("platform"), Map.of("*", Set.of("linux", "redhat", "suse", "windows", "zos"))),
      reader.getValidValuesMap()
    );
    assertEquals(Map.of(), reader.getDefaultValueMap());
  }

  @Test
  void loadSubjectScheme_element() {
    final Path src = init("attribute-element.ditamap");

    reader.loadSubjectScheme(src.toFile());

    var act = reader.getSubjectSchemeMap();
    assertFalse(act.isEmpty());
    assertSubjectSchemeEquals(
      act,
      new SubjectScheme(
        Map.of(
          QName.valueOf("platform"),
          Map.of(
            "*",
            Set.of(createSubjectDef("all-os", createSubjectDef("linux"), createSubjectDef("windows"))),
            "codeblock",
            Set.of(createSubjectDef("os", createSubjectDef("linux")))
          )
        )
      )
    );
    assertEquals(
      Map.of(QName.valueOf("platform"), Map.of("*", Set.of("linux", "windows"), "codeblock", Set.of("linux"))),
      reader.getValidValuesMap()
    );
    assertEquals(Map.of(), reader.getDefaultValueMap());
  }

  @Test
  void loadSubjectScheme_indirectSubjectdef() {
    final Path src = init("indirect-subjectdef.ditamap");

    reader.loadSubjectScheme(src.toFile());

    var act = reader.getSubjectSchemeMap();
    assertFalse(act.isEmpty());
    assertSubjectSchemeEquals(
      act,
      new SubjectScheme(
        Map.of(
          QName.valueOf("platform"),
          Map.of(
            "*",
            Set.of(
              createSubjectDef(
                "os",
                new SubjectDefinition(Set.of(), "linux", Collections.emptyList()),
                createSubjectDef("windows"),
                createSubjectDef("zos")
              )
            )
          )
        )
      )
    );
    // FIXME
    assertEquals(Map.of(QName.valueOf("platform"), Map.of("*", Set.of("windows", "zos"))), reader.getValidValuesMap());
    assertEquals(Map.of(), reader.getDefaultValueMap());
  }

  private void assertSubjectSchemeEquals(SubjectScheme act, SubjectScheme exp) {
    assertEquals(exp.subjectSchemeMap().keySet(), act.subjectSchemeMap().keySet());
    for (QName expKey : exp.subjectSchemeMap().keySet()) {
      final Map<String, Set<SubjectDefinition>> actStringSetMap = act.subjectSchemeMap().get(expKey);
      final Map<String, Set<SubjectDefinition>> expStringSetMap = exp.subjectSchemeMap().get(expKey);
      assertEquals(expStringSetMap.keySet(), actStringSetMap.keySet());
      for (String expSetKey : expStringSetMap.keySet()) {
        var expValueSet = expStringSetMap.get(expSetKey);
        var actValueSet = actStringSetMap.get(expSetKey);
        assertEquals(expValueSet, actValueSet);
      }
    }
  }

  private SubjectDefinition createSubjectDef(String keys) {
    return new SubjectDefinition(Set.of(keys.split("\\s+")), null, Collections.emptyList());
  }

  private SubjectDefinition createSubjectDef(String keys, SubjectDefinition... children) {
    return new SubjectDefinition(Set.of(keys.split("\\s+")), null, Arrays.asList(children));
  }
}
