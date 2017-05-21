package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.activity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.Constants;

//import io.ipoli.android.persian.com.chavoosh.persiancalendar.adapter.DrawerAdapter;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.service.ApplicationService;
//import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.UpdateUtils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment.ApplicationPreferenceFragment;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment.CalendarFragment;
import me.cheshmak.android.sdk.core.Cheshmak;
import me.cheshmak.android.sdk.core.CheshmakConfig;
//import com.google.android.gms.analytics.HitBuilders;
//import com.google.android.gms.analytics.Tracker;

//import me.cheshmak.android.sdk.core.Cheshmak;
//import me.cheshmak.android.sdk.core.CheshmakConfig;


/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class PersianCalendarActivity extends BaseActivity {
//    @BindView(R.id.submit_img)
//    ImageView submit_img;

    private static final int CALENDAR = 1;
    private static final int CONVERTER = 2;
    //    private UpdateUtils updateUtils;
    private static final int COMPASS = 3;
    //    private DrawerAdapter adapter;
    private static final int PREFERENCE = 4;
    private static final int ABOUT = 5;
    private static final int EXIT = 6;
    // Default selected fragment
    private static final int DEFAULT = CALENDAR;
    private final String TAG = PersianCalendarActivity.class.getName();
    public boolean dayIsPassed = false;
    private Utils utils;
    private DrawerLayout drawerLayout;
    private Class<?>[] fragments = {
            null,
            CalendarFragment.class,
//            ConverterFragment.class,
//            CompassFragment.class,
            ApplicationPreferenceFragment.class,
//            AboutFragment.class
    };
    private int menuPosition = 0; // it should be zero otherwise #selectItem won't be called
    private String lastLocale;

    //    private Tracker mTracker;
    private String lastTheme;
    private BroadcastReceiver dayPassedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dayIsPassed = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        utils = Utils.getInstance(getApplicationContext());
//        utils.setTheme(this);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
//        utils.changeAppLanguage(this);
//        utils.loadLanguageResource();
//        lastLocale = utils.getAppLanguage();
//        lastTheme = utils.getTheme();
//        updateUtils = UpdateUtils.getInstance(getApplicationContext());

//        if (!Utils.getInstance(this).isServiceRunning(ApplicationService.class)) {
//            startService(new Intent(getBaseContext(), ApplicationService.class));
//        }

//        updateUtils.update(true);

        //Start init cheshmak push notification

        //End init cheshmak push notification


//        googleAnalyticsInit();


        setContentView(R.layout.activity_cal_fa);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
//        toolbar.setBackgroundColor(Color.CYAN);
        setSupportActionBar(toolbar);
        utils.setToolbar(toolbar);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        } else {
            toolbar.setPadding(0, 0, 0, 0);
        }

//        RecyclerView navigation = (RecyclerView) findViewById(R.id.navigation_view);
//        navigation.setHasFixedSize(true);
//        adapter = new DrawerAdapter(this);
//        navigation.setAdapter(adapter);
//
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//        navigation.setLayoutManager(layoutManager);

//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
//        final View appMainView = findViewById(R.id.app_main_layout);
//        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
//            int slidingDirection = +1;
//
//            {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    if (isRTL())
//                        slidingDirection = -1;
//                }
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                super.onDrawerClosed(drawerView);
//            }
//
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                super.onDrawerSlide(drawerView, slideOffset);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                    slidingAnimation(drawerView, slideOffset);
//                }
//            }
//
//            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//            private void slidingAnimation(View drawerView, float slideOffset) {
//                appMainView.setTranslationX(slideOffset * drawerView.getWidth() * slidingDirection);
//                drawerLayout.bringChildToFront(drawerView);
//                drawerLayout.requestLayout();
//            }
//        };

//        drawerLayout.addDrawerListener(drawerToggle);
//        drawerToggle.syncState();
//
        selectItem(DEFAULT);

        LocalBroadcastManager.getInstance(this).registerReceiver(dayPassedReceiver,
                new IntentFilter(Constants.LOCAL_INTENT_DAY_PASSED));


        ImageButton submit_img = (ImageButton) findViewById(R.id.submit_img);
        submit_img.setOnClickListener(v -> {
            Log.i("selected date ", utils.dateToString(utils.getSelectedPersianDate()));
            onClose();

        });
    }

    @Override
    public void finish() {
//        Bundle bundle=new Bundle();

        Intent data = new Intent();
//        data.putExtra(io.ipoli.android.Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, DateUtils.toMillis(selectedDate));
        setResult(RESULT_OK, data);
        super.finish();
    }



//    private void googleAnalyticsInit() {
//        // Obtain the shared Tracker instance.
//        AnalyticsUtils application = (AnalyticsUtils) getApplication();
//        mTracker = application.getDefaultTracker();
//        Log.i(TAG, "Setting screen name: " + "");
//        mTracker.setScreenName("screen" + "main");
//        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
//
//    }

    @Override
    public void onBackPressed() {
        onClose();
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        utils.changeAppLanguage(this);
////        View v = findViewById(R.id.drawer);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            v.setLayoutDirection(isRTL() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
//        }
//    }

    private void onClose() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_bottom);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isRTL() {
        return getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dayIsPassed) {
            dayIsPassed = false;
            restartActivity();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dayPassedReceiver);
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Checking for the "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void beforeMenuChange(int position) {
        if (position != menuPosition) {
            // reset app lang on menu changes, ugly hack but it seems is needed
//            utils.changeAppLanguage(getApplicationContext());
        }

        // only if we are returning from preferences
        if (menuPosition != PREFERENCE)
            return;

        utils.updateStoredPreference();
//        updateUtils.update(true);

        boolean needsActivityRestart = false;

        String locale = utils.getAppLanguage();
        if (!locale.equals(lastLocale)) {
            lastLocale = locale;
            utils.changeAppLanguage(this);
            utils.loadLanguageResource();
            needsActivityRestart = true;
        }

        if (!lastTheme.equals(utils.getTheme())) {
            needsActivityRestart = true;
            lastTheme = utils.getTheme();
        }

        if (needsActivityRestart)
            restartActivity();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void selectItem(int item) {
        if (item == EXIT) {
            finish();
            return;
        }

//        beforeMenuChange(item);
        if (menuPosition != item) {
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.content_container,
                                (Fragment) fragments[item].newInstance(),
                                fragments[item].getName()
                        ).commit();
                menuPosition = item;
            } catch (Exception e) {
                Log.e(TAG, item + " is selected as an index", e);
            }
        }

//        adapter.setSelectedItem(menuPosition);

//        drawerLayout.closeDrawers();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE)
//            LocalBroadcastManager.getInstance(this).sendBroadcast(
//                    new Intent(Constants.LOCATION_PERMISSION_RESULT));
//    }


}
