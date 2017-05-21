package com.blagonravovan.anonymousvotingserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private KeyStorage mKeyStorage;

    private TextView mMessageTextView;

    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mKeyStorage = KeyStorage.getInstance(this);

        mMessageTextView = (TextView) findViewById(R.id.message_from_client);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference();
        mDatabaseReference.child("server_public_key").setValue(
                KeyStorage.keyToString(mKeyStorage.getPublicKey()));

        mDatabaseReference.child("message")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String decryptMessage = dataSnapshot.getValue(String.class);
                String message = EncryptionTools.decryptMessage(
                        decryptMessage, mKeyStorage.getPrivateKey());
                mMessageTextView.setText(message);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
