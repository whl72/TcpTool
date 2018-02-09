package com.example.sfd.tcptool;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by SFD on 2018/2/1.
 */

public class MyAdapter extends BaseAdapter {

    LayoutInflater inflater;
    List<ScanResult> list;

    public MyAdapter(Context context, List<ScanResult> list){
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View mview = null;

        mview = inflater.inflate(R.layout.wifi_item, null);
        ScanResult scanResult = list.get(i);
        TextView wifi_ssid = (TextView) mview.findViewById(R.id.text_ssid);
        ImageView wifi_rssi = (ImageView) mview.findViewById(R.id.image_rssi);
        wifi_ssid.setText(scanResult.SSID);
        WifiAdmin.level = WifiManager.calculateSignalLevel(scanResult.level, 5);
        wifi_rssi.setImageResource(R.drawable.level_list);
        wifi_rssi.setImageLevel(WifiAdmin.level);

        return mview;
    }
}
