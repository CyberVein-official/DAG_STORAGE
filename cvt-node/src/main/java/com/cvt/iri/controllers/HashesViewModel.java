package com.cvt.iri.controllers;

import com.cvt.iri.model.Hash;
import com.cvt.iri.storage.Indexable;
import com.cvt.iri.storage.Tangle;

import java.util.*;

/**
 * Created by paul on
 */
public interface HashesViewModel {
    boolean store(Tangle tangle) throws Exception;
    int size();
    boolean addHash(Hash theHash);
    Indexable getIndex();
    Set<Hash> getHashes();
    void delete(Tangle tangle) throws Exception;

    HashesViewModel next(Tangle tangle) throws Exception;
}
