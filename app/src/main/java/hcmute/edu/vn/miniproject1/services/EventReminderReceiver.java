package hcmute.edu.vn.miniproject1.services;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.activities.CalendarActivity;
import hcmute.edu.vn.miniproject1.models.Event;
public class EventReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DEBUG", "EventReminderReceiver nhận được Broadcast!");

        long eventId = intent.getLongExtra("event_id", -1);
        String title = intent.getStringExtra("event_title");

        if (eventId == -1) {
            Log.e("DEBUG", "Không nhận được event_id, thoát...");
            return;
        }
        EventDatabaseHelper dbHelper = new EventDatabaseHelper(context);
        if (isEventCompletedOrDeleted(dbHelper, eventId)) {
            Log.d("DEBUG", "Sự kiện ID: " + eventId + " đã hoàn thành hoặc bị xóa. Hủy thông báo.");
            return;
        }

        showNotification(context, eventId, title);
        scheduleRepeatNotification(context, eventId, title);
    }

    // Kiểm tra sự kiện đã hoàn thành hoặc bị xóa chưa
    private boolean isEventCompletedOrDeleted(EventDatabaseHelper dbHelper, long eventId) {
        Event event = dbHelper.getEventById(eventId);
        return event == null || event.getStatus() == 1; // Trả về true nếu sự kiện không tồn tại hoặc đã hoàn thành
    }

    // Hiển thị thông báo
    private void showNotification(Context context, long eventId, String title) {
        Log.d("DEBUG", "Bắt đầu hiển thị thông báo cho sự kiện ID: " + eventId);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.e("DEBUG", "NotificationManager không khả dụng!");
            return;
        }

        createNotificationChannel(context);

        Intent openAppIntent = new Intent(context, CalendarActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) eventId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "event_channel")
                .setSmallIcon(R.drawable.baseline_calendar_today_24)
                .setContentTitle("Bạn có 1 sự kiện!")
                .setContentText("Tiêu đề: " + title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) eventId, builder.build());
        Log.d("DEBUG", "Thông báo đã hiển thị cho sự kiện ID: " + eventId);
    }

    // lặp lại sau 30 phút
    private void scheduleRepeatNotification(Context context, long eventId, String title) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra("event_id", eventId);
        intent.putExtra("event_title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, (int) eventId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = System.currentTimeMillis() + 30 * 60 * 1000; // 30 phút sau
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }
    // Tạo kênh thông báo
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "event_channel";
            String channelName = "Lịch sự kiện";
            String channelDescription = "Thông báo nhắc nhở sự kiện";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
