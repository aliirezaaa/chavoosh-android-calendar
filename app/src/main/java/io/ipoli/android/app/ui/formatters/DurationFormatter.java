package io.ipoli.android.app.ui.formatters;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/28/16.
 */
public class DurationFormatter {

    public static String format(Context context, int duration) {
        if (duration < 0) {
            return formatEmptyDuration();
        }
        long hours = 0;
        long mins = 0;
        if (duration > 0) {
            hours = TimeUnit.MINUTES.toHours(duration);
            mins = duration - hours * 60;
        }
        if (hours > 0 && mins == 0) {
            return context.getString(R.string.quest_item_hours_duration, hours);
        } else if (hours > 0) {
            return context.getString(R.string.quest_item_full_duration, hours, mins);
        } else {
            return context.getString(R.string.quest_item_minutes_duration, mins);
        }
    }

    public static String formatReadable(int duration) {
        if (duration < 0) {
            return formatEmptyDuration();
        }
        if (duration <= Constants.QUEST_MIN_DURATION) {
            return Constants.QUEST_MIN_DURATION + " دقیقه یا کمتر";
        }
        long hours = TimeUnit.MINUTES.toHours(duration);
        long mins = duration - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return hours + "ساعت و " + mins + " دقیقه";
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? "1 ساعت" : hours + " ساعت";
        }

        return mins == 1 ? "1 دقیقه" : mins + " دقیقه";
    }

    public static String formatReadableShort(int duration) {
        if (duration < 0) {
            return "";
        }
        if (duration <= Constants.QUEST_MIN_DURATION) {
            return Constants.QUEST_MIN_DURATION + " دقیقه یا کمتر";
        }
        return doFormatShort(duration, "و");
    }

    public static String formatShort(int duration, String separator) {
        if (duration < 0) {
            return "";
        }
        return doFormatShort(duration, separator);
    }

    public static String formatShort(int duration) {
        return formatShort(duration, "و");
    }

    @NonNull
    private static String doFormatShort(int duration, String separator) {
        long hours = TimeUnit.MINUTES.toHours(duration);
        long mins = duration - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return hours + "ساعت " + separator + " " + mins + "دقیقه";
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? "1 ساعت" : hours + " ساعت";
        }

        return mins + " دقیقه";
    }



    private static String formatEmptyDuration() {
        return "معلوم نیست";
    }
}
