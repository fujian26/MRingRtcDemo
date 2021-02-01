package com.jacob.mringrtcdemo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.signal.ringrtc.CallId;
import org.signal.ringrtc.Remote;

public class RemotePeer implements Remote, Parcelable {

    public RemotePeer(CallId callId){
        this.callId = callId;
    }

    private RemotePeer(@NonNull Parcel in) {
        this.callId = new CallId(in.readLong());
    }

    public CallId getCallId() {
        return callId;
    }

    public void setCallId(CallId callId) {
        this.callId = callId;
    }

    private CallId callId;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(callId.longValue());
    }

    @Override
    public boolean recipientEquals(Remote remote) {
        return false;
    }

    public static final Creator<RemotePeer> CREATOR = new Creator<RemotePeer>() {
        @Override
        public RemotePeer createFromParcel(@NonNull Parcel in) {
            return new RemotePeer(in);
        }

        @Override
        public RemotePeer[] newArray(int size) {
            return new RemotePeer[size];
        }
    };
}
