package com.blagonravovan.cryptolibrary;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class KeyStorage {
    private static final String TAG = KeyStorage.class.getSimpleName();

    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_AES = "AES";

    private static final int KEY_SIZE = 2048;
    private static final int SECRET_KEY_SIZE = 256;


    private static final String PREF_PRIVATE_KEY = "pref_private_key";
    private static final String PREF_PUBLIC_KEY = "pref_public_key";
    private static final String PREF_SRCRET_KEY = "pref_secret_key";

    private static KeyStorage sInstance;
    private Context mContext;

    public static KeyStorage getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KeyStorage(context);
        }
        return sInstance;
    }

    private KeyStorage(Context context) {
        mContext = context;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            savePublicKey(keyPair.getPublic());
            savePrivateKey(keyPair.getPrivate());

            generateSessionKey();
        } catch (Exception e) {
            Log.e(TAG, "Generating key pair error");
        }
    }

    public void generateSessionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES);
            keyGenerator.init(SECRET_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            saveSecretKey(secretKey);
        } catch (Exception e) {
            Log.e(TAG, "Generating sectet key error");
        }
    }

    public static String keyToString(Key key) {
        return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
    }

    public static PrivateKey stringToPrivateKey(String keyString) {
        try {
            byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            Log.e(TAG, "Converting string to private key error");
            return null;
        }
    }

    public static PublicKey stringToPublicKey(String keyString) {
        try {
            byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            Log.e(TAG, "Converting string to public key error");
            return null;
        }
    }

    public static SecretKey stringToSecretKey(String keyString) {
        byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, ALGORITHM_AES);
    }

    private void savePublicKey(Key publicKey) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(PREF_PUBLIC_KEY, keyToString(publicKey))
                .apply();
    }

    private void savePrivateKey(Key privateKey) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(PREF_PRIVATE_KEY, keyToString(privateKey))
                .apply();
    }

    private void saveSecretKey(Key secretKey) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(PREF_SRCRET_KEY, keyToString(secretKey))
                .apply();
    }

    public PrivateKey getPrivateKey() {
        return stringToPrivateKey(PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(PREF_PRIVATE_KEY, null));
    }

    public PublicKey getPublicKey() {
        return stringToPublicKey(PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(PREF_PUBLIC_KEY, null));
    }

    public SecretKey getSecretKey() {
        return stringToSecretKey(PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(PREF_SRCRET_KEY, null));
    }
}
