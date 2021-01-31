package com.jacob.mringrtcdemo;

import java.util.List;

public interface MSocketListener {

    void onConnectResult(boolean conneced);

    void onOffer(long callId, byte[] opaque, String sdp, byte[] identiKey);

    void onAnswer(long callId, byte[] opaque, String sdp, byte[] identiKey);

    void onIceCandidates(long callId, List<byte[]> opaques, List<String> sdps);
}
