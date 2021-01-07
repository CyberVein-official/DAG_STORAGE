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




}
