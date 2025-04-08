package hcmute.edu.vn.miniproject1.services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hcmute.edu.vn.miniproject1.R;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    public static final String CALL_STATE_CHANGED_ACTION = "hcmute.edu.vn.miniproject1.CALL_STATE_CHANGED";

    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static String phoneNumber = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) return;

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incoming = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (state == null) return;

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            handleIncomingCall(context, incoming);
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            handleCallAnswered(context);
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            handleCallEnded(context);
        }
        lastState = state;
    }

    private void handleIncomingCall(Context context, String incomingNumber) {
        if (incomingNumber == null) return;

        phoneNumber = incomingNumber;
        Log.d(TAG, "Cuộc gọi đến từ: " + phoneNumber);

        checkBlacklist(context, phoneNumber);
        sendCallStateBroadcast(context, "RINGING", phoneNumber);
    }

    private void handleCallAnswered(Context context) {
        Log.d(TAG, "Đã nghe cuộc gọi từ: " + phoneNumber);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(lastState)) {
            sendCallStateBroadcast(context, "ANSWERED", phoneNumber);
        }
    }

    private void handleCallEnded(Context context) {
        Log.d(TAG, "Cuộc gọi kết thúc: " + phoneNumber);

        if (!phoneNumber.isEmpty()) {
            String callType = TelephonyManager.EXTRA_STATE_RINGING.equals(lastState)
                    ? "Missed" : "Incoming";

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Intent intent = new Intent(CALL_STATE_CHANGED_ACTION);
            intent.putExtra("state", "ENDED");
            intent.putExtra("phoneNumber", phoneNumber);
            intent.putExtra("callType", callType);
            intent.putExtra("callDate", date);
            context.sendBroadcast(intent);

            phoneNumber = "";
        }
    }

    private void sendCallStateBroadcast(Context context, String state, String number) {
        Intent intent = new Intent(CALL_STATE_CHANGED_ACTION);
        intent.putExtra("state", state);
        intent.putExtra("phoneNumber", number);
        context.sendBroadcast(intent);
    }

    private void checkBlacklist(Context context, String number) {
        BroadcastReceiver blacklistResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isBlacklisted = intent.getBooleanExtra(BlacklistService.EXTRA_IS_BLACKLISTED, false);
                boolean shouldBlock = intent.getBooleanExtra(BlacklistService.EXTRA_BLOCK_CALLS, false);

                if (isBlacklisted && shouldBlock) {
                    blockCall(context, number);
                }

                context.unregisterReceiver(this);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(
                    blacklistResultReceiver,
                    new IntentFilter("hcmute.edu.vn.miniproject1.BLACKLIST_CHECK_RESULT"),
                    Context.RECEIVER_NOT_EXPORTED
            );
        }

        Intent intent = new Intent(context, BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_CHECK_BLACKLIST);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, number);
        context.startService(intent);
    }

    private void blockCall(Context context, String number) {
        // Gửi intent đến service để từ chối cuộc gọi (nếu cần logic reject)
        Intent rejectIntent = new Intent(context, BlacklistService.class);
        rejectIntent.setAction("REJECT_CALL");
        context.startService(rejectIntent);

        // Thông báo chặn
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "blocked_call_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo cuộc gọi bị chặn",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Kênh thông báo khi có cuộc gọi bị chặn");
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_block)
                .setContentTitle("Đã chặn cuộc gọi")
                .setContentText("Cuộc gọi từ số " + number + " đã bị chặn.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        manager.notify((int) System.currentTimeMillis(), notification);
    }
}
