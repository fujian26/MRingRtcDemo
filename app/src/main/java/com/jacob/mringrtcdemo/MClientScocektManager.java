package com.jacob.mringrtcdemo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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
            socket = new Socket("192.168.2.10", 8083);
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

                    int opaqueLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive answer opaqueLength " + opaqueLength);
                    byte[] opaque = new byte[opaqueLength];
                    System.arraycopy(bytes, index, opaque, 0, opaqueLength);
                    index += opaque.length;

                    int sdpLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive offer sdpLength " + sdpLength);
                    byte[] sdpBytes = new byte[sdpLength];
                    System.arraycopy(bytes, index, sdpBytes, 0, sdpLength);
                    index += sdpBytes.length;
                    String sdp = new String(sdpBytes, "utf-8");
                    Log.d(TAG, "startConnect receive answer sdp " + sdp);

                    int identiKeyLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive answer identiKeyLength " + identiKeyLength);
                    byte[] identiKey = new byte[identiKeyLength];
                    System.arraycopy(bytes, index, identiKey, 0, identiKeyLength);
                    index += identiKey.length;

                    listener.onAnswer(opaque, sdp, identiKey);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            listener.onConnectResult(false);
        }
    }

    public void sendOfferInfo(byte[] opaque, String sdp, byte[] identiKey) {

        if (socket == null || socket.isClosed()) {
            Log.e(TAG, "sendOfferInfo: socket == null || socket.isClosed()");
            return;
        }

        try {
            OutputStream os = socket.getOutputStream();

            int index = 0;
            writeBytes[0] = MConstant.Socket.SEND_OFFER;
            index++;

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void asServer() {
//
//        try {
//            ServerSocket ss = new ServerSocket(8083);
//            Socket socket = ss.accept();
//
//            System.out.println("accept socket ip: " + socket.getInetAddress() + ", port: " + socket.getPort());
//
//            InputStream is = socket.getInputStream();
//            byte[] bytes = new byte[1024];
//            int count = is.read(bytes);
//
//            System.out.println("read " + new String(bytes, 0, count, Charset.forName("utf-8")));
//
//            OutputStream os = socket.getOutputStream();
//            os.write("pc端已收到消息".getBytes("utf-8"));
//            os.flush();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
}
