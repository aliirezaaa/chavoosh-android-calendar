package io.ipoli.android.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.amplitude.api.Amplitude;
import com.couchbase.lite.Database;
import com.couchbase.lite.replicator.Replication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.app.activities.SignInAsGustActivity;
import io.ipoli.android.app.locals.AnalyticsConstants;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.QuickAddActivity;
import io.ipoli.android.app.activities.SignInActivity;
import io.ipoli.android.app.activities.SyncCalendarActivity;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.api.UrlProvider;
import io.ipoli.android.app.auth.FacebookAuthService;
import io.ipoli.android.app.auth.GoogleAuthService;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.DateChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FinishSignInActivityEvent;
import io.ipoli.android.app.events.FinishSyncCalendarActivityEvent;
import io.ipoli.android.app.events.FinishTutorialActivityEvent;
import io.ipoli.android.app.events.InitAppEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.StartQuickAddEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.receivers.DateChangedReceiver;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.app.settings.events.OngoingNotificationChangeEvent;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.ui.dialogs.LoadingDialog;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.activities.ChallengeCompleteActivity;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.data.Difficulty;
import io.ipoli.android.challenge.data.PredefinedChallenge;
import io.ipoli.android.challenge.events.ChallengeCompletedEvent;
import io.ipoli.android.challenge.events.DailyChallengeCompleteEvent;
import io.ipoli.android.challenge.events.NewChallengeEvent;
import io.ipoli.android.challenge.events.RemoveBaseQuestFromChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.receivers.ScheduleDailyChallengeReminderReceiver;
import io.ipoli.android.challenge.ui.events.CompleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.DeleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.UpdateChallengeEvent;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.service.ApplicationService;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.pet.PetActivity;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.activities.LevelUpActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.events.PlayerUpdatedEvent;
import io.ipoli.android.player.events.StartReplicationEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.events.UpdateQuestEvent;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.generators.RewardProvider;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.receivers.CompleteQuestReceiver;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;
import io.ipoli.android.quest.receivers.StartQuestReceiver;
import io.ipoli.android.quest.receivers.StopQuestReceiver;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.schedulers.QuestScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.ipoli.android.sync.LocalCalendar;
import okhttp3.Cookie;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class App extends MultiDexApplication {

    private static AppComponent appComponent;

    private static String playerId;
   private static LocalCalendar localCalendar;

    @Inject
    Bus eventBus;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    LocalStorage localStorage;

    @Inject
    Database database;

    @Inject
    Api api;

    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    QuestScheduler questScheduler;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    ExperienceRewardGenerator experienceRewardGenerator;

    @Inject
    CoinsRewardGenerator coinsRewardGenerator;

    @Inject
    UrlProvider urlProvider;
    private LoadingDialog dialog;

    private void listenForChanges() {
        questPersistenceService.removeAllListeners();
        listenForDailyQuestsChange();
    }

    private void updatePet(Pet pet, int healthPoints) {
        if (pet.getState() == Pet.PetState.DEAD) {
            return;
        }

        Pet.PetState initialState = pet.getState();
        pet.addHealthPoints(healthPoints);

        Pet.PetState currentState = pet.getState();

        if (healthPoints < 0 && initialState != currentState && (currentState == Pet.PetState.DEAD || currentState == Pet.PetState.SAD)) {
            notifyPetStateChanged(pet);
        }
    }

    private void savePet(int healthPoints) {
        Player player = getPlayer();
        Pet pet = player.getPet();
        updatePet(pet, healthPoints);
        playerPersistenceService.save(player);
    }

    private void notifyPetStateChanged(Pet pet) {
        String title = pet.getState() == Pet.PetState.DEAD ? pet.getName() + " has died" : "I am so sad, don't let me die";
        String text = pet.getState() == Pet.PetState.DEAD ? "Revive " + pet.getName() + " to help you with your quests!" :
                "Complete your quests to make me happy!";

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), ResourceUtils.extractDrawableResource(this, pet.getPicture() + "_head"));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PetActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.PET_STATE_CHANGED_NOTIFICATION_ID, builder.build());
    }

    private int getDecreasePercentage(List<Quest> quests) {
        if (quests.isEmpty()) {
            return (int) (Constants.NO_QUESTS_PENALTY_COEFFICIENT * 100);
        }

        int decreasePercentage = 0;
        if (quests.size() == 1) {
            decreasePercentage += 30;
        }

        if (quests.size() == 2) {
            decreasePercentage += 20;
        }

        Set<Quest> uncompletedQuests = new HashSet<>();
        int uncompletedImportantQuestCount = 0;
        for (Quest q : quests) {
            if (!q.isCompleted()) {
                uncompletedQuests.add(q);
                if (q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
                    uncompletedImportantQuestCount++;
                }
            }
        }

        double uncompletedRatio = uncompletedQuests.size() / quests.size();

        int randomNoise = new Random().nextInt(21) - 10;
        decreasePercentage += (int) (uncompletedRatio * Constants.MAX_PENALTY_COEFFICIENT + (uncompletedImportantQuestCount * Constants.IMPORTANT_QUEST_PENALTY_PERCENT) + randomNoise);
        decreasePercentage = (int) Math.min(decreasePercentage, Constants.MAX_PENALTY_COEFFICIENT * 100);
        return decreasePercentage;
    }

    private void listenForDailyQuestsChange() {
        questPersistenceService.listenForAllNonAllDayForDate(LocalDate.now(), quests -> {
            scheduleNextReminder();
            try {
                localStorage.saveString(Constants.KEY_WIDGET_AGENDA_QUEST_LIST, objectMapper.writeValueAsString(quests));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Cant save quests for Widget", e);
            }
            requestWidgetUpdate();

            if (quests.isEmpty()) {
                updateOngoingNotification(null, 0, 0);
                return;
            }

            List<Quest> uncompletedQuests = new ArrayList<>();
            for (Quest q : quests) {
                if (!q.isCompleted()) {
                    uncompletedQuests.add(q);
                }
            }

            if (uncompletedQuests.isEmpty()) {
                updateOngoingNotification(null, quests.size(), quests.size());
                return;
            }

            Quest quest = uncompletedQuests.get(0);
            updateOngoingNotification(quest, quests.size() - uncompletedQuests.size(), quests.size());
        });
    }

    @Subscribe
    public void onOngoingNotificationChange(OngoingNotificationChangeEvent e) {
        if (!e.isEnabled) {
            NotificationManagerCompat.from(this).cancel(Constants.ONGOING_NOTIFICATION_ID);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Yekan.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
 /* add event to local calendar*/
        localCalendar = new LocalCalendar(this);

        AndroidThreeTen.init(this);
        Amplitude.getInstance().initialize(getApplicationContext(), AnalyticsConstants.PROD_FLURRY_KEY).enableForegroundTracking(this);

        getAppComponent(this).inject(this);

        registerServices();
        startPersianService();
        playerId = localStorage.readString(Constants.KEY_PLAYER_ID);

        int schemaVersion = localStorage.readInt(Constants.KEY_SCHEMA_VERSION);
        if (hasPlayer() && schemaVersion != Constants.SCHEMA_VERSION) {
            return;
        }
        if (!hasPlayer()) {
            /* change it for now
            exchange false and true
             */
            if (localStorage.readBool(Constants.KEY_SHOULD_SHOW_TUTORIAL, false)) {
                localStorage.saveBool(Constants.KEY_SHOULD_SHOW_TUTORIAL, true);
                startNewActivity(TutorialActivity.class);
            } else {
                /*remove sign in for now
                * its require to add google account api for register users
                * now all users as gust*/

                startNewActivity(SignInAsGustActivity.class);

            }
            return;
        }

        initReplication();
        initAppStart();
    }


    @Subscribe
    public void onFinishTutorialActivity(FinishTutorialActivityEvent e) {
        if (!hasPlayer()) {
            startNewActivity(SignInActivity.class);
        }
    }

    @Subscribe
    public void onFinishSignInActivity(FinishSignInActivityEvent e) {
        if (hasPlayer() && e.isNewPlayer) {
            startNewActivity(SyncCalendarActivity.class);
        } else if (!hasPlayer()) {
            System.exit(0);
        }
    }

    @Subscribe
    public void onFinishSynCalendarActivity(FinishSyncCalendarActivityEvent e) {

        //// TODO: 5/21/2017 remove pick challenge
        startNewActivity(MainActivity.class);
//        Intent intent = new Intent(this, PickChallengeActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(PickChallengeActivity.TITLE, getString(R.string.pick_challenge_to_start));
//        startActivity(intent);
    }

    private void startPersianService() {
        if (!Utils.getInstance(this).isServiceRunning(ApplicationService.class)) {
            startService(new Intent(getBaseContext(), ApplicationService.class));
        }
    }

    private void startNewActivity(Class clazz) {
        Intent intent = new Intent(this, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateOngoingNotification(Quest quest, int completedCount, int totalCount) {
        if (!localStorage.readBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, Constants.DEFAULT_ONGOING_NOTIFICATION_ENABLED)) {
            return;
        }

        String title = "";
        if (quest != null) {
            title = quest.getName();
        } else if (totalCount == 0) {
            title = getString(R.string.ongoing_notification_no_quests_title);
        } else {
            title = getString(R.string.ongoing_notification_done_title);
        }

        String text = totalCount == 0 ? getString(R.string.ongoing_notification_no_quests_text) : getString(R.string.ongoing_notification_progress_text, completedCount, totalCount);
        boolean showWhen = quest != null && quest.isScheduled();
        long when = showWhen ? Quest.getStartDateTimeMillis(quest) : 0;
        String contentInfo = quest == null ? "" : "به مدت " + DurationFormatter.format(this, quest.getDuration());
        int smallIcon = quest == null ? R.drawable.ic_notification_small : quest.getCategoryType().whiteImage;
        int iconColor = quest == null ? R.color.md_grey_500 : quest.getCategoryType().color500;

        Intent startAppIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent addIntent = new Intent(this, AddQuestActivity.class);
        addIntent.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, " " + getString(R.string.today).toLowerCase());
        //// TODO: 5/15/2017 set notification , uncommnet this

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setShowWhen(showWhen)
                .setWhen(when)
                .setContentInfo(contentInfo)
                //// TODO: 5/15/2017 smallicon
                .setSmallIcon(R.drawable.puzzle)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.puzzle))
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(R.drawable.puzzle, getString(R.string.add), PendingIntent.getActivity(this, 0, addIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColor(ContextCompat.getColor(this, iconColor))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (quest != null) {
            if (quest.isStarted()) {
                builder.addAction(R.drawable.puzzle, getString(R.string.stop).toUpperCase(), getStopPendingIntent(quest.getId()));
            } else {
                builder.addAction(R.drawable.puzzle, getString(R.string.start).toUpperCase(), getStartPendingIntent(quest.getId()));
            }
            builder.addAction(R.drawable.puzzle, getString(R.string.done).toUpperCase(), getDonePendingIntent(quest.getId()));
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.ONGOING_NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getStartPendingIntent(String questId) {
        Intent intent = new Intent(StartQuestReceiver.ACTION_START_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private PendingIntent getStopPendingIntent(String questId) {
        Intent intent = new Intent(StopQuestReceiver.ACTION_STOP_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private PendingIntent getDonePendingIntent(String questId) {
        Intent intent = new Intent(CompleteQuestReceiver.ACTION_COMPLETE_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private void initAppStart() {
        int versionCode = localStorage.readInt(Constants.KEY_APP_VERSION_CODE);
        if (versionCode != BuildConfig.VERSION_CODE) {
            scheduleDailyChallenge();
            localStorage.saveInt(Constants.KEY_APP_VERSION_CODE, BuildConfig.VERSION_CODE);
            if (versionCode > 0) {
                eventBus.post(new VersionUpdatedEvent(versionCode, BuildConfig.VERSION_CODE));
            }
        }

        scheduleDateChanged();
        scheduleNextReminder();
        listenForChanges();
    }

    private void initReplication() {
        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            return;
        }

        Player player = getPlayer();
        if (!player.isAuthenticated()) {
            return;
        }
        AccessTokenListener listener = accessToken -> {
            if (StringUtils.isEmpty(accessToken)) {
                return;
            }
            api.createSession(player.getCurrentAuthProvider(), accessToken, new Api.SessionResponseListener() {
                @Override
                public void onSuccess(String username, String email, List<Cookie> cookies, String playerId, boolean isNew, boolean shouldCreatePlayer) {
                    syncData(cookies);
                }

                @Override
                public void onError(Exception e) {
                    eventBus.post(new AppErrorEvent(e));
                }
            });
        };
        AuthProvider.Provider authProvider = player.getCurrentAuthProvider().getProviderType();
        if (authProvider == AuthProvider.Provider.GOOGLE) {
            new GoogleAuthService(eventBus).getIdToken(this, listener::onAccessTokenReceived);
        } else if (authProvider == AuthProvider.Provider.FACEBOOK) {
            listener.onAccessTokenReceived(new FacebookAuthService(eventBus).getAccessToken());
        }
    }

    @Subscribe
    public void onStartReplication(StartReplicationEvent e) {
        syncData(e.cookies);
    }

    private void syncData(List<Cookie> cookies) {
        try {
            Replication pull = database.createPullReplication(urlProvider.sync());
            for (Cookie cookie : cookies) {
                pull.setCookie(cookie.name(), cookie.value(), cookie.path(),
                        new Date(cookie.expiresAt()), cookie.secure(), cookie.httpOnly());
            }
            pull.setContinuous(true);
            List<String> channels = new ArrayList<>();
            channels.add(playerId);
            pull.setChannels(channels);

            Replication push = database.createPushReplication(urlProvider.sync());
            for (Cookie cookie : cookies) {
                push.setCookie(cookie.name(), cookie.value(), cookie.path(),
                        new Date(cookie.expiresAt()), cookie.secure(), cookie.httpOnly());
            }
            push.setContinuous(true);

            pull.start();
            push.start();
        } catch (Exception e) {
            eventBus.post(new AppErrorEvent(e));
        }
    }

    private void scheduleDailyChallenge() {
        sendBroadcast(new Intent(ScheduleDailyChallengeReminderReceiver.ACTION_SCHEDULE_DAILY_CHALLENGE_REMINDER));
    }

    @Subscribe
    public void onStartQuickAddEvent(StartQuickAddEvent e) {
        Intent intent = new Intent(this, QuickAddActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, e.additionalText);
        startActivity(intent);
    }

    @Subscribe
    public void onDailyChallengeStartTimeChanged(DailyChallengeStartTimeChangedEvent e) {
        scheduleDailyChallenge();
    }

    private void scheduleQuestsFor4WeeksAhead() {
        repeatingQuestPersistenceService.findAllNonAllDayActiveRepeatingQuests(this::scheduleRepeatingQuests);
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(this);
    }

    public static AppComponent getAppComponent(Context context) {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(context))
                    .build();
        }
        return appComponent;
    }

    @Subscribe
    public void onPlayerCreated(PlayerCreatedEvent e) {
        localStorage.saveInt(Constants.KEY_SCHEMA_VERSION, Constants.SCHEMA_VERSION);
        localStorage.saveString(Constants.KEY_PLAYER_ID, e.playerId);
        playerId = e.playerId;
        initAppStart();
    }

    @Subscribe
    public void onPlayerUpdated(PlayerUpdatedEvent e) {
        localStorage.saveInt(Constants.KEY_SCHEMA_VERSION, Constants.SCHEMA_VERSION);
        localStorage.saveString(Constants.KEY_PLAYER_ID, e.playerId);
        playerId = e.playerId;
    }

    @Subscribe
    public void onInitApp(InitAppEvent e) {
        initAppStart();
    }

    @Subscribe
    public void onShowQuestEvent(ShowQuestEvent e) {
        Intent i = new Intent(this, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        Quest q = e.quest;
        q.increaseCompletedCount();
        if (q.completedAllTimesForDay()) {
            QuestNotificationScheduler.cancelAll(q, this);
            q.setScheduledDate(LocalDate.now());
            q.setCompletedAtDate(LocalDate.now());
            q.setCompletedAtMinute(Time.now().toMinuteOfDay());
            q.setExperience(experienceRewardGenerator.generate(q));
            q.setCoins(coinsRewardGenerator.generate(q));
            questPersistenceService.save(q);
            onQuestComplete(q, e.source);
        } else {
            questPersistenceService.save(q);
            Toast.makeText(this, R.string.quest_complete, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onRemoveBaseQuestFromChallenge(RemoveBaseQuestFromChallengeEvent e) {
        BaseQuest bq = e.baseQuest;
        if (bq instanceof Quest) {
            Quest q = (Quest) bq;
            q.setChallengeId(null);
            questPersistenceService.save(q);
        } else {
            RepeatingQuest rq = (RepeatingQuest) bq;
            repeatingQuestPersistenceService.removeFromChallenge(rq);
        }
    }

    @Subscribe
    public void onUndoCompletedQuestRequest(UndoCompletedQuestRequestEvent e) {
        Quest quest = e.quest;
        quest.setDifficulty(null);
        quest.setActualStartDate(null);
        quest.setCompletedAtDate(null);
        quest.setCompletedAtMinute(null);
        if (quest.isScheduledForThePast()) {
            quest.setEndDate(null);
            quest.setStartDate(null);
            quest.setScheduledDate(null);
        }
        quest.setCompletedCount(0);
        Long xp = quest.getExperience();
        Long coins = quest.getCoins();
        quest.setExperience(null);
        quest.setCoins(null);
        questPersistenceService.save(quest);

        Player player = getPlayer();
        player.removeExperience(xp);
        if (shouldDecreaseLevel(player)) {
            player.setLevel(Math.max(Constants.DEFAULT_AVATAR_LEVEL, player.getLevel() - 1));
            while (shouldDecreaseLevel(player)) {
                player.setLevel(Math.max(Constants.DEFAULT_AVATAR_LEVEL, player.getLevel() - 1));
            }
            eventBus.post(new LevelDownEvent(player.getLevel()));
        }
        player.removeCoins(coins);

        updatePet(player.getPet(), (int) -Math.floor(xp / Constants.XP_TO_PET_HP_RATIO));
        playerPersistenceService.save(player);
        eventBus.post(new UndoCompletedQuestEvent(quest, xp, coins));
    }

    private Player getPlayer() {
        return playerPersistenceService.get();
    }

    private boolean shouldIncreaseLevel(Player player) {
        return new BigInteger(player.getExperience()).compareTo(ExperienceForLevelGenerator.forLevel(player.getLevel() + 1)) >= 0;
    }

    private boolean shouldDecreaseLevel(Player player) {
        return new BigInteger(player.getExperience()).compareTo(ExperienceForLevelGenerator.forLevel(player.getLevel())) < 0;
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = e.quest;
        quest.setDuration(Math.max(quest.getDuration(), Constants.QUEST_MIN_DURATION));

        if (quest.getEnd() != null) {
            if (quest.getEnd().hashCode() == quest.getStart().hashCode()) {
                quest.setScheduled(quest.getEnd());
            } else {
                Date scheduledDate = questScheduler.schedule(quest);
                quest.setScheduled(scheduledDate.getTime());
            }
            quest.setOriginalScheduled(quest.getScheduled());
        }

        if (quest.isScheduledForThePast()) {
            completeAtScheduledDate(quest);
        }

        if (quest.isCompleted()) {
            quest.setExperience(experienceRewardGenerator.generate(quest));
            quest.setCoins(coinsRewardGenerator.generate(quest));
        }
        Long eventID = localCalendar.onEventChange(quest);
        if (eventID != null) {
            quest.setEventID(eventID);
        }
        questPersistenceService.save(quest);
        if (quest.isCompleted()) {
            onQuestComplete(quest, e.source);
        }

        /*end*/
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
        Quest quest = e.quest;
        if (quest.isScheduledForThePast() && !quest.isCompleted()) {
            completeAtScheduledDate(quest);
        }
        if (quest.isCompleted()) {
            quest.setExperience(experienceRewardGenerator.generate(quest));
            quest.setCoins(coinsRewardGenerator.generate(quest));
        }
        localCalendar.onEventChange(quest);
        questPersistenceService.save(quest);
        if (quest.isCompleted()) {
            onQuestComplete(quest, e.source);
        }
    }

    private void completeAtScheduledDate(Quest quest) {
        int completedAtMinute = Time.now().toMinuteOfDay();
        if (quest.hasStartTime()) {
            completedAtMinute = quest.getStartMinute();
        }

        quest.setCompletedAt(quest.getScheduled());
        quest.setCompletedAtMinute(completedAtMinute);
        quest.increaseCompletedCount();
    }

    @Subscribe
    public void onUpdateRepeatingQuest(UpdateRepeatingQuestEvent e) {
        final RepeatingQuest repeatingQuest = e.repeatingQuest;
        LocalDate today = LocalDate.now();
        Recurrence.RepeatType repeatType = repeatingQuest.getRecurrence().getRecurrenceType();
        LocalDate periodStart;
        switch (repeatType) {
            case DAILY:
            case WEEKLY:
                periodStart = today.with(DayOfWeek.MONDAY);
                break;
            case MONTHLY:
                periodStart = today.withDayOfMonth(1);
                break;
            default:
                periodStart = today.withDayOfYear(1);
                break;
        }

        questPersistenceService.findAllUpcomingForRepeatingQuest(periodStart, repeatingQuest.getId(), questsSincePeriodStart -> {

            List<Quest> questsToRemove = new ArrayList<>();
            List<Quest> scheduledQuests = new ArrayList<>();

            for (Quest q : questsSincePeriodStart) {
                if (q.isCompleted() || q.getOriginalScheduledDate().isBefore(today)) {
                    scheduledQuests.add(q);
                } else {
                    questsToRemove.add(q);
                    QuestNotificationScheduler.cancelAll(q, this);
                }
            }

            long todayStartOfDay = DateUtils.toMillis(today);
            List<String> periodsToDelete = new ArrayList<>();
            for (String periodEnd : repeatingQuest.getScheduledPeriodEndDates().keySet()) {
                if (Long.valueOf(periodEnd) >= todayStartOfDay) {
                    periodsToDelete.add(periodEnd);
                }
            }
            repeatingQuest.getScheduledPeriodEndDates().keySet().removeAll(periodsToDelete);
            List<Quest> questsToCreate = repeatingQuestScheduler.schedule(repeatingQuest, today, scheduledQuests);
            repeatingQuestPersistenceService.update(repeatingQuest, questsToRemove, questsToCreate);
        });
    }

    @Subscribe
    public void onDeleteQuestRequest(DeleteQuestRequestEvent e) {
        QuestNotificationScheduler.cancelAll(e.quest, this);
        questPersistenceService.delete(e.quest);
        Toast.makeText(this, R.string.quest_deleted, Toast.LENGTH_SHORT).show();
    }

    private void onQuestComplete(Quest quest, EventSource source) {
        checkForDailyChallengeCompletion(quest);
        updateAvatar(quest);
        savePet((int) (Math.ceil(quest.getExperience() / Constants.XP_TO_PET_HP_RATIO)));
        eventBus.post(new QuestCompletedEvent(quest, source));
    }

    private void checkForDailyChallengeCompletion(Quest quest) {
        if (quest.getPriority() != Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
            return;
        }
        Date todayUtc = DateUtils.toStartOfDayUTC(LocalDate.now());
        Date lastCompleted = new Date(localStorage.readLong(Constants.KEY_DAILY_CHALLENGE_LAST_COMPLETED));
        boolean isCompletedForToday = todayUtc.equals(lastCompleted);
        if (isCompletedForToday) {
            return;
        }
        Set<Integer> challengeDays = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        if (!challengeDays.contains(currentDayOfWeek)) {
            return;
        }
        questPersistenceService.countAllCompletedWithPriorityForDate(Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY, LocalDate.now(), questCount -> {
            if (questCount != Constants.DAILY_CHALLENGE_QUEST_COUNT) {
                return;
            }
            localStorage.saveLong(Constants.KEY_DAILY_CHALLENGE_LAST_COMPLETED, todayUtc.getTime());

            long xp = experienceRewardGenerator.generateForDailyChallenge();
            long coins = coinsRewardGenerator.generateForDailyChallenge();
            Challenge dailyChallenge = new Challenge();
            dailyChallenge.setExperience(xp);
            dailyChallenge.setCoins(coins);
            updateAvatar(dailyChallenge);
            showChallengeCompleteDialog(getString(R.string.daily_challenge_complete_dialog_title), xp, coins);
            eventBus.post(new DailyChallengeCompleteEvent());
        });
    }

    private void showChallengeCompleteDialog(String title, long xp, long coins) {
        Intent intent = new Intent(this, ChallengeCompleteActivity.class);
        intent.putExtra(ChallengeCompleteActivity.TITLE, title);
        intent.putExtra(ChallengeCompleteActivity.EXPERIENCE, xp);
        intent.putExtra(ChallengeCompleteActivity.COINS, coins);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateAvatar(RewardProvider rewardProvider) {
        Player player = getPlayer();
        Long experience = rewardProvider.getExperience();
        player.addExperience(experience);
        increasePlayerLevelIfNeeded(player);
        player.addCoins(rewardProvider.getCoins());
        playerPersistenceService.save(player);
    }

    private void increasePlayerLevelIfNeeded(Player player) {
        if (shouldIncreaseLevel(player)) {
            player.setLevel(player.getLevel() + 1);
            while (shouldIncreaseLevel(player)) {
                player.setLevel(player.getLevel() + 1);
            }
            Intent intent = new Intent(this, LevelUpActivity.class);
            intent.putExtra(LevelUpActivity.LEVEL, player.getLevel());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            eventBus.post(new LevelUpEvent(player.getLevel()));
        }
    }

    @Subscribe
    public void onNewRepeatingQuest(NewRepeatingQuestEvent e) {
        RepeatingQuest repeatingQuest = e.repeatingQuest;
        repeatingQuest.setDuration(Math.max(repeatingQuest.getDuration(), Constants.QUEST_MIN_DURATION));
        List<Quest> quests = repeatingQuestScheduler.schedule(repeatingQuest, LocalDate.now());
        repeatingQuestPersistenceService.saveWithQuests(repeatingQuest, quests);
    }

    @Subscribe
    public void onDeleteRepeatingQuestRequest(final DeleteRepeatingQuestRequestEvent e) {
        repeatingQuestPersistenceService.delete(e.repeatingQuest);
    }

    private void scheduleRepeatingQuests(List<RepeatingQuest> repeatingQuests) {
        Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests = new HashMap<>();
        for (RepeatingQuest repeatingQuest : repeatingQuests) {
            List<Quest> quests = repeatingQuestScheduler.schedule(repeatingQuest, LocalDate.now());
            repeatingQuestToScheduledQuests.put(repeatingQuest, quests);
        }
        repeatingQuestPersistenceService.saveWithQuests(repeatingQuestToScheduledQuests);
    }

    private void scheduleNextReminder() {
        sendBroadcast(new Intent(ScheduleNextRemindersReceiver.ACTION_SCHEDULE_REMINDERS));
    }

    @Subscribe
    public void onDeleteChallengeRequest(DeleteChallengeRequestEvent e) {
        Challenge challenge = e.challenge;
        challengePersistenceService.delete(challenge, e.shouldDeleteQuests);
    }

    @Subscribe
    public void onCompleteChallengeRequest(CompleteChallengeRequestEvent e) {
        Challenge challenge = e.challenge;
        challenge.setCompletedAtDate(new Date());
        challenge.setExperience(experienceRewardGenerator.generate(challenge));
        challenge.setCoins(coinsRewardGenerator.generate(challenge));
        challengePersistenceService.save(challenge);
        onChallengeComplete(challenge, e.source);
    }

    private void onChallengeComplete(Challenge challenge, EventSource source) {
        updateAvatar(challenge);
        savePet((int) (Math.floor(challenge.getExperience() / Constants.XP_TO_PET_HP_RATIO)));
        showChallengeCompleteDialog(getString(R.string.challenge_complete, challenge.getName()), challenge.getExperience(), challenge.getCoins());
        eventBus.post(new ChallengeCompletedEvent(challenge, source));
    }

    private void requestWidgetUpdate() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, AgendaWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_agenda_list);
    }

    @Subscribe
    public void onNewChallenge(NewChallengeEvent e) {
        challengePersistenceService.save(e.challenge);
    }

    @Subscribe
    public void onUpdateChallenge(UpdateChallengeEvent e) {
        challengePersistenceService.save(e.challenge);
    }

    private void scheduleDateChanged() {
        Intent i = new Intent(DateChangedReceiver.ACTION_DATE_CHANGED);
        PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(this, i);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        long notificationTime = DateUtils.toStartOfDay(LocalDate.now().plusDays(1)).getTime() + 5000L;
        if (Build.VERSION.SDK_INT > 22) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else {
            //todo check android > 21
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }

    @Subscribe
    public void onDateChanged(DateChangedEvent e) {
        questPersistenceService.findAllNonAllDayForDate(LocalDate.now().minusDays(1), quests -> {
            savePet(-getDecreasePercentage(quests));
            scheduleQuestsFor4WeeksAhead();
            clearIncompleteQuests();
            scheduleDateChanged();
            listenForChanges();
            eventBus.post(new CalendarDayChangedEvent(LocalDate.now(), CalendarDayChangedEvent.Source.DATE_CHANGE));
        });
    }

    private void clearIncompleteQuests() {
        questPersistenceService.findAllIncompleteNotFromRepeatingBefore(LocalDate.now(), quests -> {
            for (Quest q : quests) {
                if (q.isStarted()) {
                    q.setScheduledDate(LocalDate.now());
                    q.setStartMinute(0);
                } else {
                    q.setScheduledDate(null);
                }
                if (q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
                    q.setPriority(null);
                }
            }
            questPersistenceService.save(quests);
        });
    }

    public static String getPlayerId() {
        return playerId;
    }

    public static boolean hasPlayer() {
        return !StringUtils.isEmpty(playerId);
    }

    public static List<PredefinedChallenge> getPredefinedChallenges() {
        List<PredefinedChallenge> challenges = new ArrayList<>();

        Challenge c = new Challenge("Stress-Free Mind");
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Be more focused");
        c.setReason2("Be relaxed");
        c.setReason3("Be healthy");
        c.setExpectedResult1("Concentrate for at least 30 min at a time");
        c.setExpectedResult2("Completely stop anxiety");
        c.setExpectedResult3("Lose 5 pounds");
        challenges.add(new PredefinedChallenge(c, "Be mindful and stay in the flow longer", R.drawable.challenge_02, R.drawable.challenge_expanded_02));

        c = new Challenge("Weight Cutter");
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Feel great");
        c.setReason2("Become more confident");
        c.setReason3("Become healthier");
        c.setExpectedResult1("Lose 10 pounds");
        c.setExpectedResult2("Reduce wear size");
        c.setExpectedResult3("Love working out");
        challenges.add(new PredefinedChallenge(c, "Start shedding some weight and feel great", R.drawable.challenge_01, R.drawable.challenge_expanded_01));


        c = new Challenge("Healthy & Fit");
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Be healthier");
        c.setReason2("Stay fit");
        c.setReason3("Feel great");
        c.setExpectedResult1("Lose 5 pounds");
        c.setExpectedResult2("Concentrate for at least 30 min at a time");
        c.setExpectedResult3("Workout 3 times a week");
        challenges.add(new PredefinedChallenge(c, "Keep working out and live healthier life", R.drawable.challenge_03, R.drawable.challenge_expanded_03));

        c = new Challenge("English Jedi");
        c.setCategoryType(Category.LEARNING);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Learn to read great books");
        c.setReason2("Participate in conversations");
        c.setReason3("Meet & speak with new people");
        c.setExpectedResult1("Read a book");
        c.setExpectedResult2("Understand movies");
        c.setExpectedResult3("Write an essey in English");
        challenges.add(new PredefinedChallenge(c, "Advance your English skills", R.drawable.challenge_04, R.drawable.challenge_expanded_04));

        c = new Challenge("Programming Ninja");
        c.setCategoryType(Category.LEARNING);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Learn to command my computer");
        c.setReason2("Understand technologies better");
        c.setReason3("Find new job");
        c.setExpectedResult1("Write simple webpage");
        c.setExpectedResult2("Understand what code editors are");
        c.setExpectedResult3("Understand how Internet works");
        challenges.add(new PredefinedChallenge(c, "Learn the fundamentals of computer programming", R.drawable.challenge_05, R.drawable.challenge_expanded_05));

        c = new Challenge("Master Presenter");
        c.setCategoryType(Category.WORK);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Better present my ideas");
        c.setReason2("Become more confident");
        c.setReason3("Explain better");
        c.setExpectedResult1("Prepare a presentation");
        c.setExpectedResult2("Present in front of an audience");
        c.setExpectedResult3("Upload my presentation on the Internet");
        challenges.add(new PredefinedChallenge(c, "Learn how to create and present effectively", R.drawable.challenge_06, R.drawable.challenge_expanded_06));

        c = new Challenge("Famous writer");
        c.setCategoryType(Category.WORK);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Better present my ideas");
        c.setReason2("Become more confident");
        c.setReason3("Meet new people");
        c.setExpectedResult1("Have a blog");
        c.setExpectedResult2("100 readers per month");
        c.setExpectedResult3("Write 5 blog posts");
        challenges.add(new PredefinedChallenge(c, "Learn how to become great writer & blogger", R.drawable.challenge_07, R.drawable.challenge_expanded_07));

        c = new Challenge("Friends & Family time");
        c.setCategoryType(Category.PERSONAL);
        c.setDifficultyType(Difficulty.NORMAL);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1("Feel more connected to others");
        c.setReason2("Have more fun");
        c.setReason3("Stay close with family");
        c.setExpectedResult1("Spend more time with family");
        c.setExpectedResult2("Go out more often");
        c.setExpectedResult3("See friends more often");
        challenges.add(new PredefinedChallenge(c, "Connect with your friends and family", R.drawable.challenge_08, R.drawable.challenge_expanded_08));

        return challenges;
    }

    interface AccessTokenListener {
        void onAccessTokenReceived(String accessToken);
    }

    public static LocalCalendar getLocalCalendar() {
        return localCalendar;
    }
}