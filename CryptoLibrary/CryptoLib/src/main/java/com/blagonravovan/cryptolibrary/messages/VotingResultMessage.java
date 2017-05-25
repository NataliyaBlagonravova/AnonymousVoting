package com.blagonravovan.cryptolibrary.messages;


import java.util.ArrayList;

public class VotingResultMessage {
    private ArrayList<String> mResults = new ArrayList<>();

    public ArrayList<String> getResulst() {
        return mResults;
    }

    public void setResulst(ArrayList<String> results) {
        mResults = results;
    }
}
