package hcmute.edu.vn.miniproject1.utils;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Song;

public class SongRepository {
    private static SongRepository instance;
    private List<Song> songList;
    private Song currentSong;

    private SongRepository() {
        songList = new ArrayList<>();
        songList.add(new Song("CHÚNG TA CÒN Ở ĐÓ KHÔNG?", "Orange", R.drawable.song1, R.raw.chungtaconodokhong_orange));
        songList.add(new Song("DÙ CHO TẬN THẾ", "Erik", R.drawable.song2, R.raw.duchotanthe_erik));
        songList.add(new Song("NOKIA", "VCC Left Hand", R.drawable.song3, R.raw.nokia_lefthand));
        songList.add(new Song("Some one you loved", "Lewis Capaldi", R.drawable.song4, R.raw.someoneyouloved_lewiscapaldi));
        songList.add(new Song("Window Shopper", "Hurrykng", R.drawable.song5, R.raw.windowshopper_hurrykng));
        songList.add(new Song("MA NƠ CANH", "Hurrykng", R.drawable.song6, R.raw.manocanh_hurrykng));
    }

    public static synchronized SongRepository getInstance() {
        if (instance == null) {
            instance = new SongRepository();
        }
        return instance;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setCurrentSong(Song song) {
        this.currentSong = song;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public Song getNextSong() {
        if (currentSong == null) return null;

        int index = songList.indexOf(currentSong);
        return songList.get((index + 1) % songList.size());  // Nếu cuối danh sách, quay lại bài đầu
    }

    public Song getPreviousSong() {
        if (currentSong == null) return null;

        int index = songList.indexOf(currentSong);
        return songList.get((index - 1 + songList.size()) % songList.size());  // Nếu đầu danh sách, quay lại bài cuối
    }
}
