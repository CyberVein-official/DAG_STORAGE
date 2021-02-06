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
    


}
