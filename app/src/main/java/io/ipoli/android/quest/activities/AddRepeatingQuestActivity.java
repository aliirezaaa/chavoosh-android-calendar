package io.ipoli.android.quest.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import net.fortuna.ical4j.model.Recur;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.note.data.Note;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CategoryChangedEvent;
import io.ipoli.android.quest.events.ChangeQuestNameRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestPriorityRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestRecurrenceRequestEvent;
import io.ipoli.android.quest.events.ChangeQuestTimeRequestEvent;
import io.ipoli.android.quest.events.NameAndCategoryPickedEvent;
import io.ipoli.android.quest.events.NewQuestChallengePickedEvent;
import io.ipoli.android.quest.events.NewQuestDurationPickedEvent;
import io.ipoli.android.quest.events.NewQuestNotePickedEvent;
import io.ipoli.android.quest.events.NewQuestPriorityPickedEvent;
import io.ipoli.android.quest.events.NewQuestRemindersPickedEvent;
import io.ipoli.android.quest.events.NewQuestSubQuestsPickedEvent;
import io.ipoli.android.quest.events.NewQuestTimePickedEvent;
import io.ipoli.android.quest.events.NewQuestTimesADayPickedEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestRecurrencePickedEvent;
import io.ipoli.android.quest.events.RepeatingQuestSummaryStart;
import io.ipoli.android.quest.events.SummaryFragmentStart;
import io.ipoli.android.quest.fragments.AddNameFragment;
import io.ipoli.android.quest.fragments.AddQuestPriorityFragment;
import io.ipoli.android.quest.fragments.AddQuestSummaryFragment;
import io.ipoli.android.quest.fragments.AddQuestTimeFragment;
import io.ipoli.android.quest.fragments.AddRepeatingQuestRecurrenceFragment;
import io.ipoli.android.reminder.data.Reminder;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddRepeatingQuestActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final int REPEATING_QUEST_NAME_FRAGMENT_INDEX = 1;
    public static final int REPEATING_QUEST_RECURRENCE_FRAGMENT_INDEX = 2;
    public static final int REPEATING_QUEST_TIME_FRAGMENT_INDEX = 3;
    public static final int REPEATING_QUEST_PRIORITY_FRAGMENT_INDEX = 4;
    private static final int REPEATING_QUEST_SUMMARY_FRAGMENT_INDEX = 0;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.wizard_pager)
    ViewPager fragmentPager;

    private RepeatingQuest repeatingQuest;

    private AddRepeatingQuestRecurrenceFragment recurrenceFragment;
    private AddQuestTimeFragment timeFragment;
    private AddQuestSummaryFragment summaryFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_quest);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
        }

        WizardFragmentPagerAdapter adapterViewPager = new WizardFragmentPagerAdapter(getSupportFragmentManager());
        fragmentPager.setAdapter(adapterViewPager);
        fragmentPager.addOnPageChangeListener(this);
        setTitle(R.string.title_fragment_wizard_repeating_quest_name);
    }

    @Override
    protected boolean useParentOptionsMenu() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragmentPager.getCurrentItem() == REPEATING_QUEST_SUMMARY_FRAGMENT_INDEX) {
                    finish();
                } else {
                    fragmentPager.setCurrentItem(0);
                }

                return true;

            case R.id.action_save:
                saveRepeatingQuest();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveRepeatingQuest() {
        KeyboardUtils.hideKeyboard(this);
        eventBus.post(new NewRepeatingQuestEvent(repeatingQuest, EventSource.ADD_REPEATING_QUEST));
        Toast.makeText(this, R.string.repeating_quest_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onNewQuestCategoryChanged(CategoryChangedEvent e) {
        colorLayout(e.category);
    }

    @Subscribe
    public void onNewQuestNameAndCategoryPicked(NameAndCategoryPickedEvent e) {
//        repeatingQuest = new RepeatingQuest(e.name);
        repeatingQuest. setName(e.name);
        repeatingQuest.addReminder(new Reminder(0));
        repeatingQuest.setCategoryType(e.category);
        KeyboardUtils.hideKeyboard(this);
        goToNextPage();
    }
    @Subscribe
    public void onRepeatingQuestSummaryStart(RepeatingQuestSummaryStart e) {
        Log.i("ev","start");
        repeatingQuest = new RepeatingQuest(e.name);
        repeatingQuest. setName(e.name);
        repeatingQuest.addReminder(new Reminder(0));
        repeatingQuest.setCategoryType(e.category);
        KeyboardUtils.hideKeyboard(this);
        Recurrence recurrence = Recurrence.create();
        Recur recur = new Recur(Recur.WEEKLY, null);
        recurrence.setRrule(recur.toString());
        recurrence.setRecurrenceType(Recurrence.RepeatType.WEEKLY);
        recurrence.setFlexibleCount(1);
        repeatingQuest.setRecurrence(recurrence);
        summaryFragment.setRepeatingQuest(repeatingQuest);

    }
    @Subscribe
    public void onNewRepeatingQuestRecurrencePicked(NewRepeatingQuestRecurrencePickedEvent e) {
        repeatingQuest.setRecurrence(e.recurrence);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestTimePicked(NewQuestTimePickedEvent e) {
        repeatingQuest.setStartTimePreference(e.timePreference);
        repeatingQuest.setStartTime(e.time);
        if (e.time != null) {
            repeatingQuest.setTimesADay(1);
        }
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestPriorityPicked(NewQuestPriorityPickedEvent e) {
        repeatingQuest.setPriority(e.priority);
        goToNextPage();
    }

    @Subscribe
    public void onNewQuestDurationPicked(NewQuestDurationPickedEvent e) {
        repeatingQuest.setDuration(e.duration);
    }

    @Subscribe
    public void onNewQuestRemindersPicked(NewQuestRemindersPickedEvent e) {
        repeatingQuest.setReminders(e.reminders);
    }

    @Subscribe
    public void onNewQuestSubQuestsPicked(NewQuestSubQuestsPickedEvent e) {
        repeatingQuest.setSubQuests(e.subQuests);
    }

    @Subscribe
    public void onNewQuestChallengePicked(NewQuestChallengePickedEvent e) {
        repeatingQuest.setChallengeId(e.challenge == null ? null : e.challenge.getId());
    }

    @Subscribe
    public void onNewQuestNotePicked(NewQuestNotePickedEvent e) {
        List<Note> notes = new ArrayList<>();
        String txt = e.text;
        if (!StringUtils.isEmpty(txt)) {
            notes.add(new Note(txt));
        }
        repeatingQuest.setNotes(notes);
    }

    @Subscribe
    public void onNewQuestTimesADayPicked(NewQuestTimesADayPickedEvent e) {
        repeatingQuest.setTimesADay(e.timesADay);
    }

    @Subscribe
    public void onChangeQuestNameRequest(ChangeQuestNameRequestEvent e) {
        fragmentPager.setCurrentItem(REPEATING_QUEST_NAME_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeRecurrenceRequest(ChangeQuestRecurrenceRequestEvent e) {
        fragmentPager.setCurrentItem(REPEATING_QUEST_RECURRENCE_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangeTimeRequest(ChangeQuestTimeRequestEvent e) {
        fragmentPager.setCurrentItem(REPEATING_QUEST_TIME_FRAGMENT_INDEX);
    }

    @Subscribe
    public void onChangePriorityRequest(ChangeQuestPriorityRequestEvent e) {
        fragmentPager.setCurrentItem(REPEATING_QUEST_PRIORITY_FRAGMENT_INDEX);
    }

    private void goToNextPage() {
        fragmentPager.postDelayed(() -> fragmentPager.setCurrentItem(0),
                getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private void colorLayout(Category category) {
        toolbar.setBackgroundResource(category.color500);
        findViewById(R.id.root_container).setBackgroundResource(category.color500);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, category.color500));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, category.color700));
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        String title = "";
        switch (position) {
            case REPEATING_QUEST_NAME_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_repeating_quest_name);
                break;
            case REPEATING_QUEST_RECURRENCE_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_date);
                recurrenceFragment.setCategory(repeatingQuest.getCategoryType());
                break;
            case REPEATING_QUEST_TIME_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_time);
                timeFragment.setCategory(repeatingQuest.getCategoryType());
                break;
            case REPEATING_QUEST_PRIORITY_FRAGMENT_INDEX:
                title = getString(R.string.title_fragment_wizard_quest_priority);
                break;
            case REPEATING_QUEST_SUMMARY_FRAGMENT_INDEX:
                summaryFragment.setRepeatingQuest(repeatingQuest);
                break;
        }
        setTitle(title);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class WizardFragmentPagerAdapter extends FragmentPagerAdapter {

        WizardFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case REPEATING_QUEST_NAME_FRAGMENT_INDEX:
                    return AddNameFragment.newInstance(R.string.add_quest_name_hint);
                case REPEATING_QUEST_RECURRENCE_FRAGMENT_INDEX:
                    return new AddRepeatingQuestRecurrenceFragment();
                case REPEATING_QUEST_TIME_FRAGMENT_INDEX:
                    return new AddQuestTimeFragment();
                case REPEATING_QUEST_PRIORITY_FRAGMENT_INDEX:
                    return new AddQuestPriorityFragment();
                default:
                    return new AddQuestSummaryFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            if (position == REPEATING_QUEST_RECURRENCE_FRAGMENT_INDEX) {
                recurrenceFragment = (AddRepeatingQuestRecurrenceFragment) createdFragment;
            } else if (position == REPEATING_QUEST_TIME_FRAGMENT_INDEX) {
                timeFragment = (AddQuestTimeFragment) createdFragment;
            } else if (position == REPEATING_QUEST_SUMMARY_FRAGMENT_INDEX) {
                summaryFragment = (AddQuestSummaryFragment) createdFragment;
            }
            return createdFragment;
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    public Toolbar getToolbar(){
        return this.toolbar;
    }
}
