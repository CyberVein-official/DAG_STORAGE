package jota.dto.response;

import jota.dto.request.CvtNeighborsRequest;

/**
 * Response of {@link CvtNeighborsRequest}.
 **/
public class GetNodeInfoResponse extends AbstractResponse {

    private String appName;
    private String appVersion;
    private String jreVersion;
    private int jreAvailableProcessors;
    private long jreFreeMemory;
    private long jreMaxMemory;
    private long jreTotalMemory;
    private String latestMilestone;
    private int latestMilestoneIndex;
    private String latestSolidSubtangleMilestone;
    private int latestSolidSubtangleMilestoneIndex;
    private int neighbors;
    private int packetsQueueSize;
    private long time;
    private int tips;
    private int transactionsToRequest;
    
    private String[] features;

    /**
     * The name of the CVT software the node currently running (IRI stands for Initial Reference Implementation).
     *
     * @return appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * The version of the CVT software the node currently running.
     *
     * @return The version of the CVT software the node currently running.
     */
    public String getAppVersion() {
        return appVersion;
    }



}