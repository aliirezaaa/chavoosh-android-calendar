package io.ipoli.android.app.tutorial;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.FinishTutorialActivityEvent;
import io.ipoli.android.app.tutorial.events.TutorialSkippedEvent;
import io.ipoli.android.app.tutorial.fragments.TutorialFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TutorialActivity extends AppIntro2 {

    @Inject
    Bus eventBus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(this).inject(this);
//todo check android sdk >21
//        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_welcome_title),
                getString(R.string.tutorial_hero_desc),
                R.drawable.tutorial_welcome,
                R.color.md_blue_700));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_challenges_title),
                getString(R.string.tutorial_challenges_desc),
                R.drawable.tutorial_challenge,
                R.color.md_light_blue_500));

        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_schedule_title),
                getString(R.string.tutorial_schedule_desc),
                R.drawable.tutorial_schedule,
                R.color.md_green_500));

        setImmersiveMode(true, true);
        setColorTransitionsEnabled(true);
        showSkipButton(false);
    }

    @Override
    public void onDonePressed(Fragment fragment) {
        doneButton.setVisibility(View.GONE);
        onFinish();
    }

    @Override
    public void onBackPressed() {
        eventBus.post(new TutorialSkippedEvent());
        onFinish();
    }

    private void onFinish() {
        eventBus.post(new FinishTutorialActivityEvent());
        finish();
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
