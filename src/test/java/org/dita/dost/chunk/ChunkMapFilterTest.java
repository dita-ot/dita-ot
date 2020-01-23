/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.chunk;

import org.apache.commons.io.FilenameUtils;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.util.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.dita.dost.chunk.ChunkOperation.Operation.*;
import static org.junit.Assert.assertEquals;

public class ChunkMapFilterTest {

    final File resourceDir = TestUtils.getResourceDir(org.dita.dost.module.ChunkModuleTest.class);
    private File tempBaseDir;
    final File srcBaseDir = new File(resourceDir, "src");
    final File expBaseDir = new File(resourceDir, "exp");

    @Before
    public void setUp() throws Exception {
        tempBaseDir = TestUtils.createTempDir(getClass());
    }

    @Test
    public void testCase1() {
        test("case1.ditamap",
                chunk(TO_CONTENT, "ditabase.dita#one", "ditabase.dita#one"),
                chunk(TO_CONTENT, "two.dita#two", "two.dita#two"),
                chunk(TO_CONTENT, "four.dita#four", "four.dita#four"),
                chunk(TO_CONTENT, "three.dita#three", "three.dita#three")
        );
    }

    @Test
    public void testCase2() {
        test("case2.ditamap",
                chunk(TO_CONTENT, "case2.dita", "case2.dita",
                        chunk(null, "ditabase.dita#one", null,
                                chunk(null, "Chunk0.dita", null,
                                        chunk(null, "nested.dita", null)
                                )
                        )
                )
        );
    }

    @Test
    public void testCase3() {
        test("case3.ditamap",
                chunk(BY_TOPIC, "ditabase.dita#", null,
                        chunk(BY_TOPIC, "ditabase.dita#one", null),
                        chunk(BY_TOPIC, "ditabase.dita#two", null),
                        chunk(BY_TOPIC, "ditabase.dita#three", null),
                        chunk(BY_TOPIC, "ditabase.dita#four", null),
                        chunk(BY_TOPIC, "ditabase.dita#five", null)
                )
        );
    }

    @After
    public void teardown() throws IOException {
        TestUtils.forceDelete(tempBaseDir);
    }

    private void test(final String testCase, final ChunkOperation... exp) {
        try {
            final String testName = FilenameUtils.getBaseName(testCase);
            final File srcDir = new File(srcBaseDir, testName);
            final File tempDir = new File(tempBaseDir, testName);
            final File expDir = new File(expBaseDir, testName);
            TestUtils.copy(srcDir, tempDir);

            final ChunkMapFilter mapFilter = new ChunkMapFilter();
            final CachingLogger logger = new CachingLogger(true);
            mapFilter.setLogger(logger);
            final Job job = new Job(tempDir);
            mapFilter.setJob(job);

            mapFilter.read(new File(tempDir, testCase));

            assertEquals(Arrays.asList(exp), simplify(mapFilter.changes, tempDir.toURI()));
//            compare(tempDir, expDir);

            logger.getMessages().stream()
                    .filter(m -> m.level == CachingLogger.Message.Level.ERROR)
                    .forEach(m -> System.err.println(m.level + ": " + m.message));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ChunkOperation chunk(final ChunkOperation.Operation operation,
                                 final String src,
                                 final String dst,
                                 final ChunkOperation... children) {
        return new ChunkOperation(
                operation,
                src != null ? URI.create(src) : null,
                dst != null ? URI.create(dst) : null,
                Arrays.asList(children)
        );
    }

    private List<ChunkOperation> simplify(final List<ChunkOperation> changes, final URI tempDir) {
        return changes.stream()
                .map(c -> new ChunkOperation(
                        c.operation,
                        c.src != null ? tempDir.relativize(c.src) : null,
                        c.dst != null ? tempDir.relativize(c.dst) : null,
                        simplify(c.children, tempDir)
                ))
                .collect(Collectors.toList());
    }

}
