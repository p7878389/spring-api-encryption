package com.shareworks.api.encryption.dto;

import com.shareworks.api.encryption.enums.EncryptionEnums;
import lombok.Data;

/**
 * @author martin.peng
 */
@Data
public class ApiSecurityRequestDTO<T> extends BaseRequestDTO {

    private Long timestamp;

    private EncryptionEnums securityType;

    private T data;

    private String sign;
}
