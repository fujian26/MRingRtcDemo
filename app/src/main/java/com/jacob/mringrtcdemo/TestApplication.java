package com.jacob.mringrtcdemo;

import android.app.Application;

import org.signal.ringrtc.CallManager;

public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CallManager.initialize(this, null);
    }
}
