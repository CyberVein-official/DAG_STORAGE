package jota.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static jota.utils.Constants.TRYTE_ALPHABET;

/**
 * This class provides a set of utility methods to are used to convert between different formats.
 */
public class Converter {

    public static final int HIGH_INTEGER_BITS = 0xFFFFFFFF;
    public static final long HIGH_LONG_BITS = 0xFFFFFFFFFFFFFFFFL;
    /**
     * The radix
     */
    private static final int RADIX = 3;
    /**
     * The maximum trit value
     */
    private static final int MAX_TRIT_VALUE = (RADIX - 1) / 2, MIN_TRIT_VALUE = -MAX_TRIT_VALUE;
    /**
     * The number of trits in a byte
     */
    private static final int NUMBER_OF_TRITS_IN_A_BYTE = 5;
    /**
     * The number of trits in a tryte
     */
    private static final int NUMBER_OF_TRITS_IN_A_TRYTE = 3;
    private static final int[][] BYTE_TO_TRITS_MAPPINGS = new int[243][];
    private static final int[][] TRYTE_TO_TRITS_MAPPINGS = new int[27][];

    static {

        final int[] trits = new int[NUMBER_OF_TRITS_IN_A_BYTE];

        for (int i = 0; i < 243; i++) {
            BYTE_TO_TRITS_MAPPINGS[i] = Arrays.copyOf(trits, NUMBER_OF_TRITS_IN_A_BYTE);
            increment(trits, NUMBER_OF_TRITS_IN_A_BYTE);
        }

        for (int i = 0; i < 27; i++) {
            TRYTE_TO_TRITS_MAPPINGS[i] = Arrays.copyOf(trits, NUMBER_OF_TRITS_IN_A_TRYTE);
            increment(trits, NUMBER_OF_TRITS_IN_A_TRYTE);
        }
    }

    /**
     * Converts the specified trits array to bytes.
     *
     * @param trits  The trits.
     * @param offset The offset to start from.
     * @param size   The size.
     * @return The bytes.
     */
    public static byte[] bytes(final int[] trits, final int offset, final int size) {

        final byte[] bytes = new byte[(size + NUMBER_OF_TRITS_IN_A_BYTE - 1) / NUMBER_OF_TRITS_IN_A_BYTE];
        for (int i = 0; i < bytes.length; i++) {

            int value = 0;
            for (int j = (size - i * NUMBER_OF_TRITS_IN_A_BYTE) < 5 ? (size - i * NUMBER_OF_TRITS_IN_A_BYTE) : NUMBER_OF_TRITS_IN_A_BYTE; j-- > 0; ) {
                value = value * RADIX + trits[offset + i * NUMBER_OF_TRITS_IN_A_BYTE + j];
            }
            bytes[i] = (byte) value;
        }

        return bytes;
    }

    public static byte[] bytes(final int[] trits) {
        return bytes(trits, 0, trits.length);
    }

    /**
     * Gets the trits from the specified bytes and stores it into the provided trits array.
     *
     * @param bytes The bytes.
     * @param trits The trits.
     */
    public static void getTrits(final byte[] bytes, final int[] trits) {

        int offset = 0;
        for (int i = 0; i < bytes.length && offset < trits.length; i++) {
            System.arraycopy(BYTE_TO_TRITS_MAPPINGS[bytes[i] < 0 ? (bytes[i] + BYTE_TO_TRITS_MAPPINGS.length) : bytes[i]], 0, trits, offset, trits.length - offset < NUMBER_OF_TRITS_IN_A_BYTE ? (trits.length - offset) : NUMBER_OF_TRITS_IN_A_BYTE);
            offset += NUMBER_OF_TRITS_IN_A_BYTE;
        }
        while (offset < trits.length) {
            trits[offset++] = 0;
        }
    }

    public static int[] convertToIntArray(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = integers.get(i);
        }
        return ret;
    }

}