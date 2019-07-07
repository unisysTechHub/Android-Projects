package com.rameshpenta.callRecorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sujatha on 12-07-2015.
 */
public class TitleBarListItem extends ArrayAdapter {
    ArrayList<TitleBarMenuItem>  titleBarMenuItems;
    Context context;
    public TitleBarListItem(Context context, int resource, ArrayList<TitleBarMenuItem> titleBarMenuItems) {
        super(context, resource, titleBarMenuItems);
        this.context=context;
        this.titleBarMenuItems=titleBarMenuItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row =LayoutInflater.from(context).inflate(R.layout.row,null);
        ImageView icon = (ImageView) row.findViewById(R.id.icon);
        TextView title  = (TextView) row.findViewById(R.id.title);

        title.setText(titleBarMenuItems.get(position).getTitle());
         icon.setImageResource(titleBarMenuItems.get(position).getTitle_icon());


        return row;
    }
}
