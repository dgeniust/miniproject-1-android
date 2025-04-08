package hcmute.edu.vn.miniproject1.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.BlacklistContact;

public class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.ViewHolder> {
    private List<BlacklistContact> blacklist;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onRemoveClick(BlacklistContact contact);
    }

    public BlacklistAdapter(List<BlacklistContact> blacklist) {
        this.blacklist = blacklist;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<BlacklistContact> newBlacklist) {
        this.blacklist = newBlacklist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blacklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlacklistContact contact = blacklist.get(position);
        holder.tvPhoneNumber.setText(contact.getPhoneNumber());

        if (contact.getName() != null && !contact.getName().isEmpty()) {
            holder.tvName.setText(contact.getName());
            holder.tvName.setVisibility(View.VISIBLE);
        } else {
            holder.tvName.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateAdded = sdf.format(new Date(contact.getDateAdded()));
        holder.tvDateAdded.setText("Ngày thêm: " + dateAdded);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blacklist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhoneNumber, tvName, tvDateAdded;
        ImageButton btnRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone_number);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDateAdded = itemView.findViewById(R.id.tv_date_added);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
