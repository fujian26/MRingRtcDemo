package com.jacob.mringrtcdemo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MClientScocektManager {

    private static final String TAG = MClientScocektManager.class.getSimpleName();
    private static MClientScocektManager instance;

    private Socket socket;
    private byte[] bytes = new byte[1024 * 100];
    private byte[] writeBytes = new byte[1024 * 100];
    private MSocketListener listener;

    private MClientScocektManager() {

    }

    public static MClientScocektManager getInstance() {
        if (instance == null) {
            synchronized (MClientScocektManager.class) {
                if (instance == null) {
                    instance = new MClientScocektManager();
                }
            }
        }

        return instance;
    }

    public void registerListener(@NonNull MSocketListener listener) {
        this.listener = listener;
    }

    public void unRegisterListener() {
        this.listener = null;
    }

    public void startConnect() {

        try {
            socket = new Socket("192.168.20.63", 8083);
            Log.d(TAG, "startConnect: isconnected " + socket.isConnected());

            listener.onConnectResult(socket.isConnected());

            while (true) {
                InputStream is = socket.getInputStream();
                int count = is.read(bytes);

                Log.d(TAG, "read count " + count);

                if (count <= 0) {
                    continue;
                }

                int index = 0;
                byte type = bytes[index];
                index++;

                if (type == MConstant.Socket.SEND_ANSWER) {

                    long callId = DataUtil.assempleLong(bytes, index);
                    index += 8;

                    int opaqueLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive answer opaqueLength " + opaqueLength);
                    byte[] opaque = new byte[opaqueLength];
                    System.arraycopy(bytes, index, opaque, 0, opaqueLength);
                    index += opaque.length;

                    int sdpLength = DataUtil.assempleLength(bytes, index);
                    String sdp = null;
                    index += 4;
                    Log.d(TAG, "startConnect receive offer sdpLength " + sdpLength);
                    if (sdpLength > 0) {
                        byte[] sdpBytes = new byte[sdpLength];
                        System.arraycopy(bytes, index, sdpBytes, 0, sdpLength);
                        index += sdpBytes.length;
                        sdp = new String(sdpBytes, "utf-8");
                        Log.d(TAG, "startConnect receive answer sdp " + sdp);
                    }

                    int identiKeyLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive answer identiKeyLength " + identiKeyLength);
                    byte[] identiKey = new byte[identiKeyLength];
                    System.arraycopy(bytes, index, identiKey, 0, identiKeyLength);
                    index += identiKey.length;

                    listener.onAnswer(callId, opaque, sdp, identiKey);

                } else if (type == MConstant.Socket.SEND_ICECANDIDATES) {
                    receiveIceCandidates(bytes);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            listener.onConnectResult(false);
        }
    }

    public void sendOfferInfo(long callId, byte[] opaque, String sdp, byte[] identiKey) {

        if (socket == null || socket.isClosed()) {
            Log.e(TAG, "sendOfferInfo: socket == null || socket.isClosed()");
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            OutputStream os = socket.getOutputStream();

            int index = 0;
            writeBytes[0] = MConstant.Socket.SEND_OFFER;
            index++;

            byte[] callIdBytes = DataUtil.obtainBigend8Bytes(callId);
            System.arraycopy(callIdBytes, 0, writeBytes, index, callIdBytes.length);
            index += callIdBytes.length;

            // opaque
            byte[] opaqueLength = DataUtil.obtainBigend4Bytes(opaque.length);
            System.arraycopy(opaqueLength, 0, writeBytes, index, opaqueLength.length);
            index += opaqueLength.length;
            System.arraycopy(opaque, 0, writeBytes, index, opaque.length);
            index += opaque.length;

            Log.d(TAG, "sendOfferInfo opaque.length " + opaque.length);

            // sdp
            byte[] sdpBytes = sdp.getBytes("utf-8");
            byte[] sdpLength = DataUtil.obtainBigend4Bytes(sdpBytes.length);
            System.arraycopy(sdpLength, 0, writeBytes, index, sdpLength.length);
            index += sdpLength.length;
            System.arraycopy(sdpBytes, 0, writeBytes, index, sdpBytes.length);
            index += sdpBytes.length;
            Log.d(TAG, "sendOfferInfo sdpBytes.length " + sdpBytes.length);

            // identiKey
            byte[] identiKeyLength = DataUtil.obtainBigend4Bytes(identiKey.length);
            System.arraycopy(identiKeyLength, 0, writeBytes, index, identiKeyLength.length);
            index += identiKeyLength.length;
            System.arraycopy(identiKey, 0, writeBytes, index, identiKey.length);
            index += identiKey.length;
            Log.d(TAG, "sendOfferInfo identiKey.length " + identiKey.length);

            os.write(writeBytes, 0, index);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendIceCandidates(long callId, List<byte[]> opaques, List<String> sdps) {

        if (socket == null || socket.isClosed()) {
            Log.e(TAG, "sendIceCandidates: socket == null || socket.isClosed()");
            return;
        }

        try {
            OutputStream os = socket.getOutputStream();

            int index = 0;
            writeBytes[0] = MConstant.Socket.SEND_ICECANDIDATES;
            index++;

            byte[] callIdBytes = DataUtil.obtainBigend8Bytes(callId);
            System.arraycopy(callIdBytes, 0, writeBytes, index, callIdBytes.length);
            index += callIdBytes.length;

            // opaque
            byte[] opaqueSize = DataUtil.obtainBigend4Bytes(opaques.size());
            System.arraycopy(opaqueSize, 0, writeBytes, index, opaqueSize.length);
            index += opaqueSize.length;

            for (int i = 0; i < opaques.size(); i++) {
                byte[] opaque = opaques.get(i);
                byte[] opaqueLength = DataUtil.obtainBigend4Bytes(opaque.length);
                System.arraycopy(opaqueLength, 0, writeBytes, index, opaqueLength.length);
                index += opaqueLength.length;
                System.arraycopy(opaque, 0, writeBytes, index, opaque.length);
                index += opaque.length;
            }

            // sdp
            byte[] sdpSize = DataUtil.obtainBigend4Bytes(sdps.size());
            System.arraycopy(sdpSize, 0, writeBytes, index, sdpSize.length);
            index += sdpSize.length;

            for (int i = 0; i < sdps.size(); i++) {
                byte[] sdpBytes = sdps.get(i).getBytes("utf-8");
                byte[] sdpLength = DataUtil.obtainBigend4Bytes(sdpBytes.length);
                System.arraycopy(sdpLength, 0, writeBytes, index, sdpLength.length);
                index += sdpLength.length;
                System.arraycopy(sdpBytes, 0, writeBytes, index, sdpBytes.length);
                index += sdpBytes.length;
            }

            Log.d(TAG, "sendIceCandidates total index " + index);

            os.write(writeBytes, 0, index);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveIceCandidates(byte[] bytes) {

        if (socket == null || socket.isClosed()) {
            Log.e(TAG, "receiveIceCandidates: socket == null || socket.isClosed()");
            return;
        }

        int index = 1;

        try {

            long callId = DataUtil.assempleLong(bytes, index);
            index += 8;

            int opaqueSize = DataUtil.assempleLength(bytes, index);
            index += 4;
            Log.d(TAG, "receiveIceCandidates opaqueSize " + opaqueSize);
            List<byte[]> opaques = new ArrayList<>();
            for (int i = 0; i < opaqueSize; i++) {
                int opaqueLength = DataUtil.assempleLength(bytes, index);
                index += 4;
                byte[] opaque = new byte[opaqueLength];
                System.arraycopy(bytes, index, opaque, 0, opaqueLength);
                index += opaque.length;
                opaques.add(opaque);
            }

            int sdpSize = DataUtil.assempleLength(bytes, index);
            index += 4;
            List<String> sdps = new ArrayList<>();
            for (int i = 0; i < sdpSize; i++) {
                int sdpLength = DataUtil.assempleLength(bytes, index);
                index += 4;
                byte[] sdpBytes = new byte[sdpLength];
                System.arraycopy(bytes, index, sdpBytes, 0, sdpLength);
                index += sdpBytes.length;
                String sdp = new String(sdpBytes, "utf-8");
                sdps.add(sdp);

                Log.d(TAG, "receiveIceCandidates sdp " + sdp);
            }

            Log.d(TAG, "receiveIceCandidates opaques size " + opaques.size() + ", sdp size " + sdps.size());

            listener.onIceCandidates(callId, opaques, sdps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
