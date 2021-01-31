package com.jacob.mringrtcdemo;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncommingActivity extends AppCompatActivity {

    private final String TAG = IncommingActivity.class.getSimpleName();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomming);

        MServerSocketManager.getInstance().registerListener(new MSocketListener() {
            @Override
            public void onConnectResult(boolean conneced) {
                Log.d(TAG, "onConnectResult conneced " + conneced);
            }

            @Override
            public void onOffer(byte[] opaque, String sdp, byte[] identiKey) {
                Log.d(TAG, "onOffer, opaque: " + Base64.encodeToString(opaque, Base64.NO_WRAP)
                        + "\nsdp: " + sdp
                        + "\nidentiKey " + Base64.encodeToString(identiKey, Base64.NO_WRAP));


                byte[] answerOpaque = new byte[]{(byte) 0x44, (byte) 0x67, (byte) 0x99};
                String answerSdp = "I received your offer, here is my answer ballalala";
                byte[] answerIdentiKey = new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xff};

                Log.d(TAG, "send answer, opaque: " + Base64.encodeToString(answerOpaque, Base64.NO_WRAP)
                        + "\nsdp: " + answerSdp
                        + "\nidentiKey " + Base64.encodeToString(answerIdentiKey, Base64.NO_WRAP));

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        MServerSocketManager.getInstance().sendAnwserInfo(answerOpaque, answerSdp, answerIdentiKey);
                    }
                });
            }

            @Override
            public void onAnswer(byte[] opaque, String sdp, byte[] identiKey) {

            }

            @Override
            public void onIceCandidates(List<byte[]> opaques, List<String> sdps) {

                Log.d(TAG, "onIceCandidates opaques size " + opaques.size() + ", sdps size " + sdps.size());

                List<byte[]> opaquesSend = new ArrayList<>();
                opaquesSend.add(new byte[]{(byte) 0x03, (byte) 0x78});

                List<String> sdpsSend = new ArrayList<>();
                sdpsSend.add("sdp one from server");
                sdpsSend.add("sdp two from server");

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        MServerSocketManager.getInstance().sendIceCandidates(opaquesSend, sdpsSend);
                    }
                });

            }
        });

        new Thread() {
            @Override
            public void run() {
                MServerSocketManager.getInstance().startServer();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MServerSocketManager.getInstance().unRegisterListener();
        MServerSocketManager.getInstance().close();
    }
}