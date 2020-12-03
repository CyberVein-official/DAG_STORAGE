package com.cvt.iri.retrofit2;

import com.cvt.iri.service.dto.AbstractResponse;

public class FileHashResponse extends AbstractResponse {

    private String fileHashStr;

    public static AbstractResponse create(String fileHashStr) {
        FileHashResponse res = new FileHashResponse();
        res.fileHashStr = fileHashStr;
        return res;
    }

    public String getFileHashStr() {
        return fileHashStr;
    }
}
