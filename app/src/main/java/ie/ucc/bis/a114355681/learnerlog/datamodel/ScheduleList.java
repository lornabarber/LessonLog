package ie.ucc.bis.a114355681.learnerlog.datamodel;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ie.ucc.bis.a114355681.learnerlog.R;
import ie.ucc.bis.a114355681.learnerlog.datamodel.Booking;

/**
 * Created by Lorna on 31/10/2017.
 */

public class ScheduleList extends ArrayAdapter<Booking> {
        private Activity context;
        //create a list to store booking information called schedule.
        List<Booking> schedule;

        public ScheduleList(Activity context, List<Booking> schedule){
        super(context, R.layout.schedule_list_layout, schedule);
            this.context = context;
            this.schedule = schedule;

        }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //inflate the schedule_list_layout
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.schedule_list_layout, null, true);
        TextView etLessonNo = (TextView) listViewItem.findViewById(R.id.etLessonNo);
        TextView etLessonDate = (TextView) listViewItem.findViewById(R.id.etLessonDate);
        TextView etLessonTime = (TextView) listViewItem.findViewById(R.id.etLessonTime);
        TextView etLessonStatus = (TextView) listViewItem.findViewById(R.id.etLessonStatus);

        //set the data in the schedule array into the text boxes in the list view.
        Booking booking = schedule.get(position);
        etLessonNo.setText(booking.getLessonType().toString());
        etLessonDate.setText(booking.getDate());
        etLessonTime.setText(booking.getTime());
        etLessonStatus.setText(booking.getStatus());

        //return the items in the list view.
        return listViewItem;


    }
}
