package io.ipoli.android.app.utils;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
import io.ipoli.android.quest.data.Category;

import static io.ipoli.android.app.utils.TimePreference.AFTERNOON;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/31/16.
 */
public class StringUtils {
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    public static String cut(String text, int startIdx, int endIdx) {
        return text.substring(0, startIdx) + (endIdx + 1 >= text.length() ? "" : text.substring(endIdx + 1));
    }

    public static String cutLength(String text, int startIdx, int lenToCut) {
        return text.substring(0, startIdx) + (startIdx + lenToCut >= text.length() ? "" : text.substring(startIdx + lenToCut));
    }

    public static String substring(String text, int startIdx, int endIdx) {
        return endIdx + 1 >= text.length() ? text.substring(startIdx) : text.substring(startIdx, endIdx + 1);
    }

    public static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }

    public static String capitalizeAndReplaceUnderscore(String text) {
        String t = Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();

        getPersianTranslate(text);
//        return t.replace("_", " ");
        return  getPersianTranslate(text);
    }

    public static String getPersianTranslate(String text) {
        ObjectMapper mapper = new ObjectMapper();
//        String json = R.raw.time_category;
        Map<String, String> map = new HashMap<String, String>();
        // convert JSON string to Map
        try {
            map = mapper.readValue(Utils.getInstance(MainActivity.getContext()).readRawResource(R.raw.time_category), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            e.getMessage();
        }
        if (map.containsKey(text)){
            return map.get(text);

        }else
            return text;

    }
}
