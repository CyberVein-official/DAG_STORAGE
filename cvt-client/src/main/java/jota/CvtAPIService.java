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

    /**
     * Get the list of latest tips (unconfirmed transactions).
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "getTips"}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<GetTipsResponse> getTips(@Body CvtCommandRequest request);

    /**
     * Find the transactions which match the specified input and return.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "findTransactions", "addresses": ["RVORZ9SIIP9RCYMREUIXXVPQIPHVCNPQ9HZWYKFWYWZRE9JQKG9REPKIASHUUECPSQO9JT9XNMVKWYGVAZETAIRPTM"]}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<FindTransactionResponse> findTransactions(@Body CvtFindTransactionsRequest request);

    /**
     * Get the inclusion states of a set of transactions. This is for determining if a transaction was accepted and confirmed by the network or not.
     * You can search for multiple tips (and thus, milestones) to get past inclusion states of transactions.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "getInclusionStates", "transactions"Q9HZWYKFWYWZRE9JQKG9REPKIASHUUECPSQO9JT9XNMVKWYGVAZETAIRPTM"], "tips" : [ZIJGAJ9AADLRPWNCYNNHUHRRAC9QOUDATEDQUMTNOTABUVRPTSTFQDGZKFYUUIE9ZEBIVCCXXXLKX9999]}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<GetInclusionStateResponse> getInclusionStates(@Body CvtGetInclusionStateRequest request);

    /**
     * Returns the raw trytes data of a transaction.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "getTrytes", "hashes": ["OAATQS9VQLSXCLDJVJJVYUGONXAXOFMJOZNSYWRZSWECMXAQQURHQBJNLD9IOFEPGZEPEMPXCIVRX9999"]}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<GetTrytesResponse> getTrytes(@Body CvtGetTrytesRequest request);

    /**
     * Tip selection which returns trunkTransaction and branchTransaction.
     * The input value is the latest coordinator milestone, as provided through the getNodeInfo API call.
     * <p>
     * {@code curl http://localhost:14265 -X POST -H 'X-CVT-API-Version: 1.4.1' -H 'Content-Type: application/json'}
     * {@code -d '{"command": "getTransactionsToApprove", "depth": 27}'}
     */
    @Headers({CONTENT_TYPE_HEADER, USER_AGENT_HEADER})
    @POST("./")
    Call<GetTransactionsToApproveResponse> getTransactionsToApprove(@Body CvtGetTransactionsToApproveRequest request);

}
