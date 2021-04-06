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
 *
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

    GetAccountDataResponse getAccountDataResponse(String seed, int threshold) throws Exception {
        return cvtAPI.getAccountData(seed, SECURITY_LEVEL, 0, false, 0, true, 0, 0, true, threshold);
    }

    protected String rndSeed(String... exceptSeeds) {
        Set<String> seedSet = seedBalanceLoader.getSeeds();
        if (null != exceptSeeds) {
            seedSet.removeAll(Sets.newLinkedHashSet(exceptSeeds));
        }
        return Lists.newArrayList(seedSet).get(new Random().nextInt(seedSet.size()));
    }

    protected SendTransferResponse doTransfer(String seed, List<Transfer> transfer, String addressRemainder) throws Exception {
        return cvtAPI.sendTransfer(seed, SECURITY_LEVEL, DEPTH, MIN_WEIGHT,
                transfer, null, addressRemainder, false, false, null);
    }

    protected void do_random_transfer_times(int times, String... exceptSeeds) throws Exception {
        int count = 1;
        while (count++ <= times) {
            do_random_transfer(exceptSeeds);
            TimeUnit.SECONDS.sleep(5);
            log.info(StringUtils.center(" Transfer Done [ " + count + " ]Times ", 120, "="));
        }
    }

    protected void do_random_transfer(String... exceptSeeds) throws Exception {

        // seed1 向 seed2 转账
        String rndSeed1;
        String rndSeed2;
        GetBalancesAndFormatResponse gbr1_before;

        do {
            rndSeed1 = rndSeed(exceptSeeds);

            // 校验余额是否大于 0
            gbr1_before = loadBalance(rndSeed1);
            if (gbr1_before.getTotalBalance() <= 0) {
                log.info("NO Enough Balance for Seed {}", rndSeed1);
            }
            while (SeedBalanceLoader.LAST_SEED.equals(rndSeed1) || gbr1_before.getTotalBalance() <= 0);

            do {
                rndSeed2 = rndSeed(exceptSeeds);
            } while (rndSeed1.equals(rndSeed2) || SeedBalanceLoader.LAST_SEED.equals(rndSeed2));

            // 每次都根据 SEED2 生成新的地址
            GetNewAddressResponse addressResponse = cvtAPI.generateNewAddresses(rndSeed2, SECURITY_LEVEL, false, 1);
            String newSeed2Address = addressResponse.getAddresses().get(0);

            // 随机生成一个金额（小于 SEED1 的余额）
            int amount = new Random().nextInt(Long.valueOf(gbr1_before.getTotalBalance()).intValue());


            Transfer transfer = new Transfer(newSeed2Address, amount, TrytesConverter.asciiToTrytes(":Hello" + amount +":"), testTag);
            SendTransferResponse response = doTransfer(rndSeed1,
                    Collections.singletonList(transfer), null);
    }


}
