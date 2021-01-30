package com.jacob.mringrtcdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutgoingActivity extends AppCompatActivity {

    private final String TAG = OutgoingActivity.class.getSimpleName();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing);

        MClientScocektManager.getInstance().registerListener(new MSocketListener() {
            @Override
            public void onConnectResult(boolean conneced) {

                if (!conneced) {
                    Log.e(TAG, "onConnectResult !conneced");
                    return;
                }

                Log.d(TAG, "onConnectResult success");

                byte[] opaque = new byte[] {(byte) 0x45, (byte) 0x67, (byte) 0x99, (byte) 0xa4,
                        (byte) 0x11, (byte) 0x3e};
                String sdp = "test sdp ip:127.0.0.1 666 hello";
                byte[] identiKey = new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc};

                Log.d(TAG, "send offer, opaque: " + Base64.encodeToString(opaque, Base64.NO_WRAP)
                        + "\nsdp: " + sdp
                        + "\nidentiKey " + Base64.encodeToString(identiKey, Base64.NO_WRAP));

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        MClientScocektManager.getInstance().sendOfferInfo(opaque, sdp, identiKey);
                    }
                });
            }

            @Override
            public void onOffer(byte[] opaque, String sdp, byte[] identiKey) {

            }

            @Override
            public void onAnswer(byte[] opaque, String sdp, byte[] identiKey) {
                Log.d(TAG, "onAnswer, opaque: " + Base64.encodeToString(opaque, Base64.NO_WRAP)
                        + "\nsdp: " + sdp
                        + "\nidentiKey " + Base64.encodeToString(identiKey, Base64.NO_WRAP));
            }
        });
    }

    public void startCall(View view) {
        new Thread() {
            @Override
            public void run() {
                MClientScocektManager.getInstance().startConnect();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MClientScocektManager.getInstance().unRegisterListener();
    }
}