package com.cvt.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * UploadConfig
 *
 * @author cvt admin
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {

    private String tmpPath;

    private String sharedUploadPath;

    private String indexPath;
}
