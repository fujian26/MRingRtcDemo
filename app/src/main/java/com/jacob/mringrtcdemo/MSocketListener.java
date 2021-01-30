package com.jacob.mringrtcdemo;

public interface MSocketListener {

    void onConnectResult(boolean conneced);

    void onOffer(byte[] opaque, String sdp, byte[] identiKey);

    void onAnswer(byte[] opaque, String sdp, byte[] identiKey);

}
