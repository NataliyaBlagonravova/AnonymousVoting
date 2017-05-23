package com.blagonravovan.anonymousvotingserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.blagonravovan.anonymousvotingserver.database.BulletinDatabase;
import com.blagonravovan.anonymousvotingserver.database.VotingDatabase;
import com.blagonravovan.anonymousvotingserver.messages.RegisterBulletinMessage;
import com.blagonravovan.anonymousvotingserver.messages.SecretKeyMessage;
import com.blagonravovan.anonymousvotingserver.messages.SignInMessage;
import com.blagonravovan.anonymousvotingserver.messages.SignedBulletinMessage;

import java.security.Key;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private KeyStorage mKeyStorage;
    VotingDatabase mVotingDatabase;
    BulletinDatabase mBulletinDatabase;

    OpenCommunicationChannel mOpenChannel;
    OpenCommunicationChannel.ServerRequestHelper mRequestHelper;

    private TextView mCounterTextView;
    private TextView[] mRatingTextViews;

    private int[] mRating;
    int mBulletinCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        mKeyStorage = KeyStorage.getInstance(this);

        mVotingDatabase = VotingDatabase.getInstance();
        mBulletinDatabase = BulletinDatabase.getInstance();

        mVotingDatabase.clear();
        mBulletinDatabase.clear();


        mOpenChannel = OpenCommunicationChannel.getInstance();
        mRequestHelper = mOpenChannel.registerAsServer(new OpenCommunicationChannel.OnClientMessageListener() {
            @Override
            public void onReceivedSignInMessage(SignInMessage message) {
                Log.d(TAG, "onReceivedSignInMessage");
                boolean isOk;
                if (!mVotingDatabase.isUserInDataBase(message.getId())) {
                    mVotingDatabase.addNewUser(
                            message.getId(),
                            KeyStorage.stringToPublicKey(message.getPublicKey()));
                    isOk = true;
                } else {
                    isOk = false;
                }
                mRequestHelper.sendSignInResponse(message.getId(), isOk);
            }

            @Override
            public void onReceivedRegisterBulletinMessage(RegisterBulletinMessage message) {
                Log.d(TAG, "onReceivedRegisterBulletinMessage");
                Key publicKey = mVotingDatabase.getPublicKey(message.getId());
                boolean isOk = CryptographicTools.checkHash(message.getBulletin(),
                        message.getSign(), publicKey);
                if (isOk) {
                    mVotingDatabase.addUserAsVoted(message.getId());

                    String sign = CryptographicTools.sign(message.getBulletin(),
                            mKeyStorage.getPrivateKey());
                    SignedBulletinMessage bulletinMessage = new SignedBulletinMessage();
                    bulletinMessage.setId(message.getId());
                    bulletinMessage.setBulletin(message.getBulletin());
                    bulletinMessage.setServerSign(sign);
                    bulletinMessage.setUserSign(message.getSign());
                    mRequestHelper.sendSignedBulletinMessage(bulletinMessage);
                }

                Log.d(TAG, "Request: " + isOk);
                mRequestHelper.sendRegisterBulletinResponse(message.getId(), isOk);

            }

            @Override
            public void onReceivedSecretKeyMessage(SecretKeyMessage message) {
                Log.d(TAG, "onReceivedSecretKeyMessage");
                boolean isOK = CryptographicTools.checkHash(message.getBulletin(),
                        message.getServerSign(), mKeyStorage.getPublicKey());
                String label = "";
                if (isOK) {
                    label = message.getLabel();
                    String secretKey = CryptographicTools.decryptMessage(
                            message.getSecretKey(), mKeyStorage.getPrivateKey());
                    String bulletin = CryptographicTools.decryptMessageSecret(
                            message.getBulletin(), KeyStorage.stringToSecretKey(secretKey));
                    mBulletinDatabase.addNewBulletin(label, bulletin);
                    updateRating(bulletin);
                    showRating();
                }

                mRequestHelper.sendBulletinCountedResponse(label, isOK);
            }
        });


        mRequestHelper.publishServerPublicKey(KeyStorage.keyToString(mKeyStorage.getPublicKey()));

    }

    private void initUI() {
        mCounterTextView = (TextView) findViewById(R.id.counter);
        mCounterTextView.setText(String.valueOf(mBulletinCounter));

        mRatingTextViews = new TextView[]{
                (TextView) findViewById(R.id.rating1),
                (TextView) findViewById(R.id.rating2),
                (TextView) findViewById(R.id.rating3),
                (TextView) findViewById(R.id.rating4),
                (TextView) findViewById(R.id.rating5)
        };

        mRating = new int[]{0, 0, 0, 0, 0};

        showRating();


    }

    private void showRating() {
        for (int i = 0; i < mRatingTextViews.length; ++i) {
            mRatingTextViews[i].setText(String.valueOf(mRating[i]));
        }
    }

    private void updateRating(String bulletin) {
        Log.d(TAG, bulletin);
        int magicNumber = Integer.valueOf(bulletin);

        int divider = 10000;
        for (int i = 0; i < mRating.length; ++i) {
            mRating[i] += magicNumber / divider;
            magicNumber %= divider;
            divider /= 10;
        }
    }

    private void updateCounter() {
        mBulletinCounter += 1;
        mCounterTextView.setText(String.valueOf(mBulletinCounter));
    }
}
