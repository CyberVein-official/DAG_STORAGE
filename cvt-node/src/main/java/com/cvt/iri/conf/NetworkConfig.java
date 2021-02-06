package com.cvt.iri.conf;

import java.util.List;

/**
 * Configurations for the node networking. Including ports, DNS settings, list of neighbors,
 * and various optimization parameters.
 */
public interface NetworkConfig extends Config {

    /**
     * @return Descriptions#UDP_RECEIVER_PORT
     */
    int getUdpReceiverPort();

    /**
     * @return Descriptions#TCP_RECEIVER_PORT
     */
    int getTcpReceiverPort();

    /**
     * @return Descriptions#P_REMOVE_REQUEST
     */
    double getpRemoveRequest();

    /**
     * @return Descriptions#SEND_LIMIT
     */
    int getSendLimit();


    /**
     * @return Descriptions#MAX_PEERS
     */
    int getMaxPeers();

    /**
     * @return Descriptions#DNS_REFRESHER_ENABLED
     */
    boolean isDnsRefresherEnabled();

    /**
     * @return Descriptions#DNS_RESOLUTION_ENABLED
     */
    boolean isDnsResolutionEnabled();

    /**
     * @return Descriptions#NEIGHBORS
     */
    List<String> getNeighbors();

    /**
     * @return Descriptions#NEIGHBORS
     */
    String getSuperNode();

    String getSharedFilePath();

    String getIndexFilePath();

    /**
     * @return Descriptions#Q_SIZE_NODE
     */

    int getqSizeNode();

    /**
     * @return Descriptions#CACHE_SIZE_BYTES
     */
    int getCacheSizeBytes();


    interface Descriptions {
        String UDP_RECEIVER_PORT = "The UDP Receiver Port.";
        String TCP_RECEIVER_PORT = "The TCP Receiver Port.";
        String P_REMOVE_REQUEST = DescriptionHelper.PROB_OF + " stopping to request a transaction. This number should be " +
                "closer to 0 so non-existing transaction hashes will eventually be removed.";
        String SEND_LIMIT = "The maximum number of packets that may be sent by this node in a 1 second interval. If this number is below 0 then there is no limit.";
        String MAX_PEERS = "The maximum number of non mutually tethered connections allowed. Works only in testnet mode";
        String DNS_REFRESHER_ENABLED = "Reconnect to neighbors that have dynamic IPs.";
        String DNS_RESOLUTION_ENABLED = "Enable using DNS for neighbor peering.";

    }


}
