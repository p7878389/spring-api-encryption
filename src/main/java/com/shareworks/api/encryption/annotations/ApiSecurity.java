package com.shareworks.api.encryption.annotations;

import com.shareworks.api.encryption.enums.EncryptionEnums;

import java.lang.annotation.*;

/**
 * @author martin.peng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSecurity {

    /**
     * 加解密类型
     *
     * @return
     */
    EncryptionEnums securityType() default EncryptionEnums.RSA;

    /**
     * 请求加密
     *
     * @return
     */
    boolean requestSecurity() default true;

    /**
     * 返回结果加密
     *
     * @return
     */
    boolean responseSecurity() default true;
}
