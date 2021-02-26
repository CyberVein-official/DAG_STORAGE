package com.cvt.client;

import com.google.gson.Gson;
import jota.dto.response.GetNewAddressResponse;
import jota.model.Transfer;
import jota.utils.TrytesConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Collections;

/**
 * 创建新地址的用例
 *
 * @author cvt admin
 * Time: 2018/11/10 : 11:26
 */
@Slf4j
public class AddressTest extends BaseTest {

    @Test
    public void test_gen_address() throws Exception {
        GetNewAddressResponse response = cvtAPI.getNewAddress(rndSeed(), 2, 0, false, 0, true);
        log.info(new Gson().toJson(response));
    }

    @Test
    public void test_gen_uncheck_address() throws Exception {
        String seed = "SEED99999999999999999999999999999999999999999999999999999999999999999999999999999";
        GetNewAddressResponse response = cvtAPI.getAddressesUnchecked(seed, 2, false, 0, 10);
        log.info(new Gson().toJson(response));
    }

    @Test
    public void attach_all_address() {
        seedBalanceLoader.getSeedBalanceMap().values().forEach(item -> {
            item.getAddressBalanceList().forEach(it -> {
                int count = 0;
                Transfer transfer = new Transfer(it.getAddress(), 0, TrytesConverter.asciiToTrytes("Address-" + (count++)), testTag);
                try {
                    doTransfer(rndSeed(), Collections.singletonList(transfer), null);
                    log.info("初始化地址: " + it.getAddress() + " 成功");
                } catch (Exception e) {
                    log.error("初始化地址错误", e);
                }
            });
        });
        log.info(StringUtils.center("初步化地址完成", 120, "="));
    }

}
