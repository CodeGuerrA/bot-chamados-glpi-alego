package com.chatbot.chatbotglpi.shared.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StackTraceUtil {

    private static final int MAX_STACK_TRACE_ELEMENTS = 10;

    private static final List<Pattern> SENSITIVE_PATTERNS = Arrays.asList(
            Pattern.compile("password[=:]\\s*[^\\s,}]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("token[=:]\\s*[^\\s,}]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("secret[=:]\\s*[^\\s,}]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("api[_-]?key[=:]\\s*[^\\s,}]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("authorization[=:]\\s*[^\\s,}]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bearer\\s+[^\\s,}]+", Pattern.CASE_INSENSITIVE)
    );

    public static List<String> sanitizeStackTrace(Throwable throwable) {
        if (throwable == null) {
            return List.of();
        }

        return Arrays.stream(throwable.getStackTrace())
                .limit(MAX_STACK_TRACE_ELEMENTS)
                .map(StackTraceElement::toString)
                .map(StackTraceUtil::sanitizeLine)
                .collect(Collectors.toList());
    }

    public static String sanitizeMessage(String message) {
        if (message == null) {
            return null;
        }

        String sanitized = message;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("$1=***REDACTED***");
        }
        return sanitized;
    }

    private static String sanitizeLine(String line) {
        if (line == null) {
            return null;
        }

        String sanitized = line;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("***REDACTED***");
        }
        return sanitized;
    }

    public static String getFullStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());
        if (throwable.getMessage() != null) {
            sb.append(": ").append(sanitizeMessage(throwable.getMessage()));
        }
        sb.append("\n");

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        if (throwable.getCause() != null) {
            sb.append("Caused by: ").append(getFullStackTrace(throwable.getCause()));
        }

        return sb.toString();
    }
}

