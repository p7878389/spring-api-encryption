package com.shareworks.api.encryption.security.wrapper;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shareworks.api.encryption.constant.SignSysConstant;
import com.shareworks.api.encryption.contextholder.EncryptContextHolder;
import com.shareworks.api.encryption.domain.UserApplication;
import com.shareworks.api.encryption.domain.UserApplicationKeyInfo;
import com.shareworks.api.encryption.enums.EncryptionEnums;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author martin.peng
 */
@Data
public class HttpInputMessageWrapper implements HttpInputMessage {

    private HttpHeaders headers;
    private HttpInputMessage inputMessage;
    /**
     * 验签key
     */
    private UserApplication userApplication;

    public HttpInputMessageWrapper(HttpInputMessage inputMessage, UserApplication userApplication) {
        this.inputMessage = inputMessage;
        this.userApplication = userApplication;
        this.headers = inputMessage.getHeaders();
    }

    @Override
    public InputStream getBody() throws IOException {
        InputStream input = inputMessage.getBody();

        //FIXME 1.参数解密处理
        String body = IOUtils.toString(input, StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(body);
        Iterator<String> iterator = jsonNode.fieldNames();
        TreeMap<String, String> bodyMap = new TreeMap<>();
        while (iterator.hasNext()) {
            String fieldName = iterator.next();
            JsonNode fieldJsonNode = jsonNode.get(fieldName);
            bodyMap.put(fieldName, fieldJsonNode instanceof ObjectNode ? fieldJsonNode.toString() : fieldJsonNode.asText());
        }

        String securityType = bodyMap.remove(SignSysConstant.SECURITY_TYPE_KEY);
        Assert.notNull(securityType, "securityType not null");
        EncryptionEnums encryptionEnums = EncryptionEnums.getSecurityType(securityType);

        String requestSign = bodyMap.remove(SignSysConstant.SIGN_KEY);
        Assert.notNull(securityType, "sign not null");

        String securityBody = bodyMap.entrySet().stream()
                .map(entry -> entry.getKey() + SignSysConstant.SIGN_FIELD_JOINER_KEY + entry.getValue())
                .collect(Collectors.joining(SignSysConstant.JOINER_KEY));
        verify(encryptionEnums, requestSign, securityBody);
        EncryptContextHolder.getInstance().set(encryptionEnums);
        return IOUtils.toInputStream(body, StandardCharsets.UTF_8);
    }

    private void verify(EncryptionEnums securityType, String requestSign, String securityBody) {
        Map<String, String> securityKeyMap = userApplication.getUserApplicationKeyInfoList()
                .stream()
                .collect(Collectors.toMap(UserApplicationKeyInfo::getKeyType, UserApplicationKeyInfo::getKeyValue));
        String dataSign;
        switch (securityType) {
            case MD5:
                dataSign = SecureUtil.md5(securityBody);
                if (!StringUtils.equals(requestSign, dataSign)) {
                    throw new RuntimeException("MD5 signature verification failed");
                }
                break;
            case RSA:
                Sign sign = SecureUtil.sign(SignAlgorithm.MD5withRSA, null, securityKeyMap.get(securityType.name()));
                if (!sign.verify(securityBody.getBytes(StandardCharsets.UTF_8), Base64.decode(requestSign))) {
                    throw new RuntimeException("RSA signature verification failed");
                }
                break;
        }
    }
}
