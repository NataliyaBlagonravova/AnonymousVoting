package com.blagonravovan.anonymousvotingclient.messages;


public class RegisterBulletinMessage {
    String mId;
    String mSign;
    String mBulletin;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getSign() {
        return mSign;
    }

    public void setSign(String sign) {
        mSign = sign;
    }

    public String getBulletin() {
        return mBulletin;
    }

    public void setBulletin(String bulletin) {
        mBulletin = bulletin;
    }
}
