/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.util;

import java.util.Locale;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static long parseDurationMillis(String input) {
        String value = input.toLowerCase(Locale.ROOT).trim();
        if (value.equals("permanent") || value.equals("perm")) {
            return Long.MAX_VALUE;
        }
        if (value.length() < 2) {
            return -1;
        }

        char unit = value.charAt(value.length() - 1);
        long multiplier = switch (unit) {
            case 's' -> 1000L;
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            default -> -1L;
        };
        if (multiplier < 0) {
            return -1;
        }
        try {
            long amount = Long.parseLong(value.substring(0, value.length() - 1));
            return amount <= 0 ? -1 : amount * multiplier;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String describeSeconds(long seconds) {
        if (seconds == Long.MAX_VALUE) {
            return "permanent";
        }
        if (seconds <= 0) {
            return "none";
        }
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + secs + "s";
        }
        return secs + "s";
    }
}
