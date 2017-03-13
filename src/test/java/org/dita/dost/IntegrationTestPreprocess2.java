/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS2;
import static org.dita.dost.AbstractIntegrationTest.Transtype.XHTML_WITH_PREPROCESS2;

public class IntegrationTestPreprocess2 extends IntegrationTest {

    Transtype xhtml = XHTML_WITH_PREPROCESS2;
    Transtype preprocess = PREPROCESS2;

    @Override
    @Test
    public void test22_TC2() throws Throwable {
        builder().name("22_TC2")
                .transtype(preprocess)
                .input(Paths.get("TC2.ditamap"))
                // FIXME no warning about duplidate copy-to
                .warnCount(0)
                .test();
    }

    @Override
    @Test
    public void test22_TC3() throws Throwable {
        builder().name("22_TC3")
                .transtype(preprocess)
                .input(Paths.get("TC3.ditamap"))
                .warnCount(2)
                .test();
    }

    @Override
    @Ignore
    @Test
    public void testexportanchors() throws Throwable {
        builder().name("exportanchors")
                .transtype(preprocess)
                .input(Paths.get("test.ditamap"))
                .put("transtype", "eclipsehelp")
                .test();
    }

    @Override
    @Test
    @Ignore
    public void testsubjectschema_case() throws Throwable {
        builder().name("subjectschema_case")
                .transtype(xhtml)
                .input(Paths.get("simplemap.ditamap"))
                .put("args.filter", Paths.get("filter.ditaval"))
                .put("clean.temp", "no")
                .test();

    }
}
