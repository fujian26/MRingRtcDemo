package com.jacob.mringrtcdemo;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.signal.ringrtc.CallException;
import org.signal.ringrtc.CallId;
import org.signal.ringrtc.CallManager;
import org.signal.ringrtc.CameraControl;
import org.signal.ringrtc.HttpHeader;
import org.signal.ringrtc.IceCandidate;
import org.signal.ringrtc.Remote;
import org.webrtc.CapturerObserver;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncommingActivity extends AppCompatActivity {

    private final String TAG = IncommingActivity.class.getSimpleName();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ExecutorService callExecutor = Executors.newSingleThreadExecutor();

    private SurfaceViewRenderer svRemote, svLocal;
    private EglBase eglBase;
    private CallManager callManager;

    private static final String privateIdentiKey = "GH/IFK7exIFO2M7+ogHCSGNCKyI+guETBV4SyMwo+Gk=";
    private static final String publicIdentiKey = "BTn1f1xCcea/vx2/C9gJb3Jr84NmjOpJAcp/V9Rr3Eo4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomming);

        svRemote = findViewById(R.id.sv_remote);
        svLocal = findViewById(R.id.sv_local);

        eglBase = EglBase.create();
        svRemote.init(eglBase.getEglBaseContext(), null);
        svRemote.setKeepScreenOn(true);
        svLocal.init(eglBase.getEglBaseContext(), null);
        svLocal.setKeepScreenOn(true);

        initListeners();
    }

    private void initListeners() {

        try {
            callManager = CallManager.createCallManager(new CallManager.Observer() {
                @Override
                public void onStartCall(Remote remote, CallId callId, Boolean aBoolean, CallManager.CallMediaType callMediaType) {

                    Log.d(TAG, "onStartCall callid " + callId);

                    callExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callManager.proceed(callId, IncommingActivity.this, eglBase, svLocal, svRemote,
                                        new CameraControl() {
                                            @Override
                                            public boolean hasCapturer() {
                                                return true;
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
                                        }, DataUtil.obtainIceServers(), false, true);
                            } catch (CallException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }

                @Override
                public void onCallEvent(Remote remote, CallManager.CallEvent callEvent) {

                }

                @Override
                public void onCallConcluded(Remote remote) {

                }

                @Override
                public void onSendOffer(CallId callId, Remote remote, Integer integer, Boolean aBoolean, @Nullable byte[] bytes, @Nullable String s, CallManager.CallMediaType callMediaType) {

                }

                @Override
                public void onSendAnswer(CallId callId, Remote remote, Integer integer, Boolean aBoolean, @Nullable byte[] bytes, @Nullable String s) {

                    Log.d(TAG, "onSendAnswer callId " + callId + "\nsdp: " + s);

                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            MServerSocketManager.getInstance().sendAnwserInfo(callId.longValue(), bytes, s, Base64.decode(publicIdentiKey, Base64.NO_WRAP));

                            callExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callManager.messageSent(callId);
                                    } catch (CallException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }

                @Override
                public void onSendIceCandidates(CallId callId, Remote remote, Integer integer, Boolean aBoolean, List<IceCandidate> list) {

                    if (list == null || list.isEmpty()) {
                        Log.e(TAG, "onSendIceCandidates list == null || list.isEmpty()");
                        return;
                    }

                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {

                            List<byte[]> opaques = new ArrayList<>();
                            List<String> sdps = new ArrayList<>();

                            for (IceCandidate candidate : list) {
                                opaques.add(candidate.getOpaque());
                                sdps.add(candidate.getSdp());
                            }

                            MServerSocketManager.getInstance().sendIceCandidates(callId.longValue(), opaques, sdps);

                            callExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        callManager.messageSent(callId);
                                    } catch (CallException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

                }

                @Override
                public void onSendHangup(CallId callId, Remote remote, Integer integer, Boolean aBoolean, CallManager.HangupType hangupType, Integer integer1, Boolean aBoolean1) {
                    callExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callManager.messageSent(callId);
                            } catch (CallException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onSendBusy(CallId callId, Remote remote, Integer integer, Boolean aBoolean) {

                }

                @Override
                public void onSendCallMessage(@NonNull UUID uuid, @NonNull byte[] bytes) {

                }

                @Override
                public void onSendHttpRequest(long l, @NonNull String s, @NonNull CallManager.HttpMethod httpMethod, @Nullable List<HttpHeader> list, @Nullable byte[] bytes) {

                }
            });
        } catch (CallException e) {
            e.printStackTrace();
        }

        MServerSocketManager.getInstance().registerListener(new MSocketListener() {
            @Override
            public void onConnectResult(boolean conneced) {
                Log.d(TAG, "onConnectResult conneced " + conneced);
            }

            @Override
            public void onOffer(long callId, byte[] opaque, String sdp, byte[] identiKey) {
                Log.d(TAG, "onOffer "
                        + "\nsdp: " + sdp
                        + "\nidentiKey " + Base64.encodeToString(identiKey, Base64.NO_WRAP)
                        + "\ncallId " + Long.toHexString(callId));

                callExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callManager.receivedOffer(new CallId(callId), new Remote() {
                                        @Override
                                        public boolean recipientEquals(Remote remote) {
                                            return false;
                                        }
                                    }, 1, opaque, sdp, 0L, CallManager.CallMediaType.VIDEO_CALL, 2,
                                    true, true, identiKey, Base64.decode(publicIdentiKey, Base64.NO_WRAP));
                        } catch (CallException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onAnswer(long callId, byte[] opaque, String sdp, byte[] identiKey) {

            }

            @Override
            public void onIceCandidates(long callId, List<byte[]> opaques, List<String> sdps) {

                if (opaques == null || sdps == null) {
                    Log.e(TAG, "onIceCandidates opaques == null || sdps == null");
                    return;
                }

                Log.d(TAG, "onIceCandidates opaques size " + opaques.size() + ", sdps size " + sdps.size()
                        + " callId " + Long.toHexString(callId));

                callExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<IceCandidate> iceCandidates = new ArrayList<>();
                            for (int i = 0; i < opaques.size(); i++) {
                                iceCandidates.add(new IceCandidate(opaques.get(i), sdps.get(i)));
                            }
                            callManager.receivedIceCandidates(new CallId(callId), 1, iceCandidates);
                        } catch (CallException e) {
                            e.printStackTrace();
                        }
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