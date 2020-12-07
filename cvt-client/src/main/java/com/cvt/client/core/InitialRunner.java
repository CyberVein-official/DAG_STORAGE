package com.cvt.client.core;

import jota.CvtAPI;
import jota.model.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.cvt.client.core.CvtConfig.*;

/**
 * 每次启动的时候：
 * 1. 生成一个 Milestone
 * 2. Attach 所有 Snapshot 地址到 Tangle
 *
 * @author cvt admin
 *         Time:
 */
@Slf4j
@Component
public class InitialRunner implements CommandLineRunner {

    @Autowired
    private CvtConfig config;

    @Autowired
    private CvtAPI cvtAPI;

    @Autowired
    private SeedBalanceLoader seedBalanceLoader;

    @Autowired
    protected MilestoneManager milestoneManager;


//
    @Override
    public void run(String... args) throws Exception {
//        log.info(StringUtils.center(" Init Data... ", 120, "="));
//
//        // create a milestone
//        milestoneManager.newMileStone(null);
//
//        log.info(StringUtils.center("Attach Address...", 120, "="));
//        // attach address
//        SeedBalance seedBalance = seedBalanceLoader.getSeedBalanceMap().values().iterator().next();
//        if (null != seedBalance.getAddressBalanceList()) {
//            String oneAddress = seedBalance.getAddressBalanceList().get(0).getAddress();
//            List<Transaction> transactionList = cvtAPI.findTransactionObjectsByAddresses(new String[] {oneAddress});
//            if(null == transactionList || transactionList.isEmpty()) {
//                TimeUnit.SECONDS.sleep(5);
//                attachAllAddressToTangle();
//            }
//        }
//
//        log.info(StringUtils.center("Init Done", 120, "="));
    }

    public void attachAllAddressToTangle() {
        seedBalanceLoader.getSeedBalanceMap().values().forEach(item -> {
            item.getAddressBalanceList().forEach(it -> {
                Transfer transfer = new Transfer(it.getAddress(), 0, config.getTestMessage(), config.getTestTag());
                try {
                    cvtAPI.sendTransfer(seedBalanceLoader.rndSeed(), SECURITY_LEVEL, DEPTH, MIN_WEIGHT,
                            Collections.singletonList(transfer), null, null, false, false, null);
                    log.info("Attach Address: " + it.getAddress() + " Successful");
                } catch (Exception e) {
                    log.error("Attach Address Error : ", e);
                }
            });
        });
    }
}
