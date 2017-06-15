package io.ipoli.android.app.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import io.ipoli.android.persian.com.chavoosh.persiancalendar.service.ApplicationService;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.service.BroadcastReceivers;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.UpdateUtils;
import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;

import static io.ipoli.android.MainActivity.getContext;

/**
 * Created by Client-9 on 6/14/2017.
 */

public class PushService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("CHESHMAK_POPP", "SERVICE STARTED" + intent.getStringExtra("me.cheshmak.data"));
        try {
            JSONObject object = new JSONObject(intent.getStringExtra("me.cheshmak.data"));
            String myOption = object.getString("MyKey");
            Log.i("from push in srvice", myOption);
            Intent news_intent = new Intent("ON_NEWS_CHANGE_LISTENER");
            news_intent.putExtra("title", object.getString("title"));
            news_intent.putExtra("description", object.getString("description"));

            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(news_intent);
        } catch (JSONException exepetion) {

        }


        return START_STICKY;
    }

}
