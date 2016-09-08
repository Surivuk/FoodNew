package com.example.aleksandarx.foodfinder.view;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.aleksandarx.foodfinder.R;
import com.example.aleksandarx.foodfinder.view.model.FriendModel;

import java.util.List;

/**
 * Created by EuroPATC on 9/8/2016.
 */
public class FriendListAdapter extends ArrayAdapter<FriendModel> {

    public FriendListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public FriendListAdapter(Context context, int resource, List<FriendModel> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.friend_list_row, null);
        }

        FriendModel p = getItem(position);
        if (p != null) {
            TextView title = (TextView) v.findViewById(R.id.friend_list_name);

            if (title != null) {
                title.setText(p.firstName + " " + p.lastName);
            }
        }
        return v;
    }
}
