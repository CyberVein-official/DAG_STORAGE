package com.cvt.client;

import com.google.gson.Gson;
import jota.dto.response.GetBalancesAndFormatResponse;
import jota.dto.response.GetBalancesResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * 余额的用例
 *
 * @author cvt admin
 * Time: 2018/11/10 : 11:26
 */
@Slf4j
public class BalanceTest extends BaseTest {

    @Test
    public void get_balance() throws Exception {

        GetBalancesAndFormatResponse response = loadBalance(SUPER_SEED);
        log.info(new Gson().toJson(response));
    }



}
