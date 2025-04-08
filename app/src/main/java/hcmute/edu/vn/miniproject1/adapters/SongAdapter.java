package hcmute.edu.vn.miniproject1.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Song;
import hcmute.edu.vn.miniproject1.utils.IClickSongItemListener;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songs;

    private IClickSongItemListener itemListener;


    public SongAdapter(List<Song> songs, IClickSongItemListener itemListener) {
        this.songs = songs;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        if (song == null) return;
        holder.imgSong.setImageResource(song.getImage());
        holder.tvTitle.setText(song.getTitle());
        holder.tvSinger.setText(song.getSinger());

        holder.layoutItemMusic.setOnClickListener(v->{
            itemListener.onClickSongItem(song);
        });
    }

    @Override
    public int getItemCount() {
        if (songs != null) {
            return songs.size();
        }
        return 0;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout layoutItemMusic;
        private ImageView imgSong;
        private TextView tvTitle;
        private TextView tvSinger;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutItemMusic = itemView.findViewById(R.id.layout_item_music);
            imgSong = itemView.findViewById(R.id.img_song);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSinger = itemView.findViewById(R.id.tv_singer);
        }
    }
}

