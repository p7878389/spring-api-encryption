package com.shareworks.api.encryption.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author martin.peng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserApplicationKeyInfo implements Serializable {

    private String keyType;

    private String keyValue;
}
