package jota;

import jota.error.BaseException;
import jota.pow.ICurl;
import jota.pow.SpongeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * CvtAPI Builder. Usage:
 * <p>
 * {@code CvtApiProxy api = CvtApiProxy.Builder}
 * {@code .protocol("http")}
 * {@code .nodeAddress("localhost")}
 * {@code .port(12345)}
 * {@code .build();}
 *
 * {@code GetNodeInfoResponse response = api.getNodeInfo();}
 *
 */
public class CvtAPI extends CvtAPICore {

    private static final Logger log = LoggerFactory.getLogger(CvtAPI.class);
    private ICurl customCurl;

    protected CvtAPI(Builder builder) {
        super(builder);
        customCurl = builder.customCurl;
    }

    /**
     * Generates a new address from a seed and returns the remainderAddress.
     * This is either done deterministically, or by providing the index of the new remainderAddress.
     * <br/><br/>
     * Deprecated -> Use the new functions {@link #getNextAvailableAddress}, {@link #getAddressesUnchecked} and {@link #generateNewAddresses}
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param index     Key index to start search from. If the index is provided, the generation of the address is not deterministic.
     * @param checksum  Adds 9-tryte address checksum.
     * @param total     Total number of addresses to generate.
     * @param returnAll If <code>true</code>, it returns all addresses which were deterministically generated (until findTransactions returns null).
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    @Deprecated
    public GetNewAddressResponse getNewAddress(final String seed, int security, final int index, final boolean checksum, final int total, final boolean returnAll) throws ArgumentException {

        // If total number of addresses to generate is supplied, simply generate
        // and return the list of all addresses
        if (total != 0) {
            return getAddressesUnchecked(seed, security, checksum, index, total);
        }

        // If !returnAll return only the last address that was generated
        if (returnAll) {
            return generateNewAddresses(seed, security, checksum, 0, 1, true);
        } else {
            return generateNewAddresses(seed, security, checksum, 0, 1, false);
        }
    }

    /**
     * Checks all addresses until the first unspent address is found. Starts at index 0.
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse getNextAvailableAddress(String seed, int security, boolean checksum) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, 0, 1, false);
    }

    /**
     * Checks all addresses until the first unspent address is found.
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse getNextAvailableAddress(String seed, int security, boolean checksum, int index) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, index, 1, false);
    }

    /**
     * Generates new addresses, meaning addresses which were not spend from, according to the connected node.
     * Starts at index 0, untill <code>amount</code> of unspent addresses are found.
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param amount    Total number of addresses to generate.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse generateNewAddresses(String seed, int security, boolean checksum, int amount) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, 0, amount, false);
    }

    /**
     * Generates new addresses, meaning addresses which were not spend from, according to the connected node.
     * Stops when <code>amount</code> of unspent addresses are found,starting from <code>index</code>
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from.
     * @param amount    Total number of addresses to generate.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse generateNewAddresses(String seed, int security, boolean checksum, int index, int amount) throws ArgumentException {
        return generateNewAddresses(seed, security, checksum, 0, amount, false);
    }

    /**
     * Generates new addresses, meaning addresses which were not spend from, according to the connected node.
     * Stops when <code>amount</code> of unspent addresses are found,starting from <code>index</code>
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from.
     * @param amount    Total number of addresses to generate.
     * @param addSpendAddresses If <code>true</code>, it returns all addresses, even those who were determined to be spent from
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse generateNewAddresses(String seed, int security, boolean checksum, int index, int amount, boolean addSpendAddresses) throws ArgumentException {
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();
        List<String> allAddresses = new ArrayList<>();

        for (int i = index, numUnspentFound=0; numUnspentFound < amount; i++) {

            final String newAddress = CvtAPIUtils.newAddress(seed, security, i, checksum, customCurl.clone());
            final FindTransactionResponse response = findTransactionsByAddresses(newAddress);


            if (response.getHashes().length == 0) {
                //Unspent address
                allAddresses.add(newAddress);
                numUnspentFound++;
            } else if (addSpendAddresses) {
                //Spend address, were interested anyways
                allAddresses.add(newAddress);
            }
        }

        return GetNewAddressResponse.create(allAddresses, stopWatch.getElapsedTimeMili());
    }

    /**
     * Generates <code>amount</code> of addresses, starting from <code>index</code>
     * This does not mean that these addresses are safe to use (unspent)
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  Security level to be used for the private key / address. Can be 1, 2 or 3.
     * @param checksum  Adds 9-tryte address checksum.
     * @param index     Key index to start search from. The generation of the address is not deterministic.
     * @param amount    Total number of addresses to generate.
     * @return GetNewAddressResponse containing an array of strings with the specified number of addresses.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetNewAddressResponse getAddressesUnchecked(String seed, int security, boolean checksum, int index, int amount) throws ArgumentException {
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();

        List<String> allAddresses = new ArrayList<>();
        for (int i = index; i < index + amount; i++) {
            allAddresses.add(CvtAPIUtils.newAddress(seed, security, i, checksum, customCurl.clone()));
        }
        return GetNewAddressResponse.create(allAddresses, stopWatch.getElapsedTimeMili());
    }

    /**
     * @param seed            Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security        The security level of private key / seed.
     * @param start           Starting key index.
     * @param end             Ending key index.
     * @param inclusionStates If <code>true</code>, it gets the inclusion states of the transfers.
     * @return Bundle of transfers.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetTransferResponse getTransfers(String seed, int security, Integer start, Integer end, Boolean inclusionStates) throws ArgumentException {

        // validate seed
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        if (start > end || end > (start + 500)) {
            throw new ArgumentException(Constants.INVALID_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();

        GetNewAddressResponse gnr = getNewAddress(seed, security, start, false, end, true);
        if (gnr != null && gnr.getAddresses() != null) {
            Bundle[] bundles = bundlesFromAddresses(gnr.getAddresses().toArray(new String[gnr.getAddresses().size()]), inclusionStates);
            return GetTransferResponse.create(bundles, stopWatch.getElapsedTimeMili());
        }
        return GetTransferResponse.create(new Bundle[]{}, stopWatch.getElapsedTimeMili());
    }
    /**
     * Internal function to get the formatted bundles of a list of addresses.
     *
     * @param addresses       List of addresses.
     * @param inclusionStates If <code>true</code>, it gets the inclusion states of the transfers.
     * @return A Transaction objects.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public Bundle[] bundlesFromAddresses(String[] addresses, final Boolean inclusionStates) throws ArgumentException {

        List<Transaction> trxs = findTransactionObjectsByAddresses(addresses);
        // set of tail transactions
        List<String> tailTransactions = new ArrayList<>();
        List<String> nonTailBundleHashes = new ArrayList<>();

        for (Transaction trx : trxs) {
            // Sort tail and nonTails
            if (trx.getCurrentIndex() == 0) {
                tailTransactions.add(trx.getHash());
            } else {
                if (nonTailBundleHashes.indexOf(trx.getBundle()) == -1) {
                    nonTailBundleHashes.add(trx.getBundle());
                }
            }
        }

        List<Transaction> bundleObjects = findTransactionObjectsByBundle(nonTailBundleHashes.toArray(new String[nonTailBundleHashes.size()]));
        for (Transaction trx : bundleObjects) {
            // Sort tail and nonTails
            if (trx.getCurrentIndex() == 0) {
                if (tailTransactions.indexOf(trx.getHash()) == -1) {
                    tailTransactions.add(trx.getHash());
                }
            }
        }

        final List<Bundle> finalBundles = new ArrayList<>();
        final String[] tailTxArray = tailTransactions.toArray(new String[tailTransactions.size()]);

        // If inclusionStates, get the confirmation status
        // of the tail transactions, and thus the bundles
        GetInclusionStateResponse gisr = null;
        if (tailTxArray.length != 0 && inclusionStates) {
            gisr = getLatestInclusion(tailTxArray);
            if (gisr == null || gisr.getStates() == null || gisr.getStates().length == 0) {
                throw new IllegalStateException(Constants.GET_INCLUSION_STATE_RESPONSE_ERROR);
            }
        }
        final GetInclusionStateResponse finalInclusionStates = gisr;
        Parallel.For(Arrays.asList(tailTxArray),
                new Parallel.Operation<String>() {
                    public void perform(String param) {

                        try {
                            GetBundleResponse bundleResponse = getBundle(param);
                            Bundle gbr = new Bundle(bundleResponse.getTransactions(), bundleResponse.getTransactions().size());
                            if (gbr.getTransactions() != null) {
                                if (inclusionStates) {
                                    boolean thisInclusion = false;
                                    if (finalInclusionStates != null) {
                                        thisInclusion = finalInclusionStates.getStates()[Arrays.asList(tailTxArray).indexOf(param)];
                                    }
                                    for (Transaction t : gbr.getTransactions()) {
                                        t.setPersistence(thisInclusion);
                                    }
                                }
                                finalBundles.add(gbr);
                            }
                            // If error returned from getBundle, simply ignore it because the bundle was most likely incorrect
                        } catch (ArgumentException e) {
                            log.warn(Constants.GET_BUNDLE_RESPONSE_ERROR);
                        }
                    }
                });

        Collections.sort(finalBundles);
        Bundle[] returnValue = new Bundle[finalBundles.size()];
        for (int i = 0; i < finalBundles.size(); i++) {
            returnValue[i] = new Bundle(finalBundles.get(i).getTransactions(), finalBundles.get(i).getTransactions().size());
        }
        return returnValue;
    }

    /**
     * Wrapper function that stores and broadcasts the specified trytes.
     *
     * @param trytes The trytes.
     * @return A BroadcastTransactionsResponse.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public BroadcastTransactionsResponse storeAndBroadcast(final String... trytes) throws ArgumentException {

        if (!InputValidator.isArrayOfAttachedTrytes(trytes)) {
            throw new ArgumentException(Constants.INVALID_TRYTES_INPUT_ERROR);
        }

        try {
            storeTransactions(trytes);
        } catch (Exception e) {
            throw new ArgumentException(e.toString());
        }
        return broadcastTransactions(trytes);
    }

    /**
     * Facade method: Gets transactions to approve, attaches to Tangle, broadcasts and stores.
     *
     * @param trytes             The trytes.
     * @param depth              The depth.
     * @param minWeightMagnitude The minimum weight magnitude.
     * @param reference          Hash of transaction to start random-walk from, used to make sure the tips returned reference a given transaction in their past.
     * @return Transactions objects.
     * @throws ArgumentException is thrown when invalid trytes is provided.
     */
    public List<Transaction> sendTrytes(final String[] trytes, final int depth, final int minWeightMagnitude, final String reference) throws ArgumentException {
        final GetTransactionsToApproveResponse txs = getTransactionsToApprove(depth, reference);

        // attach to tangle - do pow
        final GetAttachToTangleResponse res = attachToTangle(txs.getTrunkTransaction(), txs.getBranchTransaction(), minWeightMagnitude, trytes);

        try {
            storeAndBroadcast(res.getTrytes());
        } catch (ArgumentException e) {
            return new ArrayList<>();
        }

        final List<Transaction> trx = new ArrayList<>();

        for (final String tryte : Arrays.asList(res.getTrytes())) {
            trx.add(new Transaction(tryte, customCurl.clone()));
        }
        return trx;
    }

    /**
     * Wrapper function for getTrytes and transactionObjects.
     * Gets the trytes and transaction object from a list of transaction hashes.
     *
     * @param hashes The hashes
     * @return Transaction objects.
     **/
    public List<Transaction> findTransactionsObjectsByHashes(String[] hashes) throws ArgumentException {

        if (!InputValidator.isArrayOfHashes(hashes)) {
            throw new IllegalStateException(Constants.INVALID_HASHES_INPUT_ERROR);
        }

        final GetTrytesResponse trytesResponse = getTrytes(hashes);

        final List<Transaction> trxs = new ArrayList<>();

        for (final String tryte : trytesResponse.getTrytes()) {
            trxs.add(new Transaction(tryte, customCurl.clone()));
        }
        return trxs;
    }

    /**
     * Wrapper function for findTransactions, getTrytes and transactionObjects.
     * Returns the transactionObject of a transaction hash. The input can be a list of valid addresses.
     *
     * @param addresses The addresses.
     * @return Transactions.
     **/
    public List<Transaction> findTransactionObjectsByAddresses(String[] addresses) throws ArgumentException {
        List<String> addressesWithoutChecksum = new ArrayList<>();

        for (String address : addresses) {
            String addressO = Checksum.removeChecksum(address);
            addressesWithoutChecksum.add(addressO);
        }

        FindTransactionResponse ftr = findTransactions(addressesWithoutChecksum.toArray(new String[]{}), null, null, null);
        if (ftr == null || ftr.getHashes() == null)
            return new ArrayList<>();
        // get the transaction objects of the transactions
        return findTransactionsObjectsByHashes(ftr.getHashes());
    }

    /**
     * Wrapper function for findTransactions, getTrytes and transactionObjects.
     * Returns the transactionObject of a transaction hash. The input can be a list of valid tags.
     *
     * @param tags The tags.
     * @return Transactions.
     **/
    public List<Transaction> findTransactionObjectsByTag(String[] tags) throws ArgumentException {
        FindTransactionResponse ftr = findTransactions(null, tags, null, null);
        if (ftr == null || ftr.getHashes() == null)
            return new ArrayList<>();
        // get the transaction objects of the transactions
        return findTransactionsObjectsByHashes(ftr.getHashes());
    }


    /**
     * Wrapper function for findTransactions, getTrytes and transactionObjects.
     * Returns the transactionObject of a transaction hash. The input can be a list of valid approvees.
     *
     * @param approvees The approvees.
     * @return Transactions.
     **/
    public List<Transaction> findTransactionObjectsByApprovees(String[] approvees) throws ArgumentException {
        FindTransactionResponse ftr = findTransactions(null, null, approvees, null);
        if (ftr == null || ftr.getHashes() == null)
            return new ArrayList<>();
        // get the transaction objects of the transactions
        return findTransactionsObjectsByHashes(ftr.getHashes());
    }


    /**
     * Wrapper function for findTransactions, getTrytes and transactionObjects.
     * Returns the transactionObject of a transaction hash. The input can be a list of valid bundles.
     * findTransactions input
     *
     * @param bundles The bundles.
     * @return Transactions.
     **/
    public List<Transaction> findTransactionObjectsByBundle(String[] bundles) throws ArgumentException {
        FindTransactionResponse ftr = findTransactions(null, null, null, bundles);
        if (ftr == null || ftr.getHashes() == null)
            return new ArrayList<>();

        // get the transaction objects of the transactions
        return findTransactionsObjectsByHashes(ftr.getHashes());
    }


    /**
     * Prepares transfer by generating bundle, finding and signing inputs.
     *
     * @param seed           Tryte-encoded private key / seed.
     * @param security       The security level of private key / seed.
     * @param transfers      Array of transfer objects.
     * @param remainder      If defined, this address will be used for sending the remainder value (of the inputs) to.
     * @param inputs         The inputs.
     * @param tips           The starting points we walk back from to find the balance of the addresses
     * @param validateInputs whether or not to validate the balances of the provided inputs
     * @return Returns bundle trytes.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public List<String> prepareTransfers(String seed, int security, final List<Transfer> transfers, String remainder, List<Input> inputs, List<Transaction> tips, boolean validateInputs) throws ArgumentException {

        // validate seed
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        if (!InputValidator.isValidSecurityLevel(security)) {
            throw new ArgumentException(Constants.INVALID_SECURITY_LEVEL_INPUT_ERROR);
        }

        // Input validation of transfers object
        if (!InputValidator.isTransfersCollectionValid(transfers)) {
            throw new ArgumentException(Constants.INVALID_TRANSFERS_INPUT_ERROR);
        }

        // Create a new bundle
        final Bundle bundle = new Bundle();
        final List<String> signatureFragments = new ArrayList<>();

        long totalValue = 0;
        String tag = "";
        //  Iterate over all transfers, get totalValue
        //  and prepare the signatureFragments, message and tag
        for (final Transfer transfer : transfers) {

            // remove the checksum of the address if provided
            if (Checksum.isValidChecksum(transfer.getAddress())) {
                transfer.setAddress(Checksum.removeChecksum(transfer.getAddress()));
            }

            int signatureMessageLength = 1;

            // If message longer than 2187 trytes, increase signatureMessageLength (add 2nd transaction)
            if (transfer.getMessage().length() > Constants.MESSAGE_LENGTH) {

                // Get total length, message / maxLength (2187 trytes)
                signatureMessageLength += Math.floor(transfer.getMessage().length() / Constants.MESSAGE_LENGTH);

                String msgCopy = transfer.getMessage();

                // While there is still a message, copy it
                while (!msgCopy.isEmpty()) {

                    String fragment = StringUtils.substring(msgCopy, 0, Constants.MESSAGE_LENGTH);
                    msgCopy = StringUtils.substring(msgCopy, Constants.MESSAGE_LENGTH, msgCopy.length());

                    // Pad remainder of fragment

                    fragment = StringUtils.rightPad(fragment, Constants.MESSAGE_LENGTH, '9');

                    signatureFragments.add(fragment);
                }
            } else {
                // Else, get single fragment with 2187 of 9's trytes
                String fragment = transfer.getMessage();

                if (transfer.getMessage().length() < Constants.MESSAGE_LENGTH) {
                    fragment = StringUtils.rightPad(fragment, Constants.MESSAGE_LENGTH, '9');
                }
                signatureFragments.add(fragment);
            }

            tag = transfer.getTag();

            // pad for required 27 tryte length
            if (transfer.getTag().length() < Constants.TAG_LENGTH) {
                tag = StringUtils.rightPad(tag, Constants.TAG_LENGTH, '9');
            }


            // get current timestamp in seconds
            long timestamp = (long) Math.floor(Calendar.getInstance().getTimeInMillis() / 1000);

            // Add first entry to the bundle
            bundle.addEntry(signatureMessageLength, transfer.getAddress(), transfer.getValue(), tag, timestamp);
            // Sum up total value
            totalValue += transfer.getValue();
        }

        // Get inputs if we are sending tokens
        if (totalValue != 0) {

            //  Case 1: user provided inputs
            //  Validate the inputs by calling getBalances
            if (inputs != null && !inputs.isEmpty()) {

                if (!validateInputs) {
                    return addRemainder(seed, security, inputs, bundle, tag, totalValue, remainder, signatureFragments);
                }
                // Get list if addresses of the provided inputs
                List<String> inputsAddresses = new ArrayList<>();
                for (final Input i : inputs) {
                    inputsAddresses.add(i.getAddress());
                }

                List<String> tipHashes = null;
                if (tips != null) {
                    tipHashes = new ArrayList<>();

                    for (final Transaction tx: tips) {
                        tipHashes.add(tx.getHash());
                    }
                }

                GetBalancesResponse balancesResponse = getBalances(100, inputsAddresses, tipHashes);
                String[] balances = balancesResponse.getBalances();

                List<Input> confirmedInputs = new ArrayList<>();
                long totalBalance = 0;

                for (int i = 0; i < balances.length; i++) {
                    long thisBalance = Long.parseLong(balances[i]);

                    // If input has balance, add it to confirmedInputs
                    if (thisBalance > 0) {
                        totalBalance += thisBalance;
                        Input inputEl = inputs.get(i);
                        inputEl.setBalance(thisBalance);
                        confirmedInputs.add(inputEl);

                        // if we've already reached the intended input value, break out of loop
                        if (totalBalance >= totalValue) {
                            log.info("Total balance already reached ");
                            break;
                        }
                    }

                }

                // Return not enough balance error
                if (totalValue > totalBalance) {
                    throw new IllegalStateException(Constants.NOT_ENOUGH_BALANCE_ERROR);
                }

                return addRemainder(seed, security, confirmedInputs, bundle, tag, totalValue, remainder, signatureFragments);
            }

            //  Case 2: Get inputs deterministically
            //
            //  If no inputs provided, derive the addresses from the seed and
            //  confirm that the inputs exceed the threshold
            else {
                GetBalancesAndFormatResponse newinputs = getInputs(seed, security, 0, 0, totalValue);

                // If inputs with enough balance
                return addRemainder(seed, security, newinputs.getInputs(), bundle, tag, totalValue, remainder, signatureFragments);
            }
        } else {

            // If no input required, don't sign and simply finalize the bundle
            bundle.finalize(customCurl.clone());
            bundle.addTrytes(signatureFragments);

            List<Transaction> trxb = bundle.getTransactions();
            List<String> bundleTrytes = new ArrayList<>();

            for (Transaction trx : trxb) {
                bundleTrytes.add(trx.toTrytes());
            }
            Collections.reverse(bundleTrytes);
            return bundleTrytes;
        }
    }

    /**
     * Gets the inputs of a seed
     *
     * @param seed      Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security  The Security level of private key / seed.
     * @param start     Starting key index.
     * @param end       Ending key index.
     * @param threshold Min balance required.
     * @param tips      The starting points we walk back from to find the balance of the addresses
     * @throws ArgumentException is thrown when the specified input is not valid.
     **/
    public GetBalancesAndFormatResponse getInputs(String seed, int security, int start, int end, long threshold, final String... tips) throws ArgumentException {

        // validate the seed
        if ((!InputValidator.isValidSeed(seed))) {
            throw new IllegalStateException(Constants.INVALID_SEED_INPUT_ERROR);
        }

        if (!InputValidator.isValidSecurityLevel(security)) {
            throw new ArgumentException(Constants.INVALID_SECURITY_LEVEL_INPUT_ERROR);
        }

        // If start value bigger than end, return error
        // or if difference between end and start is bigger than 500 keys
        if ((start > end && end > 0) || end > (start + 500)) {
            throw new IllegalStateException(Constants.INVALID_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();

        //  Case 1: start and end
        //
        //  If start and end is defined by the user, simply iterate through the keys
        //  and call getBalances
        if (end != 0) {

            List<String> allAddresses = new ArrayList<>();

            for (int i = start; i < end; i++) {

                String address = CvtAPIUtils.newAddress(seed, security, i, false, customCurl.clone());
                allAddresses.add(address);
            }

            return getBalanceAndFormat(allAddresses, Arrays.asList(tips), threshold, start, stopWatch, security);
        }
        //  Case 2: iterate till threshold || end
        //
        //  Either start from index: 0 or start (if defined) until threshold is reached.
        //  Calls getNewAddress and deterministically generates and returns all addresses
        //  We then do getBalance, format the output and return it
        else {
            final GetNewAddressResponse res = getNewAddress(seed, security, start, false, 0, true);
            return getBalanceAndFormat(res.getAddresses(), Arrays.asList(tips), threshold, start, stopWatch, security);
        }
    }

    /**
     * Gets the balances and formats the output.
     *
     * @param addresses The addresses.
     * @param tips      The starting points we walk back from to find the balance of the addresses
     * @param threshold Min balance required.
     * @param start     Starting key index.
     * @param stopWatch the stopwatch.
     * @param security  The security level of private key / seed.
     * @return Inputs object.
     * @throws ArgumentException is thrown when the specified security level is not valid.
     **/
    public GetBalancesAndFormatResponse getBalanceAndFormat(final List<String> addresses, final List<String> tips, long threshold, int start, StopWatch stopWatch, int security) throws ArgumentException, IllegalStateException {

        if (!InputValidator.isValidSecurityLevel(security)) {
            throw new ArgumentException(Constants.INVALID_SECURITY_LEVEL_INPUT_ERROR);
        }

        GetBalancesResponse getBalancesResponse = getBalances(100, addresses, tips);
        List<String> balances = Arrays.asList(getBalancesResponse.getBalances());

        // If threshold defined, keep track of whether reached or not
        // else set default to true

        boolean thresholdReached = threshold == 0;

        List<Input> inputs = new ArrayList<>();
        long totalBalance = 0;

        for (int i = 0; i < addresses.size(); i++) {

            long balance = Long.parseLong(balances.get(i));

            if (balance > 0) {
                final Input newEntry = new Input(addresses.get(i), balance, start + i, security);

                inputs.add(newEntry);
                // Increase totalBalance of all aggregated inputs
                totalBalance += balance;

                if (!thresholdReached && totalBalance >= threshold) {
                    thresholdReached = true;
                    break;
                }
            }
        }

        if (thresholdReached) {
            return GetBalancesAndFormatResponse.create(inputs, totalBalance, stopWatch.getElapsedTimeMili());
        }
        throw new IllegalStateException(Constants.NOT_ENOUGH_BALANCE_ERROR);
    }


    /**
     * Gets the associated bundle transactions of a single transaction.
     * Does validation of signatures, total sum as well as bundle order.
     *
     * @param transaction The transaction encoded in trytes.
     * @return an array of bundle, if there are multiple arrays it means that there are conflicting bundles.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetBundleResponse getBundle(String transaction) throws ArgumentException {

        if (!InputValidator.isHash(transaction)) {
            throw new ArgumentException(Constants.INVALID_HASHES_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();

        Bundle bundle = traverseBundle(transaction, null, new Bundle());
        if (bundle == null) {
            throw new ArgumentException(Constants.INVALID_BUNDLE_ERROR);
        }

        if (!BundleValidator.isBundle(bundle)){
            throw new ArgumentException(Constants.INVALID_BUNDLE_ERROR);
        }

        return GetBundleResponse.create(bundle.getTransactions(), stopWatch.getElapsedTimeMili());
    }
    /**
     * Similar to getTransfers, just that it returns additional account data
     *
     * @param seed            Tryte-encoded seed. It should be noted that this seed is not transferred.
     * @param security        The Security level of private key / seed.
     * @param index           Key index to start search from. If the index is provided, the generation of the address is not deterministic.
     * @param checksum        Adds 9-tryte address checksum.
     * @param total           Total number of addresses to generate.
     * @param returnAll       If <code>true</code>, it returns all addresses which were deterministically generated (until findTransactions returns null).
     * @param start           Starting key index.
     * @param end             Ending key index.
     * @param inclusionStates If <code>true</code>, it gets the inclusion states of the transfers.
     * @param threshold       Min balance required.
     * @throws ArgumentException is thrown when the specified input is not valid.
     */
    public GetAccountDataResponse getAccountData(String seed, int security, int index, boolean checksum, int total, boolean returnAll, int start, int end, boolean inclusionStates, long threshold) throws ArgumentException {
        if (!InputValidator.isValidSecurityLevel(security)) {
            throw new ArgumentException(Constants.INVALID_SECURITY_LEVEL_INPUT_ERROR);
        }

        if (start > end || end > (start + 1000)) {
            throw new ArgumentException(Constants.INVALID_INPUT_ERROR);
        }

        StopWatch stopWatch = new StopWatch();

        GetNewAddressResponse gna = getNewAddress(seed, security, index, checksum, total, returnAll);
        GetTransferResponse gtr = getTransfers(seed, security, start, end, inclusionStates);
        GetBalancesAndFormatResponse gbr = getInputs(seed, security, start, end, threshold);

        return GetAccountDataResponse.create(gna.getAddresses(), gtr.getTransfers(), gbr.getInputs(), gbr.getTotalBalance(), stopWatch.getElapsedTimeMili());
    }

}
