package com.cvt.client.util;

/**
 * Table of CVT units based off of the standard system of Units.
 *
 * @author pinpong
 **/
public enum CvtUnits {

    CVT("i", 0),
    KILO_CVT("Ki", 3), // 10^3
    MEGA_CVT("Mi", 6), // 10^6
    GIGA_CVT("Gi", 9), // 10^9
    TERA_CVT("Ti", 12), // 10^12
    PETA_CVT("Pi", 15); // 10^15

    private String unit;
    private long value;

    /**
     * Initializes a new instance of the CvtUnit class.
     */
    CvtUnits(String unit, long value) {
        this.unit = unit;
        this.value = value;
    }

    /**
     * Gets the unit.
     *
     * @return The CVT Unit.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets the value.
     *
     * @return The value.
     */
    public long getValue() {
        return value;
    }
}
