package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
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
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.getkeepsafe.taptargetview.TapTarget;
import com.squareup.otto.Bus;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.target.ViewTarget;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.tutorial.InteractiveTutorial;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.LocalStorage;
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
    private WebView news_web_view;
    private String title;
    private String description;
    private String imgUrl;
    private String url;
    private boolean isGif;
    private boolean isPersistence;
    Toolbar toolbar;

    private int viewPagerPosition;
    private ScrollViewExt about_layout;
    boolean show = true;
    @Inject
    QuestPersistenceService questPersistenceService;


    @BindView(R.id.agenda_list_container)
    ViewGroup questListContainer;

    @BindView(R.id.agenda_list)
    EmptyStateRecyclerView questList;

    @BindView(R.id.news_image)
    AppCompatImageView newsImage;

    private CardView user_event;

    @Inject
    LocalStorage localStorage;
    @Inject
    InteractiveTutorial interactiveTutorial;

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
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();

        utils.setToolbar(toolbar);
        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_inbox);

        //personal events

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        questList.setLayoutManager(layoutManager);
        questList.setHasFixedSize(true);
        selectedDay = DateConverter.persianToLocalDate(utils.getSelectedPersianDate());

//        questList.setEmptyView(questListContainer, R.string.empty_agenda_text, R.drawable.ic_calendar_blank_grey_24dp);


//        if (utils.isNetworkConnected()) {
//            try {
//                showNews();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

            onReceivePush();



        about_layout = (ScrollViewExt) view.findViewById(R.id.about_layout);
        Rect scrollBounds = new Rect();
        about_layout.getHitRect(scrollBounds);

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

                if (!event.getLocalVisibleRect(scrollBounds) || scrollBounds.height() < event.getHeight()) {

                    // Any portion of the imageView, even a single pixel, is within the visible window
                } else {
                    // NONE of the imageView is within the visible window
                    Log.i("show", event.toString());

                    if (show) {
//                        showTourGuide(view);
                        show = false;
                    }

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

//        utils.setFontAndShape((TextView) view.findViewById(R.id.event_card_title));
//        utils.setFontAndShape((TextView) view.findViewById(R.id.today));
//        utils.setFontAndShape((TextView) view.findViewById(R.id.owghat_text));

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
        initLocalBrodcasts();

//        View toolbar_view = inflater.inflate(R.layout.activity_cal_fa, container, false);

//        ImageView toolbar_hint=(ImageView)view.findViewById(R.id.toolbar_hint);

//        FrameLayout toolbar_hint=(FrameLayout)view.findViewById(R.id.toolbar_hint);

//        toolbar_hint.setOnClickListener(v -> {
//            Log.i("selected date ",utils.dateToString(utils.getToday()));
//        });

        toolbar.setOnClickListener(v -> {
//            Log.i("selected date ", utils.dateToString(utils.getToday()));
            Utils.getInstance(getContext()).pickDate(PersianCalendarFragment.this, "ON_DATE_SET_FOR_MONTH_VIEW");
        });

        owghat.post(() -> {
            //create your anim here
//            showTourGuide(view);
            if (localStorage.readBool("persian_tutorial", true)) {
                createAndShowTutorials(view);
                localStorage.saveBool("persian_tutorial", false);
            }


        });


        return view;
    }

    private void createAndShowTutorials(View view) {
        List<TapTarget> targets = new ArrayList<>();
        targets.add(interactiveTutorial.createTutorialForView(
                view.findViewById(R.id.fab_add_quest),
                getActivity(),
                "دکمه جادویی",
                "با این دکمه در سراسر برنامه میتوانید کارهای خود را اضافه کنید"));

        ViewTarget target = new ViewTarget(monthViewPager);
        targets.add(interactiveTutorial.createTutorialForRect(
                target.getRect(),
                getActivity(),
                "به سرعت عمل کنید",
                "با کلیک و نگه داشتن بر روی هر روز از ماه ، یک کار در آن روز ثبت کنید"));
        targets.add(interactiveTutorial.createTutorialForView(
                toolbar.getChildAt(0),
                getActivity(),
                "در زمان سفر کنید",
                "با کلیک روی این نوار میتوانید به تاریخ مورد نظر به سرعت دسترسی داشته باشید"));
//        ViewTarget target = new ViewTarget(monthViewPager);
        targets.add(interactiveTutorial.createTutorialForNav(
                toolbar,
                getActivity(),
                "همه چیز اینجاست",
                "با این منو به همه ی قسمت های برنامه دسترسی داشته باشید"));


        interactiveTutorial.showTutorials(targets, getActivity(), "persian_cal");
    }

    private void initLocalBrodcasts() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(newsReceiver,
                new IntentFilter("ON_NEWS_CHANGE_LISTENER"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(dateReceiver,
                new IntentFilter("ON_DATE_SET_FOR_MONTH_VIEW"));
    }

    private void showTourGuide(View view) {


        new SpotlightView.Builder(this.getActivity())
                .introAnimationDuration(400)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(32)
                .headingTvText("اوقات شرعی")
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(16)
                .subHeadingTvText("با انتخاب هر شهر ?\nاوقات شرعی آن را ملاحظه کنید.")
                .maskColor(Color.parseColor("#dc000000"))
                .target(event)
                .lineAnimDuration(400)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId("8") //UNIQUE ID
                .show();


//        about_layout.scrollTo(0, owghat.getBottom());

    }

    private void showTapTargetView(View view) {
     /*   TapTargetView.showFor(getActivity(),                 // `this` is an Activity
                TapTarget.forToolbarNavigationIcon(toolbar, "This is a target", "We have the best targets, believe me")
                        // All options below are optional
                        .outerCircleColor(R.color.mdtp_accent_color)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.color4)   // Specify a color for the target circle
                        .titleTextSize(20)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.color2)      // Specify the color of the title text
                        .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.color3)  // Specify the color of the description text
                        .textColor(R.color.color1)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.md_black)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(false)                   // Whether to tint the target view's color
                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
//                        .icon(Drawable)                     // Specify a custom drawable to draw as the target
                        .targetRadius(60),                  // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
//                        doSomething();

                    }
                });*/

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
//        utils.setFont(gregorianDate);
        islamicDate = (TextView) view.findViewById(R.id.islamic_date);
//        utils.setFont(islamicDate);
        shamsiDate = (TextView) view.findViewById(R.id.shamsi_date);
//        utils.setFont(shamsiDate);
        weekDayName = (TextView) view.findViewById(R.id.week_day_name);
//        utils.setFont(weekDayName);
        today = (TextView) view.findViewById(R.id.today);
        todayIcon = (AppCompatImageView) view.findViewById(R.id.today_icon);

        fajrTextView = (TextView) view.findViewById(R.id.fajr);
//        utils.setFont(fajrTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.fajrText));

        dhuhrTextView = (TextView) view.findViewById(R.id.dhuhr);
//        utils.setFont(dhuhrTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.dhuhrText));

        asrTextView = (TextView) view.findViewById(R.id.asr);
//        utils.setFont(asrTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.asrText));

        maghribTextView = (TextView) view.findViewById(R.id.maghrib);
//        utils.setFont(maghribTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.maghribText));

        ishaTextView = (TextView) view.findViewById(R.id.isgha);
//        utils.setFont(ishaTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.ishaText));

        sunriseTextView = (TextView) view.findViewById(R.id.sunrise);
//        utils.setFont(sunriseTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.sunriseText));

        sunsetTextView = (TextView) view.findViewById(R.id.sunset);
//        utils.setFont(sunsetTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.sunsetText));

        midnightTextView = (TextView) view.findViewById(R.id.midnight);
//        utils.setFont(midnightTextView);
        utils.setFontAndShape((TextView) view.findViewById(R.id.midnightText));


        moreOwghat = (AppCompatImageView) view.findViewById(R.id.more_owghat);

        eventTitle = (TextView) view.findViewById(R.id.event_title);
//        utils.setFont(eventTitle);
        holidayTitle = (TextView) view.findViewById(R.id.holiday_title);
//        utils.setFont(holidayTitle);
        //news init
        news_card_title = (TextView) view.findViewById(R.id.news_card_title);
        news_title = (TextView) view.findViewById(R.id.news_title);
        card_news = (CardView) view.findViewById(R.id.cardNews);
        user_event = (CardView) view.findViewById(R.id.mycardEvent);

//        news_web_view=(WebView)view.findViewById(R.id.news_webview);

    }

    private void showQuestsForDate(LocalDate date) {
        eventBus.post(new CalendarDayChangedEvent(date, CalendarDayChangedEvent.Source.AGENDA_CALENDAR));
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
//        Date startOfDayDate = DateUtils.toStartOfDay(date);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setTitle(simpleDateFormat.format(startOfDayDate));
//        }
//        String dayNumberSuffix = DateUtils.getDayNumberSuffix(date.getDayOfMonth());
//        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.agenda_daily_journey_format, dayNumberSuffix));
//        journeyText.setText(getString(R.string.agenda_daily_journey, dateFormat.format(startOfDayDate)));
        questPersistenceService.findAllNonAllDayForDate(date, quests -> {
            List<AgendaViewModel> vms = new ArrayList<>();
            if (quests.size() != 0) {
                for (Quest quest : quests) {
                    vms.add(new AgendaViewModel(getContext(), quest, true));
//                Log.i("vms", quest.getName());
                }
                questList.setAdapter(new AgendaAdapter(getContext(), eventBus, vms));
                user_event.setVisibility(View.VISIBLE);
            } else {
                user_event.setVisibility(View.GONE);
            }
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
        selectedDay = DateConverter.persianToLocalDate(persianDate);
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

    public void onReceivePush() {
        if (!localStorage.readBool("show_news", false)) {
            card_news.setVisibility(View.GONE);
            return;
        }
        title = localStorage.readString("news_title");
        description = localStorage.readString("news_description");
        imgUrl = localStorage.readString("news_img_url");
        url = localStorage.readString("news_url");
        isGif = localStorage.readBool("isGif", false);

        if (isGif && imgUrl != null) {
            Glide.with(getContext())
                    .load(imgUrl)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(newsImage);
            newsImage.setVisibility(View.VISIBLE);
        } else if (imgUrl != null && !isGif) {
            Glide.with(getContext())
                    .load(imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(newsImage);
            newsImage.setVisibility(View.VISIBLE);
        }
        news_card_title.setText(title);
        news_title.setText(description);
        news_title.setOnClickListener(v -> {
            if (url != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
        card_news.setVisibility(View.VISIBLE);

    }


    private BroadcastReceiver newsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            onReceivePush();
        }
    };
    private BroadcastReceiver dateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int year = intent.getIntExtra("year", 0);
            int month = intent.getIntExtra("month", 0);
            int day = intent.getIntExtra("day", 0);

            PersianDate pDate = new PersianDate(year, month, day);
            bringDate(pDate);
            //post to next fragment


        }
    };

    public void onDestroy() {

        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(dateReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(newsReceiver);
    }
}
