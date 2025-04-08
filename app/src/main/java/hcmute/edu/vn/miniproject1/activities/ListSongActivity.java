package hcmute.edu.vn.miniproject1.activities;

import static hcmute.edu.vn.miniproject1.utils.Constants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Song;
import hcmute.edu.vn.miniproject1.adapters.SongAdapter;
import hcmute.edu.vn.miniproject1.services.SongService;
import hcmute.edu.vn.miniproject1.utils.SongRepository;



public class ListSongActivity extends AppCompatActivity {

    private SongRepository songRepository;

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;

    private RelativeLayout layoutMusicBar;
    private ImageView imgSong, imgPlayOrPause, imgClear, imgNext;
    private TextView tvTitleSong, tvSingerSong;

    private Song mSong;

    List<Song> lstSong = new ArrayList<>();

    private boolean isPlaying;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            Song receivedSong = (Song) bundle.getSerializable(OBJECT_SONG);
            isPlaying = bundle.getBoolean(ACTION_STATUS);
            Log.d("ListSongActivity", "isPlaying nhận từ Intent: " + isPlaying);

            if (receivedSong != null) {
                mSong = receivedSong;
            }

            int action = bundle.getInt(ACTION_SONG, 0);

            handlePlayerMusic(action);
        }
    };

    private void handlePlayerMusic(int action) {
        switch (action) {
            case ACTION_PAUSE:
            case ACTION_RESUME:
                setStatusButtonPlayOrPause();
                break;
            case ACTION_CLEAR:
                layoutMusicBar.setVisibility(View.GONE);
                break;
            case ACTION_START:
                layoutMusicBar.setVisibility(View.VISIBLE);
                displaySongInfo();
                setStatusButtonPlayOrPause();
                break;
            case ACTION_NEXT:
                tvTitleSong.setText(mSong.getTitle());
                tvSingerSong.setText(mSong.getSinger());
                imgSong.setImageResource(mSong.getImage());
                setStatusButtonPlayOrPause();
                break;
        }
    }

    public void displaySongInfo() {
        if (mSong != null) {
            tvTitleSong.setText(mSong.getTitle());
            tvSingerSong.setText(mSong.getSinger());
            imgSong.setImageResource(mSong.getImage());
        }
    }

    private void sendActionToService(int action) {
        Intent intent = new Intent(ListSongActivity.this, SongService.class);
        intent.putExtra(ACTION_SONG, action);

        Log.e("ListSongActivity", "Gửi action với status: " + isPlaying);

        if (action == ACTION_NEXT) {
            mSong = songRepository.getNextSong();
            Bundle bundle = new Bundle();
            bundle.putSerializable(OBJECT_SONG, mSong);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }
        } else if (action == ACTION_PREV) {
            onClickPrev();
            Bundle bundle = new Bundle();
            bundle.putSerializable(OBJECT_SONG, mSong);
            intent.putExtras(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }
        }
        else {
            startService(intent);
        }
        songRepository.setCurrentSong(mSong);
    }

    private void setStatusButtonPlayOrPause() {
        if (isPlaying) {
            imgPlayOrPause.setImageResource(R.drawable.ic_pause);
        } else {
            imgPlayOrPause.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song);

        mapping();

        songRepository = SongRepository.getInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.custom_divider);
        decoration.setDrawable(drawable);
        recyclerView.addItemDecoration(decoration);

        loadSongsAsync();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION_SEND_DATA_TO_ACTIVITY));

        imgPlayOrPause.setOnClickListener(v -> {
            if (isPlaying) {
                sendActionToService(ACTION_PAUSE);
            } else {
                sendActionToService(ACTION_RESUME);
            }
        });

        imgClear.setOnClickListener(v -> sendActionToService(ACTION_CLEAR));

        imgNext.setOnClickListener(v -> sendActionToService(ACTION_NEXT));

        layoutMusicBar.setOnClickListener(v -> {
            if (mSong != null) {
                Intent intent = new Intent(this, ItemSongActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(OBJECT_SONG, mSong);
                bundle.putBoolean(ACTION_STATUS, isPlaying);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }
    private void loadSongsAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<Song> songs = songRepository.getSongList();

            handler.post(() -> {
                lstSong = songs;
                songAdapter = new SongAdapter(songRepository.getSongList(), ListSongActivity.this::onClickGoToPlaySong);
                recyclerView.setAdapter(songAdapter);
            });
        });
    }

    private void onClickGoToPlaySong(Song song) {
        if (isPlaying) {
            clickStopService();
        }
        mSong = song;
        songRepository.setCurrentSong(mSong);
        displaySongInfo();
        clickStartService(song);
    }

    private void clickStopService() {
        sendActionToService(ACTION_CLEAR);
    }

    private void clickStartService(Song song) {
        Intent intent = new Intent(this, SongService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(OBJECT_SONG, song);
        isPlaying = true;
        Log.e("ListSongActivity", "Gửi trạng thái isPlaying: true");
        bundle.putBoolean(ACTION_STATUS, isPlaying);
        intent.putExtras(bundle);
        startForegroundServiceOnBackground(intent);
    }

    private void startForegroundServiceOnBackground(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new Handler(Looper.getMainLooper()).post(() -> startForegroundService(intent));
        }
    }

    private void mapping() {
        layoutMusicBar = findViewById(R.id.layout_music_bar);
        imgSong = findViewById(R.id.img_song);
        imgPlayOrPause = findViewById(R.id.img_play_or_pause);
        imgClear = findViewById(R.id.img_clear);
        imgNext = findViewById(R.id.img_next);
        tvTitleSong = findViewById(R.id.tv_title_song);
        tvSingerSong = findViewById(R.id.tv_singer_song);
        recyclerView = findViewById(R.id.lst_music);
    }

    private void onClickNext() {
        if (lstSong == null || lstSong.isEmpty()) return;

        for (int i = 0; i < lstSong.size(); i++) {
            if (lstSong.get(i).getTitle().equals(mSong.getTitle())) {
                mSong = (i == lstSong.size() - 1) ? lstSong.get(0) : lstSong.get(i + 1);
                break;
            }
        }
    }

    private void onClickPrev() {
        if (lstSong == null || lstSong.isEmpty()) return;

        for (int i = lstSong.size() - 1; i >= 0; i--) {
            if (lstSong.get(i).getTitle().equals(mSong.getTitle())) {
                mSong = (i == 0) ? lstSong.get(lstSong.size() - 1) : lstSong.get(i - 1);
                break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
