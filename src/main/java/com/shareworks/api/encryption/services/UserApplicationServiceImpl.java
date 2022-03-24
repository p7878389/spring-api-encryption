package com.shareworks.api.encryption.services;

import com.shareworks.api.encryption.config.RsaProperties;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author martin.peng
 */
@Service
@Data
public class UserApplicationServiceImpl {

    @Resource
    private RsaProperties rsaProperties;

    public String getDefaultSecurityKey() {
        return rsaProperties.getPublicKey();
    }
}
