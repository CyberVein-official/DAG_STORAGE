<<<<<<< HEAD
package com.cvt.iri.controllers;

import com.cvt.iri.model.Bundle;
import com.cvt.iri.model.Hash;
import com.cvt.iri.storage.Indexable;
import com.cvt.iri.storage.Persistable;
import com.cvt.iri.storage.Tangle;
import com.cvt.iri.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by paul on 5/15/17.
 */
public class BundleViewModel implements HashesViewModel {
    private Bundle self;
    private Indexable hash;

    public BundleViewModel(Hash hash) {
        this.hash = hash;
    }

    private BundleViewModel(Bundle hashes, Indexable hash) {
        self = hashes == null || hashes.set == null ? new Bundle(): hashes;
        this.hash = hash;
    }

    public static BundleViewModel load(Tangle tangle, Indexable hash) throws Exception {
        return new BundleViewModel((Bundle) tangle.load(Bundle.class, hash), hash);

    }

    public static Map.Entry<Indexable, Persistable> getEntry(Hash hash, Hash hashToMerge) throws Exception {
        Bundle hashes = new Bundle();
        hashes.set.add(hashToMerge);
        return new HashMap.SimpleEntry<>(hash, hashes);
    }

    /*
    public static boolean merge(Hash hash, Hash hashToMerge) throws Exception {
        Bundle hashes = new Bundle();
        hashes.set = new HashSet<>(Collections.singleton(hashToMerge));
        return Tangle.instance().merge(hashes, hash);
    }
    */

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
        tangle.delete(Bundle.class,hash);
    }


    public static BundleViewModel first(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> bundlePair = tangle.getFirst(Bundle.class, Hash.class);
        if(bundlePair != null && bundlePair.hi != null) {
            return new BundleViewModel((Bundle) bundlePair.hi, (Hash) bundlePair.low);
        }
        return null;
    }

    public BundleViewModel next(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> bundlePair = tangle.next(Bundle.class, hash);
        if(bundlePair != null && bundlePair.hi != null) {
            return new BundleViewModel((Bundle) bundlePair.hi, (Hash) bundlePair.low);
        }
        return null;
    }


}
=======
package com.cvt.iri.controllers;

import com.cvt.iri.model.Bundle;
import com.cvt.iri.model.Hash;
import com.cvt.iri.storage.Indexable;
import com.cvt.iri.storage.Persistable;
import com.cvt.iri.storage.Tangle;
import com.cvt.iri.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by paul on 5/15/17.
 */
public class BundleViewModel implements HashesViewModel {
    private Bundle self;
    private Indexable hash;

    public BundleViewModel(Hash hash) {
        this.hash = hash;
    }

    private BundleViewModel(Bundle hashes, Indexable hash) {
        self = hashes == null || hashes.set == null ? new Bundle(): hashes;
        this.hash = hash;
    }

    public static BundleViewModel load(Tangle tangle, Indexable hash) throws Exception {
        return new BundleViewModel((Bundle) tangle.load(Bundle.class, hash), hash);

    }

    public static Map.Entry<Indexable, Persistable> getEntry(Hash hash, Hash hashToMerge) throws Exception {
        Bundle hashes = new Bundle();
        hashes.set.add(hashToMerge);
        return new HashMap.SimpleEntry<>(hash, hashes);
    }

    /*
    public static boolean merge(Hash hash, Hash hashToMerge) throws Exception {
        Bundle hashes = new Bundle();
        hashes.set = new HashSet<>(Collections.singleton(hashToMerge));
        return Tangle.instance().merge(hashes, hash);
    }
    */

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
        tangle.delete(Bundle.class,hash);
    }


    public static BundleViewModel first(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> bundlePair = tangle.getFirst(Bundle.class, Hash.class);
        if(bundlePair != null && bundlePair.hi != null) {
            return new BundleViewModel((Bundle) bundlePair.hi, (Hash) bundlePair.low);
        }
        return null;
    }

    public BundleViewModel next(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> bundlePair = tangle.next(Bundle.class, hash);
        if(bundlePair != null && bundlePair.hi != null) {
            return new BundleViewModel((Bundle) bundlePair.hi, (Hash) bundlePair.low);
        }
        return null;
    }


}
>>>>>>> 05f4f06df55719f449d00a6466e1ae45de137138
