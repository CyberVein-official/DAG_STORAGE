package com.cvt.iri.coinbase;

/**
 * CoinBaseService
 *
 * @author admin
 * Time:
 */
public interface CoinBaseService {

    /**
     * 根据节点名称获取其 getCoinbase 的地址
     */
    String getCoinBaseAddress();

    /**
     * 根据文件大小计算奖励
     *
     * @param filePath 文件路径
     * @return 奖励
     */
    long calculateRewardAmount(String filePath);
}
