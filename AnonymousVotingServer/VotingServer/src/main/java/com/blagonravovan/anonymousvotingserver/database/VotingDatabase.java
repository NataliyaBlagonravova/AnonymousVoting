package com.blagonravovan.anonymousvotingserver.database;


import java.security.Key;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VotingDatabase {
    private Map<String, Key> mUserKeys;
    private Set<String> mVoted;

    private static VotingDatabase sInstance;

    public static VotingDatabase getInstance() {
        if (sInstance == null) {
            sInstance = new VotingDatabase();
        }
        return sInstance;
    }

    private VotingDatabase() {
        mUserKeys = new HashMap<>();
        mVoted = new HashSet<>();
    }

    public void clear() {
        mVoted.clear();
        mUserKeys.clear();
    }

    public boolean isUserInDataBase(String id) {
        return mUserKeys.containsKey(id);
    }

    public boolean isUserVoted(String id) {
        return mVoted.contains(id);
    }

    public void addNewUser(String userId, Key publicKey) {
        mUserKeys.put(userId, publicKey);
    }

    public void addUserAsVoted(String userId) {
        mVoted.add(userId);
    }

    public Key getPublicKey(String userId) {
        return mUserKeys.get(userId);
    }


}
