package jota;

import jota.dto.request.*;
import jota.dto.response.*;
import jota.error.ArgumentException;
import jota.model.Transaction;
import jota.utils.Checksum;
import jota.utils.InputValidator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static jota.utils.Constants.*;

/**
 * This class provides access to the Cvt core API
 *
 * @author Adrian
 */
public class CvtAPICore {

    // version header
    private static final String X_CVT_API_VERSION_HEADER_NAME = "X-CVT-API-Version";
    private static final String X_CVT_API_VERSION_HEADER_VALUE = "1";

    private static final Logger log = LoggerFactory.getLogger(CvtAPICore.class);

    private CvtAPIService service;
    private String protocol, host, port;
    private CvtLocalPoW localPoW;

    /**
     * Build the API core.
     *
     * @param builder The builder.
     */
    protected CvtAPICore(final Builder builder) {
        protocol = builder.protocol;
        host = builder.host;
        port = builder.port;
        localPoW = builder.localPoW;
        postConstruct();
    }

    protected static <T> Response<T> wrapCheckedException(final Call<T> call) throws ArgumentException {
        try {
            final Response<T> res = call.execute();

            String error = "";

            if (res.errorBody() != null) {
                error = res.errorBody().string();
            }

            if (res.code() == 400) {
                throw new ArgumentException(error);

            } else if (res.code() == 401) {
                throw new IllegalAccessError("401 " + error);
            } else if (res.code() == 500) {
                throw new IllegalAccessError("500 " + error);
            }
            return res;
        } catch (IOException e) {
            log.error("Execution of the API call raised exception. CVT Node not reachable?", e);
            throw new IllegalStateException(e.getMessage());
        }

    }

    private static String env(String env, String def) {
        final String value = System.getenv(env);
        if (value == null) {
            log.warn("Environment variable '{}' is not defined, and actual value has not been specified. "
                    + "Rolling back to default value: '{}'", env, def);
            return def;
        }
        return value;
    }

    /**
     * added header for IRI
     */
    private void postConstruct() {

        final String nodeUrl = protocol + "://" + host + ":" + port;
        log.info("NODE URL：{}", nodeUrl);

        // Create OkHttpBuilder
        final OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5000, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Request newRequest;

                        newRequest = request.newBuilder()
                                .addHeader(X_CVT_API_VERSION_HEADER_NAME, X_CVT_API_VERSION_HEADER_VALUE)
                                .build();

                        return chain.proceed(newRequest);
                    }
                })
                .connectTimeout(5000, TimeUnit.SECONDS)
                .build();

        // use client to create Retrofit service
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(nodeUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(CvtAPIService.class);

        log.debug("Jota-API Java proxy pointing to node url: '{}'", nodeUrl);
    }

    /**
     * Get the node information.
     *
     * @return The information about the node.
     * @throws ArgumentException
     */
    public GetNodeInfoResponse getNodeInfo() throws ArgumentException {
        final Call<GetNodeInfoResponse> res = service.getNodeInfo(CvtCommandRequest.createNodeInfoRequest());
        return wrapCheckedException(res).body();
    }
    /**
     * Get the list of neighbors from the node.
     *
     * @return The set of neighbors the node is connected with.
     * @throws ArgumentException
     */
    public GetNeighborsResponse getNeighbors() throws ArgumentException {
        final Call<GetNeighborsResponse> res = service.getNeighbors(CvtCommandRequest.createGetNeighborsRequest());
        return wrapCheckedException(res).body();
    }

    /**
     * Add a list of neighbors to the node.
     *
     * @param uris The list of URI elements.
     * @throws ArgumentException
     */
    public AddNeighborsResponse addNeighbors(String... uris) throws ArgumentException {
        final Call<AddNeighborsResponse> res = service.addNeighbors(CvtNeighborsRequest.createAddNeighborsRequest(uris));
        return wrapCheckedException(res).body();
    }
    /**
     * Removes a list of neighbors from the node.
     *
     * @param uris The list of URI elements.
     * @throws ArgumentException
     */
    public RemoveNeighborsResponse removeNeighbors(String... uris) throws ArgumentException {
        final Call<RemoveNeighborsResponse> res = service.removeNeighbors(CvtNeighborsRequest.createRemoveNeighborsRequest(uris));
        return wrapCheckedException(res).body();
    }

    /**
     * Get the list of latest tips (unconfirmed transactions).
     *
     * @return The the list of tips.
     * @throws ArgumentException
     */
    public GetTipsResponse getTips() throws ArgumentException {
        final Call<GetTipsResponse> res = service.getTips(CvtCommandRequest.createGetTipsRequest());
        return wrapCheckedException(res).body();
    }

}