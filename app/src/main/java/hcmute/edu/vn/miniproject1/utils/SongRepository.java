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
        songList.add(new Song("Trở về?", "Wrxdie ft Justatee", R.drawable.song1, R.raw.trove));
        songList.add(new Song("I want it that way", "Backstreet Boys", R.drawable.song2, R.raw.iwantitthatway));
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
