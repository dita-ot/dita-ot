/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LangUtils {
    public static <T, S> Map.Entry<T, S> pair(T left, S right) {
        return new AbstractMap.SimpleImmutableEntry<T, S>(left, right);
    }

    /**
     * Zip list with 0-based index.
     *
     * @return stream of value-index pairs
     */
    public static <T> Stream<Map.Entry<T, Integer>> zipWithIndex(List<T> src) {
        return IntStream
                .range(0, src.size())
                .mapToObj(i -> pair(src.get(i), i));
    }
}
