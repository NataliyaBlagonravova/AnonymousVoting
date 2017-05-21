package com.blagonravovan.anonymousvotingserver;


import android.util.Base64;
import android.util.Log;

import java.security.Key;

import javax.crypto.Cipher;

public class EncryptionTools {
    private static final String TAG = EncryptionTools.class.getSimpleName();

    private static final String ALGORITHM_RSA = "RSA";

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

}
