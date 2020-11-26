package com.cvt.iri.model;

import com.cvt.iri.storage.Persistable;

/**
 * Created by paul on 5/15/17.
 */
public class LongPersistable implements Persistable {
    private long value;
    public LongPersistable(){}
    public LongPersistable(Long value) {
        this.value = value;
    }

    @Override
    public byte[] bytes() {
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(value & 0xff).byteValue();
            value = value >> 8;
        }
        return b;
    }

    @Override
    public void read(byte[] bytes) {
        long s0 = bytes[0] & 0xff;// 最低位
        long s1 = bytes[1] & 0xff;
        long s2 = bytes[2] & 0xff;
        long s3 = bytes[3] & 0xff;
        long s4 = bytes[4] & 0xff;// 最低位
        long s5 = bytes[5] & 0xff;
        long s6 = bytes[6] & 0xff;
        long s7 = bytes[7] & 0xff;

        // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        value = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
    }

    @Override
    public byte[] metadata() {
        return new byte[0];
    }

    @Override
    public void readMetadata(byte[] bytes) {

    }

    @Override
    public boolean merge() {
        return false;
    }

    public long getLongValue() {
        return value;
    }
}
