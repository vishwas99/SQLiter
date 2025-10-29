package org.SQLiter.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SequenceManager {
    private static final ConcurrentHashMap<String, AtomicInteger> sequences = new ConcurrentHashMap<>();

    public static int next(String name) {
        return sequences.computeIfAbsent(name, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    public static int current(String name) {
        return sequences.getOrDefault(name, new AtomicInteger(0)).get();
    }
}

