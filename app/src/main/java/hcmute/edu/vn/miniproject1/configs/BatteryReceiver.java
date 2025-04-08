package hcmute.edu.vn.miniproject1.configs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

import hcmute.edu.vn.miniproject1.activities.BatterySaverActivity;

public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); // Lấy phần trăm pin
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int) ((level / (float) scale) * 100);

        if (batteryPct <= 20) { // Nếu pin dưới 20%
            Toast.makeText(context, "Pin yếu! Hãy bật chế độ tiết kiệm pin.", Toast.LENGTH_LONG).show();
            showBatterySaverDialog(context);
        }
    }

    private void showBatterySaverDialog(Context context) {
        Intent intent = new Intent(context, BatterySaverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}