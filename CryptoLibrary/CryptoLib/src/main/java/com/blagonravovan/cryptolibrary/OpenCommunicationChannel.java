package com.blagonravovan.cryptolibrary;

import android.util.Log;

import com.blagonravovan.cryptolibrary.messages.RegisterBulletinMessage;
import com.blagonravovan.cryptolibrary.messages.SecretKeyMessage;
import com.blagonravovan.cryptolibrary.messages.SignInMessage;
import com.blagonravovan.cryptolibrary.messages.SignedBulletinMessage;
import com.blagonravovan.cryptolibrary.messages.VotingResultMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class OpenCommunicationChannel {
    private static final String TAG = OpenCommunicationChannel.class.getSimpleName();

    private static final String SIGN_IN_MESSAGE = "sign_in_message";
    private static final String REGISTER_BULLETIN_MESSAGE = "register_bulletin_message";
    private static final String VOTE_IS_RUNNING = "vote_is_running";
    private static final String VOTE_RESULTS = "vote_results";
    private static final String SECRET_KEY_MESSAGE = "secret_key_message";
    private static final String SIGN_IN_RESPONSE = "sign_in_response";
    private static final String REGISTER_BULLETIN_RESPONSE = "register_bulletin_response";
    private static final String BULLETIN_COUNTED = "bulletin_counted";
    private static final String SERVER_PUBLIC_KEY = "server_public_key";
    private static final String SIGNED_BULLETIN_MESSAGE = "signed_bulletin_message";
    private static final String CHECK_VOTE_REQUEST = "check_vote_request";
    private static final String CHECK_VOTE_RESPONSE = "check_vote_response";


    public interface OnClientMessageListener {
        void onReceivedSignInMessage(SignInMessage message);

        void onReceivedRegisterBulletinMessage(RegisterBulletinMessage message);

        void onReceivedSecretKeyMessage(SecretKeyMessage message);

        void onReceivedCheckVoteRequest(String label);
    }

    public interface OnServerMessageListener {
        void onServerPublicKeyReceived(String key);

        void onVoteResultsReceived(VotingResultMessage message);

        void onSignedBulletinReceived(SignedBulletinMessage message);

        void onVoteStatusChanged(boolean isRunning);

        void onSignInResponseReceived(boolean isOk);

        void onRegisterBulletinResponseReceived(boolean isOk);

        void onBulletinCountedResponseReceived(boolean isOk);

        void onCheckVoteResponseReceived(String bulletin);
    }

    public class ServerRequestHelper {
        public void publishServerPublicKey(String publicKey) {
            sInstance.publishServerPublicKey(publicKey);
        }

        public void publishVoteResults(VotingResultMessage message){
            sInstance.publishVoteResults(message);
        }

        public void sendVoteStatusChanged(Boolean isRunning) {
            sInstance.sendVoteStatusChanged(isRunning);
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

        public void sendCheckVoteResponse(String label, String bulletin) {
            sInstance.sendCheckVoteResponse(label, bulletin);
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

        public void sendCheckVoteRequest(String label) {
            sInstance.sendCheckVoteRequest(label);
        }
    }


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
    }

    private void subscribeToSignInMessage() {
        mDatabaseReference.child(SIGN_IN_MESSAGE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SignInMessage message = dataSnapshot.getValue(SignInMessage.class);
                        if (message != null) {
                            Log.d(TAG, "New signMessage in message");
                            if (mClientMessageListener != null) {
                                mClientMessageListener.onReceivedSignInMessage(message);
                                mDatabaseReference.child(SIGN_IN_MESSAGE).setValue(null);
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
                                mDatabaseReference.child(REGISTER_BULLETIN_MESSAGE).setValue(null);
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
                                mDatabaseReference.child(SECRET_KEY_MESSAGE).setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToSignInResponse(final String id) {
        mDatabaseReference.child(id).child(SIGN_IN_RESPONSE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean response = dataSnapshot.getValue(Boolean.class);
                        if (response != null) {
                            Log.d(TAG, "Sign in response: " + response);
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onSignInResponseReceived(response);
                                mDatabaseReference.child(id).child(SIGN_IN_RESPONSE).setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToRegisterBulletinResponse(final String id) {
        mDatabaseReference.child(id).child(REGISTER_BULLETIN_RESPONSE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean response = dataSnapshot.getValue(Boolean.class);
                        if (response != null) {
                            Log.d(TAG, "Register bulletin response: " + response);
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onRegisterBulletinResponseReceived(response);
                                mDatabaseReference.child(id).child(REGISTER_BULLETIN_RESPONSE).setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToBulletinCountedResponse(final String label) {
        mDatabaseReference.child(label).child(BULLETIN_COUNTED)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean response = dataSnapshot.getValue(Boolean.class);
                        if (response != null) {
                            Log.d(TAG, "Bulletin counted response: " + response);
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onBulletinCountedResponseReceived(response);
                                mDatabaseReference.child(label).child(BULLETIN_COUNTED).setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void subscribeToCheckVoteResponse(final String label) {
        mDatabaseReference.child(label).child(CHECK_VOTE_RESPONSE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String bulletin = dataSnapshot.getValue(String.class);
                        if (bulletin != null) {
                            Log.d(TAG, "Check vote response: " + bulletin);
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onCheckVoteResponseReceived(bulletin);
                                mDatabaseReference.child(label).child(CHECK_VOTE_RESPONSE).setValue(null);
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

    private void subscribeToVoteResults() {
        mDatabaseReference.child(VOTE_RESULTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        VotingResultMessage message = dataSnapshot.getValue(VotingResultMessage.class);
                        if (message != null) {
                            Log.d(TAG, "Vote results published");
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onVoteResultsReceived(message);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToVoteStatusChange() {
        mDatabaseReference.child(VOTE_IS_RUNNING)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean isRunning = dataSnapshot.getValue(Boolean.class);
                        if (isRunning != null) {
                            Log.d(TAG, "Vote status changed");
                            if (mServerMessageListener != null) {
                                mServerMessageListener.onVoteStatusChanged(isRunning);
                                mDatabaseReference.child(VOTE_IS_RUNNING).setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToChangeVote() {
        mDatabaseReference.child(CHECK_VOTE_REQUEST)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String label = dataSnapshot
                                .getValue(String.class);
                        if (label != null) {
                            Log.d(TAG, "Check vote");
                            if (mClientMessageListener != null) {
                                mClientMessageListener.onReceivedCheckVoteRequest(label);
                                mDatabaseReference.child(CHECK_VOTE_REQUEST).setValue(null);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void subscribeToSignedBulletinMessage(final String id) {
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
                                mDatabaseReference.child(id).child(SIGNED_BULLETIN_MESSAGE).setValue(null);
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

    private void publishVoteResults(VotingResultMessage message){
        mDatabaseReference.child(VOTE_RESULTS).setValue(message);
    }

    private void sendVoteStatusChanged(Boolean isRunning) {
        mDatabaseReference.child(VOTE_IS_RUNNING).setValue(isRunning);
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

    private void sendCheckVoteRequest(String label) {
        mDatabaseReference.child(CHECK_VOTE_REQUEST).setValue(label);
    }

    private void sendCheckVoteResponse(String label, String bulletin) {
        mDatabaseReference.child(label).child(CHECK_VOTE_RESPONSE).setValue(bulletin);
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
        subscribeToChangeVote();
        return new ServerRequestHelper();
    }

    public ClientRequestHelper registerAsClient(OnServerMessageListener listener, String id, String label) {
        setServerMessageListener(listener);
        subscribeToBulletinCountedResponse(label);
        subscribeToRegisterBulletinResponse(id);
        subscribeToServerPublicKey();
        subscribeToSignInResponse(id);
        subscribeToVoteStatusChange();
        subscribeToSignedBulletinMessage(id);
        subscribeToCheckVoteResponse(label);
        subscribeToVoteResults();
        return new ClientRequestHelper();
    }
}
