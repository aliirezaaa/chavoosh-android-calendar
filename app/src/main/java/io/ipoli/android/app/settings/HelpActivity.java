package io.ipoli.android.app.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class HelpActivity extends AppCompatActivity {

    @BindView(R.id.sec1txt)
    TextView sec1txt;
    @BindView(R.id.sec2txt)
    TextView sec2txt;
    @BindView(R.id.sec3txt)
    TextView sec3txt;
    @BindView(R.id.sec4txt)
    TextView sec4txt;
    @BindView(R.id.sec6txt)
    TextView sec6_webSite;


    @BindView(R.id.img_sec1)
    ImageView imgSec1;

    @BindView(R.id.img_sec2)
    ImageView imgSec2;

    @BindView(R.id.img_sec3)
    ImageView imgSec3;

    @BindView(R.id.img_sec4)
    ImageView imgSec4;

    @BindView(R.id.img_sec5)
    ImageView imgSec5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Yekan.ttf");
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        ButterKnife.bind(this);
        sec1txt.setTypeface(typeface);
        sec2txt.setTypeface(typeface);
        sec3txt.setTypeface(typeface);
        sec4txt.setTypeface(typeface);
        sec6_webSite.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.chavoosh.com"));
            startActivity(browserIntent);
        });
        setBackgroundImage();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setBackgroundImage() {

        int[] images = {R.drawable.about1,
                R.drawable.about2,
                R.drawable.about3,
                R.drawable.about4,
                R.drawable.about5,
        };

        List<ImageView> image_list = new ArrayList<ImageView>();
        image_list.add(imgSec1);
        image_list.add(imgSec2);
        image_list.add(imgSec3);
        image_list.add(imgSec4);
        image_list.add(imgSec5);
        for (ImageView imgView : image_list) {
            Glide.with(getApplicationContext()).load(images[image_list.indexOf(imgView)])
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imgView);
        }

    }
}
