package com.cvt.iri.service;

import com.alibaba.fastjson.JSON;
import com.cvt.iri.coinbase.CoinBaseService;
import com.cvt.iri.coinbase.StandardCoinBaseService;
import com.cvt.iri.conf.APIConfig;
import com.cvt.iri.controllers.AddressViewModel;
import com.cvt.iri.controllers.TransactionViewModel;
import com.cvt.iri.model.Hash;
import com.cvt.iri.pdp.MockPdpService;
import com.cvt.iri.retrofit2.FileHashResponse;
import com.cvt.iri.retrofit2.VerifyStorageResponse;
import com.cvt.iri.service.dto.*;
import com.cvt.iri.service.tipselection.impl.WalkValidatorImpl;
import com.cvt.iri.utils.IotaIOUtils;
import com.cvt.iri.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.streams.ChannelInputStream;

import java.io.*;
import java.net.InetSocketAddress;
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

}

