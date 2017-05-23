package com.blagonravovan.anonymousvotingclient;

import android.util.Log;

import com.blagonravovan.anonymousvotingclient.messages.RegisterBulletinMessage;
import com.blagonravovan.anonymousvotingclient.messages.SecretKeyMessage;
import com.blagonravovan.anonymousvotingclient.messages.SignInMessage;
import com.blagonravovan.anonymousvotingclient.messages.SignedBulletinMessage;
import com.blagonravovan.anonymousvotingclient.messages.VotingResultMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class OpenCommunicationChannel {

    interface OnClientMessageListener {
        void onReceivedSignInMessage(SignInMessage message);

        void onReceivedRegisterBulletinMessage(RegisterBulletinMessage message);

        void onReceivedSecretKeyMessage(SecretKeyMessage message);
    }

    interface OnServerMessageListener {
        void onServerPublicKeyReceived(String key);

        void onSignedBulletinReceived(SignedBulletinMessage message);

        void onVotingFinish(VotingResultMessage message);

        void onSignInResponseReceived(boolean isOk);

        void onRegisterBulletinResponseReceived(boolean isOk);

        void onBulletinCountedResponseReceived(boolean isOk);
    }

    public class ServerRequestHelper {
        public void publishServerPublicKey(String publicKey) {
            sInstance.publishServerPublicKey(publicKey);
        }

        public void publishVotingResults(VotingResultMessage votingResults) {
            sInstance.publishVotingResults(votingResults);
        }

        public void sendSignInResponse(String id, boolean isOk) {
            sInstance.sendSignInResponse(id, isOk);
        }

        public void sendRegisterBulletinResponse(String id, boolean isOk) {
            sInstance.sendRegisterBulletinResponse(id, isOk);
        }

        public void sendBulletinCountedResponse(String label, boolean isOk) {
            sInstance.sendBulletinCountedResponse(label, isOk);
        }

        public void sendSignedBulletinMessage(SignedBulletinMessage message) {
            sInstance.sendSignedBulletinMessage(message);
        }


    }

    public class ClientRequestHelper {
        public void sendSignInMessage(SignInMessage message) {
            sInstance.sendSignInMessage(message);
        }

        public void sendRegisterBulletinMessage(RegisterBulletinMessage message) {
            sInstance.sendRegisterBulletinMessage(message);
        }

        public void sendSecretKeyMessage(SecretKeyMessage message) {
            sInstance.sendSecretKeyMessage(message);
        }
    }

    private static final String TAG = OpenCommunicationChannel.class.getSimpleName();

    private static final String SIGN_IN_MESSAGE = "sign_in_message";
    private static final String REGISTER_BULLETIN_MESSAGE = "register_bulletin_message";
    private static final String VOTING_START = "voting_start";
    private static final String SECRET_KEY_MESSAGE = "secret_key_message";
    private static final String SIGN_IN_RESPONSE = "sign_in_response";
    private static final String REGISTER_BULLETIN_RESPONSE = "register_bulletin_response";
    private static final String BULLETIN_COUNTED = "bulletin_counted";
    private static final String SERVER_PUBLIC_KEY = "server_public_key";
    private static final String VOTING_RESULTS = "voting_results";
    private static final String SIGNED_BULLETIN_MESSAGE = "signed_bulletin_message";


    private static OpenCommunicationChannel sInstance;
    private DatabaseReference mDatabaseReference;

    private OnClientMessageListener mClientMessageListener;
    private OnServerMessageListener mServerMessageListener;


    public static OpenCommunicationChannel getInstance() {
        if (sInstance == null) {
            sInstance = new OpenCommunicationChannel();
        }
        return sInstance;
    }

    public OpenCommunicationChannel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference();
        subscribeToSignInMessage();
        subscribeToRegisterBulletinMessage();
        subscribeToSecretKeyMessage();
    }

    private void subscribeToSignInMessage() {
        mDatabaseReference.child(SIGN_IN_MESSAGE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SignInMessage message = dataSnapshot.getValue(SignInMessage.class);
                        if (message != null) {
                            Log.d(TAG, "New sign in message");
                            if (mClientMessageListener != null) {
                                mClientMessageListener.onReceivedSignInMessage(message);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToRegisterBulletinMessage() {
        mDatabaseReference.child(REGISTER_BULLETIN_MESSAGE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        RegisterBulletinMessage message = dataSnapshot.getValue
                                (RegisterBulletinMessage.class);
                        if (message != null) {
                            Log.d(TAG, "New register bulletin message");
                            if (mClientMessageListener != null) {
                                mClientMessageListener.onReceivedRegisterBulletinMessage(message);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToSecretKeyMessage() {
        mDatabaseReference.child(SECRET_KEY_MESSAGE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SecretKeyMessage message = dataSnapshot.getValue
                                (SecretKeyMessage.class);
                        if (message != null) {
                            Log.d(TAG, "New secret message");
                            if (mClientMessageListener != null) {
                                mClientMessageListener.onReceivedSecretKeyMessage(message);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToSignInResponse(String id) {
        mDatabaseReference.child(id).child(SIGN_IN_RESPONSE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean response = dataSnapshot.getValue(Boolean.class);
                        if (response != null) {
                            Log.d(TAG, "Sign in response: " + response);
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onSignInResponseReceived(response);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToRegisterBulletinResponse(String id) {
        mDatabaseReference.child(id).child(REGISTER_BULLETIN_RESPONSE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean response = dataSnapshot.getValue(Boolean.class);
                        if (response != null) {
                            Log.d(TAG, "Register bulletin response: " + response);
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onRegisterBulletinResponseReceived(response);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToBulletinCountedResponse(String label) {
        mDatabaseReference.child(label).child(REGISTER_BULLETIN_RESPONSE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean response = dataSnapshot.getValue(Boolean.class);
                        if (response != null) {
                            Log.d(TAG, "Bulletin counted response: " + response);
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onBulletinCountedResponseReceived(response);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void subscribeToServerPublicKey() {
        mDatabaseReference.child(SERVER_PUBLIC_KEY)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = dataSnapshot.getValue(String.class);
                        if (key != null) {
                            Log.d(TAG, "Sign in response");
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onServerPublicKeyReceived(key);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToVotingResults() {
        mDatabaseReference.child(VOTING_RESULTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        VotingResultMessage votingResults = dataSnapshot
                                .getValue(VotingResultMessage.class);
                        if (votingResults != null) {
                            Log.d(TAG, "Voting results received");
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onVotingFinish(votingResults);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToSignedBulletinMessage(String id) {
        mDatabaseReference.child(id).child(SIGNED_BULLETIN_MESSAGE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SignedBulletinMessage message = dataSnapshot
                                .getValue(SignedBulletinMessage.class);
                        if (message != null) {
                            Log.d(TAG, "Signed bulletin message");
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onSignedBulletinReceived(message);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void publishServerPublicKey(String publicKey) {
        mDatabaseReference.child(SERVER_PUBLIC_KEY).setValue(publicKey);
    }

    private void publishVotingResults(VotingResultMessage votingResults) {
        mDatabaseReference.child(VOTING_RESULTS).setValue(votingResults);
    }

    private void sendSignInMessage(SignInMessage message) {
        mDatabaseReference.child(SIGN_IN_MESSAGE).setValue(message);
    }

    private void sendRegisterBulletinMessage(RegisterBulletinMessage message) {
        mDatabaseReference.child(REGISTER_BULLETIN_MESSAGE).setValue(message);
    }

    private void sendSecretKeyMessage(SecretKeyMessage message) {
        mDatabaseReference.child(SECRET_KEY_MESSAGE).setValue(message);
    }

    private void sendSignInResponse(String id, boolean isOk) {
        mDatabaseReference.child(id).child(SIGN_IN_RESPONSE).setValue(isOk);
    }


    private void sendRegisterBulletinResponse(String id, boolean isOk) {
        mDatabaseReference.child(id).child(REGISTER_BULLETIN_RESPONSE).setValue(isOk);
    }

    private void sendBulletinCountedResponse(String label, boolean isOk) {
        mDatabaseReference.child(label).child(BULLETIN_COUNTED).setValue(isOk);
    }

    private void sendSignedBulletinMessage(SignedBulletinMessage message) {
        mDatabaseReference.child(message.getId()).child(SIGNED_BULLETIN_MESSAGE).setValue(message);
    }

    private void setClientMessageListener(OnClientMessageListener listener) {
        mClientMessageListener = listener;
    }

    private void setServerMessageListener(OnServerMessageListener listener) {
        mServerMessageListener = listener;
    }

    public ServerRequestHelper registerAsServer(OnClientMessageListener listener) {
        setClientMessageListener(listener);
        subscribeToSignInMessage();
        subscribeToSecretKeyMessage();
        subscribeToRegisterBulletinMessage();
        return new ServerRequestHelper();
    }

    public ClientRequestHelper registerAsClient(OnServerMessageListener listener, String id, String label) {
        setServerMessageListener(listener);
        subscribeToBulletinCountedResponse(label);
        subscribeToRegisterBulletinResponse(id);
        subscribeToServerPublicKey();
        subscribeToSignInResponse(id);
        subscribeToVotingResults();
        subscribeToSignedBulletinMessage(id);

        return new ClientRequestHelper();

    }


}
