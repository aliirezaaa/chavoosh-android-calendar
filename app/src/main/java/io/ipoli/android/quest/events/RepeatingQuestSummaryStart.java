package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Category;

/**
 * Created by Client-9 on 6/14/2017.
 */

public class RepeatingQuestSummaryStart {
    public final String name;
    public final Category category;

    public RepeatingQuestSummaryStart(String name, Category category) {
        this.name = name;
        this.category = category;
    }
}
