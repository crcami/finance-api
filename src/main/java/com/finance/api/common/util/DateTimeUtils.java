package com.finance.api.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static Instant nowUtc() {
        return Instant.now();
    }

    public static LocalDate todayUtc() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    public static Instant atStartOfDayUtc(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public static LocalDate toLocalDateUtc(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
