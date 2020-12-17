package com.cvt.iri.hash;

import java.util.ArrayList;
import java.util.List;

import static com.cvt.iri.hash.PearlDiver.State.*;

public class PearlDiver {

    enum State {
        RUNNING,
        CANCELLED,
        COMPLETED
    }

    private static final int TRANSACTION_LENGTH = 8019;

    private static final int CURL_HASH_LENGTH = 243;
    private static final int CURL_STATE_LENGTH = CURL_HASH_LENGTH * 3;

    private static final long HIGH_BITS = 0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111111L;
    private static final long LOW_BITS = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;

    private volatile State state;
    private final Object syncObj = new Object();

    public void cancel() {
        synchronized (syncObj) {
            state = CANCELLED;
        }
    }

    private static void validateParameters(byte[] transactionTrits, int minWeightMagnitude) {
        if (transactionTrits.length != TRANSACTION_LENGTH) {
            throw new RuntimeException(
                "Invalid transaction trits length: " + transactionTrits.length);
        }
        if (minWeightMagnitude < 0 || minWeightMagnitude > CURL_HASH_LENGTH) {
            throw new RuntimeException("Invalid min weight magnitude: " + minWeightMagnitude);
        }
    }
    public synchronized boolean search(final byte[] transactionTrits, final int minWeightMagnitude,
                                       int numberOfThreads) {

        validateParameters(transactionTrits, minWeightMagnitude);
        synchronized (syncObj) {
            state = RUNNING;
        }

        final long[] midStateLow = new long[CURL_STATE_LENGTH];
        final long[] midStateHigh = new long[CURL_STATE_LENGTH];
        initializeMidCurlStates(transactionTrits, midStateLow, midStateHigh);

        if (numberOfThreads <= 0) {
            int available = Runtime.getRuntime().availableProcessors();
            numberOfThreads = Math.max(1, Math.floorDiv(available * 8, 10));
        }
        List<Thread> workers = new ArrayList<>(numberOfThreads);
        while (numberOfThreads-- > 0) {
            long[] midStateCopyLow = midStateLow.clone();
            long[] midStateCopyHigh = midStateHigh.clone();
            Runnable runnable = getRunnable(numberOfThreads, transactionTrits, minWeightMagnitude, midStateCopyLow, midStateCopyHigh);
            Thread worker = new Thread(runnable);
            workers.add(worker);
            worker.setName(this + ":worker-" + numberOfThreads);
            worker.setDaemon(true);
            worker.start();
        }
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                synchronized (syncObj) {
                    state = CANCELLED;
                }
            }
        }
        return state == COMPLETED;
    }
    private Runnable getRunnable(final int threadIndex, final byte[] transactionTrits, final int minWeightMagnitude,
                                 final long[] midStateCopyLow, final long[] midStateCopyHigh) {
        return () -> {
            for (int i = 0; i < threadIndex; i++) {
                increment(midStateCopyLow, midStateCopyHigh, 162 + CURL_HASH_LENGTH / 9,
                        162 + (CURL_HASH_LENGTH / 9) * 2);
            }

            final long[] stateLow = new long[CURL_STATE_LENGTH];
            final long[] stateHigh = new long[CURL_STATE_LENGTH];

            final long[] scratchpadLow = new long[CURL_STATE_LENGTH];
            final long[] scratchpadHigh = new long[CURL_STATE_LENGTH];

            final int maskStartIndex = CURL_HASH_LENGTH - minWeightMagnitude;
            long mask = 0;
            while (state == RUNNING && mask == 0) {

                increment(midStateCopyLow, midStateCopyHigh, 162 + (CURL_HASH_LENGTH / 9) * 2,
                        CURL_HASH_LENGTH);

                copy(midStateCopyLow, midStateCopyHigh, stateLow, stateHigh);
                transform(stateLow, stateHigh, scratchpadLow, scratchpadHigh);

                mask = HIGH_BITS;
                for (int i = maskStartIndex; i < CURL_HASH_LENGTH && mask != 0; i++) {
                    mask &= ~(stateLow[i] ^ stateHigh[i]);
                }
            }
            if (mask != 0) {
                synchronized (syncObj) {
                    if (state == RUNNING) {
                        state = COMPLETED;
                        long outMask = 1;
                        while ((outMask & mask) == 0) {
                            outMask <<= 1;
                        }
                        for (int i = 0; i < CURL_HASH_LENGTH; i++) {
                            transactionTrits[TRANSACTION_LENGTH - CURL_HASH_LENGTH + i] =
                                    (midStateCopyLow[i] & outMask) == 0 ? 1
                                            : (midStateCopyHigh[i] & outMask) == 0 ? (byte) -1 : (byte) 0;
                        }
                    }
                }
            }
        };
    }
    private static void copy(long[] srcLow, long[] srcHigh, long[] destLow, long[] destHigh) {
        System.arraycopy(srcLow, 0, destLow, 0, CURL_STATE_LENGTH);
        System.arraycopy(srcHigh, 0, destHigh, 0, CURL_STATE_LENGTH);
    }

}