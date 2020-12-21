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
    
}
