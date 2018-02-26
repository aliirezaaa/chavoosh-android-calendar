package io.ipoli.android.app.ui.formatters;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Recurrence;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/16.
 */
public class FrequencyTextFormatter {
    public static String formatReadable(Recurrence recurrence) {
        if (recurrence == null) {
            return "بدون تکرار";
        }
        return StringUtils.capitalize(recurrence.getRecurrenceType().name());
    }

    public static String formatInterval(int frequency, Recurrence recurrence) {
        String frequencyText = frequency == 1 ? "یکبار " : frequency + " بار ";
        if (recurrence.getRecurrenceType() == Recurrence.RepeatType.MONTHLY) {
            return frequencyText + "در ماه";
        }
        return frequencyText + "در هفته";
    }
}
