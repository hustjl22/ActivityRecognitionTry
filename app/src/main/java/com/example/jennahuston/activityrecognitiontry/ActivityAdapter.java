package com.example.jennahuston.activityrecognitiontry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ActivityAdapter extends ArrayAdapter<Activity> {

    private static class ViewHolder {
        private TextView itemView;
    }

    public ActivityAdapter(Context context, ArrayList<Activity> items) {
        super(context, 0, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Activity activity = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_activity, parent, false);
        }

        TextView activityTimeRange = (TextView) convertView.findViewById(R.id.activityTimeRange);
        TextView activityType = (TextView) convertView.findViewById(R.id.activityType);

        activityTimeRange.setText(activity.getTimeRangeStringShort());
        activityType.setText(activity.getTypeString());

        return convertView;
    }
}
