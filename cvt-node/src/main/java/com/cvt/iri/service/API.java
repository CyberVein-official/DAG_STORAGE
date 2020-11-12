package com.cvt.iri.service;

import com.cvt.iri.pdp.MockPdpService;
import com.cvt.iri.pdp.PdpService;
import com.cvt.iri.retrofit2.FileHashResponse;
import com.cvt.iri.retrofit2.VerifyStorageResponse;
import com.cvt.iri.service.dto.*;
import com.cvt.iri.storage.sqllite.SqliteHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static io.undertow.Handlers.path;

@SuppressWarnings("unchecked")
public class API {

    public static final String REFERENCE_TRANSACTION_NOT_FOUND = "reference transaction not found";
    public static final String REFERENCE_TRANSACTION_TOO_OLD = "reference transaction is too old";
    private static final Logger log = LoggerFactory.getLogger(API.class);
    private final IXI ixi;


    private Undertow server;

    private final Gson gson = new GsonBuilder().create();
    private volatile PearlDiver pearlDiver = new PearlDiver();

    private final AtomicInteger counter = new AtomicInteger(0);

    private Pattern trytesPattern = Pattern.compile("[9A-Z]*");

    private final static int HASH_SIZE = 81;
    private final static int TRYTES_SIZE = 2673;

    private final static long MAX_TIMESTAMP_VALUE = (long) (Math.pow(3, 27) - 1) / 2; // max positive 27-trits value

    private final int maxFindTxs;
    private final int maxRequestList;
    private final int maxGetTrytes;
    private final int maxBodyLength;
    private final boolean testNet;


    private final static String overMaxErrorMessage = "Could not complete request";
    private final static String invalidParams = "Invalid parameters";


}
