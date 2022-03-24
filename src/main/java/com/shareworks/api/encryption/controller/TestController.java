package com.shareworks.api.encryption.controller;

import com.shareworks.api.encryption.annotations.ApiSecurity;
import com.shareworks.api.encryption.dto.ApiSecurityRequestDTO;
import com.shareworks.api.encryption.dto.BaseResponseDTO;
import com.shareworks.api.encryption.dto.TestRequestDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author martin.peng
 */
@RestController
public class TestController {

    @PostMapping("/test")
    @ApiSecurity
    public BaseResponseDTO<TestRequestDTO> test(@RequestBody @Validated ApiSecurityRequestDTO<TestRequestDTO> requestDTO) {
        BaseResponseDTO<TestRequestDTO> baseResponseDTO = new BaseResponseDTO<>();
        baseResponseDTO.setCode("00000");
        baseResponseDTO.setMessage("成功");
        baseResponseDTO.setData(requestDTO.getData());
        return baseResponseDTO;
    }

    @PostMapping("/test1")
    public BaseResponseDTO<TestRequestDTO> test1(@RequestBody ApiSecurityRequestDTO<TestRequestDTO> requestDTO) {
        BaseResponseDTO<TestRequestDTO> baseResponseDTO = new BaseResponseDTO<>();
        baseResponseDTO.setCode("00000");
        baseResponseDTO.setMessage("成功");
        baseResponseDTO.setData(requestDTO.getData());
        return baseResponseDTO;
    }
}
