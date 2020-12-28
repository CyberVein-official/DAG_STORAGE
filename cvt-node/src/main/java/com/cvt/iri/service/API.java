package com.cvt.iri.service;

import com.alibaba.fastjson.JSON;
import com.cvt.iri.coinbase.CoinBaseService;
import com.cvt.iri.coinbase.StandardCoinBaseService;
import com.cvt.iri.conf.APIConfig;
import com.cvt.iri.controllers.AddressViewModel;
import com.cvt.iri.controllers.TagViewModel;
import com.cvt.iri.controllers.TransactionViewModel;
import com.cvt.iri.model.Hash;
import com.cvt.iri.pdp.MockPdpService;
import com.cvt.iri.retrofit2.FileHashResponse;
import com.cvt.iri.retrofit2.VerifyStorageResponse;
import com.cvt.iri.service.dto.*;
import com.cvt.iri.service.tipselection.impl.WalkValidatorImpl;
import com.cvt.iri.storage.CvtPersistable;
import com.cvt.iri.storage.sqllite.SqliteHelper;
import com.cvt.iri.utils.Converter;
import com.cvt.iri.utils.IotaIOUtils;
import com.cvt.iri.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.streams.ChannelInputStream;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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


    private ConcurrentHashMap<Hash, Boolean> previousEpochsSpentAddresses;

    private final static char ZERO_LENGTH_ALLOWED = 'Y';
    private final static char ZERO_LENGTH_NOT_ALLOWED = 'N';
    private Cvt instance;

    private PdpService pdpService;
    private CoinBaseService coinBaseService;

    public API(Cvt instance, IXI ixi) {
        this.instance = instance;
        this.ixi = ixi;
        APIConfig configuration = instance.configuration;
        maxFindTxs = configuration.getMaxFindTransactions();
        maxRequestList = configuration.getMaxRequestsList();
        maxGetTrytes = configuration.getMaxGetTrytes();
        maxBodyLength = configuration.getMaxBodyLength();
        testNet = configuration.isTestnet();
        milestoneStartIndex = ((ConsensusConfig) configuration).getMilestoneStartIndex();

        previousEpochsSpentAddresses = new ConcurrentHashMap<>();

        // TODO 切换 PDP 服务实现
        pdpService = new MockPdpService();
//        pdpService = new LibPdpServiceImpl("/root/git/libpdp/libpdp/key.path");
        // coinbase 服务
        coinBaseService = new StandardCoinBaseService(instance.configuration);
    }


    public void init() throws IOException {
        readPreviousEpochsSpentAddresses(testNet);

        APIConfig configuration = instance.configuration;
        final int apiPort = configuration.getPort();
        final String apiHost = configuration.getApiHost();

        log.debug("Binding JSON-REST API Undertow server on {}:{}", apiHost, apiPort);

        server = Undertow.builder().addHttpListener(apiPort, apiHost)
                .setHandler(path().addPrefixPath("/", addSecurity(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        HttpString requestMethod = exchange.getRequestMethod();
                        if (Methods.OPTIONS.equals(requestMethod)) {
                            String allowedMethods = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,CONNECT,PATCH";
                            //return list of allowed methods in response headers
                            exchange.setStatusCode(StatusCodes.OK);
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MimeMappings.DEFAULT_MIME_MAPPINGS.get("txt"));
                            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, 0);
                            exchange.getResponseHeaders().put(Headers.ALLOW, allowedMethods);
                            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
                            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "Origin, X-Requested-With, Content-Type, Accept, X-CVT-API-Version");
                            exchange.getResponseSender().close();
                            return;
                        }

                        if (exchange.isInIoThread()) {
                            exchange.dispatch(this);
                            return;
                        }
                        processRequest(exchange);
                    }
                })).addExactPath("/verify", new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        HttpString requestMethod = exchange.getRequestMethod();
                        if (Methods.OPTIONS.equals(requestMethod)) {
                            String allowedMethods = "GET,HEAD,POST,PUT,DELETE,TRACE,OPTIONS,CONNECT,PATCH";
                            //return list of allowed methods in response headers
                            exchange.setStatusCode(StatusCodes.OK);
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MimeMappings.DEFAULT_MIME_MAPPINGS.get("txt"));
                            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, 0);
                            exchange.getResponseHeaders().put(Headers.ALLOW, allowedMethods);
                            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
                            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "Origin, X-Requested-With, Content-Type, Accept, X-CVT-API-Version");
                            exchange.getResponseSender().close();
                            return;
                        }
                        handleVerify(exchange);
                    }
                }))
                .build();
        server.start();
    }


    private void readPreviousEpochsSpentAddresses(boolean isTestnet) throws IOException {
        if (isTestnet) {
            return;
        }

        String[] previousEpochsSpentAddressesFiles = instance
                .configuration
                .getPreviousEpochSpentAddressesFiles()
                .split(" ");
        for (String previousEpochsSpentAddressesFile : previousEpochsSpentAddressesFiles) {
            InputStream in = Snapshot.class.getResourceAsStream(previousEpochsSpentAddressesFile);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    this.previousEpochsSpentAddresses.put(new Hash(line), true);
                }
            } catch (Exception e) {
                log.error("Failed to load resource: {}.", previousEpochsSpentAddressesFile, e);
            }
        }
    }

    private void processRequest(final HttpServerExchange exchange) throws IOException {
        final ChannelInputStream cis = new ChannelInputStream(exchange.getRequestChannel());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        final long beginningTime = System.currentTimeMillis();
        final String body = IotaIOUtils.toString(cis, StandardCharsets.UTF_8);
        final AbstractResponse response;

//        if (!exchange.getRequestHeaders().contains("X-CVT-API-Version")) {
//            response = ErrorResponse.create("Invalid API Version");
//        } else if (body.length() > maxBodyLength) {
//            response = ErrorResponse.create("Request too long");
//        } else {
//            response = process(body, exchange.getSourceAddress());
//        }
        response = process(body, exchange.getSourceAddress());
        sendResponse(exchange, response, beginningTime);
    }

    // 处理验证或者读取文件 HASH 值（针对不同的角色进行不同的处理）
    private void handleVerify(final HttpServerExchange exchange) throws Exception {
        if (!exchange.getRequestPath().startsWith("/verify")) {
            return;
        }
        final ChannelInputStream cis = new ChannelInputStream(exchange.getRequestChannel());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");


        final long beginningTime = System.currentTimeMillis();

        String paramStr = URLDecoder.decode(IOUtils.toString(cis, StandardCharsets.UTF_8), StandardCharsets.UTF_8.name());
        Map<String, String> queryMap = UrlUtils.query2Map(paramStr);
        String storeNode = queryMap.get("storeNode");
        String filePath = queryMap.get("filePath");
        String method = queryMap.get("method");
        AbstractResponse response = null;

        if ("verify".equals(method)) {
            if (!doVerify(storeNode, filePath)) {
                response = ExceptionResponse.create("存储验证失败");
            } else {
                response = VerifyStorageResponse.create(true);
            }
        } else if ("fileHash".equals(method)) {
            response = FileHashResponse.create(JSON.toJSONString(handleHash(filePath)));
        }
        sendResponse(exchange, response, beginningTime);
    }

    private AbstractResponse process(final String requestString, InetSocketAddress sourceAddress) throws UnsupportedEncodingException {

        try {

            final Map<String, Object> request = gson.fromJson(requestString, Map.class);
            if (request == null) {
                return ExceptionResponse.create("Invalid request payload: '" + requestString + "'");
            }

            final String command = (String) request.get("command");
            if (command == null) {
                return ErrorResponse.create("COMMAND parameter has not been specified in the request.");
            }

            if (instance.configuration.getRemoteLimitApi().contains(command) &&
                    !sourceAddress.getAddress().isLoopbackAddress()) {
                return AccessLimitedResponse.create("COMMAND " + command + " is not available on this node");
            }

            log.debug("# {} -> Requesting command '{}'", counter.incrementAndGet(), command);

            switch (command) {
                case PdpService.VERIFY_STORE_COMMAND: {
                    final String filePath = (String) request.get("filePath");
                    log.info("需要验证的文件路径为：{}", filePath);
                    //pdpService.challenge(filePath);
                    pdpService.prove(filePath);
                    return VerifyStorageResponse.create(true);
                }
                case "storeMessage": {
                    if (!testNet) {
                        return AccessLimitedResponse.create("COMMAND storeMessage is only available on testnet");
                    }

                    if (!request.containsKey("address") || !request.containsKey("message")) {
                        return ErrorResponse.create("Invalid params");
                    }

                    if (invalidSubtangleStatus()) {
                        return ErrorResponse
                                .create("This operation cannot be executed: The subtangle has not been updated yet.");
                    }

                    final String address = (String) request.get("address");
                    final String message = (String) request.get("message");

                    storeMessageStatement(address, message);
                    return AbstractResponse.createEmptyResponse();
                }

                case "addNeighbors": {
                    List<String> uris = getParameterAsList(request, "uris", 0);
                    log.debug("Invoking 'addNeighbors' with {}", uris);
                    return addNeighborsStatement(uris);
                }
                case "attachToTangle": {
                    // 在这里生成账号地址信息

                    final Hash trunkTransaction = new Hash(getParameterAsStringAndValidate(request, "trunkTransaction", HASH_SIZE));
                    final Hash branchTransaction = new Hash(getParameterAsStringAndValidate(request, "branchTransaction", HASH_SIZE));
                    final int minWeightMagnitude = getParameterAsInt(request, "minWeightMagnitude");

                    final List<String> trytes = getParameterAsList(request, "trytes", TRYTES_SIZE);

                    List<String> elements = attachToTangleStatement(trunkTransaction, branchTransaction, minWeightMagnitude, trytes);
                    return AttachToTangleResponse.create(elements);
                }
                case "broadcastTransactions": {
                    // 向其它节点发起广播，告知交易信息

                    final List<String> trytes = getParameterAsList(request, "trytes", TRYTES_SIZE);
                    broadcastTransactionsStatement(trytes);
                    return AbstractResponse.createEmptyResponse();
                }
                case "findTransactions": {
                    return findTransactionsStatement(request);
                }
                case "attachNewAddress": {
                    // 生成地址
                    String seed = (String) request.get("seed");
                    String addresses = (String) request.get("address");
                    return NewAddressResponse.create(addresses);
                }
                case "getBalances": {
                    // 获取余额

                    final List<String> addresses = getParameterAsList(request, "addresses", HASH_SIZE);
                    final List<String> tips = request.containsKey("tips") ?
                            getParameterAsList(request, "tips", HASH_SIZE) :
                            null;
                    final int threshold = getParameterAsInt(request, "threshold");
                    return getBalancesStatement(addresses, tips, threshold);
                }
                case "getInclusionStates": {
                    if (invalidSubtangleStatus()) {
                        return ErrorResponse
                                .create("This operation cannot be executed: The subtangle has not been updated yet.");
                    }
                    final List<String> transactions = getParameterAsList(request, "transactions", HASH_SIZE);
                    final List<String> tips = getParameterAsList(request, "tips", HASH_SIZE);

                    return getInclusionStatesStatement(transactions, tips);
                }
                case "getNeighbors": {
                    return getNeighborsStatement();
                }
                case "getNodeInfo": {
                    return getNodeInfoStatement();
                }
                case "getTips": {
                    return getTipsStatement();
                }
                case "getTransactionsToApprove": {
                    final Optional<Hash> reference = request.containsKey("reference") ?
                            Optional.of(new Hash(getParameterAsStringAndValidate(request, "reference", HASH_SIZE)))
                            : Optional.empty();
                    final int depth = getParameterAsInt(request, "depth");
                    if (depth < 0 || depth > instance.configuration.getMaxDepth()) {
                        return ErrorResponse.create("Invalid depth input");
                    }

                    try {
                        List<Hash> tips = getTransactionsToApproveStatement(depth, reference);
                        return GetTransactionsToApproveResponse.create(tips.get(0), tips.get(1));

                    } catch (RuntimeException e) {
                        log.info("Tip selection failed: " + e.getLocalizedMessage());
                        return ErrorResponse.create(e.getLocalizedMessage());
                    }
                }
                case "getTrytes": {
                    final List<String> hashes = getParameterAsList(request, "hashes", HASH_SIZE);
                    return getTrytesStatement(hashes);
                }

                case "interruptAttachingToTangle": {
                    return interruptAttachingToTangleStatement();
                }
                case "removeNeighbors": {
                    List<String> uris = getParameterAsList(request, "uris", 0);
                    log.debug("Invoking 'removeNeighbors' with {}", uris);
                    return removeNeighborsStatement(uris);
                }

                case "storeTransactions": {
                    // 保存交易信息
                    try {
                        final List<String> trytes = getParameterAsList(request, "trytes", TRYTES_SIZE);
                        storeTransactionsStatement(trytes);
                        return AbstractResponse.createEmptyResponse();
                    } catch (Exception e) {
                        //transaction not valid
                        log.info("error while storing transaction", e);
                        return ErrorResponse.create("Invalid trytes input");
                    }
                }
                case "getMissingTransactions": {
                    //TransactionRequester.instance().rescanTransactionsToRequest();
                    synchronized (instance.transactionRequester) {
                        List<String> missingTx = Arrays.stream(instance.transactionRequester.getRequestedTransactions())
                                .map(Hash::toString)
                                .collect(Collectors.toList());
                        return GetTipsResponse.create(missingTx);
                    }
                }
                case "checkConsistency": {
                    if (invalidSubtangleStatus()) {
                        return ErrorResponse
                                .create("This operation cannot be executed: The subtangle has not been updated yet.");
                    }
                    final List<String> transactions = getParameterAsList(request, "tails", HASH_SIZE);
                    return checkConsistencyStatement(transactions);
                }
                case "wereAddressesSpentFrom": {
                    final List<String> addresses = getParameterAsList(request, "addresses", HASH_SIZE);
                    return wereAddressesSpentFromStatement(addresses);
                }
                default: {
                    AbstractResponse response = ixi.processCommand(command, request);
                    return response == null ?
                            ErrorResponse.create("Command [" + command + "] is unknown") :
                            response;
                }
            }

        } catch (final ValidationException e) {
            log.info("API Validation failed: " + e.getLocalizedMessage());
            return ErrorResponse.create(e.getLocalizedMessage());
        } catch (final InvalidAlgorithmParameterException e) {
            log.info("API InvalidAlgorithmParameter passed: " + e.getLocalizedMessage());
            return ErrorResponse.create(e.getLocalizedMessage());
        } catch (final Exception e) {
            log.error("API Exception: {}", e.getLocalizedMessage(), e);
            return ExceptionResponse.create(e.getLocalizedMessage());
        }
    }
    /**
     * Check if a list of addresses was ever spent from, in the current epoch, or in previous epochs.
     *
     * @param addresses List of addresses to check if they were ever spent from.
     * @return {@link com.cvt.iri.service.dto.wereAddressesSpentFrom}
     **/
    private AbstractResponse wereAddressesSpentFromStatement(List<String> addresses) throws Exception {
        final List<Hash> addressesHash = addresses.stream().map(Hash::new).collect(Collectors.toList());
        final boolean[] states = new boolean[addressesHash.size()];
        int index = 0;

        for (Hash address : addressesHash) {
            states[index++] = wasAddressSpentFrom(address);
        }
        return wereAddressesSpentFrom.create(states);
    }

    private boolean wasAddressSpentFrom(Hash address) throws Exception {
        if (previousEpochsSpentAddresses.containsKey(address)) {
            return true;
        }
        Set<Hash> hashes = AddressViewModel.load(instance.tangle, address).getHashes();
        for (Hash hash : hashes) {
            final TransactionViewModel tx = TransactionViewModel.fromHash(instance.tangle, hash);
            //spend
            if (tx.value() < 0) {
                //confirmed
                if (tx.snapshotIndex() != 0) {
                    return true;
                }
                //pending
                Hash tail = findTail(hash);
                if (tail != null && BundleValidator.validate(instance.tangle, tail).size() != 0) {
                    return true;
                }
            }
        }
        return false;
    }
    private Hash findTail(Hash hash) throws Exception {
        TransactionViewModel tx = TransactionViewModel.fromHash(instance.tangle, hash);
        final Hash bundleHash = tx.getBundleHash();
        long index = tx.getCurrentIndex();
        boolean foundApprovee = false;
        while (index-- > 0 && tx.getBundleHash().equals(bundleHash)) {
            Set<Hash> approvees = tx.getApprovers(instance.tangle).getHashes();
            for (Hash approvee : approvees) {
                TransactionViewModel nextTx = TransactionViewModel.fromHash(instance.tangle, approvee);
                if (nextTx.getBundleHash().equals(bundleHash)) {
                    tx = nextTx;
                    foundApprovee = true;
                    break;
                }
            }
            if (!foundApprovee) {
                break;
            }
        }
        if (tx.getCurrentIndex() == 0) {
            return tx.getHash();
        }
        return null;
    }

    /**
     * Checks the consistency of the transactions.
     * Marks state as false on the following checks<br/>
     * - Transaction does not exist<br/>
     * - Transaction is not a tail<br/>
     * - Missing a reference transaction<br/>
     * - Invalid bundle<br/>
     * - Tails of tails are invalid<br/>
     *
     * @param tails List of transactions you want to check the consistency for
     * @return {@link com.cvt.iri.service.dto.CheckConsistency}
     **/
    private AbstractResponse checkConsistencyStatement(List<String> tails) throws Exception {
        final List<Hash> transactions = tails.stream().map(Hash::new).collect(Collectors.toList());
        boolean state = true;
        String info = "";

        //check transactions themselves are valid
        for (Hash transaction : transactions) {
            TransactionViewModel txVM = TransactionViewModel.fromHash(instance.tangle, transaction);
            if (txVM.getType() == TransactionViewModel.PREFILLED_SLOT) {
                return ErrorResponse.create("Invalid transaction, missing: " + transaction);
            }
            if (txVM.getCurrentIndex() != 0) {
                return ErrorResponse.create("Invalid transaction, not a tail: " + transaction);
            }


            if (!txVM.isSolid()) {
                state = false;
                info = "tails are not solid (missing a referenced tx): " + transaction;
                break;
            } else if (BundleValidator.validate(instance.tangle, txVM.getHash()).size() == 0) {
                state = false;
                info = "tails are not consistent (bundle is invalid): " + transaction;
                break;
            }
        }

        if (state) {
            instance.milestone.latestSnapshot.rwlock.readLock().lock();
            try {
                WalkValidatorImpl walkValidator = new WalkValidatorImpl(instance.tangle, instance.ledgerValidator,
                        instance.milestone, instance.configuration);
                for (Hash transaction : transactions) {
                    if (!walkValidator.isValid(transaction)) {
                        state = false;
                        info = "tails are not consistent (would lead to inconsistent ledger state or below max depth)";
                        break;
                    }
                }
            } finally {
                instance.milestone.latestSnapshot.rwlock.readLock().unlock();
            }
        }

        return CheckConsistency.create(state, info);
    }

    private double getParameterAsDouble(Map<String, Object> request, String paramName) throws ValidationException {
        validateParamExists(request, paramName);
        final double result;
        try {
            result = ((Double) request.get(paramName));
        } catch (ClassCastException e) {
            throw new ValidationException("Invalid " + paramName + " input");
        }
        return result;
    }

    private int getParameterAsInt(Map<String, Object> request, String paramName) throws ValidationException {
        validateParamExists(request, paramName);
        final int result;
        try {
            result = ((Double) request.get(paramName)).intValue();
        } catch (ClassCastException e) {
            throw new ValidationException("Invalid " + paramName + " input");
        }
        return result;
    }
    private String getParameterAsStringAndValidate(Map<String, Object> request, String paramName, int size) throws ValidationException {
        validateParamExists(request, paramName);
        String result = (String) request.get(paramName);
        validateTrytes(paramName, size, result);
        return result;
    }

    private void validateTrytes(String paramName, int size, String result) throws ValidationException {
        if (!validTrytes(result, size, ZERO_LENGTH_NOT_ALLOWED)) {
            throw new ValidationException("Invalid " + paramName + " input");
        }
    }

    private void validateParamExists(Map<String, Object> request, String paramName) throws ValidationException {
        if (!request.containsKey(paramName)) {
            throw new ValidationException(invalidParams);
        }
    }
    private List<String> getParameterAsList(Map<String, Object> request, String paramName, int size) throws ValidationException {
        validateParamExists(request, paramName);
        final List<String> paramList = (List<String>) request.get(paramName);
        if (paramList.size() > maxRequestList) {
            throw new ValidationException(overMaxErrorMessage);
        }

        if (size > 0) {
            //validate
            for (final String param : paramList) {
                validateTrytes(paramName, size, param);
            }
        }

        return paramList;

    }

    public boolean invalidSubtangleStatus() {
        return false;
//        return (instance.milestone.latestSolidSubtangleMilestoneIndex == milestoneStartIndex);
    }
    /**
     * Temporarily removes a list of neighbors from your node.
     * The added neighbors will be added again after relaunching IRI.
     * Remove the neighbors from your config file or make sure you don't supply them in the -n command line option if you want to keep them removed after restart.
     * <p>
     * The URI (Unique Resource Identification) for removing neighbors is:
     * <b>udp://IPADDRESS:PORT</b>
     * <p>
     * Returns an {@link com.cvt.iri.service.dto.ErrorResponse} if the URI scheme is wrong
     *
     * @param uris List of URI elements.
     * @return {@link com.cvt.iri.service.dto.RemoveNeighborsResponse}
     **/
    private AbstractResponse removeNeighborsStatement(List<String> uris) {
        int numberOfRemovedNeighbors = 0;
        try {
            for (final String uriString : uris) {
                log.info("Removing neighbor: " + uriString);
                if (instance.node.removeNeighbor(new URI(uriString), true)) {
                    numberOfRemovedNeighbors++;
                }
            }
        } catch (URISyntaxException | RuntimeException e) {
            return ErrorResponse.create("Invalid uri scheme: " + e.getLocalizedMessage());
        }
        return RemoveNeighborsResponse.create(numberOfRemovedNeighbors);
    }
    /**
     * Returns the raw transaction data (trytes) of a specific transaction.
     * These trytes can then be easily converted into the actual transaction object.
     * See utility functions for more details.
     *
     * @param hashes List of transaction hashes you want to get trytes from.
     * @return {@link com.cvt.iri.service.dto.GetTrytesResponse}
     **/
    private synchronized AbstractResponse getTrytesStatement(List<String> hashes) throws Exception {
        final List<String> elements = new LinkedList<>();
        for (final String hash : hashes) {
            final TransactionViewModel transactionViewModel = TransactionViewModel.fromHash(instance.tangle, new Hash(hash));
            if (transactionViewModel != null) {
                elements.add(Converter.trytes(transactionViewModel.trits()));
            }
        }
        if (elements.size() > maxGetTrytes) {
            return ErrorResponse.create(overMaxErrorMessage);
        }
        return GetTrytesResponse.create(elements);
    }

    private static int counter_getTxToApprove = 0;

    public static int getCounter_getTxToApprove() {
        return counter_getTxToApprove;
    }

    public static void incCounter_getTxToApprove() {
        counter_getTxToApprove++;
    }

    private static long ellapsedTime_getTxToApprove = 0L;

    public static long getEllapsedTime_getTxToApprove() {
        return ellapsedTime_getTxToApprove;
    }

    public static void incEllapsedTime_getTxToApprove(long ellapsedTime) {
        ellapsedTime_getTxToApprove += ellapsedTime;
    }

    /**
     * Tip selection which returns <code>trunkTransaction</code> and <code>branchTransaction</code>.
     * The input value <code>depth</code> determines how many milestones to go back for finding the transactions to approve.
     * The higher your <code>depth</code> value, the more work you have to do as you are confirming more transactions.
     * If the <code>depth</code> is too large (usually above 15, it depends on the node's configuration) an error will be returned.
     * The <code>reference</code> is an optional hash of a transaction you want to approve.
     * If it can't be found at the specified <code>depth</code> then an error will be returned.
     *
     * @param depth     Number of bundles to go back to determine the transactions for approval.
     * @param reference Hash of transaction to start random-walk from, used to make sure the tips returned reference a given transaction in their past.
     * @return {@link com.cvt.iri.service.dto.GetTransactionsToApproveResponse}
     **/
    public synchronized List<Hash> getTransactionsToApproveStatement(int depth, Optional<Hash> reference) throws Exception {

        if (invalidSubtangleStatus()) {
            throw new IllegalStateException("This operations cannot be executed: The subtangle has not been updated yet.");
        }

        List<Hash> tips = instance.tipsSelector.getTransactionsToApprove(depth, reference);

        if (log.isDebugEnabled()) {
            gatherStatisticsOnTipSelection();
        }

        return tips;
    }

    private void gatherStatisticsOnTipSelection() {
        API.incCounter_getTxToApprove();
        if ((getCounter_getTxToApprove() % 100) == 0) {
            String sb = "Last 100 getTxToApprove consumed " + API.getEllapsedTime_getTxToApprove() / 1000000000L + " seconds processing time.";
            log.debug(sb);
            counter_getTxToApprove = 0;
            ellapsedTime_getTxToApprove = 0L;
        }
    }


    /**
     * Returns the list of tips.
     *
     * @return {@link com.cvt.iri.service.dto.GetTipsResponse}
     **/
    private synchronized AbstractResponse getTipsStatement() throws Exception {
        return GetTipsResponse.create(instance.tipsViewModel.getTips().stream().map(Hash::toString).collect(Collectors.toList()));
    }

    /**
     * Store transactions into the local storage.
     * The trytes to be used for this call are returned by <code>attachToTangle</code>.
     *
     * @param trytes List of raw data of transactions to be rebroadcast.
     **/
    public void storeTransactionsStatement(final List<String> trytes) throws Exception {
        final List<TransactionViewModel> elements = parseTransactionViewModel(trytes);
        try {
            for (final TransactionViewModel transactionViewModel : elements) {
                storeTransactionTOSqlite(transactionViewModel);

                //store transactions
                if (transactionViewModel.store(instance.tangle)) {
                    transactionViewModel.setArrivalTime(System.currentTimeMillis() / 1000L);
                    instance.transactionValidator.updateStatus(transactionViewModel);
                    transactionViewModel.updateSender("local");
                    transactionViewModel.update(instance.tangle, "sender");
                }
            }
        } catch (Exception e) {
            log.info("原始交易异常，可以忽略", e);
        }

        NodeUtils.requestSuperNodeToVerifyTransaction(instance.node.getSuperNode(), elements);
        for (final TransactionViewModel transactionViewModel : elements) {
            storeTransactionTOSqlite(transactionViewModel);
        }
        SqliteHelper.printTransaction();
    }
    private void storeTransactionTOSqlite(TransactionViewModel transactionViewModel) throws Exception {

        // heffjo: persistence
        Transaction transaction = transactionViewModel.getTransaction();
        if (null == transaction.address) {
            log.info("交易地址为空");
            return;
        }

        SqliteHelper.saveTransaction(transactionViewModel);
    }
    /**
     * 请求邻居对存储进行证明
     *
     * @param neighbor 邻居对象
     * @param params   请求参数
     */
    private void postToNeighbor(Neighbor neighbor, Map<String, Object> params) {
        String requestBody = new Gson().toJson(params);
        HttpClient httpClient = HttpClientBuilder.create().build();
        // 潜规则，WEB端口比 UDP 端口小 10
        HttpPost httpPost = new HttpPost("http://" + neighbor.getHostAddress() + ":" + (neighbor.getPort() - 10));
        httpPost.addHeader("X-CVT-API-Version", "20181207");
        ByteArrayEntity entity = new ByteArrayEntity(requestBody.getBytes(StandardCharsets.UTF_8));
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        //执行post请求
        try {
            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                Map data = new Gson().fromJson(responseBody, Map.class);
                Boolean success = (Boolean) data.getOrDefault("verifySuccess", false);
                if (!success) {
                    throw new RuntimeException("文件存储证明失败");
                }
            }
            log.info("状态码：" + response.getStatusLine());
        } catch (Exception e) {
            log.error("请求邻居" + neighbor.getAddress() + "执行储存证明异常", e);
            throw new RuntimeException("请求邻居" + neighbor.getAddress() + "执行储存证明异常", e);
        }
    }

    private String removeLast9(String str) {
        while (str.endsWith("9")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
    /**
     * Returns the set of neighbors you are connected with, as well as their activity statistics (or counters).
     * The activity counters are reset after restarting IRI.
     *
     * @return {@link com.cvt.iri.service.dto.GetNeighborsResponse}
     **/
    private AbstractResponse getNeighborsStatement() {
        return GetNeighborsResponse.create(instance.node.getNeighbors());
    }

    /**
     * Interrupts and completely aborts the <code>attachToTangle</code> process.
     *
     * @return {@link com.cvt.iri.service.dto.AbstractResponse}
     **/
    private AbstractResponse interruptAttachingToTangleStatement() {
        pearlDiver.cancel();
        return AbstractResponse.createEmptyResponse();
    }

    /**
     * Returns information about your node.
     *
     * @return {@link com.cvt.iri.service.dto.GetNodeInfoResponse}
     **/
    private AbstractResponse getNodeInfoStatement() {
        String name = instance.configuration.isTestnet() ? IRI.TESTNET_NAME : IRI.MAINNET_NAME;
        return GetNodeInfoResponse.create(name, IRI.VERSION, Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().freeMemory(), System.getProperty("java.version"), Runtime.getRuntime().maxMemory(),
                Runtime.getRuntime().totalMemory(), instance.milestone.latestMilestone, instance.milestone.latestMilestoneIndex,
                instance.milestone.latestSolidSubtangleMilestone, instance.milestone.latestSolidSubtangleMilestoneIndex, instance.milestone.milestoneStartIndex,
                instance.node.howManyNeighbors(), instance.node.queuedTransactionsSize(),
                System.currentTimeMillis(), instance.tipsViewModel.size(),
                instance.transactionRequester.numberOfTransactionsToRequest());
    }

    /**
     * Get the inclusion states of a set of transactions.
     * This is for determining if a transaction was accepted and confirmed by the network or not.
     * You can search for multiple tips (and thus, milestones) to get past inclusion states of transactions.
     * <p>
     * This API call simply returns a list of boolean values in the same order as the transaction list you submitted, thus you get a true/false whether a transaction is confirmed or not.
     * Returns an {@link com.cvt.iri.service.dto.ErrorResponse} if a tip is missing or the subtangle is not solid
     *
     * @param transactions List of transactions you want to get the inclusion state for.
     * @param tips         List of tips (including milestones) you want to search for the inclusion state.
     * @return {@link com.cvt.iri.service.dto.GetInclusionStatesResponse}
     **/
    private AbstractResponse getInclusionStatesStatement(final List<String> transactions, final List<String> tips) throws Exception {
        final List<Hash> trans = transactions.stream().map(Hash::new).collect(Collectors.toList());
        final List<Hash> tps = tips.stream().map(Hash::new).collect(Collectors.toList());
        int numberOfNonMetTransactions = transactions.size();
        final byte[] inclusionStates = new byte[numberOfNonMetTransactions];

        List<Integer> tipsIndex = new LinkedList<>();
        {
            for (Hash tip : tps) {
                TransactionViewModel tx = TransactionViewModel.fromHash(instance.tangle, tip);
                if (tx.getType() != TransactionViewModel.PREFILLED_SLOT) {
                    tipsIndex.add(tx.snapshotIndex());
                }
            }
        }
        int minTipsIndex = tipsIndex.stream().reduce((a, b) -> a < b ? a : b).orElse(0);
        if (minTipsIndex > 0) {
            int maxTipsIndex = tipsIndex.stream().reduce((a, b) -> a > b ? a : b).orElse(0);
            int count = 0;
            for (Hash hash : trans) {
                TransactionViewModel transaction = TransactionViewModel.fromHash(instance.tangle, hash);
                if (transaction.getType() == TransactionViewModel.PREFILLED_SLOT || transaction.snapshotIndex() == 0) {
                    inclusionStates[count] = -1;
                } else if (transaction.snapshotIndex() > maxTipsIndex) {
                    inclusionStates[count] = -1;
                } else if (transaction.snapshotIndex() < maxTipsIndex) {
                    inclusionStates[count] = 1;
                }
                count++;
            }
        }

        Set<Hash> analyzedTips = new HashSet<>();
        Map<Integer, Integer> sameIndexTransactionCount = new HashMap<>();
        Map<Integer, Queue<Hash>> sameIndexTips = new HashMap<>();
        for (final Hash tip : tps) {
            TransactionViewModel transactionViewModel = TransactionViewModel.fromHash(instance.tangle, tip);
            if (transactionViewModel.getType() == TransactionViewModel.PREFILLED_SLOT) {
                return ErrorResponse.create("One of the tips is absent");
            }
            int snapshotIndex = transactionViewModel.snapshotIndex();
            sameIndexTips.putIfAbsent(snapshotIndex, new LinkedList<>());
            sameIndexTips.get(snapshotIndex).add(tip);
        }
        for (int i = 0; i < inclusionStates.length; i++) {
            if (inclusionStates[i] == 0) {
                TransactionViewModel transactionViewModel = TransactionViewModel.fromHash(instance.tangle, trans.get(i));
                int snapshotIndex = transactionViewModel.snapshotIndex();
                sameIndexTransactionCount.putIfAbsent(snapshotIndex, 0);
                sameIndexTransactionCount.put(snapshotIndex, sameIndexTransactionCount.get(snapshotIndex) + 1);
            }
        }
        for (Integer index : sameIndexTransactionCount.keySet()) {
            Queue<Hash> sameIndexTip = sameIndexTips.get(index);
            if (sameIndexTip != null) {
                //has tips in the same index level
                if (!exhaustiveSearchWithinIndex(sameIndexTip, analyzedTips, trans, inclusionStates, sameIndexTransactionCount.get(index), index)) {
                    return ErrorResponse.create("The subtangle is not solid");
                }
            }
        }
        final boolean[] inclusionStatesBoolean = new boolean[inclusionStates.length];
        for (int i = 0; i < inclusionStates.length; i++) {
            inclusionStatesBoolean[i] = inclusionStates[i] == 1;
        }
        {
            return GetInclusionStatesResponse.create(inclusionStatesBoolean);
        }
    }
    private boolean exhaustiveSearchWithinIndex(Queue<Hash> nonAnalyzedTransactions, Set<Hash> analyzedTips, List<Hash> transactions, byte[] inclusionStates, int count, int index) throws Exception {
        Hash pointer;
        MAIN_LOOP:
        while ((pointer = nonAnalyzedTransactions.poll()) != null) {
            if (analyzedTips.add(pointer)) {
                final TransactionViewModel transactionViewModel = TransactionViewModel.fromHash(instance.tangle, pointer);
                if (transactionViewModel.snapshotIndex() == index) {
                    if (transactionViewModel.getType() == TransactionViewModel.PREFILLED_SLOT) {
                        return false;
                    } else {
                        for (int i = 0; i < inclusionStates.length; i++) {
                            if (inclusionStates[i] < 1 && pointer.equals(transactions.get(i))) {
                                inclusionStates[i] = 1;
                                if (--count <= 0) {
                                    break MAIN_LOOP;
                                }
                            }
                        }
                        nonAnalyzedTransactions.offer(transactionViewModel.getTrunkTransactionHash());
                        nonAnalyzedTransactions.offer(transactionViewModel.getBranchTransactionHash());
                    }
                }
            }
        }
        return true;
    }
    /**
     * Find the transactions which match the specified input and return.
     * All input values are lists, for which a list of return values (transaction hashes), in the same order, is returned for all individual elements.
     * The input fields can either be <code>bundles<code>, <code>addresses</code>, <code>tags</code> or <code>approvees</code>.
     * <b>Using multiple of these input fields returns the intersection of the values.</b>
     * <p>
     * Returns an {@link com.cvt.iri.service.dto.ErrorResponse} if more than maxFindTxs was found
     *
     * @param request the map with input fields
     * @return {@link com.cvt.iri.service.dto.FindTransactionsResponse}
     **/
    private synchronized AbstractResponse findTransactionsStatement(final Map<String, Object> request) throws Exception {

        final Set<Hash> foundTransactions = new HashSet<>();
        boolean containsKey = false;

        final Set<Hash> bundlesTransactions = new HashSet<>();
        if (request.containsKey("bundles")) {
            final HashSet<String> bundles = getParameterAsSet(request, "bundles", HASH_SIZE);
            for (final String bundle : bundles) {
                bundlesTransactions.addAll(BundleViewModel.load(instance.tangle, new Hash(bundle)).getHashes());
            }
            foundTransactions.addAll(bundlesTransactions);
            containsKey = true;
        }

        final Set<Hash> addressesTransactions = new HashSet<>();
        if (request.containsKey("addresses")) {
            final HashSet<String> addresses = getParameterAsSet(request, "addresses", HASH_SIZE);
            for (final String address : addresses) {
                addressesTransactions.addAll(AddressViewModel.load(instance.tangle, new Hash(address)).getHashes());

                CvtPersistable transaction = SqliteHelper.getTransaction(address);
                if (null != transaction) {
                    addressesTransactions.add(new Hash(address));
                }
            }
            foundTransactions.addAll(addressesTransactions);

            containsKey = true;
        }

        final Set<Hash> tagsTransactions = new HashSet<>();
        if (request.containsKey("tags")) {
            final HashSet<String> tags = getParameterAsSet(request, "tags", 0);
            for (String tag : tags) {
                tag = padTag(tag);
                tagsTransactions.addAll(TagViewModel.load(instance.tangle, new Hash(tag)).getHashes());
            }
            if (tagsTransactions.isEmpty()) {
                for (String tag : tags) {
                    tag = padTag(tag);
                    tagsTransactions.addAll(TagViewModel.loadObsolete(instance.tangle, new Hash(tag)).getHashes());
                }
            }
            foundTransactions.addAll(tagsTransactions);
            containsKey = true;
        }

        final Set<Hash> approveeTransactions = new HashSet<>();

        if (request.containsKey("approvees")) {
            final HashSet<String> approvees = getParameterAsSet(request, "approvees", HASH_SIZE);
            for (final String approvee : approvees) {
                approveeTransactions.addAll(TransactionViewModel.fromHash(instance.tangle, new Hash(approvee)).getApprovers(instance.tangle).getHashes());
            }
            foundTransactions.addAll(approveeTransactions);
            containsKey = true;
        }

        if (!containsKey) {
            throw new ValidationException(invalidParams);
        }

        //Using multiple of these input fields returns the intersection of the values.
        if (request.containsKey("bundles")) {
            foundTransactions.retainAll(bundlesTransactions);
        }
        if (request.containsKey("addresses")) {
            foundTransactions.retainAll(addressesTransactions);
        }
        if (request.containsKey("tags")) {
            foundTransactions.retainAll(tagsTransactions);
        }
        if (request.containsKey("approvees")) {
            foundTransactions.retainAll(approveeTransactions);
        }
        if (foundTransactions.size() > maxFindTxs) {
            return ErrorResponse.create(overMaxErrorMessage);
        }

        final List<String> elements = foundTransactions.stream()
                .map(Hash::toString)
                .collect(Collectors.toCollection(LinkedList::new));

        return FindTransactionsResponse.create(elements);
    }
}

