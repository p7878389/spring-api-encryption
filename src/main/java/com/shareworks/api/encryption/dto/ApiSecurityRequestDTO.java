package com.shareworks.api.encryption.dto;

import com.shareworks.api.encryption.enums.EncryptionEnums;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author martin.peng
 */
@Data
public class ApiSecurityRequestDTO<T> extends BaseRequestDTO {

    @NotNull(message = "timestamp is null")
    private Long timestamp;

    @NotNull(message = "securityType is null")
    private EncryptionEnums securityType;

    private T data;

    @NotNull(message = "sign is null")
    private String sign;
}
