package com.cvt.iri.storage;

import java.io.Serializable;

/**
 * Created by paul on
 */
public interface Indexable extends Comparable<Indexable>, Serializable {
    byte[] bytes();
    void read(byte[] bytes);
    Indexable incremented();
    Indexable decremented();
}
