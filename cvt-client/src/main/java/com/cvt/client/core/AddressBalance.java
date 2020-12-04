package com.cvt.client.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AddressBalance
 * @author cvt admin
 * Time:
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AddressBalance {
    private String address;
    private Long balance;
}
