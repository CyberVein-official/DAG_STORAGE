package com.cvt.client.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CVT配置
 *
 * @author cvt admin
 * Time:
 */
@Configuration
public class CvtConfig {

    @Getter
    String testTag = "CVTJAVASPAM999999999999999";

    @Getter
    String testMessage = "JUSTANOTHERCVTTEST";

    public static final int SECURITY_LEVEL = 2;
    public static final int DEPTH = 9;
    public static final int MIN_WEIGHT = 14;

    @Value("${cvt.protocol}")
    private String protocol;

    @Value("${cvt.host}")
    private String host;

    @Value("${cvt.port}")
    private String port;

    @Bean
    public CvtAPI api() {
        return new CvtAPI.Builder()
                .protocol(protocol)
                .host(host)
                .port(port)
                .build();
    }
}
