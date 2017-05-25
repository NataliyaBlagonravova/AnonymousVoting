package com.blagonravovan.anonymousvotingserver.database;

import java.util.HashMap;
import java.util.Map;


public class BulletinDatabase {
    private static BulletinDatabase sInstance;

    private Map<String, String> mBulletins;

    public static BulletinDatabase getInstance() {
        if (sInstance == null) {
            sInstance = new BulletinDatabase();
        }
        return sInstance;
    }

    public void clear() {
        mBulletins.clear();
    }

    private BulletinDatabase() {
        mBulletins = new HashMap<>();
    }

    public void addNewBulletin(String label, String bulletin) {
        mBulletins.put(label, bulletin);
    }

    public boolean isContainsUserBulletin(String label) {
        return mBulletins.containsKey(label);
    }

    public String getUserBulletin(String label){
        return mBulletins.get(label);
    }

}
