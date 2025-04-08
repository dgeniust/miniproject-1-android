package hcmute.edu.vn.miniproject1.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.miniproject1.R;

public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";
    public static final String SMS_RECEIVED_ACTION = "hcmute.edu.vn.miniproject1.SMS_RECEIVED";
    private static final String BLOCKED_SMS_CHANNEL_ID = "blocked_sms_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        String format = bundle.getString("format");
        String sender = "";
        StringBuilder fullMessage = new StringBuilder();

        // Parse all message parts
        for (Object pdu : pdus) {
            SmsMessage sms = parseSmsMessage(pdu, format);
            if (sms == null) continue;

            sender = sms.getDisplayOriginatingAddress();
            fullMessage.append(sms.getMessageBody());
        }

        // Kiểm tra blacklist
        if (!sender.isEmpty()) {
            checkBlacklistAndHandle(context, sender, fullMessage.toString());
        }
    }

    private SmsMessage parseSmsMessage(Object pdu, String format) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                return SmsMessage.createFromPdu((byte[]) pdu);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi phân tích SMS: " + e.getMessage());
            return null;
        }
    }

    private void checkBlacklistAndHandle(Context context, String sender, String message) {
        BroadcastReceiver blacklistResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent resultIntent) {
                boolean isBlacklisted = resultIntent.getBooleanExtra(BlacklistService.EXTRA_IS_BLACKLISTED, false);
                boolean blockMessages = resultIntent.getBooleanExtra(BlacklistService.EXTRA_BLOCK_MESSAGES, false);

                if (isBlacklisted && blockMessages) {
                    Log.d(TAG, "SMS từ " + sender + " bị chặn.");
                    abortBroadcast();
                    notifyBlockedSms(context, sender);
                } else {
                    forwardSmsToApp(context, sender, message);
                }

                context.unregisterReceiver(this);
            }
        };

        // Đăng ký receiver tạm thời để chờ phản hồi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(
                    blacklistResultReceiver,
                    new IntentFilter("hcmute.edu.vn.miniproject1.BLACKLIST_CHECK_RESULT"),
                    Context.RECEIVER_NOT_EXPORTED
            );
        }

        // Gửi yêu cầu kiểm tra blacklist
        Intent checkIntent = new Intent(context, BlacklistService.class);
        checkIntent.setAction(BlacklistService.ACTION_CHECK_BLACKLIST);
        checkIntent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, sender);
        context.startService(checkIntent);
    }

    private void notifyBlockedSms(Context context, String sender) {
        createNotificationChannelIfNeeded(context);

        Notification notification = new NotificationCompat.Builder(context, BLOCKED_SMS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_block)
                .setContentTitle("Đã chặn tin nhắn")
                .setContentText("Tin nhắn từ " + sender + " đã bị chặn")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification);
        }
    }

    private void createNotificationChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    BLOCKED_SMS_CHANNEL_ID,
                    "Thông báo chặn SMS",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo khi tin nhắn bị chặn");

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void forwardSmsToApp(Context context, String sender, String message) {
        Intent broadcastIntent = new Intent(SMS_RECEIVED_ACTION);
        broadcastIntent.putExtra("sender", sender);
        broadcastIntent.putExtra("message", message);
        context.sendBroadcast(broadcastIntent);
        Log.d(TAG, "Đã nhận SMS từ: " + sender);
    }
}
