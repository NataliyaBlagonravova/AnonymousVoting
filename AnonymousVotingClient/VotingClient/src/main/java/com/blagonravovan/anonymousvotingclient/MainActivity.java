package com.blagonravovan.anonymousvotingclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import com.blagonravovan.anonymousvotingclient.messages.RegisterBulletinMessage;
import com.blagonravovan.anonymousvotingclient.messages.SecretKeyMessage;
import com.blagonravovan.anonymousvotingclient.messages.SignInMessage;
import com.blagonravovan.anonymousvotingclient.messages.SignedBulletinMessage;
import com.blagonravovan.anonymousvotingclient.messages.VotingResultMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private KeyStorage mKeyStorage;
    private PublicKey mServerPublicKey;

    OpenCommunicationChannel mOpenChannel;
    OpenCommunicationChannel.ClientRequestHelper mRequestHelper;

    private Button mSendMessageButton;

    private RatingBar mRatingCandidate1;
    private RatingBar mRatingCandidate2;
    private RatingBar mRatingCandidate3;
    private RatingBar mRatingCandidate4;
    private RatingBar mRatingCandidate5;
    
    private String mId;
    private String mLabel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        mId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mLabel = generateLabel();

        mKeyStorage = KeyStorage.getInstance(this);
        mKeyStorage.generateSessionKey();

        mOpenChannel = OpenCommunicationChannel.getInstance();
        mRequestHelper = mOpenChannel
                .registerAsClient(new OpenCommunicationChannel.OnServerMessageListener() {
                    @Override
                    public void onServerPublicKeyReceived(String key) {
                        Log.d(TAG, "onServerPublicKeyReceived");
                        mServerPublicKey = KeyStorage.stringToPublicKey(key);
                    }

                    @Override
                    public void onSignedBulletinReceived(SignedBulletinMessage message) {
                        Log.d(TAG, "onSignedBulletinReceive");
                        boolean isIdOk = message.getId().equals(mId);
                        boolean isSignOK = CryptographicTools.checkHash(message.getBulletin(),
                                message.getUserSign(), mKeyStorage.getPublicKey());
                        boolean isServerSignOk = CryptographicTools.checkHash(message.getBulletin(),
                                message.getServerSign(), mServerPublicKey);

                        if (isIdOk && isSignOK && isServerSignOk) {
                            String secretKey = KeyStorage.keyToString(mKeyStorage.getSecretKey());
                            String encryptedSecretKey = CryptographicTools
                                    .encryptMessage(secretKey, mServerPublicKey);
                            SecretKeyMessage secretKeyMessage = new SecretKeyMessage();
                            secretKeyMessage.setLabel(mLabel);
                            secretKeyMessage.setServerSign(message.getServerSign());
                            secretKeyMessage.setBulletin(message.getBulletin());
                            secretKeyMessage.setSecretKey(encryptedSecretKey);
                            mRequestHelper.sendSecretKeyMessage(secretKeyMessage);
                        }

                    }

                    @Override
                    public void onVotingFinish(VotingResultMessage message) {
                        //TODO

                    }

                    @Override
                    public void onSignInResponseReceived(boolean isOk) {
                        Log.d(TAG, "onSignInResponseReceived: " + isOk);
                        if (isOk) {
                            mSendMessageButton.setEnabled(true);

                        }
                    }

                    @Override
                    public void onRegisterBulletinResponseReceived(boolean isOk) {
                        Log.d(TAG, "onSignInResponseReceived: " + isOk);
                    }

                    @Override
                    public void onBulletinCountedResponseReceived(boolean isOk) {
                        Log.d(TAG, "onBulletinCountedResponseReceived: " + isOk);

                    }
                }, mId, mLabel);

        SignInMessage signInMessage = new SignInMessage();
        signInMessage.setId(mId);
        signInMessage.setPublicKey(KeyStorage.keyToString(mKeyStorage.getPublicKey()));
        mRequestHelper.sendSignInMessage(signInMessage);
    }


    private String getRatingString() {
        return "" + Math.round(mRatingCandidate1.getRating()) +
                Math.round(mRatingCandidate2.getRating()) +
                Math.round(mRatingCandidate3.getRating()) +
                Math.round(mRatingCandidate4.getRating()) +
                Math.round(mRatingCandidate5.getRating());
    }

    private void initUI() {

        mRatingCandidate1 = (RatingBar) findViewById(R.id.rating1);
        mRatingCandidate2 = (RatingBar) findViewById(R.id.rating2);
        mRatingCandidate3 = (RatingBar) findViewById(R.id.rating3);
        mRatingCandidate4 = (RatingBar) findViewById(R.id.rating4);
        mRatingCandidate5 = (RatingBar) findViewById(R.id.rating5);

        mSendMessageButton = (Button) findViewById(R.id.send_button);
        mSendMessageButton.setEnabled(false);
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rating = getRatingString();
                mRequestHelper.sendRegisterBulletinMessage(createBulletinMessage(rating));
            }
        });
    }

    private RegisterBulletinMessage createBulletinMessage(String bulletin) {
        String encodedBulletin = CryptographicTools.encryptMessageSecret
                (bulletin, mKeyStorage.getSecretKey());
        String sign = CryptographicTools.sign(encodedBulletin, mKeyStorage.getPrivateKey());
        RegisterBulletinMessage message = new RegisterBulletinMessage();
        message.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        message.setBulletin(encodedBulletin);
        message.setSign(sign);
        return message;
    }

    private String generateLabel() {
        Random rand = new Random();
        BigInteger label = new BigInteger(256, rand);
        return label.toString();
    }
}
