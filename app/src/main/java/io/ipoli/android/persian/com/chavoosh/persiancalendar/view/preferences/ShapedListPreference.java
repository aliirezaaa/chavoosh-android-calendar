package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.preferences;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;


/**
 * Created by ebraminio on 2/16/16.
 */
public class ShapedListPreference extends ListPreference {
    public ShapedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShapedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShapedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapedListPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
//        Utils.getInstance(getContext()).setFontAndShape(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        summaryView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//        titleView.setTextColor(Color.RED);
    }

    public void setSelected(String selected) {
        final boolean wasBlocking = shouldDisableDependents();
        persistString(selected);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    String defaultValue = "";

    // steal default value, well, not aware of a better way
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        this.defaultValue = (String) defaultValue;
    }

    public String getSelected() {
        return getPersistedString(defaultValue);
    }
}
