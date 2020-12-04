package jota;

import jota.dto.request.*;
import jota.dto.response.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * CVT API Proxy Service definition using Retrofit2
 *
 * @author davassi
 */
public interface CvtAPIService {

    String CONTENT_TYPE_HEADER = "Content-Type: application/json";
    String USER_AGENT_HEADER = "User-Agent: JOTA-API wrapper";

    /**
     * Returns information about the node.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "getNodeInfo"}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<GetNodeInfoResponse> getNodeInfo(@Body CvtCommandRequest request);

    /**
     * Get the list of neighbors from the node.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "getNeighbors"}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<GetNeighborsResponse> getNeighbors(@Body CvtCommandRequest request);

    /**
     * Add a list of neighbors to the node.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "addNeighbors", "uris": ["udp://8.8.8.8:14265", "udp://8.8.8.5:14265"]}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<AddNeighborsResponse> addNeighbors(@Body CvtNeighborsRequest request);

    /**
     * Removes a list of neighbors from the node.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "removeNeighbors", "uris": ["udp://8.8.8.8:14265", "udp://8.8.8.5:14265"]}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<RemoveNeighborsResponse> removeNeighbors(@Body CvtNeighborsRequest request);




}
