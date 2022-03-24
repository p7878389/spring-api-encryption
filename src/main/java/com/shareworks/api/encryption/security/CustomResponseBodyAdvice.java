package com.shareworks.api.encryption.security;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shareworks.api.encryption.annotations.ApiSecurity;
import com.shareworks.api.encryption.config.RsaProperties;
import com.shareworks.api.encryption.constant.SignSysConstant;
import com.shareworks.api.encryption.contextholder.EncryptContextHolder;
import com.shareworks.api.encryption.dto.BaseResponseDTO;
import com.shareworks.api.encryption.enums.EncryptionEnums;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author martin.peng
 */
@ControllerAdvice
public class CustomResponseBodyAdvice implements ResponseBodyAdvice<BaseResponseDTO<?>> {

    @Resource
    private RsaProperties rsaProperties;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = returnType.getMethod();
        return method.isAnnotationPresent(ApiSecurity.class);
    }

    @SneakyThrows
    @Override
    public BaseResponseDTO<?> beforeBodyWrite(BaseResponseDTO<?> body, MethodParameter returnType, MediaType selectedContentType
            , Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        long timestamp = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();

        TreeMap<String, String> responseBodyMap = mapper.readValue(mapper.writeValueAsString(body), TreeMap.class);
        responseBodyMap.put(SignSysConstant.DATA_KEY, mapper.writeValueAsString(body.getData()));
        responseBodyMap.put(SignSysConstant.TIME_STAMP, String.valueOf(timestamp));
        responseBodyMap.remove(SignSysConstant.SUCCESS_KEY);
        responseBodyMap.remove(SignSysConstant.SIGN_KEY);
        String signData = responseBodyMap.entrySet().stream()
                .filter(entry -> !SignSysConstant.SIGN_KEY.equals(entry.getKey())
                        && StringUtils.isNotBlank(entry.getValue()))
                .map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(SignSysConstant.JOINER_KEY));
        body.setSign(sign(signData, EncryptContextHolder.getInstance().get()));
        body.setTimestamp(timestamp);
        EncryptContextHolder.getInstance().remove();
        return body;
    }

    private String sign(@NonNull String data, @NonNull EncryptionEnums securityType) {
        Assert.notNull(securityType, "securityType is null");
        switch (securityType) {
            case MD5:
                return SecureUtil.md5(data);
            case RSA:
                Sign sign = SecureUtil.sign(SignAlgorithm.MD5withRSA, rsaProperties.getPrivateKey(), null);
                return Base64.encode(sign.sign(data.getBytes(StandardCharsets.UTF_8)));
        }
        throw new RuntimeException("unknown signature type " + securityType.name());
    }
}
