package com.lea.transport.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Discovers every @Test method across the suite classes, runs each in isolation, prints a summary. */
public class TestRunner {
    public static void main(String[] args) throws Exception {
        Class<?>[] testClasses = {
                com.lea.transport.test.suite.AuthorizationTests.class,
                com.lea.transport.test.suite.LockingTests.class,
                com.lea.transport.test.suite.PupilRecordServiceTests.class,
                com.lea.transport.test.suite.RouteAssignmentServiceTests.class,
                com.lea.transport.test.suite.CsvImporterTests.class,
                com.lea.transport.test.suite.FacadeAuthorizationTests.class,
        };

        int total = 0, passed = 0;
        List<String> failures = new ArrayList<>();

        for (Class<?> testClass : testClasses) {
            for (Method method : testClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Test.class)) continue;
                total++;
                String testName = testClass.getSimpleName() + "." + method.getName();
                try {
                    Object instance = testClass.getDeclaredConstructor().newInstance();
                    method.invoke(instance);
                    System.out.println("[PASS] " + testName);
                    passed++;
                } catch (Exception invocationError) {
                    Throwable cause = invocationError.getCause() != null ? invocationError.getCause() : invocationError;
                    System.out.println("[FAIL] " + testName + " -> " + cause.getMessage());
                    failures.add(testName + ": " + cause.getMessage());
                }
            }
        }

        System.out.println();
        System.out.println("=================================================");
        System.out.printf("Test run complete: %d/%d passed%n", passed, total);
        System.out.println("=================================================");

        if (!failures.isEmpty()) {
            System.out.println("Failures:");
            failures.forEach(f -> System.out.println("  - " + f));
            System.exit(1);
        }
    }
}
