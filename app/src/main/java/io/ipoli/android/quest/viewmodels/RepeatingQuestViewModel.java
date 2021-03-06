package io.ipoli.android.quest.viewmodels;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import org.threeten.bp.LocalDate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.ipoli.android.MainActivity;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.persian.calendar.DateConverter;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.PeriodHistory;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/9/16.
 */
public class RepeatingQuestViewModel {

    private final RepeatingQuest repeatingQuest;
    private final LocalDate nextDate;
    private final int scheduledCount;
    private final int completedCount;
    private final int remainingScheduledCount;

    public RepeatingQuestViewModel(RepeatingQuest repeatingQuest) {
        this.repeatingQuest = repeatingQuest;
        nextDate = repeatingQuest.getNextScheduledDate(LocalDate.now());
        List<PeriodHistory> periodHistories = repeatingQuest.getPeriodHistories(LocalDate.now());
        PeriodHistory currentPeriodHistory = periodHistories.get(periodHistories.size() - 1);
        scheduledCount = currentPeriodHistory.getScheduledCount();
        completedCount = currentPeriodHistory.getCompletedCount();
        remainingScheduledCount = currentPeriodHistory.getRemainingScheduledCount();
    }

    public String getName() {
        return repeatingQuest.getName();
    }

    @ColorRes
    public int getCategoryColor() {
        return getQuestCategory().color500;
    }

    @DrawableRes
    public int getCategoryImage() {
        return getQuestCategory().colorfulImage;
    }

    private Category getQuestCategory() {
        return repeatingQuest.getCategoryType();
    }

    public RepeatingQuest getRepeatingQuest() {
        return repeatingQuest;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public String getNextText() {
        String nextText = "";
        if (nextDate == null) {
            nextText += "بدون برنامه";
        } else {
            if (DateUtils.isTodayUTC(nextDate)) {
                nextText = "امروز";
            } else if (DateUtils.isTomorrowUTC(nextDate)) {
                nextText = "فردا";
            } else {
//                nextText = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(DateUtils.toStartOfDay(nextDate));
                nextText =getDueDateText(nextDate);
            }
        }

        nextText += " ";

        int duration = repeatingQuest.getDuration();
        Time startTime = repeatingQuest.getStartTime();
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            nextText += startTime + " - " + endTime;
        } else if (duration > 0) {
            nextText += "برای " + DurationFormatter.formatReadable(duration);
        } else if (startTime != null) {
            nextText += startTime;
        }
        return "بعدی: " + nextText;
    }
    public String getDueDateText(LocalDate currentDate) {

//        return DateFormatter.formatWithoutYear(quest.getScheduledDate(), currentDate);
        PersianDate p= DateConverter.localToPersianDate(currentDate);
        return p.getDayOfMonth()+" "+ Utils.getInstance(MainActivity.getContext()).getMonthName(p);
    }
    public long getScheduledCount() {
        return scheduledCount;
    }

    public String getRepeatText() {

        int remainingCount = getRemainingScheduledCount();

        if (remainingCount <= 0) {
            return "انجام شده";
        }

        Recurrence recurrence = repeatingQuest.getRecurrence();
        if (recurrence.getRecurrenceType() == Recurrence.RepeatType.MONTHLY) {
            return remainingCount + " بار دیگر در این ماه";
        }

        return remainingCount + "  بار دیگر در این هفته";

    }

    public int getRemainingScheduledCount() {
        return remainingScheduledCount;
    }


}