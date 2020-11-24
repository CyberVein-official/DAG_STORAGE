package com.cvt.iri.retrofit2;

/**
 * IotaVerifyStorageRequest
 *
 * @author admin
 *         Time: 2018-12-05 2:30
 */
public class IotaVerifyStorageRequest extends IotaCommandRequest {

    private String filePath;

    protected IotaVerifyStorageRequest(String filePath) {
        super(IotaAPICommands.VERIFY_STORAGE);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
