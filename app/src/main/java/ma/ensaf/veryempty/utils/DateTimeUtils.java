package ma.ensaf.veryempty.utils;


import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String parseDateTime(String dateString, String originalFormat, String outputFromat){

        SimpleDateFormat formatter = new SimpleDateFormat(originalFormat, Locale.getDefault());
        Date date = null;
        try {
            date = formatter.parse(dateString);

            SimpleDateFormat dateFormat=new SimpleDateFormat(outputFromat, Locale.getDefault());

            return dateFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    // calculate time ago from given date
    public static String timeAgoTimeDiff(Date parsedDate) {

        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;


        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        long time = parsedDate.getTime();
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = currentDate.getTime();
        if (time > now || time <= 0) {
            Log.e("Dated",parsedDate.toString());
            return "in the future";
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            //return "moments ago";
            return DateFormat.format("dd-MMM-yyyy", parsedDate).toString();
        } else if (diff < 2 * MINUTE_MILLIS) {
            //return "a minute ago";
            return DateFormat.format("dd-MMM-yyyy", parsedDate).toString();
        } else if (diff < 60 * MINUTE_MILLIS) {
            //return diff / MINUTE_MILLIS + " minutes ago";
            return DateFormat.format("dd-MMM-yyyy", parsedDate).toString();
        } else if (diff < 2 * HOUR_MILLIS) {
            //return "an hour ago";
            return DateFormat.format("dd-MMM-yyyy", parsedDate).toString();
        } else if (diff < 24 * HOUR_MILLIS) {
            //return diff / HOUR_MILLIS + " hours ago";
            return DateFormat.format("dd-MMM-yyyy", parsedDate).toString();
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            if(diff / DAY_MILLIS <=6){
                return diff / DAY_MILLIS + " days ago";
            }else{
                return DateFormat.format("dd-MMM-yyyy", parsedDate).toString();
            }
        }
    }
}

