package hcmute.edu.vn.miniproject1.activities;

import static hcmute.edu.vn.miniproject1.utils.Constants.*;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Song;
import hcmute.edu.vn.miniproject1.services.SongService;
import hcmute.edu.vn.miniproject1.utils.SongRepository;

import java.text.SimpleDateFormat;

public class ItemSongActivity extends AppCompatActivity {

    private SongRepository songRepository;

    TextView tvNameSong, tvSinger, tvCurrentTime, tvTotalTime;
    ImageButton btnPre, btnPlay, btnNext, btnBack;
    SeekBar skbSong;
    Song mSong;
    boolean isPlaying;
    int currentDuration, totalDuration;

    private ObjectAnimator animator;

    // BroadcastReceiver để nhận dữ liệu từ SongService
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            mSong = (Song) bundle.getSerializable(OBJECT_SONG);
            isPlaying = bundle.getBoolean(ACTION_STATUS, isPlaying);
            currentDuration = bundle.getInt("current_position", 0);
            totalDuration = bundle.getInt("total_duration", 1);

            if (mSong != null) {
                songRepository.setCurrentSong(mSong);
            }

            displaySongInfo();
            setStatusButtonPlayOrPause();
            updateSeekBar();

            if (isPlaying) {
                startRotation();
            }

            // Kiểm tra nếu hết bài thì tự động chuyển bài
            if (currentDuration >= totalDuration) {
                sendActionToService(ACTION_NEXT);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_song);

        mapping();
        rotateDisk();

        // Đảm bảo repository đã được khởi tạo
        songRepository = SongRepository.getInstance();

        // Nhận dữ liệu từ Intent khi mở Activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mSong = (Song) bundle.getSerializable(OBJECT_SONG);
            isPlaying = bundle.getBoolean(ACTION_STATUS);
            if (mSong != null) {
                songRepository.setCurrentSong(mSong);
            }
            displaySongInfo();
            setStatusButtonPlayOrPause();
        }

        // Đăng ký BroadcastReceiver để nhận cập nhật từ SongService
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter(ACTION_SEND_DATA_TO_ACTIVITY));

        // Xử lý sự kiện Play/Pause
        btnPlay.setOnClickListener(v -> {
            if (isPlaying) {
                stopRotation();
                sendActionToService(ACTION_PAUSE);
            } else {
                startRotation();
                sendActionToService(ACTION_RESUME);
            }
        });

        // Xử lý sự kiện Next / Prev
        btnNext.setOnClickListener(v -> sendActionToService(ACTION_NEXT));
        btnPre.setOnClickListener(v -> sendActionToService(ACTION_PREV));
        btnBack.setOnClickListener(v -> finish());

        // Xử lý sự kiện kéo SeekBar để tua bài hát
        skbSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sendSeekToService(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Hiển thị thông tin bài hát
    private void displaySongInfo() {
        if (mSong != null) {
            tvNameSong.setText(mSong.getTitle());
            tvSinger.setText(mSong.getSinger());
        }
    }

    // Cập nhật nút Play/Pause
    private void setStatusButtonPlayOrPause() {
        btnPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    // Cập nhật SeekBar và thời gian bài hát
    private void updateSeekBar() {
        skbSong.setMax(totalDuration);
        skbSong.setProgress(currentDuration);

        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
        tvCurrentTime.setText(timeFormat.format(currentDuration));
        tvTotalTime.setText(timeFormat.format(totalDuration));
    }

    // Gửi lệnh điều khiển tới SongService
    private void sendActionToService(int action) {
        Intent intent = new Intent(this, SongService.class);
        intent.putExtra(ACTION_SONG, action);

        if (action == ACTION_NEXT) {
            mSong = songRepository.getNextSong();
        } else if (action == ACTION_PREV) {
            mSong = songRepository.getPreviousSong();
        }

        if (mSong != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(OBJECT_SONG, mSong);
            intent.putExtras(bundle);
        }

        // Nếu Service chưa chạy, gọi startForegroundService
        if (action == ACTION_NEXT || action == ACTION_PREV) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }
        } else {
            startService(intent);
        }

        // Cập nhật trạng thái ngay lập tức trên giao diện
        if (action == ACTION_PAUSE) {
            isPlaying = false;
        } else if (action == ACTION_RESUME) {
            isPlaying = true;
        }

        setStatusButtonPlayOrPause();
    }

    // Gửi yêu cầu tua bài hát đến Service
    private void sendSeekToService(int position) {
        Intent intent = new Intent(this, SongService.class);
        intent.putExtra(ACTION_SONG, ACTION_SEEK_TO);
        intent.putExtra("seek_position", position);
        startService(intent);
    }

    // Ánh xạ View
    private void mapping() {
        tvNameSong = findViewById(R.id.tvNameSong);
        tvSinger = findViewById(R.id.tvSinger);
        btnPre = findViewById(R.id.btnPre);
        btnPlay = findViewById(R.id.btnPlay);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btnNext);
        skbSong = findViewById(R.id.seekBarSong);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_completed_time);
    }

    private void rotateDisk() {
        ImageView imgDisk = findViewById(R.id.imgDisk);

        animator = ObjectAnimator.ofFloat(imgDisk, "rotation", 0f, 360f);
        animator.setDuration(5000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
    }

    private void startRotation() {
        if (animator != null) {
            if (animator.isPaused()) {
                animator.resume(); // Tiếp tục từ vị trí dừng trước đó
            } else if (!animator.isRunning()) {
                animator.start(); // Chỉ chạy nếu chưa chạy
            }
        }
    }

    private void stopRotation() {
        if (animator != null && animator.isRunning()) {
            animator.pause(); // Dừng lại nhưng giữ nguyên vị trí
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
