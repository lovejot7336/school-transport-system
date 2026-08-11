package com.lea.transport.test;

import java.util.Objects;

/** Minimal assertion helpers, in the style of JUnit's Assert/Assertions classes. */
public final class Assertions {
    private Assertions() {}

    public static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void assertFalse(boolean condition, String message) { assertTrue(!condition, message); }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " (expected <" + expected + "> but was <" + actual + ">)");
        }
    }

    public static void assertNull(Object value, String message) {
        if (value != null) throw new AssertionError(message + " (expected null but was <" + value + ">)");
    }

    public static void assertNotNull(Object value, String message) {
        if (value == null) throw new AssertionError(message);
    }

    @FunctionalInterface
    public interface ThrowingAction { void run() throws Exception; }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingAction action, String message) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) return expectedType.cast(t);
            throw new AssertionError(message + " (expected " + expectedType.getSimpleName()
                    + " but caught " + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
        }
        throw new AssertionError(message + " (expected " + expectedType.getSimpleName() + " but nothing was thrown)");
    }
}
