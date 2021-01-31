package com.jacob.mringrtcdemo;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

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

public class OutgoingActivity extends AppCompatActivity {

    private final String TAG = OutgoingActivity.class.getSimpleName();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ExecutorService callExecutor = Executors.newSingleThreadExecutor();
    private SurfaceViewRenderer remoteSv, localSv;
    private EglBase eglBase;
    private CallManager callManager;

    private static final String privateIdentiKey = "YHr9+1aPZLd5xZ77zzll3y5+fMrzAPOWwKYgtxO+n34=";
    private static final String publicIdentiKey = "Bey65Z/fjKnQ3GtD/dSsyPd3iTCGDlEfqmPtusfUphBm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing);

        remoteSv = findViewById(R.id.sv_remote);
        localSv = findViewById(R.id.sv_local);
        eglBase = EglBase.create();

        remoteSv.init(eglBase.getEglBaseContext(), null);
        remoteSv.setKeepScreenOn(true);

        localSv.init(eglBase.getEglBaseContext(), null);
        localSv.setKeepScreenOn(true);

        initListeners();
    }

    private void initListeners() {

        try {
            callManager = CallManager.createCallManager(new CallManager.Observer() {
                @Override
                public void onStartCall(Remote remote, CallId callId, Boolean aBoolean, CallManager.CallMediaType callMediaType) {

                    Log.d(TAG, "onStartCall callMediaType " + callMediaType + ", callId " + callId);

                    callExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callManager.proceed(callId, OutgoingActivity.this, eglBase, localSv, remoteSv,
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

                    Log.d(TAG, "onSendOffer sdp " + s + ", callId: " + callId);

                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            MClientScocektManager.getInstance().sendOfferInfo(callId.longValue(), bytes, s, Base64.decode(publicIdentiKey, Base64.NO_WRAP));

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
                public void onSendAnswer(CallId callId, Remote remote, Integer integer, Boolean aBoolean, @Nullable byte[] bytes, @Nullable String s) {

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

                            List<byte[]> opaques = new ArrayList<>(list.size());
                            List<String> sdps = new ArrayList<>(list.size());

                            for (IceCandidate candidate : list) {
                                opaques.add(candidate.getOpaque());
                                sdps.add(candidate.getSdp());
                            }

                            MClientScocektManager.getInstance().sendIceCandidates(callId.longValue(), opaques, sdps);

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

        MClientScocektManager.getInstance().registerListener(new MSocketListener() {
            @Override
            public void onConnectResult(boolean conneced) {

                if (!conneced) {
                    Log.e(TAG, "onConnectResult !conneced");
                    return;
                }
            }

            @Override
            public void onOffer(long callId, byte[] opaque, String sdp, byte[] identiKey) {

            }

            @Override
            public void onAnswer(long callId, byte[] opaque, String sdp, byte[] identiKey) {
                Log.d(TAG, "onAnswer, opaque: " + Base64.encodeToString(opaque, Base64.NO_WRAP)
                        + "\nsdp: " + sdp
                        + "\nidentiKey " + Base64.encodeToString(identiKey, Base64.NO_WRAP)
                        + "\ncallId: " + Long.toHexString(callId));

                callExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callManager.receivedAnswer(new CallId(callId), 2, opaque, sdp, true,
                                    identiKey, Base64.decode(publicIdentiKey, Base64.NO_WRAP));
                        } catch (CallException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
                            callManager.receivedIceCandidates(new CallId(callId), 2, iceCandidates);
                        } catch (CallException e) {
                            e.printStackTrace();
                        }
                    }
                });
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

        callExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callManager.call(new Remote() {
                        @Override
                        public boolean recipientEquals(Remote remote) {
                            return false;
                        }
                    }, CallManager.CallMediaType.VIDEO_CALL, 1);
                } catch (CallException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MClientScocektManager.getInstance().unRegisterListener();
        MClientScocektManager.getInstance().closeSocket();
    }
}