package com.blagonravovan.anonymousvotingserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blagonravovan.anonymousvotingserver.database.BulletinDatabase;
import com.blagonravovan.anonymousvotingserver.database.VotingDatabase;


import com.blagonravovan.cryptolibrary.CryptographicTools;
import com.blagonravovan.cryptolibrary.KeyStorage;
import com.blagonravovan.cryptolibrary.OpenCommunicationChannel;
import com.blagonravovan.cryptolibrary.messages.RegisterBulletinMessage;
import com.blagonravovan.cryptolibrary.messages.SecretKeyMessage;
import com.blagonravovan.cryptolibrary.messages.SignInMessage;
import com.blagonravovan.cryptolibrary.messages.SignedBulletinMessage;
import com.blagonravovan.cryptolibrary.messages.VotingResultMessage;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private KeyStorage mKeyStorage;
    VotingDatabase mVotingDatabase;
    BulletinDatabase mBulletinDatabase;

    OpenCommunicationChannel mOpenChannel;
    OpenCommunicationChannel.ServerRequestHelper mRequestHelper;

    private TextView[] mRatingTextViews;
    private Button mStartFinishButton;
    private Button mPublishResultsButton;

    private int[] mRating;

    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        mKeyStorage = KeyStorage.getInstance(this);

        mVotingDatabase = VotingDatabase.getInstance();
        mBulletinDatabase = BulletinDatabase.getInstance();

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
                boolean isHashOk = CryptographicTools.checkHash(message.getBulletin(),
                        message.getSign(), publicKey);
                boolean isFistUserBulletin  = !mVotingDatabase.isUserVoted(message.getId());
                boolean isOk = isHashOk && isFistUserBulletin;
                if (isOk) {
                    mVotingDatabase.addUserAsVoted(message.getId());

                    String sign = CryptographicTools.signMessage(message.getBulletin(),
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

            @Override
            public void onReceivedCheckVoteRequest(String label) {
                if (label != null) {
                    Log.d(TAG, "onReceivedCheckVoteRequest");
                    String bulletin = mBulletinDatabase.getUserBulletin(label);
                    mRequestHelper.sendCheckVoteResponse(label, bulletin);
                }

            }
        });

        mRequestHelper.publishServerPublicKey(KeyStorage.keyToString(mKeyStorage.getPublicKey()));
        mRequestHelper.publishVoteResults(null);

    }

    private void initUI() {
        mRatingTextViews = new TextView[]{
                (TextView) findViewById(R.id.rating1),
                (TextView) findViewById(R.id.rating2),
                (TextView) findViewById(R.id.rating3),
                (TextView) findViewById(R.id.rating4),
                (TextView) findViewById(R.id.rating5)
        };

        mStartFinishButton = (Button) findViewById(R.id.start_finish_voting);
        mStartFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = !isRunning;
                mRequestHelper.sendVoteStatusChanged(isRunning);

                if (isRunning){
                    mStartFinishButton.setText(getResources().getString(R.string.finish_voting));
                    mVotingDatabase.clear();
                    mBulletinDatabase.clear();
                    clearResults();
                    showRating();
                    mPublishResultsButton.setEnabled(false);

                }else {
                    mStartFinishButton.setText(getResources().getString(R.string.start_voting));
                    mPublishResultsButton.setEnabled(true);

                }
            }
        });

        mPublishResultsButton = (Button) findViewById(R.id.publish_results);
        mPublishResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VotingResultMessage message = new VotingResultMessage();
                ArrayList<String> results = new ArrayList<String>();
                for (int i = 0; i < mRating.length; ++i) {
                   results.add(mRatingTextViews[i].getText().toString());
                }
                message.setResulst(results);
                mRequestHelper.publishVoteResults(message);
            }
        });
        mPublishResultsButton.setEnabled(false);

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

    private void clearResults(){
        for (int i = 0; i < mRating.length; ++i) {
            mRating[i] = 0;
        }
    }
}
