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

import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS;
import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS2;
import static org.dita.dost.AbstractIntegrationTest.Transtype.XHTML_WITH_PREPROCESS2;

public class IntegrationTestPreprocess2 extends IntegrationTest {

    @Override
    Transtype getTranstype(Transtype transtype) {
        switch (transtype) {
            case PREPROCESS:
                return PREPROCESS2;
            case XHTML:
                return XHTML_WITH_PREPROCESS2;
            default:
                return transtype;
        }
    }

    @Override
    @Ignore
    @Test
    public void testexportanchors() throws Throwable {
        builder().name("exportanchors")
                .transtype(PREPROCESS)
                .input(Paths.get("test.ditamap"))
                .put("transtype", "eclipsehelp")
                .test();
    }

}
