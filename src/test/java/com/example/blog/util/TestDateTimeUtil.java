package com.example.blog.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class TestDateTimeUtil {

    public static OffsetDateTime of(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second
    ) {
        var ldt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
        var zoneId = ZoneId.of("Asia/Tokyo");
        return ZonedDateTime.of(ldt, zoneId).toOffsetDateTime();
    }

}
