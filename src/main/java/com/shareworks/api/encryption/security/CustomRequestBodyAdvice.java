package com.shareworks.api.encryption.security;

import com.shareworks.api.encryption.annotations.ApiSecurity;
import com.shareworks.api.encryption.security.wrapper.HttpInputMessageWrapper;
import com.shareworks.api.encryption.services.UserApplicationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author martin.peng
 */
@Slf4j
@ControllerAdvice
public class CustomRequestBodyAdvice implements RequestBodyAdvice {

    @Resource
    private UserApplicationServiceImpl userApplicationService;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = methodParameter.getMethod();
        ApiSecurity apiSecurity = method.getAnnotation(ApiSecurity.class);
        if (Objects.isNull(apiSecurity)) {
            return false;
        }
        return apiSecurity.requestSecurity();
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return new HttpInputMessageWrapper(inputMessage, userApplicationService.getDefaultSecurityKey());
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
