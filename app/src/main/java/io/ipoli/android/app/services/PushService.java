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

import javax.inject.Inject;

import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.LocalStorage;
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

    @Inject
    LocalStorage localStorage;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Log.d("CHESHMAK_POPP", "SERVICE STARTED" + intent.getStringExtra("me.cheshmak.data"));
        App.getAppComponent(getApplicationContext()).inject(this);
        try {
            JSONObject object = new JSONObject(intent.getStringExtra("me.cheshmak.data"));
//            String myOption = object.getString("MyKey");
//            Log.i("from push in service", object.toString());
            Intent news_intent = new Intent("ON_NEWS_CHANGE_LISTENER");
//            news_intent.putExtra("title", object.getString("title"));
//            news_intent.putExtra("description", object.getString("description"));
//            news_intent.putExtra("img_url", object.getString("img_url"));
//            news_intent.putExtra("url", object.getString("url"));
//            news_intent.putExtra("isGif", object.getBoolean("isGif"));
//            news_intent.putExtra("showNews", object.getBoolean("showNews"));
            Log.i("push receive",object.toString());
            updateData(object);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(news_intent);
        } catch (JSONException exepetion) {

        }

        return START_STICKY;
    }
private void updateData(JSONObject object) throws JSONException {
    localStorage.saveBool("isGif",object.getBoolean("isGif"));
    localStorage.saveString("news_title",object.getString("title"));
    localStorage.saveString("news_description", object.getString("description"));
    localStorage.saveString("news_url",object.getString("url"));
    localStorage.saveString("news_img_url",object.getString("img_url"));
//    localStorage.saveBool("is_persistence",object.getBoolean("is_persistence"));
    localStorage.saveBool("show_news",object.getBoolean("show_news"));
}
}
