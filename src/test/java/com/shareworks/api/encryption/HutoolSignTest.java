package com.shareworks.api.encryption;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shareworks.api.encryption.constant.SignSysConstant;
import com.shareworks.api.encryption.dto.ApiSecurityRequestDTO;
import com.shareworks.api.encryption.dto.TestRequestDTO;
import com.shareworks.api.encryption.enums.EncryptionEnums;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class HutoolSignTest {

    @Test
    public void md5WithRSA() throws JsonProcessingException {

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
