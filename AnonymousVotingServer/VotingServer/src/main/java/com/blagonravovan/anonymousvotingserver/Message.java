package com.blagonravovan.anonymousvotingserver;


import java.io.Serializable;

public class Message implements Serializable {
    private String mBulletin;
    private String mSign;

    public String getBulletin() {
        return mBulletin;
    }

    public void setBulletin(String bulletin) {
        mBulletin = bulletin;
    }

    public String getSign() {
        return mSign;
    }

    public void setSign(String sign) {
        mSign = sign;
    }
}
