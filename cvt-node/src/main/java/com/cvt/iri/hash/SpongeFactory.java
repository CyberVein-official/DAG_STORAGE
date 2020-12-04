package com.cvt.iri.hash;

/**
 *
 */
public abstract class SpongeFactory {
    public enum Mode {
        CURLP81,
        CURLP27,
        KERL,
        //BCURLT
    }
    public static Sponge create(Mode mode){
        switch (mode) {
            case CURLP81: return new Curl(mode);
            case CURLP27: return new Curl(mode);
            case KERL: return new Kerl();
            //case BCURLT: return new Curl(true, mode);
            default: return null;
        }
    }
}
