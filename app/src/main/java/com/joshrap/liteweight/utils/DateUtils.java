package com.joshrap.liteweight.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    public static String getFormattedLocalDateTime(String utcDateTime) {
        String formattedDateTime = null;
        DateFormat dateFormatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
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
}
