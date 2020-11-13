package jota;

/**
 * Interface for an implementation to perform local PoW.
 */
public interface CvtLocalPoW {
    String performPoW(String trytes, int minWeightMagnitude);
}
