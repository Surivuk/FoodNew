package com.example.aleksandarx.foodfinder.view;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.aleksandarx.foodfinder.R;

import java.util.List;

/**
 * Created by EuroPATC on 9/7/2016.
 */
public class BluetoothListAdapter extends ArrayAdapter<BluetoothDevice> {

    public BluetoothListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public BluetoothListAdapter(Context context, int resource, List<BluetoothDevice> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_view_row, null);
        }

        BluetoothDevice p = getItem(position);
        if (p != null) {
            TextView title = (TextView) v.findViewById(R.id.name);
            TextView mac = (TextView) v.findViewById(R.id.mac);

            if (title != null) {
                title.setText(p.getName());
                mac.setText(p.getAddress());
            }
        }
        return v;
    }
}
