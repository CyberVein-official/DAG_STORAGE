package com.cvt.iri.storage;

import java.io.Serializable;

/**
 * Created by paul on
 */
public interface Persistable extends Serializable {
    byte[] bytes();
    void read(byte[] bytes);
    byte[] metadata();
    void readMetadata(byte[] bytes);
    boolean merge();
}
