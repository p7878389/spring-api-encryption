package com.shareworks.api.encryption.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author martin.peng
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponseDTO<T> {

    private String code;

    private String message;

    private T data;

    private Long timestamp;

    private String sign;

    public boolean isSuccess() {
        return "00000".equals(code);
    }
}
