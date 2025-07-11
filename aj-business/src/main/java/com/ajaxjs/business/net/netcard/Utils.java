package com.ajaxjs.business.net.netcard;

import java.net.InetAddress;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Predicate;

public class Utils {
    /**
     * Combines multiple Predicates into a single Predicate with logical AND.
     *
     * @param predicates the Predicates to combine
     * @return a Predicate that applies all given Predicates and returns true only
     * if all return true
     */
    @SafeVarargs
    public static Predicate<InetAddress> and(Predicate<InetAddress>... predicates) {
        // If no predicates are provided, return a predicate that always returns true
        if (predicates.length == 0)
            return ip -> true;

        // Start with the first predicate and apply .and() for each subsequent predicate
        Predicate<InetAddress> combinedPredicate = predicates[0];
        for (int i = 1; i < predicates.length; i++)
            combinedPredicate = combinedPredicate.and(predicates[i]);

        return combinedPredicate;
    }

    // Alternative implementation using Stream API for better readability
    @SafeVarargs
    public static Predicate<InetAddress> andStream(Predicate<InetAddress>... predicates) {
        return Arrays.stream(predicates).reduce(ip -> true, Predicate::and);
    }

    /**
     * Returns an immutable list view of the specified byte array.
     *
     * @param array the byte array to be wrapped into a List
     * @return an immutable list view of the specified byte array
     */
    public static List<Byte> asList(byte... array) {
        return new ByteArrayAsList(array);
    }

    private static class ByteArrayAsList extends AbstractList<Byte> implements RandomAccess {
        final byte[] array;

        ByteArrayAsList(byte[] array) {
            this.array = array.clone(); // Clone the array to prevent external modifications
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public Byte get(int index) {
            return array[index]; // Auto-boxing from byte to Byte
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (!(o instanceof List))
                return false;

            List<?> that = (List<?>) o;

            return that.size() == size() && containsAll(that);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(array);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');

            for (int i = 0; i < size(); i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(get(i));
            }

            return sb.append(']').toString();
        }
    }
}
