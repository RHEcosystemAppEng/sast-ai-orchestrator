package com.redhat.sast.api.enums;

public enum TimePeriod {
    ONE_HOUR("1h", 3600, 900, 4), // 1 hour: 15-min intervals, 4 points
    SIX_HOURS("6h", 21600, 900, 24), // 6 hours: 15-min intervals, 24 points
    TWELVE_HOURS("12h", 43200, 900, 48), // 12 hours: 15-min intervals, 48 points
    TWENTY_FOUR_HOURS("24h", 86400, 3600, 24), // 24 hours: 1-hour intervals, 24 points
    SEVEN_DAYS("7d", 604800, 21600, 28), // 7 days: 6-hour intervals, 28 points
    THIRTY_DAYS("30d", 2592000, 86400, 30); // 30 days: 1-day intervals, 30 points

    private final String code;
    private final long totalSeconds;
    private final long intervalSeconds;
    private final int dataPoints;

    TimePeriod(String code, long totalSeconds, long intervalSeconds, int dataPoints) {
        this.code = code;
        this.totalSeconds = totalSeconds;
        this.intervalSeconds = intervalSeconds;
        this.dataPoints = dataPoints;
    }

    public String getCode() {
        return code;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public int getDataPoints() {
        return dataPoints;
    }

    public static TimePeriod fromCode(String code) {
        for (TimePeriod period : TimePeriod.values()) {
            if (period.code.equalsIgnoreCase(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException(
                "Invalid time period: " + code + ". Valid values are: 1h, 6h, 12h, 24h, 7d, 30d");
    }
}
