package com.blagonravovan.anonymousvotingserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private KeyStorage mKeyStorage;

    private TextView mMessageTextView;

    private TextView mRating1TextView;
    private TextView mRating2TextView;
    private TextView mRating3TextView;
    private TextView mRating4TextView;
    private TextView mRating5TextView;

    private TextView[] mRatingTextViews;

    private int[] mRating;

    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mKeyStorage = KeyStorage.getInstance(this);

        mRating1TextView = (TextView) findViewById(R.id.rating1);
        mRating2TextView = (TextView) findViewById(R.id.rating2);
        mRating3TextView = (TextView) findViewById(R.id.rating3);
        mRating4TextView = (TextView) findViewById(R.id.rating4);
        mRating5TextView = (TextView) findViewById(R.id.rating5);

        mRatingTextViews = new TextView[]{
                mRating1TextView,
                mRating2TextView,
                mRating3TextView,
                mRating4TextView,
                mRating5TextView
        };


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference();
        mDatabaseReference.child("server_public_key").setValue(
                KeyStorage.keyToString(mKeyStorage.getPublicKey()));

        mDatabaseReference.child("message")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String decryptBulletin = dataSnapshot.getValue(String.class);
                        String bulletin  = EncryptionTools.decryptMessage(
                                decryptBulletin, mKeyStorage.getPrivateKey());
                        Log.d(TAG, "New bulletin: " + bulletin);
                        if (bulletin.length() == 5) {
                            updateRating(bulletin);
                            showRating();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        mRating = new int[]{0, 0, 0, 0, 0};

        showRating();
    }

    private void showRating(){
        for (int i = 0; i < mRatingTextViews.length; ++i){
            mRatingTextViews[i].setText(String.valueOf(mRating[i]));
        }
    }

    private void updateRating(String bulletin){
        Log.d(TAG, bulletin);
        int magicNumber = Integer.valueOf(bulletin);

        int divider = 10000;
        for (int i = 0; i < mRating.length; ++i) {
            mRating[i] += magicNumber / divider;
            magicNumber %= divider;
            divider /= 10;
        }
    }
}
