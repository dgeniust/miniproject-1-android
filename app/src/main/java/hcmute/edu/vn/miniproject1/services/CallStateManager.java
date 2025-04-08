package hcmute.edu.vn.miniproject1.services;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class CallStateManager {
    private static final String TAG = "CallStateManager";

    private final Context context;
    private final TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private String currentNumber = "";
    private Date callStartTime;
    private boolean isIncoming;

    public CallStateManager(Context context) {
        this.context = context;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void startListening() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                handleStateChange(state, phoneNumber);
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d(TAG, "Đã bắt đầu lắng nghe trạng thái cuộc gọi.");
    }

    public void stopListening() {
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            Log.d(TAG, "Đã dừng lắng nghe trạng thái cuộc gọi.");
        }
    }

    private void handleStateChange(int state, String number) {
        Log.d(TAG, "Trạng thái gọi thay đổi: " + stateToString(state) + " - Số: " + number);

        if (state == lastState && number.equals(currentNumber)) return;

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                onIncomingCallStarted(number);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                onCallAnsweredOrOutgoing(number);
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                onCallEnded();
                break;
        }

        lastState = state;
    }

    private void onIncomingCallStarted(String number) {
        currentNumber = number;
        isIncoming = true;
        callStartTime = new Date();

        checkBlacklist(number);
        sendCallStateBroadcast("RINGING", number);
    }

    private void onCallAnsweredOrOutgoing(String number) {
        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
            isIncoming = true;
            sendCallStateBroadcast("ANSWERED", currentNumber);
        } else {
            isIncoming = false;
            currentNumber = number;
            callStartTime = new Date();
            sendCallStateBroadcast("DIALING", number);
        }
    }

    private void onCallEnded() {
        if (lastState == TelephonyManager.CALL_STATE_RINGING) {
            broadcastCallEnded(currentNumber, "Missed", 0);
        } else if (lastState == TelephonyManager.CALL_STATE_OFFHOOK && callStartTime != null) {
            long duration = calculateDuration(callStartTime);
            String type = isIncoming ? "Incoming" : "Outgoing";
            broadcastCallEnded(currentNumber, type, duration);
        }

        // Reset state
        currentNumber = "";
        callStartTime = null;
    }

    private void checkBlacklist(String phoneNumber) {
        Intent intent = new Intent(context, BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_CHECK_BLACKLIST);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        context.startService(intent);
    }

    private void sendCallStateBroadcast(String state, String phoneNumber) {
        Intent intent = new Intent(CallReceiver.CALL_STATE_CHANGED_ACTION);
        intent.putExtra("state", state);
        intent.putExtra("phoneNumber", phoneNumber);
        context.sendBroadcast(intent);
    }

    private void broadcastCallEnded(String phoneNumber, String type, long duration) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Intent intent = new Intent(CallReceiver.CALL_STATE_CHANGED_ACTION);
        intent.putExtra("state", "ENDED");
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("callType", type);
        intent.putExtra("callDate", date);
        intent.putExtra("callDuration", duration);
        context.sendBroadcast(intent);
    }

    private long calculateDuration(Date startTime) {
        return (new Date().getTime() - startTime.getTime()) / 1000;
    }

    private String stateToString(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                return "RINGING";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "OFFHOOK";
            case TelephonyManager.CALL_STATE_IDLE:
                return "IDLE";
            default:
                return "UNKNOWN";
        }
    }
}
