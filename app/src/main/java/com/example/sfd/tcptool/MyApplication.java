package com.example.sfd.tcptool;

import android.app.Application;
import android.content.Context;

/**
 * Created by SFD on 2018/2/1.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
