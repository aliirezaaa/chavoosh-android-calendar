//package io.ipoli.android.persian.com.chavoosh.persiancalendar;
//
//import android.appwidget.AppWidgetProvider;
//import android.content.Context;
//import android.content.Intent;
//
//import com.chavoosh.persiancalendar.service.ApplicationService;
//import com.chavoosh.persiancalendar.util.UpdateUtils;
//import com.chavoosh.persiancalendar.util.Utils;
//
//public class Widget2x2 extends AppWidgetProvider {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (!Utils.getInstance(context).isServiceRunning(ApplicationService.class)) {
//            context.startService(new Intent(context, ApplicationService.class));
//        }
//        UpdateUtils.getInstance(context).update(true);
//    }
//}
