package jota.utils;

import jota.error.ArgumentException;
import jota.model.Bundle;
import jota.pow.ICurl;
import jota.pow.SpongeFactory;

import java.util.Arrays;

/**
 * @author pinpong
 */
public class Multisig {

    private ICurl curl;
    private Signing signingInstance;

    /**
     * Initializes a new instance of the Multisig class.
     */
    public Multisig(ICurl customCurl) {
        this.curl = customCurl;
        this.curl.reset();
        this.signingInstance = new Signing(curl.clone());
    }

    /**
     * Initializes a new instance of the Multisig class.
     */
    public Multisig() {
        this(SpongeFactory.create(SpongeFactory.Mode.KERL));
    }

    /**
     * @param seed     Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security Secuirty level of private key / seed.
     * @param index    Key index to start search from. If the index is provided, the generation of the address is not deterministic.
     * @return trytes
     * @throws ArgumentException is thrown when the specified security level is not valid.
     **/
    public String getDigest(String seed, int security, int index) throws ArgumentException {
        int[] key = signingInstance.key(Converter.trits(seed, 243), index, security);
        return Converter.trytes(signingInstance.digests(key));
    }

    /**
     * Initiates the generation of a new multisig address or adds the key digest to an existing one
     *
     * @param digestTrytes
     * @param curlStateTrytes
     * @return trytes.
     **/
    public String addAddressDigest(String digestTrytes, String curlStateTrytes) {


        int[] digest = Converter.trits(digestTrytes, digestTrytes.length() * 3);

        // If curlStateTrytes is provided, convert into trits
        // else use empty state and initiate the creation of a new address

        int[] curlState = !curlStateTrytes.isEmpty() ? Converter.trits(curlStateTrytes,
                digestTrytes.length() * 3) : new int[digestTrytes.length() * 3];

        // initialize Curl with the provided state
        curl.setState(curlState);
        // absorb the key digest
        curl.absorb(digest);

        return Converter.trytes(curl.getState());
    }

    /**
     * Gets the key value of a seed
     *
     * @param seed  Tryte-encoded seed. It should be noted that this seed is not transferred
     * @param index Key index to start search from. If the index is provided, the generation of the address is not deterministic.
     * @return trytes.
     * @throws ArgumentException is thrown when the specified security level is not valid.
     **/

    public String getKey(String seed, int index, int security) throws ArgumentException {

        return Converter.trytes(signingInstance.key(Converter.trits(seed, 81 * security), index, security));
    }

    /**
     * Generates a new address
     *
     * @param curlStateTrytes
     * @return address
     **/
    public String finalizeAddress(String curlStateTrytes) {

        int[] curlState = Converter.trits(curlStateTrytes);

        // initialize Curl with the provided state
        curl.setState(curlState);

        int[] addressTrits = new int[243];
        curl.squeeze(addressTrits);

        // Convert trits into trytes and return the address
        return Converter.trytes(addressTrits);
    }

    /**
     * Validates  a generated multisig address
     *
     * @param multisigAddress
     * @param digests
     * @returns boolean
     **/
    public boolean validateAddress(String multisigAddress, int[][] digests) {

        // initialize Curl with the provided state
        curl.reset();


        for (int[] keyDigest : digests) {
            curl.absorb(keyDigest);
        }

        int[] addressTrits = new int[243];
        curl.squeeze(addressTrits);

        // Convert trits into trytes and return the address
        return Converter.trytes(addressTrits).equals(multisigAddress);
    }
}