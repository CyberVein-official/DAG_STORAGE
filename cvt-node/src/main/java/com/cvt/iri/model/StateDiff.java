package com.cvt.iri.model;

import com.cvt.iri.storage.Persistable;
import com.cvt.iri.utils.Serializer;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 5/6/17.
 */
public class StateDiff implements Persistable {
    public Map<Hash, Long> state;

    public byte[] bytes() {
        return state.entrySet().parallelStream()
                .map(entry -> ArrayUtils.addAll(entry.getKey().bytes(), Serializer.serialize(entry.getValue())))
                .reduce(ArrayUtils::addAll)
                .orElse(new byte[0]);
    }
    public void read(byte[] bytes) {
        int i;
        state = new HashMap<>();
        if(bytes != null) {
            for (i = 0; i < bytes.length; i += Hash.SIZE_IN_BYTES + Long.BYTES) {
                state.put(new Hash(bytes, i, Hash.SIZE_IN_BYTES),
                        Serializer.getLong(Arrays.copyOfRange(bytes, i + Hash.SIZE_IN_BYTES, i + Hash.SIZE_IN_BYTES + Long.BYTES)));
            }
        }
    }

}
