package hcmute.edu.vn.miniproject1.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Event;
import hcmute.edu.vn.miniproject1.services.EventDatabaseHelper;

public class CompletedEventAdapter extends BaseAdapter {
    private Context context;
    private List<Event> eventList;
    private EventDatabaseHelper dbHelper;

    public CompletedEventAdapter(Context context, List<Event> eventList, EventDatabaseHelper dbHelper) {
        this.context = context;
        this.eventList = eventList;
        this.dbHelper = dbHelper;
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Object getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return eventList.get(position).getId();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.completed_event_item, parent, false);

            holder = new ViewHolder();
            holder.txtEventTitle = convertView.findViewById(R.id.txt_Event_Title);
            holder.txtEventDate = convertView.findViewById(R.id.txt_Event_Date);
            holder.txtCompletedTime = convertView.findViewById(R.id.txt_Completed_Time);
            holder.btnDelete = convertView.findViewById(R.id.btn_Delete); //  Lấy nút Xóa

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = eventList.get(position);
        holder.txtEventTitle.setText("Tiêu Đề: " + event.getTitle());
        holder.txtEventDate.setText("Ngày sự kiện: " + event.getDate());
        holder.txtCompletedTime.setText("Giờ hoàn thành: " + event.getCompletedTime());

        //  Xử lý khi bấm nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Xóa sự kiện")
                    .setMessage("Bạn có chắc muốn xóa sự kiện này?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        dbHelper.deleteEvent(event.getId()); // Xóa khỏi database
                        eventList.remove(position); // Xóa khỏi danh sách hiển thị
                        notifyDataSetChanged(); // Cập nhật lại ListView
                        Toast.makeText(context, "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return convertView;
    }
    static class ViewHolder {
        TextView txtEventTitle;
        TextView txtEventDate;
        TextView txtCompletedTime;
        ImageView btnDelete;
    }


}
