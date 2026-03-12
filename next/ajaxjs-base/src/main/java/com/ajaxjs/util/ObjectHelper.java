/*
 * Copyright (C) 2025 Frank Cheung<frank@ajaxjs.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.util;

import java.util.*;

/**
 * Object Utility Helper - Provides common utility methods for working with Java objects,
 * collections, maps, and various data structures.
 *
 * <p>This class includes helper methods for:
 * - Text content checking and validation
 * - Collection and array emptiness checks
 * - Convenient factory methods for creating collections (maps, lists, sets)
 * - Capacity calculation utilities
 * <p>
 * It simplifies common object manipulation patterns across Java applications.
 */
public class ObjectHelper {
    /**
     * Checks if the given String has actual text content.
     * More specifically, returns {@code true} if the string is not {@code null},
     * its length is greater than 0, and it contains at least one non-whitespace character.
     *
     * @param str the String to check
     * @return {@code true} if the String has non-whitespace text content
     */
    public static boolean hasText(String str) {
        return str != null && !str.isEmpty() && containsText(str);
    }

    /**
     * Checks if the given String is empty or contains only whitespace.
     * This is the opposite of {@link #hasText(String)}.
     *
     * @param str the String to check
     * @return {@code true} if the String is empty or contains only whitespace
     */
    public static boolean isEmptyText(String str) {
        return !hasText(str);
    }

    /**
     * Checks if the given CharSequence contains any non-whitespace characters.
     *
     * @param str the CharSequence to check
     * @return {@code true} if the CharSequence contains at least one non-whitespace character
     */
    private static boolean containsText(CharSequence str) {
        int strLen = str.length();

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i)))
                return true;
        }

        return false;
    }

    /**
     * Determine whether the given array is empty:
     * i.e. {@code null} or of zero length.
     *
     * @param array the array to check
     * @return {@code true} if the array is {@code null} or of zero lengths
     */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * An empty, immutable Map. Useful as a default or sentinel value when no parameters are needed.
     */
    public static final Map<String, Object> EMPTY_PARAMS_MAP = Collections.unmodifiableMap(new HashMap<>());

    /**
     * Creates a new HashMap with a single key-value pair.
     *
     * @param k1  the first key
     * @param v1  the first value
     * @param <K> the key type
     * @param <V> the value type
     * @return the newly created HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);

        return map;
    }

    /**
     * Creates a new HashMap with two key-value pairs.
     *
     * @param k1  the first key
     * @param v1  the first value
     * @param k2  the second key
     * @param v2  the second value
     * @param <K> the key type
     * @param <V> the value type
     * @return the newly created HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);

        return map;
    }

    /**
     * Creates a new HashMap with three key-value pairs.
     *
     * @param k1  the first key
     * @param v1  the first value
     * @param k2  the second key
     * @param v2  the second value
     * @param k3  the third key
     * @param v3  the third value
     * @param <K> the key type
     * @param <V> the value type
     * @return the newly created HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = mapOf(3);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);

        return map;
    }

    /**
     * Default load factor for hash maps, optimized for time and space efficiency.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Creates a HashMap with a specified expected number of entries.
     * The initial capacity and load factor are calculated to minimize resizing.
     *
     * @param expectedSize the expected number of entries in the map
     * @return a new HashMap with optimal initial capacity and load factor
     */

    public static int getInitialCapacity(int expectedSize) {
        // Calculate the initial capacity as the next power of two greater than or equal to expectedSize / default_load_factor
        int initialCapacity = (int) Math.ceil(expectedSize / DEFAULT_LOAD_FACTOR);
        initialCapacity = Integer.highestOneBit(initialCapacity - 1) << 1;

        // Ensure that the initial capacity is at least 16 (the default capacity)
        if (initialCapacity < 16)
            initialCapacity = 16;

        return initialCapacity;
    }

    /**
     * Creates a HashMap optimized for the expected number of entries.
     * The initial capacity is calculated to minimize resizing operations.
     *
     * @param expectedSize the expected number of entries in the map
     * @param <K>          the key type
     * @param <V>          the value type
     * @return a new HashMap with optimal initial capacity
     */
    public static <K, V> Map<K, V> mapOf(int expectedSize) {
        return new HashMap<>(getInitialCapacity(expectedSize), DEFAULT_LOAD_FACTOR);// Create and return the HashMap with the calculated initial capacity and default load factor
    }

    /**
     * Input multiple elements and returns a list of those elements.
     * Just like `List.of()` in Java 9.
     *
     * @param arr The elements
     * @param <T> The type of the elements
     * @return A new list containing the elements of the array
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... arr) {
        return Collections.unmodifiableList(Arrays.asList(arr));
    }

    /**
     * Creates an immutable Set containing the specified elements.
     * Similar to Java 9+ Set.of() but available in earlier Java versions.
     * Automatically removes duplicates and returns an unmodifiable collection.
     *
     * @param elements the elements to include in the set
     * @param <T>      the element type
     * @return an immutable Set containing the specified elements
     * @throws IllegalArgumentException if null elements are provided
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        if (elements == null)
            throw new IllegalArgumentException("Elements array cannot be null");

        Set<T> set = new HashSet<>();

        for (T element : elements) {
            if (element == null)
                throw new IllegalArgumentException("Set elements cannot contain null");

            set.add(element);
        }

        return Collections.unmodifiableSet(set);
    }
}