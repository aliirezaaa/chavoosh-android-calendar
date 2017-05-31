package io.ipoli.android.app.ui.formatters;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/15/16.
 */
public class TimesADayFormatter {

    public static String formatReadable(int value) {
        if(value <= 0) {
            value = 1;
        }
        if(value == 1) {
            return "یکبار";
        } else if(value == 2) {
            return "دو بار";
        }
        return value + " بار";
    }

    public static String formatReadable(int value, String suffix) {
        if(value <= 0) {
            value = 1;
        }
        if(value == 1) {
            return "یکبار " + suffix;
        } else if(value == 2) {
            return "دو بار " + suffix;
        }

        return value + " بار " + suffix;
    }

}
