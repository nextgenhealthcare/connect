package com.mirth.connect.util;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DurationUtil {
    
    private static PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().appendSuffix("d").appendHours().appendSuffix("h").appendMinutes().appendSuffix("m").appendSeconds().appendSuffix("s").toFormatter();
    
    public static long parseMillisFromString(String duration) {
        return formatter.parsePeriod(duration).toStandardDuration().getMillis();
    }
}
