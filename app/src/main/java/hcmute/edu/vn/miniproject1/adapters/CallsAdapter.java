package hcmute.edu.vn.miniproject1.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.IncomingCall;
import hcmute.edu.vn.miniproject1.services.BlacklistService;

public class CallsAdapter extends RecyclerView.Adapter<CallsAdapter.ViewHolder> {

    public interface OnCallItemClickListener {
        void onCallClick(String phoneNumber);
        void onAddToBlacklist(String phoneNumber);
    }

    private final List<IncomingCall> callList;
    private OnCallItemClickListener listener;

    public CallsAdapter(List<IncomingCall> callList) {
        this.callList = callList;
    }

    public void setOnCallItemClickListener(OnCallItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncomingCall call = callList.get(position);
        Context context = holder.itemView.getContext();

        holder.txtPhoneNumber.setText(call.getPhoneNumber());
        holder.txtCallType.setText(call.getCallType());
        holder.txtCallDate.setText(call.getCallDate());

        bindDuration(holder, call);
        bindCallIcon(holder, call.getCallType());
        bindClickActions(holder, context, call);
    }

    private void bindDuration(ViewHolder holder, IncomingCall call) {
        String duration = call.getDuration();
        if (duration != null && !duration.isEmpty()) {
            holder.txtCallDuration.setVisibility(View.VISIBLE);
            holder.txtCallDuration.setText("Thời lượng: " + duration + "s");
        } else {
            holder.txtCallDuration.setVisibility(View.GONE);
        }
    }

    private void bindCallIcon(ViewHolder holder, String callType) {
        int iconRes;
        int color;

        switch (callType) {
            case "Incoming":
                iconRes = R.drawable.ic_call_incoming;
                color = Color.GREEN;
                break;
            case "Outgoing":
                iconRes = R.drawable.ic_call_outgoing;
                color = Color.BLUE;
                break;
            case "Missed":
                iconRes = R.drawable.ic_call_missed;
                color = Color.RED;
                break;
            default:
                iconRes = R.drawable.ic_call;
                color = Color.GRAY;
        }

        holder.imgCallType.setImageResource(iconRes);
        holder.imgCallType.setColorFilter(color);
    }

    private void bindClickActions(ViewHolder holder, Context context, IncomingCall call) {
        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(call.getPhoneNumber());
            } else {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + call.getPhoneNumber()));
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onAddToBlacklist(call.getPhoneNumber());
            } else {
                Intent intent = new Intent(context, BlacklistService.class);
                intent.setAction(BlacklistService.ACTION_ADD_TO_BLACKLIST);
                intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, call.getPhoneNumber());
                context.startService(intent);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPhoneNumber, txtCallType, txtCallDate, txtCallDuration;
        ImageView imgCallType;
        ImageButton btnCall;

        public ViewHolder(View itemView) {
            super(itemView);
            txtPhoneNumber = itemView.findViewById(R.id.txt_phone_number);
            txtCallType = itemView.findViewById(R.id.txt_call_type);
            txtCallDate = itemView.findViewById(R.id.txt_call_date);
            txtCallDuration = itemView.findViewById(R.id.txt_call_duration);
            imgCallType = itemView.findViewById(R.id.img_call_type);
            btnCall = itemView.findViewById(R.id.btn_call);
        }
    }
}
