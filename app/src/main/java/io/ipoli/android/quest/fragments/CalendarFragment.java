package io.ipoli.android.quest.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.FabMenuView;
import io.ipoli.android.app.ui.events.FabMenuTappedEvent;
import io.ipoli.android.app.ui.events.ToolbarCalendarTapEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;

import io.ipoli.android.persian.calendar.CivilDate;
import io.ipoli.android.persian.calendar.DateConverter;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.activity.PersianCalendarActivity;
import io.ipoli.android.quest.activities.AgendaActivity;
import io.ipoli.android.quest.events.ScrollToTimeEvent;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class CalendarFragment extends BaseFragment implements View.OnClickListener {
    public static final int SHOW_AGENDA_REQUEST_CODE = 100;

    public static final int MID_POSITION = 49;
    public static final int MAX_VISIBLE_DAYS = 100;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.toolbar_expand_container)
    View toolbarExpandContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.calendar_pager)
    ViewPager calendarPager;

    @BindView(R.id.fab_menu)
    FabMenuView fabMenu;

    @Inject
    Bus eventBus;

    private FragmentStatePagerAdapter adapter;

    private LocalDate currentMidDate;
    private Utils utils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        /*change direction of fragment to rtl or else

         */
        changDirection();

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();

        toolbarExpandContainer.setOnClickListener(this);
        //todo try to convert date
        utils = Utils.getInstance(getContext());

        PersianDate date = DateConverter.civilToPersian(new CivilDate(makeCalendarFromDate(new Date())));
        currentMidDate = LocalDate.now();
//        toolbarTitle.setText(String.valueOf(date.getYear()));
        changeTitle(date);
        Log.i("calendar fragment",String.valueOf(date));

        fabMenu.addFabClickListener(name -> eventBus.post(new FabMenuTappedEvent(name, EventSource.CALENDAR)));

        adapter = createAdapter();

        calendarPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                LocalDate date = currentMidDate.plusDays(position - MID_POSITION);
//                changeTitle(date);
                eventBus.post(new CalendarDayChangedEvent(date, CalendarDayChangedEvent.Source.SWIPE));
            }
        });

        calendarPager.setAdapter(adapter);
        calendarPager.setCurrentItem(MID_POSITION);

        return view;
    }

    private void changDirection() {
        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale("fa"));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    public Calendar makeCalendarFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (true) {
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Tehran"));
        }
        calendar.setTime(date);
        return calendar;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                eventBus.post(new CalendarDayChangedEvent(LocalDate.now(), CalendarDayChangedEvent.Source.MENU));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_menu, menu);
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
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

    private void changeTitle(LocalDate date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        toolbarTitle.setText(simpleDateFormat.format(DateUtils.toStartOfDay(date)));
    }
    private void changeTitle(PersianDate date) {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        String displayDate=String.valueOf(date.getYear()+"-"+date.getMonth()+"-"+utils.getWeekDayName(date));
        toolbarTitle.setText(displayDate);
    }


    private int getToolbarText(LocalDate date) {
        if (date.isEqual(LocalDate.now().minusDays(1))) {
            return R.string.yesterday_calendar_format;
        }
        if (date.isEqual(LocalDate.now())) {
            return R.string.today_calendar_format;
        }
        if (date.isEqual(LocalDate.now().plusDays(1))) {
            return R.string.tomorrow_calendar_format;
        }
        return R.string.calendar_format;
    }

    @Subscribe
    public void onCurrentDayChanged(CalendarDayChangedEvent e) {
        if (e.source == CalendarDayChangedEvent.Source.SWIPE) {
            return;
        }
        changeCurrentDay(e.date, e.time);
    }

    private void changeCurrentDay(LocalDate date) {
        changeCurrentDay(date, null);
    }

    private void changeCurrentDay(LocalDate date, Time time) {
        currentMidDate = date;
//        changeTitle(currentMidDate);
        adapter.notifyDataSetChanged();

        calendarPager.setCurrentItem(MID_POSITION, false);
        if (time != null) {
            eventBus.post(new ScrollToTimeEvent(time));
        }
    }

    private FragmentStatePagerAdapter createAdapter() {
        return new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                int plusDays = position - MID_POSITION;
                return DayViewFragment.newInstance(currentMidDate.plusDays(plusDays));
            }

            @Override
            public int getCount() {
                return MAX_VISIBLE_DAYS;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_calendar, R.string.help_dialog_calendar_title, "calendar").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onClick(View v) {
        LocalDate currentDate = currentMidDate.plusDays(calendarPager.getCurrentItem() - MID_POSITION);
        Intent i = new Intent(getContext(), PersianCalendarActivity.class);
        i.putExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, DateUtils.toMillis(currentDate));
        startActivity(i);
//        startActivityForResult(i, SHOW_AGENDA_REQUEST_CODE);
//        getActivity().overridePendingTransition(R.anim.slide_in_top, android.R.anim.fade_out);
//        eventBus.post(new ToolbarCalendarTapEvent());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_AGENDA_REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.hasExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY)) {
            Long dateMillis = data.getLongExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, DateUtils.toMillis(LocalDate.now()));
            changeCurrentDay(DateUtils.fromMillis(dateMillis));
        }
    }
}