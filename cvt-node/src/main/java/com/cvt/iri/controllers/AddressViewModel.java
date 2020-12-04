package com.cvt.iri.controllers;

import com.cvt.iri.model.Address;
import com.cvt.iri.model.Hash;
import com.cvt.iri.storage.Indexable;
import com.cvt.iri.storage.Persistable;
import com.cvt.iri.utils.Pair;

import java.util.Set;

/**
 * Created by paul on
 */
public class AddressViewModel implements HashesViewModel {
    private Address self;
    private Indexable hash;

    public AddressViewModel(Hash hash) {
        this.hash = hash;
    }

    private AddressViewModel(Address hashes, Indexable hash) {
        self = hashes == null || hashes.set == null ? new Address(): hashes;
        this.hash = hash;
    }

    public static AddressViewModel load(Tangle tangle, Indexable hash) throws Exception {
        return new AddressViewModel((Address) tangle.load(Address.class, hash), hash);
    }

    public boolean store(Tangle tangle) throws Exception {
        return tangle.save(self, hash);
    }

    public int size() {
        return self.set.size();
    }

    public boolean addHash(Hash theHash) {
        return getHashes().add(theHash);
    }

    public Indexable getIndex() {
        return hash;
    }

    public Set<Hash> getHashes() {
        return self.set;
    }
    @Override
    public void delete(Tangle tangle) throws Exception {
        tangle.delete(Address.class,hash);
    }

    public static AddressViewModel first(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> bundlePair = tangle.getFirst(Address.class, Hash.class);
        if(bundlePair != null && bundlePair.hi != null) {
            return new AddressViewModel((Address) bundlePair.hi, (Hash) bundlePair.low);
        }
        return null;
    }

    public AddressViewModel next(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> bundlePair = tangle.next(Address.class, hash);
        if(bundlePair != null && bundlePair.hi != null) {
            return new AddressViewModel((Address) bundlePair.hi, (Hash) bundlePair.low);
        }
        return null;
    }
}
