package com.jacob.mringrtcdemo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MServerSocketManager {

    private static final String TAG = MServerSocketManager.class.getSimpleName();

    private static MServerSocketManager instance;

    private ServerSocket ss;
    private Socket socket;
    private MSocketListener listener;
    private byte[] bytes = new byte[1024 * 10];

    private MServerSocketManager() {

    }

    public static MServerSocketManager getInstance() {
        if (instance == null) {
            synchronized (MServerSocketManager.class) {
                if (instance == null) {
                    instance = new MServerSocketManager();
                }
            }
        }

        return instance;
    }

    public void startServer(@NonNull MSocketListener listener) {
        try {
            ss = new ServerSocket(8083);
            socket = ss.accept();

            while (true) {

                InputStream is = socket.getInputStream();
                int count = is.read(bytes);
                if (count <= 0) {
                    continue;
                }

                int index = 0;
                byte type = bytes[index];
                index++;

                if (type == MConstant.Socket.SEND_OFFER) {

                    int opaqueLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startServer receive offer opaqueLength " + opaqueLength);
                    byte[] opaque = new byte[opaqueLength];
                    System.arraycopy(bytes, index, opaque, 0, opaqueLength);

                    int sdpLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startServer receive offer sdpLength " + sdpLength);
                    byte[] sdpBytes = new byte[sdpLength];
                    System.arraycopy(bytes, index, sdpBytes, 0, sdpLength);
                    String sdp = new String(sdpBytes, "utf-8");
                    Log.d(TAG, "startServer receive offer sdp " + sdp);

                    int identiKeyLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startServer receive offer identiKeyLength " + identiKeyLength);
                    byte[] identiKey = new byte[identiKeyLength];
                    System.arraycopy(bytes, index, identiKey, 0, identiKeyLength);

                    listener.onOffer(opaque, sdp, identiKey);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAnwserInfo(byte[] opaque, String sdp, byte[] identiKey) {

        if (socket == null || socket.isClosed()) {
            Log.e(TAG, "sendAnwserInfo: socket == null || socket.isClosed()");
            return;
        }

        try {
            OutputStream os = socket.getOutputStream();

            os.write(new byte[]{MConstant.Socket.SEND_ANSWER}); // send

            // opaque
            os.write(DataUtil.obtainBigend4Bytes(opaque.length));
            os.write(opaque);
            Log.d(TAG, "sendAnwserInfo opaque.length " + opaque.length);

            // sdp
            byte[] sdpBytes = sdp.getBytes("utf-8");
            os.write(DataUtil.obtainBigend4Bytes(sdpBytes.length));
            os.write(sdpBytes);
            Log.d(TAG, "sendAnwserInfo sdpBytes.length " + sdpBytes.length);

            // identiKey
            os.write(DataUtil.obtainBigend4Bytes(identiKey.length));
            os.write(identiKey);
            Log.d(TAG, "sendAnwserInfo identiKey.length " + identiKey.length);

            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
