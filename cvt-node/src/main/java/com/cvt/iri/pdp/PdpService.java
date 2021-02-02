package com.cvt.iri.pdp;

/**
 * 存储证明服务
 *
 * @author admin
 * Time: 2018-12-08 16:25
 */
public interface PdpService {

    String VERIFY_STORE_COMMAND = "verifyStorage";

    /**
     * 存储文件
     *
     * @param filePath 存储的文件路径
     */
    void storeFile(String filePath) throws PdpException;

    /**
     * 发起证明挑战
     *
     * @param filePath 存储的文件路径
     */

    void challenge(String filePath) throws PdpException;

    /**
     * 进行存储证明
     *
     * @param filePath 存储的文件路径
     * throws PdpException
     */
    void prove(String filePath) throws PdpException;

}
