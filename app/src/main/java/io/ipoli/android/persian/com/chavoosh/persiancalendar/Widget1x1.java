//package io.ipoli.android.persian.com.chavoosh.persiancalendar;
//
//import android.appwidget.AppWidgetProvider;
//import android.content.Context;
//import android.content.Intent;
//
//import io.ipoli.android.persian.com.chavoosh.persiancalendar.service.ApplicationService;
//import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
//
//
///**
// * 1x1 widget provider, implementation is on {@code CalendarWidget}
// *
// * @author ebraminio
// */
//public class Widget1x1 extends AppWidgetProvider {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (!Utils.getInstance(context).isServiceRunning(ApplicationService.class)) {
//            context.startService(new Intent(context, ApplicationService.class));
//        }
//        UpdateUtils.getInstance(context).update(true);
//    }
//}
