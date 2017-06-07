package io.ipoli.android.sync;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;

import java.util.concurrent.TimeUnit;

import android.net.ParseException;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import io.ipoli.android.MainActivity;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Client-9 on 6/6/2017.
 */

public class LocalCalendar {
    MultiDexApplication app;

    public LocalCalendar(MultiDexApplication app) {
        this.app = app;
    }

    public Long onEventChange(Quest quest) {
        long calID = 1;
        long startMillis = 0;
        long endMillis = 0;
//        Calendar beginTime = Calendar.getInstance();
//        beginTime.set(2017, 5, 7, 17, 20, 0);
//
        String startTimeFormat = "yyyy-M-dd HH:mm";
        String endTimeFormat = "mm";
        String startTime = quest.getScheduledDate() + " " + quest.getStartTime();
//        String endTime = quest.get;

//        beginTime.set(quest.getScheduledDate().getYear(),
//                quest.getScheduledDate().getMonthValue(),
//                quest.getScheduledDate().getDayOfMonth());
        startMillis = dateToMillis(startTime, startTimeFormat);
//        Calendar endTime = Calendar.getInstance(new Locale("en"));
//        endTime.set(quest.getEndDate().getYear(),
//                quest.getEndDate().getMonthValue(),
//                quest.getEndDate().getDayOfMonth());
        endMillis = startMillis + TimeUnit.MINUTES.toMillis(quest.getDuration());


        ContentResolver cr = app.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, quest.getName());
        values.put(CalendarContract.Events.DESCRIPTION, StringUtils.getPersianTranslate(quest.getCategory()));
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Tehran");
        if (ActivityCompat.checkSelfPermission(app, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        if (quest.getEventID() != null && quest.getScheduledDate() != null) {
            /*update exiting event */
            Uri updateUri = null;
            updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, quest.getEventID());
            int rows = app.getContentResolver().update(updateUri, values, null, null);

            Log.i("updated", quest.getName() + " " + quest.getEventID() + " start:" + quest.getEndDate() + " end:" + quest.getDuration());
//            Log.i("start", quest.getStartDate().toString());
//            Log.i("end", quest.getEndDate().toString());
//            Log.i("scheduled", quest.getScheduledDate().toString());
            return null;
        } else if (quest.getStartDate() != null && quest.getScheduledDate() != null) {
            /*insert new event from app to local calendar*/
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

// get the event ID that is the last element in the Uri
            long eventID = Long.parseLong(uri.getLastPathSegment());
            Log.i("inserted", quest.getName() + " " + eventID);
//            Log.i("start", quest.getStartDate().toString());
//            Log.i("end", quest.getEndDate().toString());
//            Log.i("scheduled", quest.getScheduledDate().toString());
            return eventID;

        } else if (quest.getEventID() != null && quest.getScheduledDate() == null) {
            /*if event in calendar but moved to inbox now*/
            Uri deleteUri = null;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, quest.getEventID());
            int rows = app.getContentResolver().delete(deleteUri, null, null);
            return null;
        } else {
            return null;
        }

    }

    private Long dateToMillis(String dateTime, String format) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd HH:mm");
        Date convertedDate = new Date();
        try {
            convertedDate = formatter.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return convertedDate.getTime();
    }


}
