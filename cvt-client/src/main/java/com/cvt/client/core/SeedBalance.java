package com.cvt.client.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SeedBalance
 *
 * @author cvt admin
 * Time:
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SeedBalance {
    private String seed;
    private List<AddressBalance> addressBalanceList;
}