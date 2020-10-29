/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 *
 * Simple object pool.
 *
 * @since 3.6
 */
public class Pool<T> {

    private final Queue<T> objects;
    private final Supplier<T> create;

    public Pool(Supplier<T> create) {
        this.create = create;
        this.objects = new ConcurrentLinkedQueue<T>();
    }

    public T borrowObject() {
        T t;
        if ((t = objects.poll()) == null) {
            t = create.get();
        }
        return t;
    }

    public void returnObject(T object) {
        this.objects.offer(object);
    }
}
