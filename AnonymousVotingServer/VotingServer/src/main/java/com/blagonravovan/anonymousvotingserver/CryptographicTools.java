package com.blagonravovan.anonymousvotingserver;


import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;


public class CryptographicTools {
    private static final String TAG = CryptographicTools.class.getSimpleName();

    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_AES = "AES";

    public static String encryptMessage(String message, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(message.getBytes());
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption error");
            return null;
        }
    }

    public static byte[] encryptMessage(byte[] message, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Encryption error");
            return null;
        }
    }

    public static String encryptMessageSecret(String message, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(message.getBytes());
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption error");
            return null;
        }
    }

    public static String decryptMessage(String encryptedMessage, Key key) {
        try {
            byte[] encryptedData = Base64.decode(encryptedMessage, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encryptedData));
        } catch (Exception e) {
            Log.e(TAG, "Decryption error");
            return null;
        }
    }

    public static String decryptMessageSecret(String encryptedMessage, Key key) {
        try {
            byte[] encryptedData = Base64.decode(encryptedMessage, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encryptedData));
        } catch (Exception e) {
            Log.e(TAG, "Decryption error");
            return null;
        }
    }

    public static byte[] decryptMessage(byte[] encryptedMessage, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedMessage);
        } catch (Exception e) {
            Log.e(TAG, "Decryption error");
            return null;
        }
    }

    public static byte[] hash(byte[] message) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(message);
        } catch (Exception e) {
            Log.e(TAG, "Calculating hash error");
            return null;
        }
    }

    public static boolean checkHash(String message, String sign, Key publicKey) {
        byte[] messageBytes = Base64.decode(message, Base64.DEFAULT);
        byte[] signByted = Base64.decode(sign, Base64.DEFAULT);
        byte[] checkHash = hash(messageBytes);
        byte[] decHashed = decryptMessage(signByted, publicKey);

        if (checkHash.length != 16 || decHashed.length != 16) {
            return false;
        }
        for (int i = 0; i < 16; i++) {
            if (checkHash[i] != decHashed[i]) {
                return false;
            }
        }
        return true;
    }

    public static String sign(String message, Key privateKey) {
        byte[] d1 = Base64.decode(message, Base64.DEFAULT);
        return Base64.encodeToString(encryptMessage(hash(d1), privateKey), Base64.DEFAULT);
    }
}

