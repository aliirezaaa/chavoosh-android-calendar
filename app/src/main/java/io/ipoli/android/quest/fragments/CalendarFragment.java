package io.ipoli.android.quest.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
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
//import com.ibm.icu.util.Calendar;

import com.ibm.icu.util.ULocale;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

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
import io.ipoli.android.quest.events.ScrollToTimeEvent;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class CalendarFragment extends BaseFragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {
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
    private PersianDate persianCurrentMidDate;
    private Utils utils;

    private DatePickerDialog dpd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        /*change direction of fragment to rtl or else

         */

        initLocalTimeBroadCast();
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

//        PersianDate date = DateConverter.civilToPersian(new CivilDate(makeCalendarFromDate(new Date())));
        currentMidDate = LocalDate.now();
//        toolbarTitle.setText(String.valueOf(date.getYear()));

        changeTitle(currentMidDate);


        fabMenu.addFabClickListener(name -> eventBus.post(new FabMenuTappedEvent(name, EventSource.CALENDAR)));

        adapter = createAdapter();

        calendarPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                LocalDate date = currentMidDate.plusDays(position - MID_POSITION);
                changeTitle(date);
                eventBus.post(new CalendarDayChangedEvent(date, CalendarDayChangedEvent.Source.SWIPE));
            }
        });

        calendarPager.setAdapter(adapter);
        calendarPager.setCurrentItem(MID_POSITION);

        //define date picker

        com.ibm.icu.util.Calendar now = com.ibm.icu.util.Calendar.getInstance(new ULocale("fa_IR"));

        dpd = DatePickerDialog.newInstance(
                CalendarFragment.this,
                now.get(com.ibm.icu.util.Calendar.YEAR),
                now.get(com.ibm.icu.util.Calendar.MONTH),
                now.get(com.ibm.icu.util.Calendar.DAY_OF_MONTH)
        );
//        Log.i("calendar now after define ",""+dpd.getEndDate().get(2));
        //end define date picker
        return view;
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

    private void changeTitle(PersianDate date) {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
//

       toolbarTitle.setText(utils.shape(utils.dateToString(date)));



    }

    private void changeTitle(LocalDate date) {
        CivilDate cDate = new CivilDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());

        PersianDate pDate = DateConverter.civilToPersian(cDate);
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
//        String displayDate = String.valueOf(date.getYear() + "-" + date.getMonth() + "-" + utils.getWeekDayName(date));
//        Utils.getInstance(getContext()).setFontAndShape(toolbarTitle);
//        Utils.getInstance(getContext().)


        toolbarTitle.setText(utils.shape(utils.dateToString(pDate)));

//        toolbarTitle.setText(utils.shape(utils.dateToString(pDate)));
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

        changeCurrentDay(e.date, e.time, utils.getSelectedPersianDate());
    }

    private void changeCurrentDay(LocalDate date, PersianDate pDate) {
        changeCurrentDay(date, null, pDate);
    }

    private void changeCurrentDay(LocalDate date, Time time, PersianDate pDate) {

        currentMidDate = date;
        changeTitle(pDate);
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

    //click on toolbar text and goes to persian calendar activity to choose specific  date
    @Override
    public void onClick(View v) {
//        pickTime();
//        pickDate();
//todo change this to go persian calnedar activity
//        LocalDate currentDate = currentMidDate.plusDays(calendarPager.getCurrentItem() - MID_POSITION);
//        Intent i = new Intent(getContext(), PersianCalendarActivity.class);
//        i.putExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, DateUtils.toMillis(currentDate));
////        startActivity(i);
//
//
//        //todo uncomment this
//        startActivityForResult(i, SHOW_AGENDA_REQUEST_CODE);
//        getActivity().overridePendingTransition(R.anim.slide_in_top, android.R.anim.fade_out);
//        eventBus.post(new ToolbarCalendarTapEvent());


        Utils.getInstance(getContext()).pickDate(CalendarFragment.this, "ON_DATE_SET_FOR_MAIN");

    }

    private void pickDate() {

        dpd.setThemeDark(false);
        dpd.vibrate(true);
        dpd.dismissOnPause(true);
        dpd.showYearPickerFirst(false);
        if (true) {
            dpd.setAccentColor(Color.parseColor("#9C27B0"));
        }
        if (true) {
            dpd.setTitle("DatePicker Title");
        }
        if (false) {
            com.ibm.icu.util.Calendar[] dates = new com.ibm.icu.util.Calendar[13];
            for (int i = -6; i <= 6; i++) {
                com.ibm.icu.util.Calendar date = com.ibm.icu.util.Calendar.getInstance(new ULocale("fa_IR"));
                date.add(com.ibm.icu.util.Calendar.MONTH, i);
                dates[i + 6] = date;
            }
            dpd.setSelectableDays(dates);
        }
        if (false) {
            com.ibm.icu.util.Calendar[] dates = new com.ibm.icu.util.Calendar[13];
            for (int i = -6; i <= 6; i++) {
                com.ibm.icu.util.Calendar date = com.ibm.icu.util.Calendar.getInstance(new ULocale("fa_IR"));
                date.add(com.ibm.icu.util.Calendar.WEEK_OF_YEAR, i);
                dates[i + 6] = date;
            }
            dpd.setHighlightedDays(dates);
        }
//        dpd.initialize();
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }


    private void pickTime() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                CalendarFragment.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );

        tpd.setThemeDark(true);
        tpd.vibrate(true);
        tpd.dismissOnPause(true);
        tpd.enableSeconds(true);
        tpd.enableMinutes(true);
        if (true) {
            tpd.setAccentColor(Color.parseColor("#9C27B0"));
        }
        if (true) {
            tpd.setTitle("TimePicker Title");
        }
        if (true) {
            tpd.setTimeInterval(2, 5, 10);
        }
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
            }
        });
//        tpd.show(getFragmentManager(), "Timepickerdialog");
//        tpd.show(getFragmentManager(),"");
//        FragmentManager fm=getFragmentManager();
        tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
    }


    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = second < 10 ? "0" + second : "" + second;
        String time = "You picked the following time: " + hourString + "h" + minuteString + "m" + secondString + "s";
//        timeTextView.setText(time);
        Log.i("onTimeset ", time);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_AGENDA_REQUEST_CODE && resultCode == RESULT_OK) {
//            Long dateMillis = data.getLongExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, DateUtils.toMillis(LocalDate.now()));
//            changeCurrentDay(DateUtils.fromMillis(dateMillis));
            CivilDate cDate = DateConverter.persianToCivil(utils.getSelectedPersianDate());
            LocalDate lDate = LocalDate.of(cDate.getYear(), cDate.getMonth(), cDate.getDayOfMonth());
            changeCurrentDay(lDate, null, utils.getSelectedPersianDate());
//            changeTitle(utils.getSelectedPersianDate());
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String date = "You picked the following date: " + dayOfMonth + "/" + (++monthOfYear) + "/" + year;
        PersianDate pDate = new PersianDate(year, monthOfYear, dayOfMonth);
        CivilDate civilDate = DateConverter.persianToCivil(pDate);

//        dateTextView.setText(date);
        Log.i("onDateset ", "" + civilDate.getYear() + civilDate.getMonth() + civilDate.getDayOfMonth());
        Log.i("persian date ", "" + pDate.getYear() + pDate.getMonth() + pDate.getDayOfMonth());
    }


    private void initLocalTimeBroadCast() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(dateReciever,
                new IntentFilter("ON_DATE_SET_FOR_MAIN"));
    }

    private BroadcastReceiver dateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int year = intent.getIntExtra("year", 0);
            int month = intent.getIntExtra("month", 0);
            int day = intent.getIntExtra("day", 0);
            Log.d("receiver", "Got message: " + year);
            Log.d("receiver", "Got message: " + month);
            Log.d("receiver", "Got message: " + day);
//      \      Time tm=Time.at(hour,minute);
//            postEvent(new NewQuestTimePickedEvent(tm));
//            month++;

            PersianDate pDate=new PersianDate(year, month, day);
            changeCurrentDay(DateConverter.persianToLocalDate(pDate), null, pDate);

        }
    };
}