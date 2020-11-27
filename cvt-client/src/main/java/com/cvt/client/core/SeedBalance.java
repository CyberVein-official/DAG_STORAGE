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
 * Time: 2018/11/19 : 15:48
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SeedBalance {
    private String seed;
    private List<AddressBalance> addressBalanceList;
}