package ma.ensaf.veryempty.utils

import android.text.format.DateFormat
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    @JvmStatic
    fun parseDateTime(dateString: String, originalFormat: String?, outputFromat: String?): String {
        val formatter = SimpleDateFormat(originalFormat, Locale.getDefault())
        val date: Date
        return try {
            date = formatter.parse(dateString)!!
            val dateFormat = SimpleDateFormat(outputFromat, Locale.getDefault())
            dateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            dateString
        }
    }

    // calculate time ago from given date
    @JvmStatic
    fun timeAgoTimeDiff(parsedDate: Date): String {
        val SECOND_MILLIS = 1000
        val MINUTE_MILLIS = 60 * SECOND_MILLIS
        val HOUR_MILLIS = 60 * MINUTE_MILLIS
        val DAY_MILLIS = 24 * HOUR_MILLIS
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        var time = parsedDate.time
        if (time < 1000000000000L) {
            time *= 1000
        }
        val now = currentDate.time
        if (time > now || time <= 0) {
            Log.e("Dated", parsedDate.toString())
            return "in the future"
        }
        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            //return "moments ago";
            DateFormat.format("dd-MMM-yyyy", parsedDate).toString()
        } else if (diff < 2 * MINUTE_MILLIS) {
            //return "a minute ago";
            DateFormat.format("dd-MMM-yyyy", parsedDate).toString()
        } else if (diff < 60 * MINUTE_MILLIS) {
            //return diff / MINUTE_MILLIS + " minutes ago";
            DateFormat.format("dd-MMM-yyyy", parsedDate).toString()
        } else if (diff < 2 * HOUR_MILLIS) {
            //return "an hour ago";
            DateFormat.format("dd-MMM-yyyy", parsedDate).toString()
        } else if (diff < 24 * HOUR_MILLIS) {
            //return diff / HOUR_MILLIS + " hours ago";
            DateFormat.format("dd-MMM-yyyy", parsedDate).toString()
        } else if (diff < 48 * HOUR_MILLIS) {
            "yesterday"
        } else {
            if (diff / DAY_MILLIS <= 6) {
                "${diff / DAY_MILLIS} days ago"
            } else {
                DateFormat.format("dd-MMM-yyyy", parsedDate).toString()
            }
        }
    }
}