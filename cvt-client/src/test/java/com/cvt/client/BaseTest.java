package com.cvt.client;

import com.cvt.client.core.MilestoneManager;
import com.cvt.client.core.SeedBalanceLoader;
import jota.CvtAPI;
import jota.dto.response.GetAccountDataResponse;
import jota.dto.response.GetBalancesAndFormatResponse;
import jota.dto.response.GetNewAddressResponse;
import jota.dto.response.SendTransferResponse;
import jota.model.Transfer;
import jota.utils.TrytesConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 测试基类
 *
 * @author cvt admin
 * Time: 2018/11/10 : 11:27
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class BaseTest {

    public static final String SUPER_SEED = "SEED99999999999999999999999999999999999999999999999999999999999999999999999999999";

    @Autowired
    CvtAPI cvtAPI;

    @Autowired
    protected SeedBalanceLoader seedBalanceLoader;

    @Autowired
    protected MilestoneManager milestoneManager;

    String testTag = "CVTJAVASPAM999999999999999";
    String testMessage = "JUSTANOTHERCVTTEST";

    int SECURITY_LEVEL = 2;
    int DEPTH = 9;
    int MIN_WEIGHT = 14;


    protected GetBalancesAndFormatResponse loadBalance(String seed) throws Exception {
        return cvtAPI.getInputs(seed, SECURITY_LEVEL, 0, 0, 0);
    }




}
