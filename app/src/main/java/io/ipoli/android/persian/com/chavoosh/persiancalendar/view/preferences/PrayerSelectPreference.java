package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.preferences;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;


import java.util.Set;

import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;

public class PrayerSelectPreference extends DialogPreference {
    Utils utils;

    public PrayerSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        utils = Utils.getInstance(context);
    }

    public void setPrayers(Set<String> prayers) {
        final boolean wasBlocking = shouldDisableDependents();
        persistString(utils.setToCommaSeparated(prayers));
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    public Set<String> getPrayers() {
        return utils.commaSeparatedToSet(getPersistedString(""));
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//        utils.setFontAndShape(holder);
    }
}
