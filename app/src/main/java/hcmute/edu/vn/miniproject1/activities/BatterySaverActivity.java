package hcmute.edu.vn.miniproject1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.miniproject1.R;

public class BatterySaverActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_saver);

        TextView txtMessage = findViewById(R.id.txtMessage);
        Button btnEnable = findViewById(R.id.btnEnable);
        Button btnCancel = findViewById(R.id.btnCancel);

        txtMessage.setText("Pin yếu! Bạn có muốn bật chế độ tiết kiệm pin không?");

        btnEnable.setOnClickListener(v -> {
            enableBatterySaver();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void enableBatterySaver() {
        Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
        startActivity(intent);
    }
}