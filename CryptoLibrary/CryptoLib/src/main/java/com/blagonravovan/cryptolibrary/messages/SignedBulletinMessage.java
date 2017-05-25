package com.blagonravovan.cryptolibrary.messages;

public class SignedBulletinMessage {
    String id;
    String userSign;
    String mBulletin;
    String mServerSign;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserSign() {
        return userSign;
    }

    public void setUserSign(String userSign) {
        this.userSign = userSign;
    }

    public String getBulletin() {
        return mBulletin;
    }

    public void setBulletin(String bulletin) {
        mBulletin = bulletin;
    }

    public String getServerSign() {
        return mServerSign;
    }

    public void setServerSign(String serverSign) {
        mServerSign = serverSign;
    }
}
