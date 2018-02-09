package com.example.sfd.tcptool;

import android.os.Message;

import static com.example.sfd.tcptool.MainActivity.mHandler;

/**
 * Created by SFD on 2018/2/5.
 */

public class MyMessage {
    public static void sendMyMessage(String data, int type){
        Message tmpMessage = mHandler.obtainMessage();
        tmpMessage.what = type;
        tmpMessage.obj = data;
        mHandler.sendMessage(tmpMessage);
    }

    public static void sendMyMessage(int type){
        Message tmpMessage = mHandler.obtainMessage();
        tmpMessage.what = type;
        mHandler.sendMessage(tmpMessage);
    }
}
