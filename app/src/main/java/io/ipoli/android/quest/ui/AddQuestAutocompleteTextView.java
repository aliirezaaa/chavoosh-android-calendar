package io.ipoli.android.quest.ui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.AutoCompleteTextView;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/18/16.
 */
public class AddQuestAutocompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    public AddQuestAutocompleteTextView(Context context) {
        super(context);
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public AddQuestAutocompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
        super(context,attrs,defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }

    @Override
    protected void replaceText(CharSequence text) {

    }
}
