package com.cvt.iri.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on
 */
public class CvtPersistable implements Serializable {

    private String address;

    private Long value;

    private Long timestamp;

    public CvtPersistable() {
    }

    public CvtPersistable(String address, Long value, Long timestamp) {
        this.address = address;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("address", address);
        map.put("value", value);
        map.put("timestamp", timestamp);
        return map;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
