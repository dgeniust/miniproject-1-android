package hcmute.edu.vn.miniproject1.services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.BlacklistContact;

public class BlacklistService extends Service {
    private static final String TAG = "BlacklistService";

    public static final String ACTION_ADD_TO_BLACKLIST = "hcmute.edu.vn.miniproject1.ACTION_ADD_TO_BLACKLIST";
    public static final String ACTION_REMOVE_FROM_BLACKLIST = "hcmute.edu.vn.miniproject1.ACTION_REMOVE_FROM_BLACKLIST";
    public static final String ACTION_CHECK_BLACKLIST = "hcmute.edu.vn.miniproject1.ACTION_CHECK_BLACKLIST";
    public static final String ACTION_GET_BLACKLIST = "hcmute.edu.vn.miniproject1.ACTION_GET_BLACKLIST";
    public static final String ACTION_UPDATE_BLACKLIST_ITEM = "hcmute.edu.vn.miniproject1.ACTION_UPDATE_BLACKLIST_ITEM";
    public static final String ACTION_HEALTH_CHECK = "hcmute.edu.vn.miniproject1.ACTION_HEALTH_CHECK";

    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_CONTACT_NAME = "contact_name";
    public static final String EXTRA_IS_BLACKLISTED = "is_blacklisted";
    public static final String EXTRA_BLACKLIST = "blacklist";
    public static final String EXTRA_BLOCK_CALLS = "block_calls";
    public static final String EXTRA_BLOCK_MESSAGES = "block_messages";

    private static final String PREF_NAME = "blacklist_prefs";
    private static final String KEY_BLACKLIST = "blacklist";

    private static final String CHANNEL_ID = "blacklist_channel";
    private static final String UPDATES_CHANNEL_ID = "blacklist_updates";

    private List<BlacklistContact> blacklistedContacts;
    private Map<String, BlacklistContact> blacklistMap;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Dịch vụ Blacklist đã khởi tạo");

        gson = new Gson();
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        loadBlacklist();
        createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_CHECK_BLACKLIST.equals(action) ||
                ACTION_REMOVE_FROM_BLACKLIST.equals(action) ||
                ACTION_UPDATE_BLACKLIST_ITEM.equals(action)) {

            String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                Log.e(TAG, "Số điện thoại rỗng hoặc null khi thực hiện action: " + action);
                return START_STICKY;
            }
        }

        switch (action) {
            case ACTION_ADD_TO_BLACKLIST:
                String phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                String contactName = intent.getStringExtra(EXTRA_CONTACT_NAME);
                boolean blockCalls = intent.getBooleanExtra(EXTRA_BLOCK_CALLS, true);
                boolean blockMessages = intent.getBooleanExtra(EXTRA_BLOCK_MESSAGES, true);
                addToBlacklist(phoneNumber, contactName, blockCalls, blockMessages);
                break;

            case ACTION_REMOVE_FROM_BLACKLIST:
                phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                removeFromBlacklist(phoneNumber);
                break;

            case ACTION_CHECK_BLACKLIST:
                phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                boolean isBlacklisted = isBlacklisted(phoneNumber);
                boolean shouldBlockCalls = shouldBlockCalls(phoneNumber);
                boolean shouldBlockMessages = shouldBlockMessages(phoneNumber);

                Intent resultIntent = new Intent("hcmute.edu.vn.miniproject1.BLACKLIST_CHECK_RESULT");
                resultIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
                resultIntent.putExtra(EXTRA_IS_BLACKLISTED, isBlacklisted);
                resultIntent.putExtra(EXTRA_BLOCK_CALLS, shouldBlockCalls);
                resultIntent.putExtra(EXTRA_BLOCK_MESSAGES, shouldBlockMessages);
                sendBroadcast(resultIntent);

                if (isBlacklisted && shouldBlockCalls) {
                    rejectCall();
                }
                break;

            case ACTION_GET_BLACKLIST:
                Intent blacklistIntent = new Intent("hcmute.edu.vn.miniproject1.BLACKLIST_RESULT");
                blacklistIntent.putExtra(EXTRA_BLACKLIST, gson.toJson(blacklistedContacts));
                sendBroadcast(blacklistIntent);
                break;

            case ACTION_UPDATE_BLACKLIST_ITEM:
                phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                boolean newBlockCalls = intent.getBooleanExtra(EXTRA_BLOCK_CALLS, true);
                boolean newBlockMessages = intent.getBooleanExtra(EXTRA_BLOCK_MESSAGES, true);
                updateBlacklistSettings(phoneNumber, newBlockCalls, newBlockMessages);
                break;

            case ACTION_HEALTH_CHECK:
                Intent healthIntent = new Intent("hcmute.edu.vn.miniproject1.BLACKLIST_HEALTH");
                healthIntent.putExtra("status", "đang chạy");
                healthIntent.putExtra("count", blacklistedContacts.size());
                sendBroadcast(healthIntent);
                break;
        }

        return START_STICKY;
    }

    public void addToBlacklist(String phoneNumber, String contactName, boolean blockCalls, boolean blockMessages) {
        Log.d(TAG, "Đang thêm số vào danh sách chặn: " + phoneNumber);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "Không thể thêm: số điện thoại bị rỗng");
            return;
        }

        if (blacklistMap.containsKey(phoneNumber)) {
            Log.d(TAG, "Số đã tồn tại, xóa cũ để thêm mới: " + phoneNumber);
            removeFromBlacklist(phoneNumber);
        }

        BlacklistContact contact = new BlacklistContact(phoneNumber, contactName);
        blacklistedContacts.add(contact);
        blacklistMap.put(phoneNumber, contact);

        saveBlacklist();
        notifyBlacklistChanged(phoneNumber, contactName, "add");
        Log.d(TAG, "Đã thêm vào danh sách chặn: " + phoneNumber);
    }

    private void removeFromBlacklist(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;

        BlacklistContact contact = blacklistMap.get(phoneNumber);
        if (contact != null) {
            String contactName = contact.getName();
            blacklistedContacts.remove(contact);
            blacklistMap.remove(phoneNumber);
            saveBlacklist();
            notifyBlacklistChanged(phoneNumber, contactName, "remove");
            Log.d(TAG, "Đã xóa khỏi danh sách chặn: " + phoneNumber);
        }
    }

    private void updateBlacklistSettings(String phoneNumber, boolean blockCalls, boolean blockMessages) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;

        BlacklistContact contact = blacklistMap.get(phoneNumber);
        if (contact != null) {
            saveBlacklist();
            Log.d(TAG, "Đã cập nhật cài đặt chặn cho: " + phoneNumber);
        }
    }

    private boolean isBlacklisted(String phoneNumber) {
        return phoneNumber != null && blacklistMap.containsKey(phoneNumber);
    }

    private boolean shouldBlockCalls(String phoneNumber) {
        return isBlacklisted(phoneNumber);
    }

    private boolean shouldBlockMessages(String phoneNumber) {
        return isBlacklisted(phoneNumber);
    }

    private void loadBlacklist() {
        String json = sharedPreferences.getString(KEY_BLACKLIST, "[]");
        Type type = new TypeToken<ArrayList<BlacklistContact>>() {}.getType();
        blacklistedContacts = gson.fromJson(json, type);

        if (blacklistedContacts == null) {
            blacklistedContacts = new ArrayList<>();
            Log.w(TAG, "Không đọc được danh sách từ SharedPreferences, tạo mới danh sách rỗng");
        }

        blacklistMap = new HashMap<>();
        for (BlacklistContact contact : blacklistedContacts) {
            blacklistMap.put(contact.getPhoneNumber(), contact);
        }

        Log.d(TAG, "Tải danh sách chặn: " + blacklistedContacts.size() + " số");
    }

    private void saveBlacklist() {
        String json = gson.toJson(blacklistedContacts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_BLACKLIST, json);
        boolean success = editor.commit();
        Log.d(TAG, "Đã lưu " + blacklistedContacts.size() + " số. Thành công: " + success);

        Log.d(TAG, "Current blacklist: " + json);
    }

    private void rejectCall() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> clazz = Class.forName(telephonyManager.getClass().getName());
            Method method = clazz.getDeclaredMethod("endCall");
            method.invoke(telephonyManager);
            Log.d(TAG, "Cuộc gọi đã bị từ chối thành công");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi từ chối cuộc gọi: " + e.getMessage());
        }
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Dịch vụ Chặn cuộc gọi", NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Kênh thông báo cho dịch vụ chặn cuộc gọi");
            notificationManager.createNotificationChannel(serviceChannel);

            NotificationChannel updatesChannel = new NotificationChannel(
                    UPDATES_CHANNEL_ID, "Thông báo danh sách chặn", NotificationManager.IMPORTANCE_DEFAULT
            );
            updatesChannel.setDescription("Thông báo khi thay đổi danh sách chặn");
            notificationManager.createNotificationChannel(updatesChannel);
        }
    }

    private void notifyBlacklistChanged(String phoneNumber, String contactName, String action) {
        String title;
        String message;
        String displayName = (contactName != null && !contactName.isEmpty()) ? contactName : phoneNumber;

        if ("add".equals(action)) {
            title = "Đã thêm số vào danh sách chặn";
            message = displayName + " sẽ bị chặn cuộc gọi và tin nhắn.";
        } else {
            title = "Đã xóa số khỏi danh sách chặn";
            message = displayName + " đã được gỡ khỏi danh sách chặn.";
        }

        Notification notification = new NotificationCompat.Builder(this, UPDATES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_block)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
