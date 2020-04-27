/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

public class IntegrationTestPreprocess1 extends IntegrationTest {

    public IntegrationTestPreprocess1 builder() {
        return new IntegrationTestPreprocess1();
    }

    @Override
    Transtype getTranstype(Transtype transtype) {
        return transtype;
    }

}
