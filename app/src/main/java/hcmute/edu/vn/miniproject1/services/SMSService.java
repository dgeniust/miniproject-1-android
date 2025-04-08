package hcmute.edu.vn.miniproject1.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class SMSService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("phoneNumber")) {
            String phoneNumber = intent.getStringExtra("phoneNumber");

            // Tạo Intent mở khung chat SMS mặc định
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
            smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(smsIntent);
        }

        // Dừng Service ngay sau khi thực hiện
        stopSelf();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
