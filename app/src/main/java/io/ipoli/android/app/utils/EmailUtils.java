package io.ipoli.android.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/15/16.
 */
public class EmailUtils {

    public static void send(Context context, String subject, String chooserTitle) {
        send(context, subject, "", chooserTitle);
    }

    public static void send(Context context, String subject, String playerId, String chooserTitle) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", Constants.IPOLI_EMAIL, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (!StringUtils.isEmpty(playerId)) {
            String body = "\n\nلطفا خط های پایین را پاک نکنید\n=====================\nاین اطلاعات میتواند سرعت رفع مشکلات را بیشتر کند:"  +
                    "\nVERSION_NAME : " + BuildConfig.VERSION_NAME
                    +"\nMANUFACTURER : " + Build.MANUFACTURER
                    +"\nMODEL : " + Build.MODEL
                    +"\nSDK : " + Build.VERSION.SDK_INT;
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        }
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }
}
