package hcmute.edu.vn.miniproject1.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.adapters.CallsAdapter;
import hcmute.edu.vn.miniproject1.models.IncomingCall;
import hcmute.edu.vn.miniproject1.services.CallReceiver;
import hcmute.edu.vn.miniproject1.services.CallStateManager;

public class CallsActivity extends AppCompatActivity {

    private static final String TAG = "CallsActivity";
    private static final int REQUEST_CALL_PERMISSION = 1002;

    private RecyclerView recyclerViewCalls;
    private CallsAdapter callsAdapter;
    private List<IncomingCall> callList;
    private CallStateManager callStateManager;

    private BroadcastReceiver callReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            String phoneNumber = intent.getStringExtra("phoneNumber");

            if ("ENDED".equals(state)) {
                String callType = intent.getStringExtra("callType");
                String callDate = intent.getStringExtra("callDate");
                long callDuration = intent.getLongExtra("callDuration", 0);

                addCallLogEntry(phoneNumber, callType, callDate, callDuration);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call); // layout mới sẽ tạo ở bước sau

        recyclerViewCalls = findViewById(R.id.recycler_view_calls);
        recyclerViewCalls.setLayoutManager(new LinearLayoutManager(this));

        callList = new ArrayList<>();
        callsAdapter = new CallsAdapter(callList);
        recyclerViewCalls.setAdapter(callsAdapter);

        callStateManager = new CallStateManager(this);

        FloatingActionButton fabCall = findViewById(R.id.fab_call);
        fabCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            startActivity(intent);
        });

        FloatingActionButton fabAddBlacklist = findViewById(R.id.fab_add_blacklist);
        fabAddBlacklist.setOnClickListener(v -> {
            Intent intent = new Intent(CallsActivity.this, BlacklistActivity.class);
            startActivity(intent);
        });

        requestCallPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(callReceiver,
                    new IntentFilter(CallReceiver.CALL_STATE_CHANGED_ACTION),
                    Context.RECEIVER_NOT_EXPORTED);
        }

        callStateManager.startListening();

        if (hasRequiredPermissions()) {
            loadCallLog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(callReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Receiver unregister error: " + e.getMessage());
        }

        callStateManager.stopListening();
    }

    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCallPermissions() {
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS
                    },
                    REQUEST_CALL_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCallLog();
            } else {
                Toast.makeText(this, "Cần cấp quyền cuộc gọi", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadCallLog() {
        callList.clear();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI, null, null, null,
                    CallLog.Calls.DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                do {
                    String phoneNumber = cursor.getString(numberIndex);
                    int callTypeCode = cursor.getInt(typeIndex);
                    long callDate = cursor.getLong(dateIndex);
                    long duration = cursor.getLong(durationIndex);

                    String callType;
                    switch (callTypeCode) {
                        case CallLog.Calls.INCOMING_TYPE: callType = "Incoming"; break;
                        case CallLog.Calls.OUTGOING_TYPE: callType = "Outgoing"; break;
                        case CallLog.Calls.MISSED_TYPE: callType = "Missed"; break;
                        default: callType = "Unknown"; break;
                    }

                    String formattedDate = dateFormat.format(new Date(callDate));
                    String formattedDuration = formatDuration(duration);

                    IncomingCall entry = new IncomingCall(phoneNumber, callType, formattedDate);
                    entry.setDuration(formattedDuration);
                    callList.add(entry);
                } while (cursor.moveToNext());

                cursor.close();
            }

            callsAdapter.notifyDataSetChanged();
        }
    }

    private void addCallLogEntry(String phoneNumber, String callType, String callDate, long duration) {
        String formattedDuration = formatDuration(duration);
        IncomingCall entry = new IncomingCall(phoneNumber, callType, callDate);
        entry.setDuration(formattedDuration);

        callList.add(0, entry);
        callsAdapter.notifyItemInserted(0);
        recyclerViewCalls.scrollToPosition(0);
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " sec";
        } else {
            return (seconds / 60) + " min " + (seconds % 60) + " sec";
        }
    }
}
