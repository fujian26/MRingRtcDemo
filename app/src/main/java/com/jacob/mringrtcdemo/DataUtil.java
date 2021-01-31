package com.jacob.mringrtcdemo;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {

    public static byte[] obtainBigend4Bytes(int length) {
        return new byte[]{(byte) ((length >> 24) & 0xff), (byte) ((length >> 16) & 0xff),
                (byte) ((length >> 8) & 0xff), (byte) ((length) & 0xff)};
    }

    public static int assempleLength(byte[] bytes, int offset) {

        return (0xff000000 & (bytes[offset] << 24))
                | (0x00ff0000 & (bytes[offset + 1] << 16))
                | (0x0000ff00 & (bytes[offset + 2] << 8))
                | (0x000000ff & (bytes[offset + 3]));

    }

    public static List<PeerConnection.IceServer> obtainIceServers() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer iceServer = new PeerConnection.IceServer("stun:stun1.l.google.com:19302");
        iceServers.add(iceServer);

        return iceServers;
    }

    public static byte[] obtainBigend8Bytes(long id) {
        return new byte[]{(byte) ((id >> 56) & 0xff), (byte) ((id >> 48) & 0xff),
                (byte) ((id >> 40) & 0xff), (byte) ((id >> 32) & 0xff),
                (byte) ((id >> 24) & 0xff), (byte) ((id >> 16) & 0xff),
                (byte) ((id >> 8) & 0xff), (byte) ((id) & 0xff)};
    }

    public static long assempleLong(byte[] bytes, int offset) {

        StringBuilder builder = new StringBuilder();
        for (int i = offset; i <= 8; i++) {
            builder.append(String.format("%02x", bytes[i]));
        }
        return Long.parseLong(builder.toString(), 16);

//        return (0xff000000 & (bytes[offset] << 56))
//                | (0x00ff0000 & (bytes[offset + 1] << 48))
//                | (0x0000ff00 & (bytes[offset + 2] << 40))
//                | (0x000000ff & (bytes[offset + 3] << 32))
//                | (0x000000ff & (bytes[offset + 4] << 24))
//                | (0x000000ff & (bytes[offset + 5] << 16))
//                | (0x000000ff & (bytes[offset + 6] << 8))
//                | (0x000000ff & (bytes[offset + 7]));

    }
}
