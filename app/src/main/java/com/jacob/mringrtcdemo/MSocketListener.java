package com.jacob.mringrtcdemo;

public interface MSocketListener {

    void onOffer(byte[] opaque, String sdp, byte[] identiKey);

    void onAnswer(byte[] opaque, String sdp, byte[] identiKey);

}
