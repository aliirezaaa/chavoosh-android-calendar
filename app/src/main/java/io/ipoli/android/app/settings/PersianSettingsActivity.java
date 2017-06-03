package io.ipoli.android.app.settings;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment.ApplicationPreferenceFragment;


public class PersianSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persian_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("تنظیمات اوقات شرعی");
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        /*start persian fragment*/
        Fragment fragment=new ApplicationPreferenceFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.persian_settings_container, fragment, fragment.getClass().getName()).commit();

        getSupportFragmentManager().executePendingTransactions();
    }
    @Override
    protected void onPause(){
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_bottom);

        super.onPause();
    }

}
