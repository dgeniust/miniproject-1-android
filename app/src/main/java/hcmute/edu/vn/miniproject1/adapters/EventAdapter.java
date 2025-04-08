package hcmute.edu.vn.miniproject1.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hcmute.edu.vn.miniproject1.R;
import hcmute.edu.vn.miniproject1.models.Event;

public class EventAdapter extends BaseAdapter {
    private Context context;
    private List<Event> eventList;
    private LayoutInflater inflater;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return (eventList != null) ? eventList.size() : 0; // Tránh lỗi NullPointerException
    }

    @Override
    public Object getItem(int position) {
        return (eventList != null && position < eventList.size()) ? eventList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        Event event = (Event) getItem(position);
        return (event != null) ? event.getId() : -1; // Tránh lỗi nếu event bị null
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_event, parent, false);
            holder = new ViewHolder();
            holder.txtEventTitle = convertView.findViewById(R.id.txt_Event_Title);
            holder.txtEventDate = convertView.findViewById(R.id.txt_Event_Date);
            holder.txtEventTime = convertView.findViewById(R.id.txt_Event_Time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = (Event) getItem(position);
        if (event != null) {
            holder.txtEventTitle.setText(event.getTitle());
            holder.txtEventDate.setText("Ngày: " + event.getDate());
            holder.txtEventTime.setText("Giờ: " + (event.getTime().isEmpty() ? "Chưa chọn" : event.getTime())); //Tránh null
            Log.d("EventAdapter", "Event: " + event.getTitle() + ", Date: " + event.getDate() + ", Time: " + event.getTime());
        }

        return convertView;
    }

    // Phương thức cập nhật danh sách sự kiện
    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged(); // Cập nhật lại ListView
    }

    private static class ViewHolder {
        TextView txtEventTitle, txtEventDate, txtEventTime;
    }
}
