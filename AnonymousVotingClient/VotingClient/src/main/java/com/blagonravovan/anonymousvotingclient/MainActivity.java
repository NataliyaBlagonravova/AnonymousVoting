package com.blagonravovan.anonymousvotingclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.blagonravovan.cryptolibrary.CryptographicTools;

import com.blagonravovan.cryptolibrary.KeyStorage;
import com.blagonravovan.cryptolibrary.OpenCommunicationChannel;
import com.blagonravovan.cryptolibrary.messages.RegisterBulletinMessage;
import com.blagonravovan.cryptolibrary.messages.SecretKeyMessage;
import com.blagonravovan.cryptolibrary.messages.SignInMessage;
import com.blagonravovan.cryptolibrary.messages.SignedBulletinMessage;
import com.blagonravovan.cryptolibrary.messages.VotingResultMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private KeyStorage mKeyStorage;
    private PublicKey mServerPublicKey;

    OpenCommunicationChannel mOpenChannel;
    OpenCommunicationChannel.ClientRequestHelper mRequestHelper;

    private Button mSendBulletinButton;
    private Button mCheckBulletinButton;

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
                            mSendBulletinButton.setEnabled(true);
                            enabledRating();
                            Toast.makeText(MainActivity.this,
                                    R.string.you_can_vote, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this,
                                    R.string.you_can_not_vote, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onRegisterBulletinResponseReceived(boolean isOk) {
                        Log.d(TAG, "onSignInResponseReceived: " + isOk);
                        if (isOk){
                            Toast.makeText(MainActivity.this,
                                    R.string.bulletin_was_register, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this,
                                    R.string.bulletin_was_not_register, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onBulletinCountedResponseReceived(boolean isOk) {
                        Log.d(TAG, "onBulletinCountedResponseReceived: " + isOk);
                        if (isOk){
                            Toast.makeText(MainActivity.this,
                                    R.string.bulletin_counted, Toast.LENGTH_SHORT).show();
                            mSendBulletinButton.setEnabled(false);
                            mCheckBulletinButton.setEnabled(true);
                            disabledRating();
                        }else {
                            Toast.makeText(MainActivity.this,
                                    R.string.bulletin_not_counted, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCheckVoteResponseReceived(String bulletin) {
                        Log.d(TAG, "onCheckVoteResponseReceived");
                        Toast.makeText(MainActivity.this, getResources()
                                .getString(R.string.your_bulletin_is, bulletin), Toast.LENGTH_SHORT)
                                .show();
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

    private void disabledRating(){
        mRatingCandidate1.setEnabled(false);
        mRatingCandidate2.setEnabled(false);
        mRatingCandidate3.setEnabled(false);
        mRatingCandidate4.setEnabled(false);
        mRatingCandidate5.setEnabled(false);
    }

    private void enabledRating(){
        mRatingCandidate1.setEnabled(true);
        mRatingCandidate2.setEnabled(true);
        mRatingCandidate3.setEnabled(true);
        mRatingCandidate4.setEnabled(true);
        mRatingCandidate5.setEnabled(true);
    }



    private void initUI() {

        mRatingCandidate1 = (RatingBar) findViewById(R.id.rating1);
        mRatingCandidate2 = (RatingBar) findViewById(R.id.rating2);
        mRatingCandidate3 = (RatingBar) findViewById(R.id.rating3);
        mRatingCandidate4 = (RatingBar) findViewById(R.id.rating4);
        mRatingCandidate5 = (RatingBar) findViewById(R.id.rating5);

        disabledRating();

        mSendBulletinButton = (Button) findViewById(R.id.send_button);
        mSendBulletinButton.setEnabled(false);
        mSendBulletinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rating = getRatingString();
                mRequestHelper.sendRegisterBulletinMessage(createBulletinMessage(rating));
            }
        });

        mCheckBulletinButton = (Button) findViewById(R.id.check_button);
        mCheckBulletinButton.setEnabled(false);
        mCheckBulletinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestHelper.sendCheckVoteRequest(mLabel);
            }
        });
    }

    private RegisterBulletinMessage createBulletinMessage(String bulletin) {
        String encodedBulletin = CryptographicTools.encryptMessageSecret
                (bulletin, mKeyStorage.getSecretKey());
        String sign = CryptographicTools.signMessage(encodedBulletin, mKeyStorage.getPrivateKey());
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
