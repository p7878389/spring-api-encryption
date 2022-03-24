package com.shareworks.api.encryption.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author martin.peng
 */
@Component
@ConfigurationProperties(prefix = "rsa")
@Data
public class RsaProperties {

    private String privateKey;

    private String publicKey;
}
