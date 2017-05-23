package com.blagonravovan.anonymousvotingserver.messages;


public class SignInMessage {
    String mId;
    String mPublicKey;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getPublicKey() {
        return mPublicKey;
    }

    public void setPublicKey(String publicKey) {
        this.mPublicKey = publicKey;
    }
}
