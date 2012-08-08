package com.fbudassi.neddy.util;

import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class to format dates in the HTTP/1.1 way. Dates are formated
 * according to RFC 1123.
 *
 * @author fbudassi
 */
public class DateUtil {

    private static final DateTimeFormatter RFC_1123_FORMAT;
    private static final String RFC_1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

    static {
        RFC_1123_FORMAT = DateTimeFormat.forPattern(RFC_1123_PATTERN).withLocale(Locale.US).withZoneUTC();
    }

    /**
     * Returns the current date/time formatted according to RFC 1123.
     *
     * @return
     */
    public static String getCurrent() {
        return RFC_1123_FORMAT.print(DateTime.now());
    }

    /**
     * Returns the date passed formatted according to RFC 1123.
     *
     * @param date
     * @return
     */
    public static String formatDate(long date) {
        return RFC_1123_FORMAT.print(date);
    }
}
