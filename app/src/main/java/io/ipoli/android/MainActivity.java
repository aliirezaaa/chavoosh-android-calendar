package io.ipoli.android;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.LocalDate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FriendsInvitedEvent;
import io.ipoli.android.app.events.InviteFriendsCanceledEvent;
import io.ipoli.android.app.events.InviteFriendsEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.settings.HelpActivity;
import io.ipoli.android.app.settings.SettingsActivity;
import io.ipoli.android.app.share.ShareQuestDialog;
import io.ipoli.android.app.tutorial.InteractiveTutorial;
import io.ipoli.android.app.utils.EmailUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.fragments.ChallengeListFragment;
import io.ipoli.android.persian.calendar.DateConverter;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment.PersianCalendarFragment;
//import io.ipoli.android.pet.PetActivity;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.activities.PickAvatarPictureActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.PickAvatarRequestEvent;
import io.ipoli.android.player.fragments.GrowthFragment;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DuplicateQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.SnoozeQuestRequestEvent;
import io.ipoli.android.quest.events.StartQuestRequestEvent;
import io.ipoli.android.quest.events.StopQuestRequestEvent;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.fragments.RepeatingQuestListFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.events.EditRepeatingQuestRequestEvent;
import io.ipoli.android.reminder.data.Reminder;

/*import me.cheshmak.android.sdk.core.Cheshmak;
import me.cheshmak.android.sdk.core.CheshmakConfig;*/
//import me.cheshmak.android.sdk.core.Cheshmak;
//import me.cheshmak.android.sdk.core.CheshmakConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int PICK_PLAYER_PICTURE_REQUEST_CODE = 101;
    public static final int INVITE_FRIEND_REQUEST_CODE = 102;
    private static final int PROGRESS_BAR_MAX_VALUE = 100;

    private boolean dShowAction;
    DuplicateQuestRequestEvent dEvent;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.content_container)
    View contentContainer;

    @BindView(R.id.setting_spot)
    LinearLayout settingSpot;

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    InteractiveTutorial interactiveTutorial;

    Fragment currentFragment;

    private boolean isRateDialogShown;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private MenuItem navigationItemSelected;
    private static Application instance;
    private Quest snoozeQuest;
    private boolean snoozeAction;
    private Quest tSnoozeQuest;
    private boolean tShowAction;
    private Toolbar toolbar;
    private boolean restartFragment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        changeLanguage("fa");

//        changDirection();

        instance = this.getApplication();
        appComponent().inject(this);

        if (!App.hasPlayer()) {
            finish();
            return;
        }

//        int schemaVersion = localStorage.readInt(Constants.KEY_SCHEMA_VERSION);
//        if (App.hasPlayer() && schemaVersion != Constants.SCHEMA_VERSION) {
        // should migrate
//            startActivity(new Intent(this, MigrationActivity.class));
//            finish();
//            return;
//        }
//       startActivity(new Intent(this, ChooseAccountActivity.class));
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        localStorage.increment(Constants.KEY_APP_RUN_COUNT);

        isRateDialogShown = false;

        navigationView.setNavigationItemSelectedListener(this);

        onReceivePush();
//        startCalendar();
        startPersianCalendar();
        LocalBroadcastManager.getInstance(this).registerReceiver(preferences_update,
                new IntentFilter("UPDATE_LOCATION"));
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                if (toolbar != null) {
                }
                navigationItemSelected = null;
                if (localStorage.readBool("main_tutorial", true)) {
                    showTourGuide();
                    localStorage.saveBool("main_tutorial", false);
                }

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (navigationItemSelected == null) {
                    return;
                }
                onItemSelectedFromDrawer();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        Utils.getInstance(getContext()).changLayoutDirection(this);
        initLocalDateBroadCast();
//        isOnScreen(navigationView.getChildAt(0));
//        toolbar.post(() -> {
//            //create your anim here
////            showTourGuide(view);
//                showTapTargetView();
////            localStorage.saveBool("week",true);
//        });


    }

    private void onReceivePush() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getExtras() != null && intent.getExtras().getString("title") != null) {
                Toast.makeText(this, "Cheshmak push notification data" +
                        intent.getExtras().getString("me.cheshmak.data") + "\n" +
                        intent.getExtras().getString("title") + "\n" +
                        intent.getExtras().getString("message") + "\n", Toast.LENGTH_SHORT).show();
            }
        }
        //some code​
    }

    /* private void cheshmakInit() {
         CheshmakConfig config = new CheshmakConfig();
         config.setIsEnableAutoActivityReports(true);
         config.setIsEnableExceptionReporting(true);
         Cheshmak.with(getApplicationContext(), config);

         Cheshmak.initTracker("G0qe5bjhhByjKq6K26y1RQ==");
     }
 */


    private void changeLanguage(String langouage) {
        Resources res = getResources();
// Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(langouage)); // API 17+ only.
// Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
    }

    private void changDirection() {
        Configuration configuration = getResources().getConfiguration();
        configuration.setLayoutDirection(new Locale("fa"));
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        updatePlayerInDrawer(getPlayer());
        if (restartFragment) {
            startPersianCalendar();
            restartFragment = false;
        }

    }

    private void onItemSelectedFromDrawer() {
        navigationView.setCheckedItem(navigationItemSelected.getItemId());

        EventSource source = null;
        switch (navigationItemSelected.getItemId()) {

            case R.id.home:
                source = EventSource.CALENDAR;
                startCalendar();
                break;
            case R.id.persian_home:
                source = EventSource.PERSIAN_CALENDAR;
                startPersianCalendar();
                break;

            case R.id.overview:
                source = EventSource.OVERVIEW;
                startOverview();
                break;

            case R.id.inbox:
                source = EventSource.INBOX;
                changeCurrentFragment(new InboxFragment());
                break;

            case R.id.repeating_quests:
                source = EventSource.REPEATING_QUESTS;
                changeCurrentFragment(new RepeatingQuestListFragment());
                break;

            case R.id.challenges:
                source = EventSource.CHALLENGES;
                changeCurrentFragment(new ChallengeListFragment());
                break;

            case R.id.growth:
                source = EventSource.GROWTH;
                changeCurrentFragment(new GrowthFragment());
                break;

//            case R.id.rewards:
//                source = EventSource.REWARDS;
//                changeCurrentFragment(new RewardListFragment());
//                break;

//            case R.id.store:
//                source = EventSource.STORE;
//                startActivity(new Intent(this, CoinStoreActivity.class));
//                break;

            case R.id.invite_friends:

                inviteFriends();
//                changeCurrentFragment(new ApplicationPreferenceFragment());
//                startActivity(new Intent(this,SyncCalendarActivity.class));
                break;

            case R.id.settings:
                source = EventSource.SETTINGS;
                startActivity(new Intent(this, SettingsActivity.class));
                break;

//            case R.id.feedback:
//                eventBus.post(new FeedbackTapEvent());
//                RateDialog.newInstance(RateDialog.State.FEEDBACK).show(getSupportFragmentManager());
//                break;

            case R.id.contact_us:
                eventBus.post(new ContactUsTapEvent());
                EmailUtils.send(MainActivity.this, "درباره تقویم چاووش", localStorage.readString(Constants.KEY_PLAYER_ID), getString(R.string.contact_us_email_chooser_title));
                break;

            case R.id.about_us:
                startActivity(new Intent(this, HelpActivity.class));
                break;
        }

        if (source != null) {
            eventBus.post(new ScreenShownEvent(source));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePlayerInDrawer(Player player) {

//        View header = navigationView.getHeaderView(0);
//        TextView level = (TextView) header.findViewById(R.id.player_level);
//        int playerLevel = player.getLevel();
//        String[] playerTitles = getResources().getStringArray(R.array.player_titles);
//        String title = playerTitles[Math.min(playerLevel / 10, playerTitles.length - 1)];
//        level.setText("Level " + playerLevel + ": " + title);
//
//        TextView coins = (TextView) header.findViewById(R.id.player_coins);
//        coins.setText(String.valueOf(player.getCoins()));
//        coins.setOnClickListener(view -> {
//            startActivity(new Intent(this, CoinStoreActivity.class));
//            eventBus.post(new AvatarCoinsTappedEvent());
//        });
//
//        ProgressBar experienceBar = (ProgressBar) header.findViewById(R.id.player_experience);
//        experienceBar.setMax(PROGRESS_BAR_MAX_VALUE);
//        experienceBar.setProgress(getCurrentProgress(player));
//
//        CircleImageView avatarPictureView = (CircleImageView) header.findViewById(R.id.player_picture);
//        avatarPictureView.setImageResource(ResourceUtils.extractDrawableResource(MainActivity.this, player.getPicture()));
//        avatarPictureView.setOnClickListener(v -> eventBus.post(new PickAvatarRequestEvent(EventSource.NAVIGATION_DRAWER)));
//
//        TextView currentXP = (TextView) header.findViewById(R.id.player_current_xp);
//        currentXP.setText(String.format(getString(R.string.nav_drawer_player_xp), player.getExperience()));
//        updatePetInDrawer(player.getPet());
//
//
//        Button signIn = (Button) header.findViewById(R.id.sign_in);
//        if (player.isAuthenticated()) {
//            signIn.setVisibility(View.GONE);
//            signIn.setOnClickListener(null);
//        } else {
//            signIn.setVisibility(View.VISIBLE);
//            signIn.setOnClickListener(v -> startActivity(new Intent(this, SignInActivity.class)));
//        }
    }

    private void updatePetInDrawer(Pet pet) {
//        View header = navigationView.getHeaderView(0);

//        CircleImageView petPictureView = (CircleImageView) header.findViewById(R.id.pet_picture);
//        petPictureView.setImageResource(ResourceUtils.extractDrawableResource(MainActivity.this, pet.getPicture() + "_head"));
//        petPictureView.setOnClickListener(v -> startActivity(new Intent(this, PetActivity.class)));

//        ImageView petStateView = (ImageView) header.findViewById(R.id.pet_state);
//        GradientDrawable drawable = (GradientDrawable) petStateView.getBackground();
//        drawable.setColor(ContextCompat.getColor(this, pet.getStateColor()));
    }

    private int getCurrentProgress(Player player) {
        int currentLevel = player.getLevel();
        BigInteger requiredXPForCurrentLevel = ExperienceForLevelGenerator.forLevel(currentLevel);
        BigDecimal xpForNextLevel = new BigDecimal(ExperienceForLevelGenerator.forLevel(currentLevel + 1).subtract(requiredXPForCurrentLevel));
        BigDecimal currentXP = new BigDecimal(new BigInteger(player.getExperience()).subtract(requiredXPForCurrentLevel));
        return (int) (currentXP.divide(xpForNextLevel, 2, RoundingMode.HALF_UP).doubleValue() * PROGRESS_BAR_MAX_VALUE);
    }

    public void startCalendar() {
        changeCurrentFragment(new CalendarFragment());
    }

    public void startPersianCalendar() {
        changeCurrentFragment(new PersianCalendarFragment());
//        if (restartFragment) {
//
//            Fragment frg;
//            frg = getSupportFragmentManager().findFragmentByTag(PersianCalendarFragment.class.getName());
//            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.detach(frg);
//            ft.attach(frg);
//            ft.commit();
//            restartFragment=false;
//        } else {
//
//        }


    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    private void changeCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, fragment, fragment.getClass().getName()).commit();
        currentFragment = fragment;
        getSupportFragmentManager().executePendingTransactions();
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        Quest q = e.quest;
        long experience = q.getExperience();
        long coins = q.getCoins();
//// TODO: 5/28/2017 translate

        Snackbar snackbar = Snackbar
                .make(contentContainer, "کار به اتمام رسید",
                        Snackbar.LENGTH_LONG);
//
//        snackbar.setAction(R.string.share, view -> {
//            eventBus.post(new ShareQuestEvent(q, EventSource.SNACKBAR));
//        });

        snackbar.show();

//        if (shouldShowRateDialog()) {
//            isRateDialogShown = true;
//            new RateDialog().show(getSupportFragmentManager());
//        }
    }

    private boolean shouldShowRateDialog() {
//        int appRun = localStorage.readInt(Constants.KEY_APP_RUN_COUNT);
//        if (isRateDialogShown || appRun < RateDialogConstants.MIN_APP_RUN_FOR_RATE_DIALOG ||
//                !localStorage.readBool(RateDialogConstants.KEY_SHOULD_SHOW_RATE_DIALOG, true)) {
//            return false;
//        }
//        return new Random().nextBoolean();
        return false;
    }

    @Subscribe
    public void onUndoCompletedQuest(UndoCompletedQuestEvent e) {
        Quest q = e.quest;
        String text = q.getScheduledDate() == null ? "کار به میز کار منتقل شد" : "کار از حالت انجام شده خارج شد";
        Snackbar.make(contentContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    public void initToolbar(Toolbar toolbar, @StringRes int title) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        actionBarDrawerToggle.syncState();
        this.toolbar = toolbar;
        this.toolbar.inflateMenu(R.menu.main_navigation_drawer_menu);
//        showTapTargetView();
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.questId);
        startActivity(i);
    }

    @Subscribe
    public void onEditRepeatingQuestRequest(EditRepeatingQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY, e.repeatingQuest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onDuplicateQuestRequest(DuplicateQuestRequestEvent e) {
        boolean showAction = e.source != EventSource.OVERVIEW;
        if (e.date == null) {
            dShowAction = e.source != EventSource.OVERVIEW;
            dEvent = e;
//            DatePickerFragment fragment = DatePickerFragment.newInstance(LocalDate.now(), true,
//                    date ->
//                            duplicateQuest(e.quest, date, showAction));
//            fragment.show(getSupportFragmentManager());
            Utils.getInstance(getContext()).pickDate(MainActivity.this, "ON_DATE_SET_FOR_DUPLICATE");

        } else {
            duplicateQuest(e.quest, e.date, showAction);
        }
    }

    private void duplicateQuest(Quest quest, LocalDate scheduledDate, boolean showAction) {
        boolean isForSameDay = quest.getScheduledDate().isEqual(scheduledDate);
        quest.setId(null);
        quest.setCreatedAt(new Date().getTime());
        quest.setUpdatedAt(new Date().getTime());
        quest.setActualStartDate(null);
        quest.setStartDate(scheduledDate);
        quest.setEndDate(scheduledDate);
        quest.setScheduledDate(scheduledDate);
        quest.setCompletedAtMinute(null);
        quest.setCompletedAtDate(null);
        quest.setCompletedCount(0);
        if (isForSameDay) {
            quest.setStartMinute(null);
        }
        List<Reminder> reminders = quest.getReminders();
        List<Reminder> newReminders = new ArrayList<>();
        int notificationId = new Random().nextInt();
        for (Reminder r : reminders) {
            newReminders.add(new Reminder(r.getMinutesFromStart(), String.valueOf(notificationId)));
        }
        quest.setReminders(newReminders);
        eventBus.post(new NewQuestEvent(quest, EventSource.CALENDAR));

        Snackbar snackbar = Snackbar.make(contentContainer, R.string.quest_duplicated, Snackbar.LENGTH_LONG);

        if (!isForSameDay && showAction) {
            snackbar.setAction(R.string.view, view -> {
                Time scrollToTime = null;
                if (quest.getStartMinute() != null) {
                    scrollToTime = Time.of(quest.getStartMinute());
                }
                eventBus.post(new CalendarDayChangedEvent(scheduledDate, scrollToTime, CalendarDayChangedEvent.Source.DUPLICATE_QUEST_SNACKBAR));
            });
        }

        snackbar.show();
    }

    @Subscribe
    public void onSnoozeQuestRequest(SnoozeQuestRequestEvent e) {
        boolean showAction = e.source != EventSource.OVERVIEW;
        Quest quest = e.quest;
        if (e.showDatePicker) {
            pickDateAndSnoozeQuest(quest, showAction);
        } else if (e.showTimePicker) {
            pickTimeAndSnoozeQuest(quest, showAction);
        } else {
            boolean isDateChanged = false;
            if (e.minutes > 0) {
                int newMinutes = quest.getStartMinute() + e.minutes;
                if (newMinutes >= Time.MINUTES_IN_A_DAY) {
                    newMinutes = newMinutes % Time.MINUTES_IN_A_DAY;
                    quest.setScheduledDate(quest.getScheduledDate().plusDays(1));
                    isDateChanged = true;
                }
                quest.setStartMinute(newMinutes);

            } else {
                isDateChanged = true;
                quest.setScheduledDate(e.date);
            }
            saveSnoozedQuest(quest, isDateChanged, showAction);
        }
    }

    private void pickTimeAndSnoozeQuest(Quest quest, boolean showAction) {
        tSnoozeQuest = quest;
        tShowAction = showAction;
//        Time time = quest.hasStartTime() ? Time.of(quest.getStartMinute()) : null;
//        TimePickerFragment.newInstance(false, time, newTime -> {
//            quest.setStartMinute(newTime.toMinuteOfDay());
//            saveSnoozedQuest(quest, false, showAction);
//        }).show(getSupportFragmentManager());
        Utils.getInstance(getContext()).pickTime(MainActivity.this, "ON_DATE_SET_FOR_SNOOZE_TIME");
    }

    private void pickDateAndSnoozeQuest(Quest quest, boolean showAction) {
        snoozeQuest = quest;
        snoozeAction = showAction;
//        DatePickerFragment.newInstance(LocalDate.now(), true, date -> {
//            quest.setScheduledDate(date);
//            saveSnoozedQuest(quest, true, showAction);
//        }).show(getSupportFragmentManager());
        Utils.getInstance(getContext()).pickDate(MainActivity.this, "ON_DATE_SET_FOR_SNOOZE");
    }

    private void saveSnoozedQuest(Quest quest, boolean isDateChanged, boolean showAction) {
        App.getLocalCalendar().onEventChange(quest);
        questPersistenceService.save(quest);
        String message = getString(R.string.quest_snoozed);
        if (quest.getScheduledDate() == null) {
            message = getString(R.string.quest_moved_to_inbox);
        }

        Snackbar snackbar = Snackbar.make(contentContainer, message, Snackbar.LENGTH_LONG);

        if (isDateChanged && showAction) {
            snackbar.setAction(R.string.view, view -> {
                if (quest.getScheduledDate() == null) {
                    changeCurrentFragment(new InboxFragment());
                } else {
                    Time scrollToTime = null;
                    if (quest.getStartMinute() != null) {
                        scrollToTime = Time.of(quest.getStartMinute());
                    }
                    eventBus.post(new CalendarDayChangedEvent(quest.getScheduledDate(), scrollToTime, CalendarDayChangedEvent.Source.SNOOZE_QUEST_SNACKBAR));
                }
            });
        }

        snackbar.show();
    }

    @Subscribe
    public void onShareQuest(ShareQuestEvent e) {
        ShareQuestDialog.show(this, e.quest, eventBus);
    }

    @Subscribe
    public void onLevelDown(LevelDownEvent e) {
        showLevelDownMessage(e.newLevel);
    }

    @Subscribe
    public void onStartQuestRequest(StartQuestRequestEvent e) {
        new StartQuestCommand(this, e.quest, questPersistenceService).execute();
    }

    @Subscribe
    public void onStopQuestRequest(StopQuestRequestEvent e) {
        new StopQuestCommand(this, e.quest, questPersistenceService).execute();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigationItemSelected = item;
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void inviteFriends() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status == ConnectionResult.SUCCESS) {
            //alarm to go and install Google Play Services
            eventBus.post(new InviteFriendsEvent());
            Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invite_title))
                    .setMessage(getString(R.string.invite_message))
                    .setCustomImage(Uri.parse(Constants.INVITE_IMAGE_URL))
                    .setCallToActionText(getString(R.string.invite_call_to_action))
                    .build();
            startActivityForResult(intent, INVITE_FRIEND_REQUEST_CODE);
        } else if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            Toast.makeText(getContext(), "لطفا گوگل پلی سرویس دستگاه خود را بروزرسانی کنید", Toast.LENGTH_SHORT).show();
        }

    }

    @Subscribe
    public void onPickAvatarRequest(PickAvatarRequestEvent e) {
        startActivityForResult(new Intent(MainActivity.this, PickAvatarPictureActivity.class), PICK_PLAYER_PICTURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PLAYER_PICTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String picture = data.getStringExtra(Constants.PICTURE_NAME_EXTRA_KEY);
            if (!TextUtils.isEmpty(picture)) {
                Player player = getPlayer();
//                ImageView avatarImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.player_picture);
//                avatarImage.setImageResource(ResourceUtils.extractDrawableResource(this, picture));
//                player.setPicture(picture);
                playerPersistenceService.save(player);
            }
        }

        if (requestCode == INVITE_FRIEND_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String[] inviteIds = AppInviteInvitation.getInvitationIds(resultCode, data);
                if (inviteIds == null) {
                    inviteIds = new String[]{};
                }
                eventBus.post(new FriendsInvitedEvent(inviteIds));
            } else {
                eventBus.post(new InviteFriendsCanceledEvent());
            }
        }
    }

    public void startOverview() {
        changeCurrentFragment(new OverviewFragment());
    }

    public static Context getContext() {

        return instance.getApplicationContext();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void initLocalDateBroadCast() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(dateReceiver,
                new IntentFilter("ON_DATE_SET_FOR_DUPLICATE"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(snoozeDateReceiver,
                new IntentFilter("ON_DATE_SET_FOR_SNOOZE"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(snoozeTimeReceiver,
                new IntentFilter("ON_DATE_SET_FOR_SNOOZE_TIME"));

    }

    private BroadcastReceiver dateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int year = intent.getIntExtra("year", 0);
            int month = intent.getIntExtra("month", 0);
            int day = intent.getIntExtra("day", 0);
            PersianDate pDate = new PersianDate(year, month, day);
            duplicateQuest(dEvent.quest, DateConverter.persianToLocalDate(pDate), dShowAction);
        }
    };
    private BroadcastReceiver snoozeDateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int year = intent.getIntExtra("year", 0);
            int month = intent.getIntExtra("month", 0);
            int day = intent.getIntExtra("day", 0);
            PersianDate pDate = new PersianDate(year, month, day);
            snoozeQuest.setScheduledDate(DateConverter.persianToLocalDate(pDate));
            saveSnoozedQuest(snoozeQuest, true, snoozeAction);
        }
    };
    private BroadcastReceiver snoozeTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int hour = intent.getIntExtra("hour", 0);
            int minute = intent.getIntExtra("minute", 0);
//
            Time tm = Time.at(hour, minute);

            tSnoozeQuest.setStartMinute(tm.toMinuteOfDay());
            saveSnoozedQuest(tSnoozeQuest, false, tShowAction);

        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(dateReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(snoozeDateReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(snoozeTimeReceiver);
        super.onDestroy();

    }

    private void showTourGuide() {
        navigationView.post(() -> {
      /*  new SpotlightView.Builder(this)
                .introAnimationDuration(400)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(32)
                .headingTvText("Love")
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(16)
                .subHeadingTvText("Like the picture?\nLet others know.")
                .maskColor(Color.parseColor("#dc000000"))
                .target(view)
                .lineAnimDuration(400)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId("1") //UNIQUE ID
                .show();*/
//        RecyclerView recyclerView = (RecyclerView) navigationView.getChildAt(0);
//        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//        layoutManager.scrollToPositionWithOffset(4, 0);

            NavigationMenuView navView = (NavigationMenuView) navigationView.getChildAt(0);
            Menu menu = navigationView.getMenu();
            MenuItem setting = menu.findItem(R.id.settings);
            Log.i("menu find", setting.getTitle().toString());
//            ViewTarget target = new ViewTarget(navView.getChildAt(11));
//            NavigationMenuItemView item = (NavigationMenuItemView) navView.getChildAt(11);
////        Log.i("getitemid", item.getChildAt(0) + "");
            smoothScrollToView(navView);

//            ViewTarget target_item = new ViewTarget(item.getChildAt(0));
//            Rect rect = new Rect(navView.getWidth()+navView.getRight(),
//                    navView.getTop() + navView.getHeight() - 100,
//                    navView.getWidth()/2-navView.getWidth()/4,
//                    navView.getBottom());
            List<TapTarget> targets = new ArrayList<>();
            targets.add(interactiveTutorial.createTutorialForView(
                    settingSpot,
                    this,
                    "شما تصمیم بگیرید",
                    "به تنظیمات برنامه سر بزنید و برنامه را برای استفاده خود شخصی سازی کنید"));
//        ViewTarget target = new ViewTarget(toolbar).getView();
//                isOnScreen(navigationView.getChildAt(0));

            interactiveTutorial.showTutorials(targets, this, "cal_view");
            settingSpot.setVisibility(View.GONE);
        });
    }

    public void smoothScrollToView(View view) {
        navigationView.post(() -> {
                    int[] location = new int[2];
                    view.getLocationInWindow(location);
                    final int bottom = location[1];
                    //Do something after 100ms
                    NavigationMenuView navView = (NavigationMenuView) navigationView.getChildAt(0);
                    navView.smoothScrollBy(0, navigationView.getBottom());

//            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(settingScroll, "scrollY", view.getTop()).setDuration(200);
//            objectAnimator.start();

                }

        );


    }

    private boolean isOnScreen(View view) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int i[] = new int[2];
                final Rect scrollBounds = new Rect();

                view.getHitRect(scrollBounds);
                navigationView.getLocationOnScreen(i);

                if (i[1] >= scrollBounds.bottom) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
//                            sView.smoothScrollTo(0, sView.getScrollY() + (i[1] - scrollBounds.bottom));
                            Log.i("observ", "its on screen");
                        }
                    });
                }

                vto.removeOnGlobalLayoutListener(this);
            }
        });


        return false;
    }

    private boolean isViewVisible(View view) {
        Rect scrollBounds = new Rect();
        view.getDrawingRect(scrollBounds);

        float top = view.getY();
        float bottom = top + view.getHeight();

        if (scrollBounds.top < top && scrollBounds.bottom > bottom) {
            Log.i("isvisible", "its on screen");
            return true;
        } else {
            return false;
        }
    }

    private BroadcastReceiver preferences_update = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            restartFragment = true;

        }
    };
}