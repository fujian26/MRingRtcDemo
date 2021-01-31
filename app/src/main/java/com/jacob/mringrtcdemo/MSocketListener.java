package com.jacob.mringrtcdemo;

import java.util.List;

public interface MSocketListener {

    void onConnectResult(boolean conneced);

    void onOffer(byte[] opaque, String sdp, byte[] identiKey);

    void onAnswer(byte[] opaque, String sdp, byte[] identiKey);

    void onIceCandidates(List<byte[]> opaques, List<String> sdps);
}
