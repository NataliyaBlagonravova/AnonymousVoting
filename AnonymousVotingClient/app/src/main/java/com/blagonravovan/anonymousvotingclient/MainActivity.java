package com.blagonravovan.anonymousvotingclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.PublicKey;

public class MainActivity extends AppCompatActivity {

    private KeyStorage mKeyStorage;
    private PublicKey mServerKey;

    private EditText mMessageEditText;
    private Button mSendMessageButton;

    private RatingBar mRatingCandidate1;
    private RatingBar mRatingCandidate2;
    private RatingBar mRatingCandidate3;
    private RatingBar mRatingCandidate4;
    private RatingBar mRatingCandidate5;

    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mKeyStorage = KeyStorage.getInstance(this);

        mMessageEditText = (EditText) findViewById(R.id.message_to_server);

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
                String message = mMessageEditText.getText().toString();
                String rating = getRatingString();
                String encryptMessage = EncryptionTools.encryptMessage(message+ rating, mServerKey);
                mDatabaseReference.child("message").setValue(encryptMessage);
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference();
        mDatabaseReference.child("server_public_key")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String keyString = dataSnapshot.getValue(String.class);
                mServerKey = KeyStorage.stringToPublicKey(keyString);
                mSendMessageButton.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getRatingString(){
     return "" +  Math.round(mRatingCandidate1.getRating()) +
                  Math.round(mRatingCandidate2.getRating()) +
                  Math.round(mRatingCandidate3.getRating()) +
                  Math.round(mRatingCandidate4.getRating()) +
                  Math.round(mRatingCandidate5.getRating());
}}
