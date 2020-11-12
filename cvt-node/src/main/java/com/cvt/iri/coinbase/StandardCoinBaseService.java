package com.cvt.iri.coinbase;

import com.cvt.iri.conf.IotaConfig;

/**
 * CoinBaseService
 *
 * @author admin
 * Time:
 */
public class StandardCoinBaseService implements CoinBaseService {

    private IotaConfig iotaConfig;

    public StandardCoinBaseService(IotaConfig iotaConfig) {
        this.iotaConfig = iotaConfig;
    }

    @Override
    public String getCoinBaseAddress() {
        return iotaConfig.getCoinbase();
    }

    @Override
    public long calculateRewardAmount(String filePath) {
        return 0;
    }
}
