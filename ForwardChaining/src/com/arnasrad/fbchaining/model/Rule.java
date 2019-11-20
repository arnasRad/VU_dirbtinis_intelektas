package com.arnasrad.fbchaining.model;

import com.arnasrad.fbchaining.utility.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class Rule {

    private String result;
    private ArrayList<String> facts;
    private byte flag;

    public Rule() {

        result = null;
        facts = new ArrayList<>();
        flag = 0;
    }

    public Rule(String result, String[] facts) {

        this.result = result;
        this.facts = new ArrayList<>(Arrays.asList(facts));
        this.flag = 0;
    }

    public ArrayList<String> getFacts() {

        return this.facts;
    }

    public String getResult() {

        return this.result;
    }

    public byte getFlag() {

        return this.flag;
    }

    public void setFlag(byte flag) {

        this.flag = flag;
    }

    @Override
    public String toString() {
        return Utils.getListString(facts) + " -> " + result;
    }
}
