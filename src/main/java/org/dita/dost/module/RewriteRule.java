/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import java.util.Collection;
import org.dita.dost.util.Job;

public interface RewriteRule {
    /**
     * Rewrite {@link org.dita.dost.util.Job.FileInfo#result}.
     */
    Collection<Job.FileInfo> rewrite(Collection<Job.FileInfo> fis);
}
