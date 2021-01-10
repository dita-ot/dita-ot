/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;

public class ChunkModule extends AbstractPipelineModuleImpl {
    public enum Action {
        COMBINE("combine"),
        SPLIT("split");
        public final String name;

        Action(final String name) {
            this.name = name;
        }
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {

    }
}
