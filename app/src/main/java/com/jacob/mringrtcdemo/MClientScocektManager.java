package com.jacob.mringrtcdemo;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class MClientScocektManager {

    private static final String TAG = MClientScocektManager.class.getSimpleName();
    private static MClientScocektManager instance;

    private Socket socket;
    private byte[] bytes = new byte[1024 * 10];
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

    public void startConnect() {

        try {
            socket = new Socket("192.168.20.25", 8083);
            Log.d(TAG, "startConnect: isconnected " + socket.isConnected());

            while (true) {
                InputStream is = socket.getInputStream();
                int count = is.read(bytes);

                if (count <= 0) {
                    continue;
                }

                int index = 0;
                byte type = bytes[index];
                index++;

                if (type == MConstant.Socket.SEND_ANSWER) {

                    int opaqueLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive offer opaqueLength " + opaqueLength);
                    byte[] opaque = new byte[opaqueLength];
                    System.arraycopy(bytes, index, opaque, 0, opaqueLength);

                    int sdpLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive offer sdpLength " + sdpLength);
                    byte[] sdpBytes = new byte[sdpLength];
                    System.arraycopy(bytes, index, sdpBytes, 0, sdpLength);
                    String sdp = new String(sdpBytes, "utf-8");
                    Log.d(TAG, "startConnect receive offer sdp " + sdp);

                    int identiKeyLength = DataUtil.assempleLength(bytes, index);
                    index += 4;
                    Log.d(TAG, "startConnect receive offer identiKeyLength " + identiKeyLength);
                    byte[] identiKey = new byte[identiKeyLength];
                    System.arraycopy(bytes, index, identiKey, 0, identiKeyLength);

                    listener.onAnswer(opaque, sdp, identiKey);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendOfferInfo(byte[] opaque, String sdp, byte[] identiKey) {

        if (socket == null || socket.isClosed()) {
            Log.e(TAG, "sendOfferInfo: socket == null || socket.isClosed()");
            return;
        }

        try {
            OutputStream os = socket.getOutputStream();

            os.write(new byte[]{MConstant.Socket.SEND_OFFER}); // send

            // opaque
            os.write(DataUtil.obtainBigend4Bytes(opaque.length));
            os.write(opaque);
            Log.d(TAG, "sendOfferInfo opaque.length " + opaque.length);

            // sdp
            byte[] sdpBytes = sdp.getBytes("utf-8");
            os.write(DataUtil.obtainBigend4Bytes(sdpBytes.length));
            os.write(sdpBytes);
            Log.d(TAG, "sendOfferInfo sdpBytes.length " + sdpBytes.length);

            // identiKey
            os.write(DataUtil.obtainBigend4Bytes(identiKey.length));
            os.write(identiKey);
            Log.d(TAG, "sendOfferInfo identiKey.length " + identiKey.length);

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
