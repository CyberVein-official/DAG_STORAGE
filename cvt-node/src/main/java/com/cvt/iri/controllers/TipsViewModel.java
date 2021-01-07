package com.cvt.iri.controllers;

import com.cvt.iri.model.Hash;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class TipsViewModel {

    public static final int MAX_TIPS = 5000;

    private final FifoHashCache<Hash> tips = new FifoHashCache<>(TipsViewModel.MAX_TIPS);
    private final FifoHashCache<Hash> solidTips = new FifoHashCache<>(TipsViewModel.MAX_TIPS);

    private final SecureRandom seed = new SecureRandom();
    private final Object sync = new Object();

    public void addTipHash(Hash hash) {
        synchronized (sync) {
            tips.add(hash);
        }
    }

    public void removeTipHash(Hash hash) {
        synchronized (sync) {
            if (!tips.remove(hash)) {
                solidTips.remove(hash);
            }
        }
    }

    public void setSolid(Hash tip) {
        synchronized (sync) {
            if (tips.remove(tip)) {
                solidTips.add(tip);
            }
        }
    }
    public Set<Hash> getTips() {
        Set<Hash> hashes = new HashSet<>();
        synchronized (sync) {
            Iterator<Hash> hashIterator;
            hashIterator = tips.iterator();
            while (hashIterator.hasNext()) {
                hashes.add(hashIterator.next());
            }

            hashIterator = solidTips.iterator();
            while (hashIterator.hasNext()) {
                hashes.add(hashIterator.next());
            }
        }
        return hashes;
    }

}
