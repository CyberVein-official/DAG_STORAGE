package jota.pow;

import jota.utils.Converter;
import jota.utils.Pair;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * (c) 2016 Come-from-Beyond
 *
 * JCurl belongs to the sponge function family.
 */
public class JCurl implements ICurl {

    /**
     * The hash length.
     */
    public static final int HASH_LENGTH = 243;
    private static final int STATE_LENGTH = 3 * HASH_LENGTH;

    public static final int NUMBER_OF_ROUNDSP81 = 81;
    public static final int NUMBER_OF_ROUNDSP27 = 27;
    private final int numberOfRounds;

    private static final int[] TRUTH_TABLE = {1, 0, -1, 2, 1, -1, 0, 2, -1, 1, 0};
    private final long[] stateLow;
    private final long[] stateHigh;
    private final int[] scratchpad = new int[STATE_LENGTH];
    private int[] state;

    public JCurl(boolean pair, SpongeFactory.Mode mode) {
        switch (mode) {
            case CURLP27: {
                numberOfRounds = NUMBER_OF_ROUNDSP27;
            }
            break;
            case CURLP81: {
                numberOfRounds = NUMBER_OF_ROUNDSP81;
            }
            break;
            default:
                throw new NoSuchElementException("Only Curl-P-27 and Curl-P-81 are supported.");
        }
        if (pair) {
            stateHigh = new long[STATE_LENGTH];
            stateLow = new long[STATE_LENGTH];
            state = null;
            set();
        } else {
            state = new int[STATE_LENGTH];
            stateHigh = null;
            stateLow = null;
        }
    }

    public JCurl(SpongeFactory.Mode mode) {
        switch (mode) {
            case CURLP27: {
                numberOfRounds = NUMBER_OF_ROUNDSP27;
            }
            break;
            case CURLP81: {
                numberOfRounds = NUMBER_OF_ROUNDSP81;
            }
            break;
            default:
                throw new NoSuchElementException("Only Curl-P-27 and Curl-P-81 are supported.");
        }
        state = new int[STATE_LENGTH];
        stateHigh = null;
        stateLow = null;
    }
    /**
     * Absorbs the specified trits.
     *
     * @param trits  The trits.
     * @param offset The offset to start from.
     * @param length The length.
     * @return The ICurl instance (used for method chaining).
     */
    public JCurl absorb(final int[] trits, int offset, int length) {

        do {
            System.arraycopy(trits, offset, state, 0, length < HASH_LENGTH ? length : HASH_LENGTH);
            transform();
            offset += HASH_LENGTH;
        } while ((length -= HASH_LENGTH) > 0);

        return this;
    }

    /**
     * Absorbs the specified trits.
     *
     * @param trits The trits.
     * @return The ICurl instance (used for method chaining).
     */
    public JCurl absorb(final int[] trits) {
        return absorb(trits, 0, trits.length);
    }

    /**
     * Transforms this instance.
     *
     * @return The ICurl instance (used for method chaining).
     */
    public JCurl transform() {

        int scratchpadIndex = 0;
        int prev_scratchpadIndex = 0;
        for (int round = 0; round < numberOfRounds; round++) {
            System.arraycopy(state, 0, scratchpad, 0, STATE_LENGTH);
            for (int stateIndex = 0; stateIndex < STATE_LENGTH; stateIndex++) {
                prev_scratchpadIndex = scratchpadIndex;
                if (scratchpadIndex < 365) {
                    scratchpadIndex += 364;
                } else {
                    scratchpadIndex += -365;
                }
                state[stateIndex] = TRUTH_TABLE[scratchpad[prev_scratchpadIndex] + (scratchpad[scratchpadIndex] << 2) + 5];
            }
        }

        return this;
    }
    /**
     * Resets this state.
     *
     * @return The ICurl instance (used for method chaining).
     */
    public JCurl reset() {
        Arrays.fill(state, 0);
        return this;
    }

    public JCurl reset(boolean pair) {
        if (pair) {
            set();
        } else {
            reset();
        }
        return this;
    }

    /**
     * Squeezes the specified trits.
     *
     * @param trits  The trits.
     * @param offset The offset to start from.
     * @param length The length.
     * @return The squeezes trits.
     */
    public int[] squeeze(final int[] trits, int offset, int length) {

        do {
            System.arraycopy(state, 0, trits, offset, length < HASH_LENGTH ? length : HASH_LENGTH);
            transform();
            offset += HASH_LENGTH;
        } while ((length -= HASH_LENGTH) > 0);

        return state;
    }

    /**
     * Squeezes the specified trits.
     *
     * @param trits The trits.
     * @return The squeezes trits.
     */
    public int[] squeeze(final int[] trits) {
        return squeeze(trits, 0, trits.length);
    }

    /**
     * Gets the states.
     *
     * @return The state.
     */
    public int[] getState() {
        return state;
    }

}
