package com.shareworks.api.encryption.services;

import com.shareworks.api.encryption.config.RsaProperties;
import com.shareworks.api.encryption.domain.UserApplication;
import com.shareworks.api.encryption.domain.UserApplicationKeyInfo;
import com.shareworks.api.encryption.enums.EncryptionEnums;
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

    public UserApplication getDefaultSecurityKey() {

        UserApplication userApplication = UserApplication.builder()
                .applicationName("test")
                .build();

        userApplication.addKeyInfo(UserApplicationKeyInfo.builder()
                .keyType(EncryptionEnums.RSA.name())
                .keyValue(rsaProperties.getPublicKey())
                .build());
        return userApplication;
    }
}
