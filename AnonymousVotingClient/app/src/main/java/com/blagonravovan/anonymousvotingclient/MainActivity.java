package com.blagonravovan.anonymousvotingclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mKeyStorage = KeyStorage.getInstance(this);

        mMessageEditText = (EditText) findViewById(R.id.message_to_server);
        mSendMessageButton = (Button) findViewById(R.id.send_button);
        mSendMessageButton.setEnabled(false);
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mMessageEditText.getText().toString();
                String encryptMessage = EncryptionTools.encryptMessage(message, mServerKey);
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
}
