package hcmute.edu.vn.miniproject1.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.adapters.SMSAdapter;
import hcmute.edu.vn.miniproject1.models.SMS;

public class SMSActivity extends AppCompatActivity {

    private static final String TAG = "SMSActivity";
    private static final int REQUEST_SMS_PERMISSION = 1001;
    private static final int REQUEST_CONTACT_PERMISSION = 1010;

    private RecyclerView recyclerViewSMS;
    private SMSAdapter smsAdapter;
    private List<SMS> smsList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        recyclerViewSMS = findViewById(R.id.recycler_view_sms);
        recyclerViewSMS.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progress_bar);

        smsList = new ArrayList<>();
        smsAdapter = new SMSAdapter(smsList);
        recyclerViewSMS.setAdapter(smsAdapter);

        // Kiểm tra và yêu cầu quyền danh bạ
        if (!hasContactPermission()) {
            requestContactPermission();
        }

        // Kiểm tra quyền SMS trước khi tải dữ liệu
        if (hasSmsPermissions()) {
            loadSmsMessages();
        } else {
            requestSmsPermissions();
        }

        // FAB gửi tin nhắn
        FloatingActionButton fabNewMessage = findViewById(R.id.fab_call);
        fabNewMessage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:"));
            startActivity(intent);
        });
    }

    // 👉 QUYỀN: Danh bạ
    private boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestContactPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CONTACT_PERMISSION);
    }

    // 👉 QUYỀN: SMS
    private boolean hasSmsPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                REQUEST_SMS_PERMISSION);
    }

    // 👉 XỬ LÝ KẾT QUẢ CẤP QUYỀN
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSmsMessages();
            } else {
                Toast.makeText(this, "Cần quyền để đọc tin nhắn SMS", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền truy cập danh bạ", Toast.LENGTH_SHORT).show();
                loadSmsMessages(); // Load lại nếu cần
            } else {
                Toast.makeText(this, "Không thể hiển thị tên liên hệ vì thiếu quyền danh bạ", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 👉 TẢI DANH SÁCH TIN NHẮN
    @SuppressLint("StaticFieldLeak")
    private void loadSmsMessages() {
        progressBar.setVisibility(View.VISIBLE);
        smsList.clear();

        new AsyncTask<Void, List<SMS>, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                List<SMS> batchList = new ArrayList<>();

                try (Cursor cursor = getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        null, null, null, "date DESC")) {

                    if (cursor != null && cursor.moveToFirst()) {
                        int addressIdx = cursor.getColumnIndex("address");
                        int bodyIdx = cursor.getColumnIndex("body");

                        do {
                            String address = cursor.getString(addressIdx);
                            String body = cursor.getString(bodyIdx);
                            String sender = address;

                            if (hasContactPermission() && android.telephony.PhoneNumberUtils.isGlobalPhoneNumber(address)) {
                                String contactName = getContactName(address);
                                if (contactName != null) sender = contactName;
                            }

                            batchList.add(new SMS(sender, body));

                        } while (cursor.moveToNext());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi load SMS: ", e);
                }

                publishProgress(batchList);
                return null;
            }

            @Override
            protected void onProgressUpdate(List<SMS>... values) {
                smsList.addAll(values[0]);
                smsAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    // 👉 TÌM TÊN LIÊN HỆ
    private String getContactName(String phoneNumber) {
        String name = null;

        try (Cursor cursor = getContentResolver().query(
                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(phoneNumber)),
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }
            }


        } catch (SecurityException e) {
            Log.e(TAG, "Thiếu quyền truy cập danh bạ", e);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy tên liên hệ", e);
        }

        return name;
    }
}
