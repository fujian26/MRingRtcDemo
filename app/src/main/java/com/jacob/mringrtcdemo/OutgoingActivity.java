package com.jacob.mringrtcdemo;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
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

                List<byte[]> opaques = new ArrayList<>();
                opaques.add(new byte[]{(byte) 0x11, (byte) 0x55});
                opaques.add(new byte[]{(byte) 0xee, (byte) 0x66});
                opaques.add(new byte[]{(byte) 0x14, (byte) 0x77});

                List<String> sdps = new ArrayList<>();
                sdps.add("sdp one from client");
                sdps.add("sdp two from client");
                sdps.add("sdp three from client");
                sdps.add("sdp four from client");

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        MClientScocektManager.getInstance().sendIceCandidates(opaques, sdps);
                    }
                });
            }

            @Override
            public void onIceCandidates(List<byte[]> opaques, List<String> sdps) {
                Log.d(TAG, "onIceCandidates opaques size " + opaques.size() + ", sdps size " + sdps.size());
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
        MClientScocektManager.getInstance().closeSocket();
    }
}