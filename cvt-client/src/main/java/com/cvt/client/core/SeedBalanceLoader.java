package com.cvt.client.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Seed balance loader
 *
 * @author cvt admin
 * Time: 2018/11/19 : 14:34
 */
@Slf4j
@Component
public class SeedBalanceLoader {

    @Getter
    private Map<String, SeedBalance> seedBalanceMap = new LinkedHashMap<>();

    private static final String DATA_PATH = "seed_balance.txt";
    private static final String SEED_PREFIX = "Seed: ";
    public static String LAST_SEED = "";

    public Set<String> getSeeds() {
        return seedBalanceMap.keySet();
    }

    public Long getBalance(String seed) {
        SeedBalance seedBalance = seedBalanceMap.get(seed);
        if (null == seedBalance) {
            return 0L;
        }
        AtomicReference<Long> balance = new AtomicReference<>(0L);
        seedBalance.getAddressBalanceList().forEach(item -> {
            balance.updateAndGet(v -> v + item.getBalance());
        });
        return balance.get();
    }

    @PostConstruct
    public void load() throws Exception {
        Resource res = new ClassPathResource(DATA_PATH);
        List<String> lines = IOUtils.readLines(res.getInputStream(), "UTF-8");
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            if (line.startsWith(SEED_PREFIX)) {
                LAST_SEED = line.substring(line.indexOf(SEED_PREFIX) + SEED_PREFIX.length());
                seedBalanceMap.computeIfAbsent(LAST_SEED, key -> new SeedBalance(key, new ArrayList<>()));
            } else {
                SeedBalance seedBalance = seedBalanceMap.get(LAST_SEED);
                String[] tmp = line.split(":");
                if (StringUtils.isBlank(tmp[0]) || StringUtils.isBlank(tmp[1])) {
                    continue;
                }
                seedBalance.getAddressBalanceList().add(new AddressBalance(tmp[0].trim(), Long.valueOf(tmp[1].trim())));
            }
        }
    }


    public void dump() {
        final StringBuilder sb = new StringBuilder();
        seedBalanceMap.forEach((key, value) -> {
            sb.append("\n").append(SEED_PREFIX).append(key).append("\n");
            value.getAddressBalanceList().forEach(item -> {
                sb.append(item.getAddress()).append(": ").append(item.getBalance()).append("\n");
            });
        });
        log.info(sb.toString());
    }

    protected String rndSeed(String... exceptSeeds) {
        Set<String> seedSet = getSeeds();
        if (null != exceptSeeds) {
            seedSet.removeAll(Sets.newHashSet(exceptSeeds));
        }
        return Lists.newArrayList(seedSet).get(new Random().nextInt(seedSet.size()));
    }
}