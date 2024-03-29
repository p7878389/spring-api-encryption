### spring接口验签

- 通过重写**RequestBodyAdvice、ResponseBodyAdvice**来实现接口签名验签功能
- 实现**HttpInputMessage**接口 重写**getBody**方法，在该方法中实现验签逻辑

~~~java
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
import com.shareworks.api.encryption.domain.UserApplication;
import com.shareworks.api.encryption.domain.UserApplicationKeyInfo;
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
                .map(entry -> entry.getKey() + "=" + entry.getValue())
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
~~~

- 实现**RequestBodyAdvice**、**ResponseBodyAdvice**两个类重写里面关键方法
    - **RequestBodyAdvice**重写**supports**、**beforeBodyRead**方法
    - **ResponseBodyAdvice**重写**supports**、**beforeBodyWrite**方法
  > **supports**方法用于判断是否包含指定注解，请求或返回是否需要签名验签；
  > **beforeBodyRead**用于request请求验签；**beforeBodyWrite**方法用于response签名

    - **RequestBodyAdvice**重写关键方法
  ~~~java
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = methodParameter.getMethod();
        ApiSecurity apiSecurity = method.getAnnotation(ApiSecurity.class);
        if (Objects.isNull(apiSecurity)) {
            return false;
        }
        return apiSecurity.requestSecurity();
    }

    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return new HttpInputMessageWrapper(inputMessage, userApplicationService.getDefaultSecurityKey());
    }
  ~~~

    - **ResponseBodyAdvice**重写关键方法
  ~~~java
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = returnType.getMethod();
        ApiSecurity apiSecurity = method.getAnnotation(ApiSecurity.class);
        if (Objects.isNull(apiSecurity)) {
            return false;
        }
        return apiSecurity.responseSecurity();
    }

    @SneakyThrows
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
  ~~~