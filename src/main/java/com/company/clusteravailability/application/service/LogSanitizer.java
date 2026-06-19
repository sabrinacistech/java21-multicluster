package com.company.clusteravailability.application.service;

final class LogSanitizer {

    private LogSanitizer() {
    }

    static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\r\\n\\t]", "_");
    }
}
