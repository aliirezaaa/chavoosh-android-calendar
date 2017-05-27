package io.ipoli.android.quest.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.persian.calendar.DateConverter;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.quest.adapters.QuestOptionsAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.events.NewQuestDatePickedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddQuestDateFragment extends BaseFragment  {

    @BindView(R.id.new_quest_date_options)
    RecyclerView dateOptions;

    @BindView(R.id.new_quest_date_image)
    ImageView image;

    private Unbinder unbinder;
    private DatePickerDialog dpd;
    private boolean isSomeday;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_date, container, false);
        unbinder = ButterKnife.bind(this, view);

        dateOptions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        dateOptions.setHasFixedSize(true);
        initLocalDateBroadCast();
        List<Pair<String, View.OnClickListener>> options = new ArrayList<>();

        options.add(new Pair<>(getString(R.string.by_the_end_of_week), v ->
                postEvent(new NewQuestDatePickedEvent(LocalDate.now(), LocalDate.now().with(DayOfWeek.FRIDAY)))
        ));

        options.add(new Pair<>(getString(R.string.by_the_end_of_month), v ->
                postEvent(new NewQuestDatePickedEvent(LocalDate.now(), Utils.getInstance(getContext()).getEndOfPersianMonth()))));

        options.add(new Pair<>(getString(R.string.someday_by), v -> {
//            DatePickerFragment fragment = DatePickerFragment.newInstance(LocalDate.now(), true, false,
//                    date -> postEvent(new NewQuestDatePickedEvent(LocalDate.now(), date)));
//            fragment.show(getFragmentManager());
            isSomeday = true;
            Utils.getInstance(getContext()).pickDate(AddQuestDateFragment.this, "ON_DATE_SET_FOR_QUEST");
        }));

        options.add(new Pair<>(getString(R.string.today), v ->
                postEvent(new NewQuestDatePickedEvent(LocalDate.now(), LocalDate.now()))));

        options.add(new Pair<>(getString(R.string.tomorrow), v ->
                postEvent(new NewQuestDatePickedEvent(LocalDate.now().plusDays(1), LocalDate.now().plusDays(1)))));

        options.add(new Pair<>(getString(R.string.on), v -> {

//            DatePickerFragment fragment = DatePickerFragment.newInstance(LocalDate.now(), true, false,
//                    date -> {
////                        postEvent(new NewQuestDatePickedEvent(date, date));
//                        Log.i("on", "" + date.getYear() + " " + date.getMonth() + " " + date.getDayOfMonth() + "");
//                        Log.i("instance of ", "" + date.getClass());
//
//                    });
//            fragment.show(getFragmentManager());
            isSomeday = false;
            Utils.getInstance(getContext()).pickDate(AddQuestDateFragment.this, "ON_DATE_SET_FOR_QUEST");
//            pickDate();
        }));

        options.add(new Pair<>(getString(R.string.do_not_know), v -> {
            postEvent(new NewQuestDatePickedEvent(null, null));
        }));

        dateOptions.setAdapter(new QuestOptionsAdapter(options));


        return view;
    }

    public void setCategory(Category category) {
        switch (category) {
            case LEARNING:
                image.setImageResource(R.drawable.new_learning_quest);
                break;
            case WELLNESS:
                image.setImageResource(R.drawable.new_wellness_quest);
                break;
            case WORK:
                image.setImageResource(R.drawable.new_work_quest);
                break;
            case PERSONAL:
                image.setImageResource(R.drawable.new_personal_quest);
                break;
            case FUN:
                image.setImageResource(R.drawable.new_fun_quest);
                break;
            case CHORES:
                image.setImageResource(R.drawable.new_chores_quest);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
    private void initLocalDateBroadCast() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(dateReceiver,
                new IntentFilter("ON_DATE_SET_FOR_QUEST"));
    }
    private BroadcastReceiver dateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int year = intent.getIntExtra("year", 0);
            int month = intent.getIntExtra("month", 0);
            int day = intent.getIntExtra("day", 0);
            Log.d("receiver", "Got message: " + year);
            Log.d("receiver", "Got message: " + month);
            Log.d("receiver", "Got message: " + day);

            PersianDate pDate=new PersianDate(year, month, day);

            //post to next fragment
            if (isSomeday == false) {
                //if on... selected
                postEvent(new NewQuestDatePickedEvent(DateConverter.persianToLocalDate(pDate), DateConverter.persianToLocalDate(pDate)));
            } else {
                postEvent(new NewQuestDatePickedEvent(LocalDate.now(), DateConverter.persianToLocalDate(pDate)));
            }

        }
    };

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }




}
