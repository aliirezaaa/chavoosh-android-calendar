package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.Constants;

import io.ipoli.android.persian.com.chavoosh.persiancalendar.adapter.CalendarAdapter;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.HttpHandler;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.ScrollViewExt;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.ScrollViewListener;
//import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.dialog.SelectDayDialog;
//import com.github.praytimes.Clock;
//import com.github.praytimes.Coordinate;
//import com.github.praytimes.PrayTime;
//import com.github.praytimes.PrayTimesCalculator;


import org.json.JSONException;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.ipoli.android.persian.calendar.CivilDate;
import io.ipoli.android.persian.calendar.DateConverter;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.persian.com.github.praytimes.Clock;
import io.ipoli.android.persian.com.github.praytimes.Coordinate;
import io.ipoli.android.persian.com.github.praytimes.PrayTime;
import io.ipoli.android.persian.com.github.praytimes.PrayTimesCalculator;
import io.ipoli.android.quest.adapters.AgendaAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.AgendaViewModel;


public class PersianCalendarFragment extends BaseFragment
        implements View.OnClickListener, ViewPager.OnPageChangeListener {
    @Inject
    Bus eventBus;
    private ViewPager monthViewPager;
    private Utils utils;

    private Calendar calendar = Calendar.getInstance();

    private Coordinate coordinate;

    private PrayTimesCalculator prayTimesCalculator;
    private TextView fajrTextView;
    private TextView dhuhrTextView;
    private TextView asrTextView;
    private TextView maghribTextView;
    private TextView ishaTextView;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private TextView midnightTextView;

    private TextView weekDayName;
    private TextView gregorianDate;
    private TextView islamicDate;
    private TextView shamsiDate;
    private TextView eventTitle;
    private TextView holidayTitle;
    private TextView today;
    private TextView news_title;

    private TextView news_card_title;
    private AppCompatImageView todayIcon;

    private AppCompatImageView moreOwghat;

    private CardView owghat;
    private CardView event;
    private CardView card_news;

    private RelativeLayout fajrLayout;
    private RelativeLayout sunriseLayout;
    private RelativeLayout dhuhrLayout;
    private RelativeLayout asrLayout;
    private RelativeLayout sunsetLayout;
    private RelativeLayout maghribLayout;
    private RelativeLayout ishaLayout;
    private RelativeLayout midnightLayout;
    private LocalDate selectedDay;

    private int viewPagerPosition;
    private ScrollViewExt about_layout;
    @Inject
    QuestPersistenceService questPersistenceService;


    @BindView(R.id.agenda_list_container)
    ViewGroup questListContainer;

    @BindView(R.id.agenda_list)
    EmptyStateRecyclerView questList;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_calendar_fa, container, false);
//        eventBus = new Bus();
        App.getAppComponent(getContext()).inject(this);
//        Log.i("pcf", getContext().toString());
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
//        eventBus.register(this);


        utils = Utils.getInstance(getContext());
        utils.clearYearWarnFlag();
        viewPagerPosition = 0;
        uiInit(view);
        //toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();

        utils.setToolbar(toolbar);

        //personal events

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        questList.setLayoutManager(layoutManager);
        questList.setHasFixedSize(true);
        selectedDay=DateConverter.persianToLocalDate(utils.getSelectedPersianDate());

//        questList.setEmptyView(questListContainer, R.string.empty_agenda_text, R.drawable.ic_calendar_blank_grey_24dp);


        if (utils.isNetworkConnected()) {
            try {
                showNews();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        about_layout = (ScrollViewExt) view.findViewById(R.id.about_layout);
        about_layout.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
                View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

                // if diff is zero, then the bottom has been reached
                if (diff == 0) {
                    // do stuff
                    Log.i("scroll", "  reached");
                }
            }
        });


        //end init
        owghat = (CardView) view.findViewById(R.id.owghat);
        event = (CardView) view.findViewById(R.id.cardEvent);

        monthViewPager = (ViewPager) view.findViewById(R.id.calendar_pager);

        coordinate = utils.getCoordinate();
        prayTimesCalculator = new PrayTimesCalculator(utils.getCalculationMethod());

        monthViewPager.setAdapter(new CalendarAdapter(getChildFragmentManager()));
        monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);

        monthViewPager.addOnPageChangeListener(this);

        owghat.setOnClickListener(this);
        today.setOnClickListener(this);
        todayIcon.setOnClickListener(this);
        gregorianDate.setOnClickListener(this);
        islamicDate.setOnClickListener(this);
        shamsiDate.setOnClickListener(this);

        utils.setFontAndShape((TextView) view.findViewById(R.id.event_card_title));
        utils.setFontAndShape((TextView) view.findViewById(R.id.today));
        utils.setFontAndShape((TextView) view.findViewById(R.id.owghat_text));

        String cityName = utils.getCityName(false);
        if (!TextUtils.isEmpty(cityName)) {
            ((TextView) view.findViewById(R.id.owghat_text))
                    .append(" (" + utils.shape(cityName) + ")");
        }

        // This will immediately be replaced by the same functionality on fragment but is here to
        // make sure enough space is dedicated to actionbar's title and subtitle, kinda hack anyway
        PersianDate today = utils.getToday();
        utils.setActivityTitleAndSubtitle(getActivity(), utils.getMonthName(today),
                utils.formatNumber(today.getYear()));


//        View toolbar_view = inflater.inflate(R.layout.activity_cal_fa, container, false);

//        ImageView submit_img=(ImageView)toolbar_view.findViewById(R.id.submit_img);
//        submit_img.setOnClickListener(v -> {
//            Log.i("selected date ",utils.dateToString(utils.getToday()));
//        });
        return view;
    }

    private void uiInit(View view) {
        fajrLayout = (RelativeLayout) view.findViewById(R.id.fajrLayout);
        sunriseLayout = (RelativeLayout) view.findViewById(R.id.sunriseLayout);
        dhuhrLayout = (RelativeLayout) view.findViewById(R.id.dhuhrLayout);
        asrLayout = (RelativeLayout) view.findViewById(R.id.asrLayout);
        sunsetLayout = (RelativeLayout) view.findViewById(R.id.sunsetLayout);
        maghribLayout = (RelativeLayout) view.findViewById(R.id.maghribLayout);
        ishaLayout = (RelativeLayout) view.findViewById(R.id.ishaLayout);
        midnightLayout = (RelativeLayout) view.findViewById(R.id.midnightLayout);

        gregorianDate = (TextView) view.findViewById(R.id.gregorian_date);
        utils.setFont(gregorianDate);
        islamicDate = (TextView) view.findViewById(R.id.islamic_date);
        utils.setFont(islamicDate);
        shamsiDate = (TextView) view.findViewById(R.id.shamsi_date);
        utils.setFont(shamsiDate);
        weekDayName = (TextView) view.findViewById(R.id.week_day_name);
        utils.setFont(weekDayName);
        today = (TextView) view.findViewById(R.id.today);
        todayIcon = (AppCompatImageView) view.findViewById(R.id.today_icon);

        fajrTextView = (TextView) view.findViewById(R.id.fajr);
        utils.setFont(fajrTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.fajrText));

        dhuhrTextView = (TextView) view.findViewById(R.id.dhuhr);
        utils.setFont(dhuhrTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.dhuhrText));

        asrTextView = (TextView) view.findViewById(R.id.asr);
        utils.setFont(asrTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.asrText));

        maghribTextView = (TextView) view.findViewById(R.id.maghrib);
        utils.setFont(maghribTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.maghribText));

        ishaTextView = (TextView) view.findViewById(R.id.isgha);
        utils.setFont(ishaTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.ishaText));

        sunriseTextView = (TextView) view.findViewById(R.id.sunrise);
        utils.setFont(sunriseTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.sunriseText));

        sunsetTextView = (TextView) view.findViewById(R.id.sunset);
        utils.setFont(sunsetTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.sunsetText));

        midnightTextView = (TextView) view.findViewById(R.id.midnight);
        utils.setFont(midnightTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.midnightText));


        moreOwghat = (AppCompatImageView) view.findViewById(R.id.more_owghat);

        eventTitle = (TextView) view.findViewById(R.id.event_title);
        utils.setFont(eventTitle);
        holidayTitle = (TextView) view.findViewById(R.id.holiday_title);
        utils.setFont(holidayTitle);
        //news init
        news_card_title = (TextView) view.findViewById(R.id.news_card_title);
        news_title = (TextView) view.findViewById(R.id.news_title);
        card_news = (CardView) view.findViewById(R.id.cardNews);
    }

    private void showQuestsForDate(LocalDate date) {
        eventBus.post(new CalendarDayChangedEvent(date, CalendarDayChangedEvent.Source.AGENDA_CALENDAR));
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        Date startOfDayDate = DateUtils.toStartOfDay(date);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setTitle(simpleDateFormat.format(startOfDayDate));
//        }
//        String dayNumberSuffix = DateUtils.getDayNumberSuffix(date.getDayOfMonth());
//        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.agenda_daily_journey_format, dayNumberSuffix));
//        journeyText.setText(getString(R.string.agenda_daily_journey, dateFormat.format(startOfDayDate)));
        questPersistenceService.findAllNonAllDayForDate(date, quests -> {
            List<AgendaViewModel> vms = new ArrayList<>();
            for (Quest quest : quests) {
                vms.add(new AgendaViewModel(getContext(), quest, true));
                Log.i("vms", quest.getName());
            }
            questList.setAdapter(new AgendaAdapter(getContext(), eventBus, vms));
        });
    }

    private void showNews() throws JSONException {
        HttpHandler sh = new HttpHandler(news_title);
        // Making a request to url and getting response
        String url = "https://jsonplaceholder.typicode.com/posts/1";
//        String jsonStr = sh.getResults();
////        JSONObject json = new JSONObject(jsonStr);
//        json.get("title");

        news_card_title.setText("خبر");
//        news_title.setText(json.getString("title"));
//        Log.i("title",jsonStr);
        card_news.setVisibility(View.VISIBLE);


    }

    public void changeMonth(int position) {
        monthViewPager.setCurrentItem(monthViewPager.getCurrentItem() + position, true);
    }

    public void selectDay(PersianDate persianDate) {
        weekDayName.setText(utils.shape(utils.getWeekDayName(persianDate)));
        shamsiDate.setText(utils.shape(utils.dateToString(persianDate)));
        CivilDate civilDate = DateConverter.persianToCivil(persianDate);
        gregorianDate.setText(utils.shape(utils.dateToString(civilDate)));
        islamicDate.setText(utils.shape(utils.dateToString(
                DateConverter.civilToIslamic(civilDate, utils.getIslamicOffset()))));

        if (utils.getToday().equals(persianDate)) {
            today.setVisibility(View.GONE);
            todayIcon.setVisibility(View.GONE);
            if (utils.iranTime) {
                weekDayName.setText(weekDayName.getText() +
                        utils.shape(" (" + getString(R.string.iran_time) + ")"));
            }
        } else {
            today.setVisibility(View.VISIBLE);
            todayIcon.setVisibility(View.VISIBLE);
        }
        Log.i("select day ", utils.dateToString(persianDate));
        setOwghat(civilDate);
        showEvent(persianDate);
        selectedDay=DateConverter.persianToLocalDate(persianDate);
        showQuestsForDate(selectedDay);
        utils.setSelectedPersianDate(persianDate);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void addEventOnCalendar(PersianDate persianDate) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);

        CivilDate civil = DateConverter.persianToCivil(persianDate);

        intent.putExtra(CalendarContract.Events.DESCRIPTION,
                utils.dayTitleSummary(persianDate));

        Calendar time = Calendar.getInstance();
        time.set(civil.getYear(), civil.getMonth() - 1, civil.getDayOfMonth());

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                time.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

        startActivity(intent);
    }

    private void showEvent(PersianDate persianDate) {
        String holidays = utils.getEventsTitle(persianDate, true);
        String events = utils.getEventsTitle(persianDate, false);

        event.setVisibility(View.GONE);
        holidayTitle.setVisibility(View.GONE);
        eventTitle.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(holidays)) {
            holidayTitle.setText(utils.shape(holidays));
            holidayTitle.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(events)) {
            eventTitle.setText(utils.shape(events));
            eventTitle.setVisibility(View.VISIBLE);
            event.setVisibility(View.VISIBLE);
        }
    }

    private void setOwghat(CivilDate civilDate) {
        if (coordinate == null) {
            return;
        }

        calendar.set(civilDate.getYear(), civilDate.getMonth() - 1, civilDate.getDayOfMonth());
        Date date = calendar.getTime();

        Map<PrayTime, Clock> prayTimes = prayTimesCalculator.calculate(date, coordinate);

        fajrTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.FAJR)));
        sunriseTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNRISE)));
        dhuhrTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.DHUHR)));
        asrTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ASR)));
        sunsetTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.SUNSET)));
        maghribTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MAGHRIB)));
        ishaTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.ISHA)));
        midnightTextView.setText(utils.getPersianFormattedClock(prayTimes.get(PrayTime.MIDNIGHT)));

        owghat.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.owghat:
                fajrLayout.setVisibility(View.VISIBLE);
                sunriseLayout.setVisibility(View.VISIBLE);
                dhuhrLayout.setVisibility(View.VISIBLE);
                asrLayout.setVisibility(View.VISIBLE);
                sunsetLayout.setVisibility(View.VISIBLE);
                maghribLayout.setVisibility(View.VISIBLE);
                ishaLayout.setVisibility(View.VISIBLE);
                midnightLayout.setVisibility(View.VISIBLE);

                moreOwghat.setVisibility(View.GONE);
                break;

            case R.id.today:
            case R.id.today_icon:
                bringTodayYearMonth();
                break;

            case R.id.islamic_date:
            case R.id.shamsi_date:
            case R.id.gregorian_date:
                utils.copyToClipboard(v);
                break;
        }
    }

    private void bringTodayYearMonth() {
        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT,
                Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        if (monthViewPager.getCurrentItem() != Constants.MONTHS_LIMIT / 2) {
            monthViewPager.setCurrentItem(Constants.MONTHS_LIMIT / 2);
        }

        selectDay(utils.getToday());
    }

    public void bringDate(PersianDate date) {
        PersianDate today = utils.getToday();
        viewPagerPosition =
                (today.getYear() - date.getYear()) * 12 + today.getMonth() - date.getMonth();

        monthViewPager.setCurrentItem(viewPagerPosition + Constants.MONTHS_LIMIT / 2);

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, date.getDayOfMonth());

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        selectDay(date);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        viewPagerPosition = position - Constants.MONTHS_LIMIT / 2;

        Intent intent = new Intent(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT);
        intent.putExtra(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT, viewPagerPosition);
        intent.putExtra(Constants.BROADCAST_FIELD_SELECT_DAY, -1);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

        today.setVisibility(View.VISIBLE);
        todayIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
//        inflater.inflate(R.menu.action_button, menu);
    }


    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.go_to:
//                SelectDayDialog dialog = new SelectDayDialog();
//                dialog.show(getChildFragmentManager(), SelectDayDialog.class.getName());
//                break;
            default:
                break;
        }
        return true;
    }

    public int getViewPagerPosition() {
        return viewPagerPosition;
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        showQuestsForDate(selectedDay);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }
}
