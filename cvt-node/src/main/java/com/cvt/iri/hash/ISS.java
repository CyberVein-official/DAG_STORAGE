package com.cvt.iri.hash;

import com.cvt.iri.model.Hash;

import java.util.Arrays;

/**
 * (c)
 */
public class ISS {

    public static final int NUMBER_OF_FRAGMENT_CHUNKS = 27;
    public static final int FRAGMENT_LENGTH = Curl.HASH_LENGTH * NUMBER_OF_FRAGMENT_CHUNKS;
    private static final int NUMBER_OF_SECURITY_LEVELS = 3;

    private static final int MIN_TRIT_VALUE = -1, MAX_TRIT_VALUE = 1;
    public static final int TRYTE_WIDTH = 3;
    private static final int MIN_TRYTE_VALUE = -13, MAX_TRYTE_VALUE = 13;
    public static final int NORMALIZED_FRAGMENT_LENGTH = Curl.HASH_LENGTH / TRYTE_WIDTH / NUMBER_OF_SECURITY_LEVELS;

    public static byte[] subseed(SpongeFactory.Mode mode, final byte[] seed, int index) {

        if (index < 0) {
            throw new RuntimeException("Invalid subseed index: " + index);
        }

        final byte[] subseedPreimage = Arrays.copyOf(seed, seed.length);

        while (index-- > 0) {

            for (int i = 0; i < subseedPreimage.length; i++) {

                if (++subseedPreimage[i] > MAX_TRIT_VALUE) {
                    subseedPreimage[i] = MIN_TRIT_VALUE;
                } else {
                    break;
                }
            }
        }

        final byte[] subseed = new byte[Curl.HASH_LENGTH];

        final Sponge hash = SpongeFactory.create(mode);
        hash.absorb(subseedPreimage, 0, subseedPreimage.length);
        hash.squeeze(subseed, 0, subseed.length);
        return subseed;
    }

    public static byte[] key(SpongeFactory.Mode mode, final byte[] subseed, final int numberOfFragments) {

        if (subseed.length != Curl.HASH_LENGTH) {
            throw new RuntimeException("Invalid subseed length: " + subseed.length);
        }
        if (numberOfFragments <= 0) {
            throw new RuntimeException("Invalid number of key fragments: " + numberOfFragments);
        }

        final byte[] key = new byte[FRAGMENT_LENGTH * numberOfFragments];

        final Sponge hash = SpongeFactory.create(mode);
        hash.absorb(subseed, 0, subseed.length);
        hash.squeeze(key, 0, key.length);
        return key;
    }

    public static byte[] digests(SpongeFactory.Mode mode, final byte[] key) {

        if (key.length == 0 || key.length % FRAGMENT_LENGTH != 0) {
            throw new RuntimeException("Invalid key length: " + key.length);
        }

        final byte[] digests = new byte[key.length / FRAGMENT_LENGTH * Curl.HASH_LENGTH];
        final Sponge hash = SpongeFactory.create(mode);

        for (int i = 0; i < key.length / FRAGMENT_LENGTH; i++) {

            final byte[] buffer = Arrays.copyOfRange(key, i * FRAGMENT_LENGTH, (i + 1) * FRAGMENT_LENGTH);
            for (int j = 0; j < NUMBER_OF_FRAGMENT_CHUNKS; j++) {

                for (int k = MAX_TRYTE_VALUE - MIN_TRYTE_VALUE; k-- > 0; ) {
                    hash.reset();
                    hash.absorb(buffer, j * Curl.HASH_LENGTH, Curl.HASH_LENGTH);
                    hash.squeeze(buffer, j * Curl.HASH_LENGTH, Curl.HASH_LENGTH);
                }
            }
            hash.reset();
            hash.absorb(buffer, 0, buffer.length);
            hash.squeeze(digests, i * Curl.HASH_LENGTH, Curl.HASH_LENGTH);
        }
        return digests;
    }

}
