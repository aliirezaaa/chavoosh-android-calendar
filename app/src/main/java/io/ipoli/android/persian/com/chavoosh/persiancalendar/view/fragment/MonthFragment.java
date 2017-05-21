package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.ViewTarget;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.Constants;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.adapter.MonthAdapter;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.entity.DayEntity;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.fragment.CalendarFragment;


public class MonthFragment extends Fragment implements View.OnClickListener {
    private Utils utils;
    private CalendarFragment calendarFragment;
    private PersianDate persianDate;
    private int offset;
    private MonthAdapter adapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        utils = Utils.getInstance(getContext());
        View view = inflater.inflate(R.layout.fragment_month, container, false);

        offset = getArguments().getInt(Constants.OFFSET_ARGUMENT);
        List<DayEntity> days = utils.getDays(offset);
        setBackgroundImage(view, offset);
        Log.i("sweep", Integer.toString(offset));
        AppCompatImageView prev = (AppCompatImageView) view.findViewById(R.id.prev);
        AppCompatImageView next = (AppCompatImageView) view.findViewById(R.id.next);
        prev.setOnClickListener(this);
        next.setOnClickListener(this);

        persianDate = utils.getToday();
        int month = persianDate.getMonth() - offset;
        month -= 1;
        int year = persianDate.getYear();

        year = year + (month / 12);
        month = month % 12;
        if (month < 0) {
            year -= 1;
            month += 12;
        }

        month += 1;
        persianDate.setMonth(month);
        persianDate.setYear(year);
        persianDate.setDayOfMonth(1);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.RecyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MonthAdapter(getContext(), this, days);
        recyclerView.setAdapter(adapter);

        calendarFragment = (CalendarFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(CalendarFragment.class.getName());

        if (offset == 0 && calendarFragment.getViewPagerPosition() == offset) {
            calendarFragment.selectDay(utils.getToday());
            updateTitle();
        }

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(setCurrentMonthReceiver,
                new IntentFilter(Constants.BROADCAST_INTENT_TO_MONTH_FRAGMENT));

        return view;
    }

    private void setBackgroundImage(View view, int offset) {
        Toolbar t = utils.getToolbar();
        int[] images = {R.drawable.url1,
                R.drawable.url2,
                R.drawable.url3,
                R.drawable.url4,
                R.drawable.url5,
                R.drawable.url6,
                R.drawable.url7,
                R.drawable.url8,
                R.drawable.url9,
                R.drawable.url10,
                R.drawable.url11,
                R.drawable.url12};
        int[] colors = {R.color.color1, R.color.color2, R.color.color3, R.color.color4};
        int imgNum;
        if (offset < 0) {
            offset = offset * -1;
            if (offset >= 12) {
                imgNum = 0;
            } else {
                imgNum = offset;
            }
        } else if (offset >= 12) {
            imgNum = 0;
        } else {
            imgNum = offset;
        }

//        view.setBackgroundResource(images[imgNum]);
//glide
        ImageView month_bac_img = (ImageView) view.findViewById(R.id.month_bac_img);

        Glide.with(getContext()).load(images[imgNum])
                .crossFade()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(month_bac_img);
//        t.setBackgroundColor(getResources().getColor(colors[imgNum]));
    }

    private BroadcastReceiver setCurrentMonthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getExtras().getInt(Constants.BROADCAST_FIELD_TO_MONTH_FRAGMENT);
            if (value == offset) {
                updateTitle();

                int day = intent.getExtras().getInt(Constants.BROADCAST_FIELD_SELECT_DAY);
                if (day != -1) {
                    adapter.selectDay(day);
                }

                utils.checkYearAndWarnIfNeeded(persianDate.getYear());

            } else if (value == Constants.BROADCAST_TO_MONTH_FRAGMENT_RESET_DAY) {
                adapter.clearSelectedDay();
            }
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(setCurrentMonthReceiver);
        super.onDestroy();
    }

    public void onClickItem(PersianDate day) {
        calendarFragment.selectDay(day);
    }

    public void onLongClickItem(PersianDate day) {
        calendarFragment.addEventOnCalendar(day);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.next:
                calendarFragment.changeMonth(1);
                break;

            case R.id.prev:
                calendarFragment.changeMonth(-1);
                break;
        }
    }

    private void updateTitle() {
        utils.setActivityTitleAndSubtitle(
                getActivity(),
                utils.getMonthName(persianDate),
                utils.formatNumber(persianDate.getYear()));

    }

}
