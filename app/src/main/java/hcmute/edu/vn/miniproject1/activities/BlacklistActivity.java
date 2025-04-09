package hcmute.edu.vn.miniproject1.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.adapters.BlacklistAdapter;
import hcmute.edu.vn.miniproject1.models.BlacklistContact;
import hcmute.edu.vn.miniproject1.services.BlacklistService;

public class BlacklistActivity extends AppCompatActivity {

    private static final String TAG = "BlacklistActivity";

    private RecyclerView recyclerView;
    private BlacklistAdapter adapter;
    private List<BlacklistContact> blacklist;
    private FloatingActionButton fabAdd;

    private final BroadcastReceiver blacklistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String blacklistJson = intent.getStringExtra(BlacklistService.EXTRA_BLACKLIST);
            if (blacklistJson != null) {
                Type type = new TypeToken<ArrayList<BlacklistContact>>() {}.getType();
                blacklist = new Gson().fromJson(blacklistJson, type);
                adapter.updateData(blacklist);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        recyclerView = findViewById(R.id.recycler_view_blacklist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        blacklist = new ArrayList<>();
        adapter = new BlacklistAdapter(blacklist);
        adapter.setOnItemClickListener(new BlacklistAdapter.OnItemClickListener() {
            @Override
            public void onRemoveClick(BlacklistContact contact) {
                removeFromBlacklist(contact.getPhoneNumber());
            }
        });
        recyclerView.setAdapter(adapter);

        fabAdd = findViewById(R.id.fab_add_blacklist);
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Request blacklist ngay khi mở activity
        requestBlacklist();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(blacklistReceiver,
                    new IntentFilter("hcmute.edu.vn.miniproject1.BLACKLIST_RESULT"),
                    Context.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(blacklistReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
    }

    private void requestBlacklist() {
        Intent intent = new Intent(this, BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_GET_BLACKLIST);
        startService(intent);
    }

    private void addToBlacklist(String phoneNumber, String name) {
        Intent intent = new Intent(this, BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_ADD_TO_BLACKLIST);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        intent.putExtra(BlacklistService.EXTRA_CONTACT_NAME, name);
        startService(intent);

        new Handler().postDelayed(this::requestBlacklist, 500);
    }

    private void removeFromBlacklist(String phoneNumber) {
        Intent intent = new Intent(this, BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_REMOVE_FROM_BLACKLIST);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        startService(intent);

        new Handler().postDelayed(this::requestBlacklist, 500);
    }

    private void updateBlacklistSettings(String phoneNumber, boolean blockCalls, boolean blockMessages) {
        Intent intent = new Intent(this, BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_UPDATE_BLACKLIST_ITEM);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        intent.putExtra(BlacklistService.EXTRA_BLOCK_CALLS, blockCalls);
        intent.putExtra(BlacklistService.EXTRA_BLOCK_MESSAGES, blockMessages);
        startService(intent);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to Blacklist");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_blacklist, null);
        EditText etPhoneNumber = view.findViewById(R.id.et_phone_number);
        EditText etName = view.findViewById(R.id.et_name);
        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (!phoneNumber.isEmpty()) {
                addToBlacklist(phoneNumber, name.isEmpty() ? null : name);
            } else {
                Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
