package io.ipoli.android.persian.com.chavoosh.persiancalendar.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class PushNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_push_notification);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getExtras() != null) {
                Toast.makeText(this, "Cheshmak push notification data" +intent.getExtras().getString("me.cheshmak.data")+"\n"+intent.getExtras().getString("title") + "\n"+intent.getExtras().getString("message") + "\n", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
