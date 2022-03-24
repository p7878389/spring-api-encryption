package com.shareworks.api.encryption.security.wrapper;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shareworks.api.encryption.constant.SignSysConstant;
import com.shareworks.api.encryption.contextholder.EncryptContextHolder;
import com.shareworks.api.encryption.dto.ApiSecurityRequestDTO;
import com.shareworks.api.encryption.dto.TestRequestDTO;
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
    private String securityKey;
    /**
     * 验签类型
     */
    private EncryptionEnums encryptionType;

    public HttpInputMessageWrapper(HttpInputMessage inputMessage, String securityKey, EncryptionEnums encryptionType) {
        this.inputMessage = inputMessage;
        this.securityKey = securityKey;
        this.encryptionType = encryptionType;
        this.headers = inputMessage.getHeaders();
    }

    @Override
    public InputStream getBody() throws IOException {
        InputStream input = inputMessage.getBody();

        //FIXME 1.参数解密处理
        String body = IOUtils.toString(input, "UTF-8");

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
                .map(entry -> entry.getKey() + "=" + entry.getValue().toString())
                .collect(Collectors.joining(SignSysConstant.JOINER_KEY));
        verify(encryptionEnums, requestSign, securityBody);
        EncryptContextHolder.getInstance().set(encryptionEnums);
        return IOUtils.toInputStream(body, StandardCharsets.UTF_8);
    }

    private void verify(EncryptionEnums securityType, String requestSign, String securityBody) {
        String dataSign;
        switch (securityType) {
            case MD5:
                dataSign = SecureUtil.md5(securityBody);
                if (!StringUtils.equals(requestSign, dataSign)) {
                    throw new RuntimeException("MD5 signature verification failed");
                }
                break;
            case RSA:
                Sign sign = SecureUtil.sign(SignAlgorithm.MD5withRSA, null, securityKey);
                if (!sign.verify(securityBody.getBytes(StandardCharsets.UTF_8), Base64.decode(requestSign))) {
                    throw new RuntimeException("RSA signature verification failed");
                }
                break;
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        TestRequestDTO requestDTO = new TestRequestDTO();
        requestDTO.setKey("11111");
        TestRequestDTO.ValueDTO valueDTO = new TestRequestDTO.ValueDTO();
        valueDTO.setValue("22222");
        requestDTO.setValueDTO(valueDTO);
        ObjectMapper objectMapper = new ObjectMapper();

        ApiSecurityRequestDTO<TestRequestDTO> apiSecurityRequestDTO = new ApiSecurityRequestDTO<>();
        apiSecurityRequestDTO.setData(requestDTO);
        apiSecurityRequestDTO.setSecurityType(EncryptionEnums.RSA);
        apiSecurityRequestDTO.setTimestamp(System.currentTimeMillis());

        String requestBody = objectMapper.writeValueAsString(apiSecurityRequestDTO);
        System.out.println(requestBody);

        TreeMap<String, String> signBodyTreeMap = objectMapper.readValue(objectMapper.writeValueAsString(apiSecurityRequestDTO), TreeMap.class);
        signBodyTreeMap.put(SignSysConstant.DATA_KEY, objectMapper.writeValueAsString(requestDTO));
        signBodyTreeMap.put(SignSysConstant.TIME_STAMP, String.valueOf(apiSecurityRequestDTO.getTimestamp()));
        signBodyTreeMap.remove(SignSysConstant.SIGN_KEY);
        signBodyTreeMap.remove(SignSysConstant.SECURITY_TYPE_KEY);

        String data = signBodyTreeMap.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(SignSysConstant.JOINER_KEY));

        System.out.println("data = " + data);

        RSA privateKey = new RSA("MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKE93zRXKE/q4iaok2R7uvOh40+ysJl6B4bUUjXBp+RHivwowdwX9ccl2XhqXLSCKLd2xTvel7RfKRXn5ix754laVMDlhJehFDslEu3GZI4sDS7SwBTzB+lVhxIDfpM/+9GGd6JtHRavwjBmT6Ul2R9KuS1332q4dZ1FbpZEu/KDAgMBAAECgYAi2vQgJWMie3ztrG1IQHL2bZ1swgzo8HcmD0nCMx2u/EQwJENb55YKucTcAeoOX0CcAEG8om6a4CNKh/m6zgIL1di//2gDpqeOS+QWszq5LMqCX67S6KCKAQO5MLY39+Hxnltv0h2lr8ZdB5wZSfLrSyMQEjahSCD60pZWDjpBkQJBAP/2Zp/i4kxVu0jAgukBsFuPZYOVcTN3LyVDwvh1gbKiyEmEbZenC683/eL8yUlcJE+XWR9i786eVUfBMgOibrsCQQChQ+s1v27F5VCnPi+t4+FPjGJS6+zsn83ygQhhgwel2j8sDa0LbaA3x4fuXnYym1oZ73IznEZQyCzpXROBseLZAkBPBLpZ0aka97yBkSzY0fSnz3T/UjtIMBsV3f1qxpvrkeVTDNboYmFxDlAKqw9Y3mE3UPnqfpiZqLebF0FhbdvRAkEAj3b7xioS4w0Zjl9dTWaAaCNsyXZHpU6ZihbOnOlToXGiZ4+XFaamNVlWDXKN1oGsS4xgXZe2WmFqywTZuLTHKQJAdzSstExq7ehDO5YN52rYEzxP0OkCcTrLMs/x7JYj6yahPylS/6cIh1KtiPLHul0yMx0nYh6nTfnNlUnHeYBEzw==", null);
        Sign privateSign = SecureUtil.sign(SignAlgorithm.MD5withRSA, privateKey.getPrivateKeyBase64(), null);
        String signStr = Base64.encode(privateSign.sign(data));
        System.out.println("signStr = " + signStr);
        Sign sign = SecureUtil.sign(SignAlgorithm.MD5withRSA, null, "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQChPd80VyhP6uImqJNke7rzoeNPsrCZegeG1FI1wafkR4r8KMHcF/XHJdl4aly0gii3dsU73pe0XykV5+Yse+eJWlTA5YSXoRQ7JRLtxmSOLA0u0sAU8wfpVYcSA36TP/vRhneibR0Wr8IwZk+lJdkfSrktd99quHWdRW6WRLvygwIDAQAB");
        System.out.println(sign.verify(data.getBytes(StandardCharsets.UTF_8), Base64.decode(signStr)));
    }
}
