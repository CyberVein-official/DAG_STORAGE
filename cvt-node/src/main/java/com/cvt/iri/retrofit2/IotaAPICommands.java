package com.cvt.iri.retrofit2;

/**
 * CVT's node command list
 *
 */
public enum IotaAPICommands {

    VERIFY_STORAGE("verifyStorage");

    private String command;

    /**
     * Initializes a new instance of the IotaAPICommands class.
     */
    IotaAPICommands(String command) {
        this.command = command;
    }

}

