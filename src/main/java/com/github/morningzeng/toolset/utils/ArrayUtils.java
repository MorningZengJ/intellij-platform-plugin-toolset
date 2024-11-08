package com.github.morningzeng.toolset.utils;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * @author Morning Zeng
 * @since 2024-11-08
 */
public class ArrayUtils {

    @SafeVarargs
    public static <T> T[] merge(IntFunction<T[]> generator, T[] a, T... b) {
        return Stream.concat(
                        Arrays.stream(a),
                        Arrays.stream(b)
                )
                .toArray(generator);
    }

}
