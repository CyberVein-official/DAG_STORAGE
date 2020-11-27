package com.cvt.iri.retrofit2;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * CVT API Proxy Service definition using Retrofit2
 *
 * @author davassi
 */
public interface IotaAPIService {

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
    Call<VerifyStorageResponse> verifyStorage(@Body IotaCommandRequest request);
}
