package com.joshrap.liteweight.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    public static final String ZULU_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static String getFormattedLocalDateTime(String utcDateTime) {
        String formattedDateTime = null;
        DateFormat dateFormatInput = new SimpleDateFormat(ZULU_TIME_FORMAT, Locale.ENGLISH);
        dateFormatInput.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = dateFormatInput.parse(utcDateTime);

            // format date to the user's local timezone
            DateFormat dateFormatOutput = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
            dateFormatOutput.setTimeZone(TimeZone.getDefault());
            formattedDateTime = dateFormatOutput.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDateTime;
    }


    /**
     * @param time time in milliseconds
     * @return Returns a string in the format MM:SS
     */
    public static String getClockDisplay(long time) {
        int minutes = (int) (time / 60000);
        int seconds = (int) (time / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
