package com.cvt.iri.model;

import com.cvt.iri.storage.Indexable;

/**
 * Created by paul on 5/15/17.
 */
public class SeedAddressPair implements Indexable {

    private String seed;
    private String address;

    public SeedAddressPair(String seed, String address) {
        this.seed = seed;
        this.address = address;
    }

    public static final String SEP = ";";

    @Override
    public byte[] bytes() {
        return (seed + SEP + address).getBytes();
    }


    @Override
    public void read(byte[] bytes) {
        String str = new String(bytes);
        String[] pair = str.split(";");
        this.seed = pair[0];
        this.address = pair[1];
    }

    @Override
    public Indexable incremented() {
        return null;
    }

    @Override
    public Indexable decremented() {
        return null;
    }

    public String getSeed() {
        return seed;
    }



}
