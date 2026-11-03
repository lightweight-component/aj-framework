package com.ajaxjs.security.dfa;

import com.ajaxjs.security.dfa.model.FilterResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveWordFilterTest {
    @Test
    void overlappingWordsAreReplacedOnlyOnce() {
        SensitiveWordFilter filter = new SensitiveWordFilter();
        assertEquals("*****!", filter.filter("apple!", "*"));
        assertEquals("[x]".repeat(5) + "!", filter.filter("apple!", "[x]"));
        assertEquals("!", filter.filter("apple!", ""));
        assertEquals(2, filter.findAllWords("apple!").size());
        SensitiveWordFilter crossing = new SensitiveWordFilter(List.of("aba", "bab"));
        assertEquals("****!", crossing.filter("abab!", "*"));
        assertEquals("!", crossing.filter("abab!", ""));
        assertEquals("**-**!", new SensitiveWordFilter(List.of("ab", "cd")).filter("ab-cd!", "*"));
    }

    @Test
    void normalizationIsSharedByAllEntryPoints() {
        SensitiveWordFilter filter = new SensitiveWordFilter(Arrays.asList(null, "", "  ", " app "));
        assertTrue(filter.containsSensitiveWord("app"));
        assertEquals("***", filter.filter("APP", null));
        filter.reloadWords(Arrays.asList(null, " x ", ""));
        assertTrue(filter.containsSensitiveWord("x"));
        assertFalse(filter.containsSensitiveWord("app"));
        filter.addWord(" A ");
        assertTrue(filter.containsSensitiveWord("a"));
        filter.addWord(null);
        filter.reloadWords(List.of());
        assertFalse(filter.containsSensitiveWord("anything"));
        assertNull(filter.filter(null, "*"));
        assertEquals("", filter.filter("", "*"));
    }

    @Test
    void unicodeOffsetsReferToOriginalUtf16Text() {
        SensitiveWordFilter filter = new SensitiveWordFilter(List.of("x"));
        var match = filter.findAllWords("İx").get(0);
        assertEquals("x", match.getWord());
        assertEquals(1, match.getStart());
        assertEquals(1, match.getEnd());
        assertEquals("İ*", filter.filter("İx", "*"));
        // Supplementary characters use two UTF-16 units but one replacement.
        SensitiveWordFilter supplementary = new SensitiveWordFilter(List.of("😀", "\uD801\uDC00"));
        assertEquals("a*b*", supplementary.filter("a😀b\uD801\uDC28", "*"));
        assertEquals(2, supplementary.findAllWords("a😀b").get(0).getEnd());
    }

    @Test
    void caseMappingDoesNotDependOnDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(new SensitiveWordFilter(List.of("I")).containsSensitiveWord("i"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void concurrentAddsDoNotLoseWords() throws Exception {
        SensitiveWordFilter filter = new SensitiveWordFilter(List.of());
        ExecutorService pool = Executors.newFixedThreadPool(6);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<?>> tasks = new java.util.ArrayList<>();
            for (int worker = 0; worker < 6; worker++) {
                int number = worker;
                tasks.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < 100; i++)
                        filter.addWord("prefix-" + number + "-" + i + "!");
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks)
                task.get(10, TimeUnit.SECONDS);
            for (int worker = 0; worker < 6; worker++)
                for (int i = 0; i < 100; i++)
                    assertTrue(filter.containsSensitiveWord("prefix-" + worker + "-" + i + "!"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void reloadAndAnalysisReturnInternallyConsistentResults() throws Exception {
        SensitiveWordFilter filter = new SensitiveWordFilter(List.of("apple"));
        ExecutorService pool = Executors.newFixedThreadPool(3);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<?> writer = pool.submit(() -> {
                start.await();
                for (int i = 0; i < 1000; i++)
                    filter.reloadWords(List.of(i % 2 == 0 ? "apple" : "orange"));
                return null;
            });

            Callable<Void> reader = () -> {
                start.await();

                for (int i = 0; i < 1000; i++) {
                    FilterResult result = filter.analyze("apple orange", "*");
                    assertTrue(result.isHasSensitive());
                    assertEquals(1, result.getSensitiveWordCount());
                    String matched = result.getSensitiveWords().get(0).getWord();
                    assertEquals(matched.equals("apple") ? "***** orange" : "apple ******", result.getFilteredText());
                }
                return null;
            };
            Future<?> first = pool.submit(reader), second = pool.submit(reader);
            start.countDown();
            writer.get(10, TimeUnit.SECONDS);
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }
}
