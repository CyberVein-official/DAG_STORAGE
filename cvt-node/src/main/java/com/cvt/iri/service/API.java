package com.cvt.iri.service;

import com.cvt.iri.pdp.MockPdpService;
import com.cvt.iri.pdp.PdpService;
import com.cvt.iri.retrofit2.FileHashResponse;
import com.cvt.iri.retrofit2.VerifyStorageResponse;
import com.cvt.iri.service.dto.*;
import com.cvt.iri.storage.sqllite.SqliteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.undertow.Handlers.path;

@SuppressWarnings("unchecked")
public class API {

    public static final String REFERENCE_TRANSACTION_NOT_FOUND = "reference transaction not found";
    public static final String REFERENCE_TRANSACTION_TOO_OLD = "reference transaction is too old";
    private static final Logger log = LoggerFactory.getLogger(API.class);
    private final IXI ixi;


}
