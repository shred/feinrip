/*
 * feinrip
 *
 * Copyright (C) 2017 Richard "Shred" Körber
 *   https://codeberg.org/shred/feinrip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.feinrip.progress;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A consumer that forwards all values to the next consumer unless they fail to match
 * the filter.
 *
 * @author Richard "Shred" Körber
 */
public class FilteringConsumer<T> implements Consumer<T> {

    private final Predicate<T> filter;
    private final Consumer<T> then;

    /**
     * Creates a new {@link FilteringConsumer}.
     *
     * @param filter
     *            Filter to test
     * @param then
     *            Consumer to pass all values accepted by the filter
     */
    public FilteringConsumer(Predicate<T> filter, Consumer<T> then) {
        this.filter = filter;
        this.then = then;
    }

    @Override
    public void accept(T value) {
        if (filter.test(value)) {
            then.accept(value);
        }
    }

}
