package hcmute.edu.vn.miniproject1.models;

import java.io.Serializable;
import java.util.Objects;

public class Song implements Serializable {
    private String title;
    private String singer;
    private int image;
    private int resource;

    public Song(String title, String singer, int image, int resource) {
        this.title = title;
        this.singer = singer;
        this.image = image;
        this.resource = resource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Song song = (Song) obj;
        return Objects.equals(title, song.title) &&
                Objects.equals(singer, song.singer);
    }

}