package hcmute.edu.vn.miniproject1;

import static hcmute.edu.vn.miniproject1.utils.Constants.CHANNEL_ID;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        createChannelNotification();
    }
    private void createChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("This channel is used for foreground service notifications");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}