package hcmute.edu.vn.miniproject1.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SongReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra("action_music", 0);

        Intent serviceIntent = new Intent(context, SongService.class);
        serviceIntent.putExtra("action_music", action);

        context.startService(serviceIntent);
    }
}
