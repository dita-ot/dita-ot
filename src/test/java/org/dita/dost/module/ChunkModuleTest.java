package org.dita.dost.module;

import org.dita.dost.TestUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

public class ChunkModuleTest extends AbstractModuleTest {

    @Override
    AbstractPipelineInput getAbstractPipelineInput() {
        final AbstractPipelineInput input = new PipelineHashIO();
        input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
        return input;
    }

    @Override
    AbstractPipelineModule getModule(final File tempDir) {
        try {
            final ChunkModule chunkModule = new ChunkModule();
            final Job job = new Job(tempDir);
            chunkModule.setJob(job);
            final TestUtils.CachingLogger logger = new TestUtils.CachingLogger();
            chunkModule.setLogger(logger);
            return chunkModule;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCase1() {
        test("case1.ditamap");
    }

    @Test
    public void testCase2() {
        test("case2.ditamap");
    }

    @Test
    public void testCase3() {
        test("case3.ditamap");
    }

    @Test
    public void testCase4() {
        test("case4.ditamap");
    }

    @Test
    public void testCase5() {
        test("case5.ditamap");
    }

    @Test
    public void testCase6() {
        test("case6.ditamap");
    }

    @Test
    public void testCase7() {
        test("case7.ditamap");
    }

    @Test
    public void testConflictByTopic() {
        test("conflict_by_topic.ditamap");
    }

    @Test
    public void testCopyTo1() {
        test("copy_to1.ditamap");
    }

    @Test
    public void testCopyTo2() {
        test("copy_to2.ditamap");
    }

    @Test
    public void testConflictSameId() {
        test("conflict_same_id.ditamap");
    }

    @Test
    public void testConflictToContent() {
        test("conflict_to_content.ditamap");
    }

    @Test
    public void testExternalChunk() {
        test("external_chunk.ditamap");
    }

    @Test
    public void testLink1() {
        test("link1.ditamap");
    }

    @Test
    public void testLink2() {
        test("link2.ditamap");
    }

    @Test
    public void testAnchor1() {
        test("anchor1.ditamap");
    }

    @Test
    public void testAnchor2() {
        test("anchor2.ditamap");
    }

}
