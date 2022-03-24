package com.shareworks.api.encryption.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author martin.peng
 */
public enum EncryptionEnums {

    AES, RSA, MD5;

    private static final List<EncryptionEnums> encryptionEnumsList = Arrays.asList(EncryptionEnums.values());

    public static EncryptionEnums getSecurityType(String securityType) {
        Optional<EncryptionEnums> optional = encryptionEnumsList.stream()
                .filter(encryptionEnums -> encryptionEnums.name().equalsIgnoreCase(securityType))
                .findFirst();
        if (!optional.isPresent()) {
            throw new RuntimeException("unknown encryption type " + securityType);
        }
        return optional.get();
    }
}
