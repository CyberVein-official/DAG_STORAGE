package com.cvt.iri.pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MockPdpService
 *
 * @author admin
 *
 */
public class MockPdpService implements PdpService {

    private Logger logger = LoggerFactory.getLogger(PdpService.class);

    @Override
    public void storeFile(String filePath) throws PdpException {
        logger.info("存储文件：{}", filePath);
    }

    @Override
    public void challenge(String filePath) throws PdpException {
        logger.info("向存储文件发起挑战：{}", filePath);
    }

    @Override
    public void prove(String filePath) throws PdpException {
        logger.info("证明存储文件成功：{}", filePath);
    }
}
