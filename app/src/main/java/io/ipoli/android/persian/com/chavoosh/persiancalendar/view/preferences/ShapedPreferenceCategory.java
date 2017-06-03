package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.preferences;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;

import static io.ipoli.android.R.string.view;


/**
 * Created by ebraminio on 2/16/16.
 */
public class ShapedPreferenceCategory extends PreferenceCategory {
    public ShapedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShapedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShapedPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapedPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setTextColor(Color.RED);
//        Utils.getInstance(getContext()).setFontAndShape(holder);
    }
}
