package com.cvt.iri.service.dto;

/**
 * This class represents the core API error for accessing a command which is limited by this Node.
 */
public class NewAddressResponse extends AbstractResponse {

    private String address;

    public static AbstractResponse create(String address) {
        NewAddressResponse res = new NewAddressResponse();
        res.address = address;
        return res;
    }


    /**
     * Gets the error
     *
     * @return The error.
     */

    public String getAddress() {
        return address;
    }
}
