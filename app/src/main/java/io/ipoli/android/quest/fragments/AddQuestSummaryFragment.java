package io.ipoli.android.quest.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Range;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.ibm.icu.util.LocaleData;
import com.squareup.otto.Bus;
import com.wooplr.spotlight.target.ViewTarget;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.tutorial.InteractiveTutorial;
import io.ipoli.android.app.ui.dialogs.TextPickerFragment;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.ui.formatters.FrequencyTextFormatter;
import io.ipoli.android.app.ui.formatters.PriorityFormatter;
import io.ipoli.android.app.ui.formatters.ReminderTimeFormatter;
import io.ipoli.android.app.ui.formatters.TimesADayFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.persian.calendar.CivilDate;
import io.ipoli.android.persian.calendar.DateConverter;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.activities.AddRepeatingQuestActivity;
import io.ipoli.android.quest.adapters.EditQuestSubQuestListAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.events.ChangeQuestDateRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestNameRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestPriorityRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestRecurrenceRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestTimeRequestEvent;
import io.ipoli.android.quest.events.NameAndCategoryPickedEvent;
import io.ipoli.android.quest.events.NewQuestChallengePickedEvent;
import io.ipoli.android.quest.events.NewQuestDurationPickedEvent;
import io.ipoli.android.quest.events.NewQuestNotePickedEvent;
import io.ipoli.android.quest.events.NewQuestRemindersPickedEvent;
import io.ipoli.android.quest.events.NewQuestSubQuestsPickedEvent;
import io.ipoli.android.quest.events.NewQuestTimesADayPickedEvent;
import io.ipoli.android.quest.events.RepeatingQuestSummaryStart;
import io.ipoli.android.quest.events.SummaryFragmentStart;
import io.ipoli.android.quest.ui.AddSubQuestView;
import io.ipoli.android.quest.ui.dialogs.ChallengePickerFragment;
import io.ipoli.android.quest.ui.dialogs.DurationPickerFragment;
import io.ipoli.android.quest.ui.dialogs.EditReminderFragment;
import io.ipoli.android.quest.ui.dialogs.TimesADayPickerFragment;
import io.ipoli.android.reminder.ReminderMinutesParser;
import io.ipoli.android.reminder.TimeOffsetType;
import io.ipoli.android.reminder.data.Reminder;

import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */
public class AddQuestSummaryFragment extends BaseFragment {

    @Inject
    Bus eventBus;
    @Inject
    InteractiveTutorial interactiveTutorial;

    private Unbinder unbinder;

    @BindView(R.id.add_quest_summary_name)
    TextView name;

    @BindView(R.id.add_quest_reminders_container)
    ViewGroup questRemindersContainer;

    @BindView(R.id.add_quest_summary_date_container)
    ViewGroup dateContainer;

    @BindView(R.id.add_quest_summary_recurrence_container)
    ViewGroup recurrenceContainer;

    @BindView(R.id.add_quest_summary_times_a_day_container)
    ViewGroup timesADayContainer;

    @BindView(R.id.add_quest_summary_times_a_day_horizontal_line)
    View timesADayHorizontalLine;

    @BindView(R.id.add_quest_summary_recurrence)
    TextView recurrenceText;

    @BindView(R.id.add_quest_summary_times_a_day)
    TextView timesADayText;

    @BindView(R.id.sub_quests_container)
    ViewGroup subQuestsContainer;

    @BindView(R.id.sub_quests_list)
    RecyclerView subQuestsList;

    @BindView(R.id.add_sub_quest_container)
    AddSubQuestView addSubQuestView;

    @BindView(R.id.add_sub_quest_clear)
    ImageButton clearAddSubQuest;

    @BindView(R.id.add_quest_summary_challenge)
    TextView challengeText;

    @BindView(R.id.add_quest_summary_date)
    TextView scheduledDate;

    @BindView(R.id.add_quest_summary_time)
    TextView startTime;

    @BindView(R.id.add_quest_summary_duration)
    TextView durationText;

    @BindView(R.id.add_quest_summary_priority)
    TextView priorityText;

    @BindView(R.id.add_quest_summary_note)
    TextView noteText;

    @Inject
    LocalStorage localStorage;

    private EditQuestSubQuestListAdapter subQuestListAdapter;
    private boolean use24HourFormat;
    private boolean shouldShow = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_summary, container, false);
        unbinder = ButterKnife.bind(this, view);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        use24HourFormat = shouldUse24HourFormat();
        initSubQuestsUI();
        if (shouldShow) {
            if (getActivity().getClass().toString().contains("AddQuestActivity")) {
                postEvent(new SummaryFragmentStart("بدون نام", Category.WORK));
                noteText.post(() -> {
                    if(localStorage.readBool("add_quest",true)){
                        createAndShowTutorials(view);
                        localStorage.saveBool("add_quest",false);
                    }

                });
            } else {
                postEvent(new RepeatingQuestSummaryStart("بدون نام", Category.WORK));
                noteText.post(() -> {
                    if(localStorage.readBool("add_repeat_quest",true)){
                        createAndShowTutorialsForSummary(view);
                        localStorage.saveBool("add_repeat_quest",false);
                    }

                });
            }
            shouldShow = false;
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
//        shouldShow=true;
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_quest_wizard_summary_menu, menu);
    }


    @OnClick(R.id.add_quest_summary_reminders)
    public void onRemindersClicked(View view) {
        EditReminderFragment f = EditReminderFragment.newInstance((reminder, editMode) -> {
            if (reminder != null) {
                addReminder(reminder);
            }
        });
        f.show(getFragmentManager());
    }

    private void addReminder(Reminder reminder) {
        if (reminderWithSameTimeExists(reminder)) {
            return;
        }

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.add_quest_reminder_item, questRemindersContainer, false);
        populateReminder(reminder, v);
        questRemindersContainer.addView(v);
        postEvent(new NewQuestRemindersPickedEvent(getReminders()));

        v.setOnClickListener(view -> {
            EditReminderFragment f = EditReminderFragment.newInstance((Reminder) v.getTag(), (editedReminder, mode) -> {
                if (editedReminder == null || reminderWithSameTimeExists(editedReminder)) {
                    questRemindersContainer.removeView(v);
                    postEvent(new NewQuestRemindersPickedEvent(getReminders()));
                    return;
                }
                populateReminder(editedReminder, v);
                postEvent(new NewQuestRemindersPickedEvent(getReminders()));
            });
            f.show(getFragmentManager());
        });
    }

    private void clearReminders() {
        questRemindersContainer.removeAllViews();
    }

    private boolean reminderWithSameTimeExists(Reminder reminder) {
        for (Reminder r : getReminders()) {
            if (!reminder.getNotificationId().equals(r.getNotificationId())
                    && reminder.getMinutesFromStart() == r.getMinutesFromStart()) {
                return true;
            }
        }
        return false;
    }

    private void populateReminder(Reminder reminder, View reminderView) {
        String text = "";
        Pair<Long, TimeOffsetType> parsedResult = ReminderMinutesParser.parseCustomMinutes(Math.abs(reminder.getMinutesFromStart()));
        if (parsedResult != null) {
            text = ReminderTimeFormatter.formatTimeOffset(parsedResult.first, parsedResult.second);
        }
        ((TextView) reminderView.findViewById(R.id.reminder_text)).setText(text);
        reminderView.setTag(reminder);
    }

    private List<Reminder> getReminders() {
        List<Reminder> reminders = new ArrayList<>();
        for (int i = 0; i < questRemindersContainer.getChildCount(); i++) {
            reminders.add((Reminder) questRemindersContainer.getChildAt(i).getTag());
        }
        return reminders;
    }

    private void initSubQuestsUI() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        subQuestsList.setLayoutManager(layoutManager);

        subQuestListAdapter = new EditQuestSubQuestListAdapter(getActivity(), eventBus, new ArrayList<>(), R.layout.add_quest_sub_quest_list_item);
        subQuestsList.setAdapter(subQuestListAdapter);
        subQuestListAdapter.setItemChangeListener(() ->
                postEvent(new NewQuestSubQuestsPickedEvent(subQuestListAdapter.getSubQuests())));

        addSubQuestView.setSubQuestAddedListener(this::addSubQuest);
        addSubQuestView.setOnClosedListener(() -> addSubQuestView.setVisibility(View.GONE));
    }

    @OnClick(R.id.add_quest_summary_name)
    public void onNameClicked(View v) {
        postEvent(new ChangeQuestNameRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_recurrence_container)
    public void onRecurrenceClicked(View v) {
        postEvent(new ChangeQuestRecurrenceRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_date_container)
    public void onDateClicked(View v) {
        postEvent(new ChangeQuestDateRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_time_container)
    public void onTimeClicked(View v) {
        postEvent(new ChangeQuestTimeRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_duration_container)
    public void onDurationClicked(View v) {
        DurationPickerFragment fragment = DurationPickerFragment.newInstance((int) durationText.getTag(), duration -> {
            postEvent(new NewQuestDurationPickedEvent(duration));
            Log.i("addQ", duration + "");
            showDuration(duration);
        });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.add_quest_summary_priority_container)
    public void onPriorityClicked(View v) {
        postEvent(new ChangeQuestPriorityRequestEvent());
    }

    @OnClick(R.id.add_quest_summary_times_a_day_container)
    public void onTimesADayClicked(View v) {
        TimesADayPickerFragment fragment = TimesADayPickerFragment.newInstance((int) timesADayText.getTag(), timesADay -> {
            postEvent(new NewQuestTimesADayPickedEvent(timesADay));
            showTimesADay(timesADay);
        });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.add_quest_summary_challenge_container)
    public void onChallengeClicked(View v) {
        ChallengePickerFragment fragment = ChallengePickerFragment.newInstance(challenge -> {
            postEvent(new NewQuestChallengePickedEvent(challenge));
            if (challenge != null) {
                challengeText.setText(challenge.getName());
            } else {
                challengeText.setText(R.string.set_challenge);
            }
        });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.add_quest_summary_note_container)
    public void onNoteClicked(View v) {
        TextPickerFragment fragment = TextPickerFragment.newInstance((String) noteText.getTag(), R.string.pick_note_title, text -> {
            postEvent(new NewQuestNotePickedEvent(text));
            noteText.setTag(text);
            if (StringUtils.isEmpty(text)) {
                noteText.setText(R.string.note);
            } else {
                noteText.setText(text);
            }
        });
        fragment.show(getFragmentManager());
    }

    @OnClick(R.id.sub_quests_container)
    public void onAddSubQuestClicked(View v) {
        addSubQuestView.setVisibility(View.VISIBLE);
        KeyboardUtils.showKeyboard(getContext());
        addSubQuestView.setInEditMode();
    }

    private void addSubQuest(String name) {
        Log.i("name of sub summary", name);
        if (StringUtils.isEmpty(name)) {
            return;
        }

        SubQuest sq = new SubQuest(name);
        subQuestListAdapter.addSubQuest(sq);
    }

    public void setQuest(Quest quest) {
        name.setText(quest.getName());
        showScheduledDate(quest);
        showStartTime(quest.getStartTime(), quest.getStartTimePreference());
        showDuration(quest.getDuration());
        showPriority(quest.getPriority());
        showReminders(quest.getReminders());
        if (quest.getStartTime() == null) {
            showTimesADay(quest.getTimesADay());
        }
    }

    public void setRepeatingQuest(RepeatingQuest repeatingQuest) {
        name.setText(repeatingQuest.getName());
        showRecurrence(repeatingQuest);
        showStartTime(repeatingQuest.getStartTime(), repeatingQuest.getStartTimePreference());
        showDuration(repeatingQuest.getDuration());
        showPriority(repeatingQuest.getPriority());
        showReminders(repeatingQuest.getReminders());
        if (repeatingQuest.getStartTime() == null) {
            showTimesADay(repeatingQuest.getTimesADay());
        }
    }

    private void showRecurrence(RepeatingQuest repeatingQuest) {
        recurrenceContainer.setVisibility(View.VISIBLE);
        dateContainer.setVisibility(View.GONE);
        recurrenceText.setText(FrequencyTextFormatter.formatInterval(repeatingQuest.getFrequency(), repeatingQuest.getRecurrence()));
    }

    private void showReminders(List<Reminder> reminders) {
        clearReminders();
        for (Reminder reminder : reminders) {
            addReminder(reminder);
        }
    }

    private void showDuration(int duration) {
//        durationText.setText("به مدت " + DurationFormatter.formatReadable(duration));
//        int[] availableDurations = Constants.DURATIONS;
//        if (duration == Constants.QUEST_MIN_DURATION) {
        durationText.setText("به مدت " + DurationFormatter.formatReadable(duration));
        durationText.setTag(duration);
//        } else {
//            durationText.setText("به مدت " +DurationFormatter.formatReadable(availableDurations[duration]));
//            durationText.setTag(availableDurations[duration]);
//        }


    }

    private void showTimesADay(int timesADay) {
        timesADayContainer.setVisibility(View.VISIBLE);
        timesADayHorizontalLine.setVisibility(View.VISIBLE);
        timesADayText.setText(TimesADayFormatter.formatReadable(timesADay, "در روز"));
        timesADayText.setTag(timesADay);
    }

    private void showPriority(int priority) {
        priorityText.setText(PriorityFormatter.format(getContext(), priority));
    }

    private void showStartTime(Time questStartTime, TimePreference startTimePreference) {
        if (questStartTime != null) {
            startTime.setText("در " + questStartTime.toString(use24HourFormat));
        } else {
            switch (startTimePreference) {
                case PERSONAL_HOURS:
                case WORK_HOURS:
                case EVENING:
                case MORNING:
                case AFTERNOON:
                    startTime.setText(StringUtils.capitalizeAndReplaceUnderscore(startTimePreference.name()));
//                    startTime.setText(StringUtils.getPersianTranslate(startTimePreference));
                    break;
                default:
                    startTime.setText(R.string.at_any_reasonable_time);
            }
        }

    }

    private void showScheduledDate(Quest quest) {
        dateContainer.setVisibility(View.VISIBLE);
        recurrenceContainer.setVisibility(View.GONE);
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (Objects.equals(quest.getStart(), quest.getEnd())) {
////                scheduledDate.setText(DateFormatter.formatWithoutYear(quest.getEndDate()));
//                scheduledDate.setText(getPersianDateFromQuest(quest));
//
//            }
//        } else
        if (quest.getStart() == null || quest.getEnd() == null) {
            scheduledDate.setText("مشخص نیست");
            return;
        }
        if (quest.getStart().equals(quest.getEnd())) {
//            scheduledDate.setText(DateFormatter.formatWithoutYear(quest.getEndDate()));
            scheduledDate.setText(getPersianDateFromQuest(quest));

        } else {
            LocalDate byDate = quest.getEndDate();
            Log.i("byDate", byDate.toString());
            LocalDate today = LocalDate.now();
//            Range<Integer> endOfMonth=Range.create(20,31);
            int endOfMonth = byDate.getDayOfMonth();
            if (byDate.equals(today.with(DayOfWeek.FRIDAY))) {
                scheduledDate.setText(R.string.by_end_of_week);

            } else if (endOfMonth > 20 && endOfMonth < 32) {
                scheduledDate.setText(R.string.by_end_of_month);
            } else {
//                String dayNumberSuffix = DateUtils.getDayNumberSuffix(byDate.getDayOfMonth());
//                DateFormat dateFormat = new SimpleDateFormat(getString(R.string.agenda_daily_journey_format, dayNumberSuffix));
//                scheduledDate.setText(getString(R.string.add_quest_by_date, dateFormat.format(DateUtils.toStartOfDay(byDate))));

                scheduledDate.setText(getPersianDateFromQuest(quest));
                Log.i("add quest summary", getPersianDateFromQuest(quest));
            }
        }
    }

    public String getPersianDateFromQuest(Quest quest) {
        CivilDate cDate = new CivilDate(quest.getEndDate().getYear(), quest.getEndDate().getMonthValue(), quest.getEndDate().getDayOfMonth());
        PersianDate pDate = DateConverter.civilToPersian(cDate);
        return Utils.getInstance(getContext()).dateToString(pDate);
    }

    private void createAndShowTutorials(View view) {
        List<TapTarget> targets = new ArrayList<>();
        targets.add(interactiveTutorial.createTutorialForView(
                view.findViewById(R.id.add_quest_summary_name),
                getActivity(),
                "نام فعالیت کاری",
                "نام کاری که میخواهید انجام دهید چیست؟"));

        ViewTarget target = new ViewTarget(scheduledDate);
        targets.add(interactiveTutorial.createTutorialForRect(
                target.getRect(),
                getActivity(),
                "تاریخ فعالیت کاری",
                "در چه تاریخی قصد انجام کار خود را دارید؟"));
        ViewTarget time_target = new ViewTarget(startTime);
        targets.add(interactiveTutorial.createTutorialForRect(
                time_target.getRect(),
                getActivity(),
                "ساعت فعالیت کاری",
                "در چه ساعتی از روز قصد انجام کار خود را دارید؟"));
        targets.add(interactiveTutorial.createTutorialForView(
                view.findViewById(R.id.add_quest_summary_duration),
                getActivity(),
                "طول مدت کار",
                "کار مد نظر شما در چه مدتی انجام می شود؟"));
//        ViewTarget target = new ViewTarget(monthViewPager);
        targets.add(interactiveTutorial.createTutorialForView(
                view.findViewById(R.id.add_quest_summary_priority),
                getActivity(),
                "اهمیت کار",
                "اهمیت انجام این کار برای شما چقدر است؟"));
        Toolbar toolbar=((AddQuestActivity)getActivity()).getToolbar();
        targets.add(interactiveTutorial.createTutorialForMenuItem(
                toolbar,
                R.id.action_save,
                getActivity(),
                "ذخیره کار",
                "در پایان ، پس از انجام تغییرات لازم ، کار خود را ذخیره کنید"));



        interactiveTutorial.showTutorials(targets, getActivity(), "add_quest_summary");
    }

    private void createAndShowTutorialsForSummary(View view) {
        List<TapTarget> targets = new ArrayList<>();
        targets.add(interactiveTutorial.createTutorialForView(
                view.findViewById(R.id.add_quest_summary_name),
                getActivity(),
                "نام فعالیت کاری",
                "نام کاری که میخواهید انجام دهید چیست؟"));

        ViewTarget target = new ViewTarget(recurrenceText);
        targets.add(interactiveTutorial.createTutorialForRect(
                target.getRect(),
                getActivity(),
                "تعداد تکرار",
                "چند بار میخواهید این کار را انجام دهید؟"));
        ViewTarget time_target = new ViewTarget(startTime);
        targets.add(interactiveTutorial.createTutorialForRect(
                time_target.getRect(),
                getActivity(),
                "ساعت فعالیت کاری",
                "در چه ساعتی از روز قصد انجام کار خود را دارید؟"));
        targets.add(interactiveTutorial.createTutorialForView(
                view.findViewById(R.id.add_quest_summary_duration),
                getActivity(),
                "طول مدت کار",
                "کار مد نظر شما در چه مدتی انجام می شود؟"));
        targets.add(interactiveTutorial.createTutorialForView(
                view.findViewById(R.id.add_quest_summary_priority),
                getActivity(),
                "اهمیت کار",
                "اهمیت انجام این کار برای شما چقدر است؟"));
//        ViewTarget target = new ViewTarget(monthViewPager);
        Toolbar toolbar=((AddRepeatingQuestActivity)getActivity()).getToolbar();
        targets.add(interactiveTutorial.createTutorialForMenuItem(
                toolbar,
                R.id.action_save,
                getActivity(),
                "ذخیره کار",
                "در پایان ، پس از انجام تغییرات لازم ، کار خود را ذخیره کنید"));


        interactiveTutorial.showTutorials(targets, getActivity(), "add_quest_summary");
    }

}
