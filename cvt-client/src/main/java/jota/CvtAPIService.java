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


}
