import com.cvt.iri.conf.IotaConfig;
import com.cvt.iri.conf.TipSelConfig;
import com.cvt.iri.controllers.TipsViewModel;
import com.cvt.iri.controllers.TransactionViewModel;
import com.cvt.iri.hash.SpongeFactory;
import com.cvt.iri.network.Node;
import com.cvt.iri.network.TransactionRequester;
import com.cvt.iri.network.UDPReceiver;
import com.cvt.iri.network.replicator.Replicator;
import com.cvt.iri.service.TipsSolidifier;
import com.cvt.iri.service.tipselection.*;
import com.cvt.iri.service.tipselection.impl.*;
import com.cvt.iri.storage.CvtPersistable;
import com.cvt.iri.storage.Indexable;
import com.cvt.iri.storage.Persistable;
import com.cvt.iri.storage.rocksDB.RocksDBPersistenceProvider;
import com.cvt.iri.storage.sqllite.SqliteHelper;
import com.cvt.iri.utils.Pair;
import com.cvt.iri.zmq.MessageQ;
import com.google.gson.Gson;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

/**
 * Created by paul on
 */
public class Cvt {
    private static final Logger log = LoggerFactory.getLogger(Cvt.class);

    public final LedgerValidator ledgerValidator;
    public final Milestone milestone;
    public final Tangle tangle;
    public final TransactionValidator transactionValidator;
    public final TipsSolidifier tipsSolidifier;
    public final TransactionRequester transactionRequester;
    public final Node node;
    public final UDPReceiver udpReceiver;
    public final Replicator replicator;
    public final IotaConfig configuration;
    public final TipsViewModel tipsViewModel;
    public final MessageQ messageQ;
    public final TipSelector tipsSelector;

    public Cvt(IotaConfig configuration) throws IOException {
        this.configuration = configuration;
        Snapshot initialSnapshot = Snapshot.init(configuration).clone();
        tangle = new Tangle();
        messageQ = MessageQ.createWith(configuration);
        tipsViewModel = new TipsViewModel();
        transactionRequester = new TransactionRequester(tangle, messageQ);
        transactionValidator = new TransactionValidator(tangle, tipsViewModel, transactionRequester, messageQ,
                configuration);
        milestone = new Milestone(tangle, transactionValidator, messageQ, initialSnapshot, configuration);
        node = new Node(tangle, transactionValidator, transactionRequester, tipsViewModel, milestone, messageQ,
                configuration);
        replicator = new Replicator(node, configuration);
        udpReceiver = new UDPReceiver(node, configuration);
        ledgerValidator = new LedgerValidator(tangle, milestone, transactionRequester, messageQ);
        tipsSolidifier = new TipsSolidifier(tangle, transactionValidator, tipsViewModel);
        tipsSelector = createTipSelector(configuration);
    }

    public void init() throws Exception {
        initializeTangle();
        tangle.init();

        if (configuration.isRescanDb()) {
            rescan_db();
        }

        if (configuration.isRevalidate()) {
            tangle.clearColumn(com.cvt.iri.model.Milestone.class);
            tangle.clearColumn(com.cvt.iri.model.StateDiff.class);
            tangle.clearMetadata(com.cvt.iri.model.Transaction.class);
        }
        milestone.init(SpongeFactory.Mode.CURLP27, ledgerValidator);
        transactionValidator.init(configuration.isTestnet(), configuration.getMwm());
        tipsSolidifier.init();
        transactionRequester.init(configuration.getpRemoveRequest());
        udpReceiver.init();
        replicator.init();
        node.init();

        printBalance();
    }

    private void printBalance() throws Exception {
        List<CvtPersistable> balanceList = SqliteHelper.getBalances();
        log.info("余额详情如下：{} ", balanceList.size());
        log.info(StringUtils.center("Balance List", 80, "="));
        for (CvtPersistable transaction : balanceList) {
            log.info(new Gson().toJson(transaction));
        }
        log.info(StringUtils.center("Balance List", 80, "="));
    }

    private void rescan_db() throws Exception {
        //delete all transaction indexes
        tangle.clearColumn(com.cvt.iri.model.Address.class);
        tangle.clearColumn(com.cvt.iri.model.Bundle.class);
        tangle.clearColumn(com.cvt.iri.model.Approvee.class);
        tangle.clearColumn(com.cvt.iri.model.ObsoleteTag.class);
        tangle.clearColumn(com.cvt.iri.model.Tag.class);
        tangle.clearColumn(com.cvt.iri.model.Milestone.class);
        tangle.clearColumn(com.cvt.iri.model.StateDiff.class);
        tangle.clearMetadata(com.cvt.iri.model.Transaction.class);

        //rescan all tx & refill the columns
        TransactionViewModel tx = TransactionViewModel.first(tangle);
        int counter = 0;
        while (tx != null) {
            if (++counter % 10000 == 0) {
                log.info("Rescanned {} Transactions", counter);
            }
            List<Pair<Indexable, Persistable>> saveBatch = tx.getSaveBatch();
            saveBatch.remove(5);
            tangle.saveBatch(saveBatch);
            tx = tx.next(tangle);
        }
    }

    public void shutdown() throws Exception {
        milestone.shutDown();
        tipsSolidifier.shutdown();
        node.shutdown();
        udpReceiver.shutdown();
        replicator.shutdown();
        transactionValidator.shutdown();
        tangle.shutdown();
        messageQ.shutdown();
    }

    private void initializeTangle() throws Exception {
        switch (configuration.getMainDb()) {
            case "rocksdb": {
                tangle.addPersistenceProvider(new RocksDBPersistenceProvider(
                        configuration.getDbPath(),
                        configuration.getDbLogPath(),
                        configuration.getDbCacheSize()));
                break;
            }
            default: {
                throw new NotImplementedException("No such database type.");
            }
        }
        if (configuration.isExport()) {
            tangle.addPersistenceProvider(new FileExportProvider());
        }
        if (configuration.isZmqEnabled()) {
            tangle.addPersistenceProvider(new ZmqPublishProvider(messageQ));
        }
    }

    private TipSelector createTipSelector(TipSelConfig config) {
        EntryPointSelector entryPointSelector = new EntryPointSelectorImpl(tangle, milestone, config);
        RatingCalculator ratingCalculator = new CumulativeWeightCalculator(tangle);
        TailFinder tailFinder = new TailFinderImpl(tangle);
        Walker walker = new WalkerAlpha(tailFinder, tangle, messageQ, new SecureRandom(), config);
        return new TipSelectorImpl(tangle, ledgerValidator, entryPointSelector, ratingCalculator,
                walker, milestone, config);
    }
}
