package com.cvt.client.core;

import jota.CvtAPI;
import jota.dto.response.GetAttachToTangleResponse;
import jota.dto.response.GetNodeInfoResponse;
import jota.dto.response.GetTransactionsToApproveResponse;
import jota.model.Bundle;
import jota.model.Transaction;
import jota.utils.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MilestoneManager
 *
 * @author cvt admin
 * Time: 2018/11/19 : 16:59
 */
@Slf4j
@Component
public class MilestoneManager {

    public static final String NULL_HASH = "999999999999999999999999999999999999999999999999999999999999999999999999999999999";
    public static final String TESTNET_COORDINATOR_ADDRESS = "EQQFCZBIHRHWPXKMTOLMYUYPCN9XLMJPYZVFJSAY9FQHCCLWTOLLUGKKMXYFDBOOYFBLBI9WUEILGECYM";
    public static final String NULL_ADDRESS = "999999999999999999999999999999999999999999999999999999999999999999999999999999999";
    public static final int TAG_TRINARY_SIZE = 81;

    public boolean HEARTBEAT = true;

    @Autowired
    private CvtAPI api;

    public void newMileStone(String tip2) throws Exception {
        GetNodeInfoResponse nodeInfo = api.getNodeInfo();
        int milestone = nodeInfo.getLatestMilestoneIndex();
        if (nodeInfo.getLatestMilestone().equals(NULL_HASH)) {
            // As of 1.4.2.4, at least two milestones are required so that the latest solid subtangle milestone gets updated.
            newMilestone(api, NULL_HASH, NULL_HASH, milestone + 1);
            newMilestone(api, NULL_HASH, NULL_HASH, milestone + 2);
        } else if (nodeInfo.getLatestSolidSubtangleMilestone().equals(NULL_HASH)) {
            newMilestone(api, NULL_HASH, NULL_HASH, milestone + 1);
        } else {
            GetTransactionsToApproveResponse x = api.getTransactionsToApprove(10);
            String secondTransaction = StringUtils.defaultIfBlank(tip2, x.getBranchTransaction());
            newMilestone(api, x.getTrunkTransaction(), secondTransaction, milestone + 1);
        }
        log.info(StringUtils.center("New milestone created.", 120, "="));
    }


}
