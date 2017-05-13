package io.ipoli.android.quest.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ibm.icu.util.ULocale;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.dialogs.DatePickerFragment;
import io.ipoli.android.persian.calendar.CivilDate;
import io.ipoli.android.persian.calendar.DateConverter;
import io.ipoli.android.persian.calendar.PersianDate;
import io.ipoli.android.quest.adapters.QuestOptionsAdapter;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.events.NewQuestDatePickedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class AddQuestDateFragment extends BaseFragment implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    @BindView(R.id.new_quest_date_options)
    RecyclerView dateOptions;

    @BindView(R.id.new_quest_date_image)
    ImageView image;

    private Unbinder unbinder;
    private DatePickerDialog dpd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_date, container, false);
        unbinder = ButterKnife.bind(this, view);

        dateOptions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        dateOptions.setHasFixedSize(true);

        List<Pair<String, View.OnClickListener>> options = new ArrayList<>();

        options.add(new Pair<>(getString(R.string.by_the_end_of_week), v ->
                postEvent(new NewQuestDatePickedEvent(LocalDate.now(), LocalDate.now().with(DayOfWeek.SUNDAY)))));

        options.add(new Pair<>(getString(R.string.by_the_end_of_month), v ->
                postEvent(new NewQuestDatePickedEvent(LocalDate.now(), LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())))));

        options.add(new Pair<>(getString(R.string.someday_by), v -> {
            DatePickerFragment fragment = DatePickerFragment.newInstance(LocalDate.now(), true, false,
                    date -> postEvent(new NewQuestDatePickedEvent(LocalDate.now(), date)));
            fragment.show(getFragmentManager());
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
            pickDate();
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

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }


    private void pickDate() {
        com.ibm.icu.util.Calendar now = com.ibm.icu.util.Calendar.getInstance(new ULocale("fa_IR"));
//        Log.i("calendar.now ",
//                "" + now.get(com.ibm.icu.util.Calendar.YEAR)
//                +""+ now.get(com.ibm.icu.util.Calendar.MONTH)+
//                ""+  now.get(com.ibm.icu.util.Calendar.DAY_OF_MONTH));

        dpd = DatePickerDialog.newInstance(
                AddQuestDateFragment.this,
                now.get(com.ibm.icu.util.Calendar.YEAR),
                now.get(com.ibm.icu.util.Calendar.MONTH),
                now.get(com.ibm.icu.util.Calendar.DAY_OF_MONTH)
        );


        dpd.setThemeDark(false);
        dpd.vibrate(true);
        dpd.dismissOnPause(true);
        dpd.showYearPickerFirst(false);
        if (true) {
            dpd.setAccentColor(Color.parseColor("#9C27B0"));
        }
        if (true) {
            dpd.setTitle("DatePicker Title");
        }
        if (false) {
            com.ibm.icu.util.Calendar[] dates = new com.ibm.icu.util.Calendar[13];
            for (int i = -6; i <= 6; i++) {
                com.ibm.icu.util.Calendar date = com.ibm.icu.util.Calendar.getInstance(new ULocale("fa_IR"));
                date.add(com.ibm.icu.util.Calendar.MONTH, i);
                dates[i + 6] = date;
            }
            dpd.setSelectableDays(dates);
        }
        if (false) {
            com.ibm.icu.util.Calendar[] dates = new com.ibm.icu.util.Calendar[13];
            for (int i = -6; i <= 6; i++) {
                com.ibm.icu.util.Calendar date = com.ibm.icu.util.Calendar.getInstance(new ULocale("fa_IR"));
                date.add(com.ibm.icu.util.Calendar.WEEK_OF_YEAR, i);
                dates[i + 6] = date;
            }
            dpd.setHighlightedDays(dates);
        }
//        dpd.initialize();
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
//        String date = "You picked the following date: " + dayOfMonth + "/" + (++monthOfYear) + "/" + year;
        PersianDate pDate = new PersianDate(year, ++monthOfYear, dayOfMonth);
        //convert to civil date and then convert to local
        CivilDate civilDate = DateConverter.persianToCivil(pDate);
        LocalDate lDate = LocalDate.of(civilDate.getYear(), civilDate.getMonth(), civilDate.getDayOfMonth());

        //post to next fragment
        postEvent(new NewQuestDatePickedEvent(lDate, lDate));
//        dateTextView.setText(date);
//        Log.i("arguments ", "" + year + monthOfYear + dayOfMonth);
//        Log.i("civil date ", "" + civilDate.getYear() + civilDate.getMonth() + civilDate.getDayOfMonth());
//        Log.i("persian date ", "" + pDate.getYear() + pDate.getMonth() + pDate.getDayOfMonth());

        Log.i("local date ", "" + lDate.getYear() + lDate.getMonth() + lDate.getDayOfMonth());

    }
}
