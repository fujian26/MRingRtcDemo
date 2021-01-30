package com.jacob.mringrtcdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.signal.ringrtc.CallException;
import org.signal.ringrtc.CallId;
import org.signal.ringrtc.CallManager;
import org.signal.ringrtc.CameraControl;
import org.signal.ringrtc.HttpHeader;
import org.signal.ringrtc.IceCandidate;
import org.signal.ringrtc.Remote;
import org.webrtc.CapturerObserver;
import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGL10;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private static final String[] NEED_PERMISSSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CHANGE_NETWORK_STATE
    };

    private final ExecutorService serviceExecutor = Executors.newSingleThreadExecutor();
    private CallManager callManager;

    private final int mRedSize = 8;
    private final int mGreenSize = 8;
    private final int mBlueSize = 8;
    private final int mAlphaSize = 8;
    private final int mDepthSize = 8;
    private final int mStencilSize = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> permissionList = new ArrayList<>();
        for (String permission : NEED_PERMISSSIONS) {
            permissionList.add(permission);
        }

        String[] newPermissions = new String[permissionList.size()];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionList.toArray(newPermissions), 1);
        }
    }

    public void onClick(View view) {
        startActivity(new Intent(this, OutgoingActivity.class));
    }

    private void startOutGoing() {

        try {
            callManager = CallManager.createCallManager(new CallManager.Observer() {
                @Override
                public void onStartCall(Remote remote, CallId callId, Boolean aBoolean, CallManager.CallMediaType callMediaType) {

                    serviceExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            List<PeerConnection.IceServer> iceServers = new ArrayList<>();
                            PeerConnection.IceServer iceServer = new PeerConnection.IceServer("stun:stun1.l.google.com:19302");
                            iceServers.add(iceServer);

                            Log.d(TAG, "onStartCall callId " + callId.longValue() + " isOutgoing " + aBoolean
                                    + ", time " + System.currentTimeMillis());
                            try {
                                callManager.proceed(callId, MainActivity.this, new EglBase10Impl(null,
                                                new int[]{
                                                        EGL10.EGL_RED_SIZE, mRedSize,
                                                        EGL10.EGL_GREEN_SIZE, mGreenSize,
                                                        EGL10.EGL_BLUE_SIZE, mBlueSize,
                                                        EGL10.EGL_ALPHA_SIZE, mAlphaSize,
                                                        EGL10.EGL_DEPTH_SIZE, mDepthSize,
                                                        EGL10.EGL_STENCIL_SIZE, mStencilSize,
                                                        EGL10.EGL_RENDERABLE_TYPE, 4,//egl版本  2.0
                                                        EGL10.EGL_NONE}), null, null, new CameraControl() {
                                            @Override
                                            public boolean hasCapturer() {
                                                return false;
                                            }

                                            @Override
                                            public void initCapturer(@NonNull CapturerObserver capturerObserver) {

                                            }

                                            @Override
                                            public void setEnabled(boolean b) {

                                            }

                                            @Override
                                            public void flip() {

                                            }
                                        },
                                        iceServers,
                                        true, false);
                            } catch (CallException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });
                }

                @Override
                public void onCallEvent(Remote remote, CallManager.CallEvent callEvent) {
                    Log.d(TAG, "onCallEvent callEvent " + callEvent.name()
                            + ", time " + System.currentTimeMillis());
                }

                @Override
                public void onCallConcluded(Remote remote) {
                    Log.d(TAG, "onCallConcluded");
                }

                @Override
                public void onSendOffer(CallId callId, Remote remote, Integer integer, Boolean aBoolean, @Nullable byte[] bytes, @Nullable String s, CallManager.CallMediaType callMediaType) {
                    Log.d(TAG, "onSendOffer s " + s + " callMediaType " + callMediaType + " bytes.length " + bytes.length);
                }

                @Override
                public void onSendAnswer(CallId callId, Remote remote, Integer integer, Boolean aBoolean, @Nullable byte[] bytes, @Nullable String s) {
                    Log.d(TAG, "onSendAnswer callId " + callId);
                }

                @Override
                public void onSendIceCandidates(CallId callId, Remote remote, Integer integer, Boolean aBoolean, List<IceCandidate> list) {
                    Log.d(TAG, "onSendIceCandidates callId " + callId);
                }

                @Override
                public void onSendHangup(CallId callId, Remote remote, Integer integer, Boolean aBoolean, CallManager.HangupType hangupType, Integer integer1, Boolean aBoolean1) {
                    Log.d(TAG, "onSendHangup callId " + callId);
                }

                @Override
                public void onSendBusy(CallId callId, Remote remote, Integer integer, Boolean aBoolean) {
                    Log.d(TAG, "onSendBusy callId " + callId);
                }

                @Override
                public void onSendCallMessage(@NonNull UUID uuid, @NonNull byte[] bytes) {
                    Log.d(TAG, "onSendCallMessage uuid " + uuid);
                }

                @Override
                public void onSendHttpRequest(long l, @NonNull String s, @NonNull CallManager.HttpMethod httpMethod, @Nullable List<HttpHeader> list, @Nullable byte[] bytes) {
                    Log.d(TAG, "onSendHttpRequest s " + s);
                }
            });

        } catch (CallException e) {
            e.printStackTrace();
        }


        serviceExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callManager.call(new Remote() {
                        @Override
                        public boolean recipientEquals(Remote remote) {
                            return true;
                        }
                    }, CallManager.CallMediaType.AUDIO_CALL, 1);
                } catch (CallException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void onClick2(View view) {
        startActivity(new Intent(this, IncommingActivity.class));
    }
}