package hcmute.edu.vn.miniproject1.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.configs.BatteryReceiver;

public class MainActivity extends AppCompatActivity {

    LinearLayout btnPlayMusic, btnSMS, btnAlarm, btnCall;

    private BatteryReceiver batteryReceiver = new BatteryReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlayMusic = findViewById(R.id.btn_play_music);
        btnSMS = findViewById(R.id.btn_sms);
        btnAlarm = findViewById(R.id.btn_alarm);
        btnCall = findViewById(R.id.btn_call);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        btnPlayMusic.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListSongActivity.class);
            startActivity(intent);
        });

        btnSMS.setOnClickListener(v -> {
            Intent intent = new Intent(this, SMSActivity.class);
            startActivity(intent);
        });
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(this, CallsActivity.class);
            startActivity(intent);
        });
        btnAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}