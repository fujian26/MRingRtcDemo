package com.jacob.mringrtcdemo;

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

}
