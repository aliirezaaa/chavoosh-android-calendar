package io.ipoli.android.quest.suggestions.providers;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.MatcherType;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.TextEntityType;


/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class MainSuggestionsProvider implements SuggestionsProvider {
    private Context context;

    private Set<MatcherType> usedTypes = new HashSet<>();

    private Set<TextEntityType> disabledEntityTypes = new HashSet<>();

    public void addUsedMatcherType(MatcherType matcherType) {
        usedTypes.add(matcherType);
    }

    public void removeUsedMatcherType(MatcherType matcherType) {
        usedTypes.remove(matcherType);
    }

    public Set<MatcherType> getUsedTypes() {
        return usedTypes;
    }

    public MainSuggestionsProvider(Set<TextEntityType> disabledEntityTypes) {
        this.disabledEntityTypes = disabledEntityTypes;
    }

    public MainSuggestionsProvider() {
        this(new HashSet<>());
    }


    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        context = MainActivity.getContext();
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();

        if (!usedTypes.contains(MatcherType.DATE) && !disabledEntityTypes.contains(TextEntityType.DUE_DATE)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_event_black_18dp, context.getString(R.string.on_date), "on"));
        }
        if (!usedTypes.contains(MatcherType.TIME) && !disabledEntityTypes.contains(TextEntityType.START_TIME)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_clock_black_24dp, context.getApplicationContext().getString(R.string.at_time), "at"));
        }
        if (!usedTypes.contains(MatcherType.DURATION) && !disabledEntityTypes.contains(TextEntityType.DURATION)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_timer_black_18dp, context.getApplicationContext().getString(R.string.for_while), "for"));
        }
        if (!usedTypes.contains(MatcherType.DATE) && !disabledEntityTypes.contains(TextEntityType.RECURRENT)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_repeat_black_24dp, "every ...", "every"));
        }
        if (!usedTypes.contains(MatcherType.DATE) && !disabledEntityTypes.contains(TextEntityType.FLEXIBLE)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_multiply_black_24dp_transparent, "times a ...", "", TextEntityType.FLEXIBLE));
        }
        return suggestions;
    }
}
