package com.blagonravovan.anonymousvotingclient.messages;


public class SecretKeyMessage {
    private String mLabel;
    private String mSecretKey;
    private String mServerSign;
    private String mBulletin;

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public String getSecretKey() {
        return mSecretKey;
    }

    public void setSecretKey(String secretKey) {
        mSecretKey = secretKey;
    }

    public String getServerSign() {
        return mServerSign;
    }

    public void setServerSign(String serverSign) {
        mServerSign = serverSign;
    }

    public String getBulletin() {
        return mBulletin;
    }

    public void setBulletin(String bulletin) {
        mBulletin = bulletin;
    }

}
