package com.shareworks.api.encryption.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author martin.peng
 */
@Data
public class TestRequestDTO implements Serializable {
    private String key;
    ValueDTO valueDTO;

    @Data
   public static class ValueDTO {
        private String value;
    }
}
