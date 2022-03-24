package com.shareworks.api.encryption.contextholder;

import com.shareworks.api.encryption.enums.EncryptionEnums;

/**
 * @author martin.peng
 */
public class EncryptContextHolder extends AbstractContextHolder<EncryptionEnums> {

    private static final ThreadLocal<EncryptionEnums> CONTEXT_HOLDER = new ThreadLocal<>();

    private static final EncryptContextHolder INSTANCE = new EncryptContextHolder();

    @Override
    public ThreadLocal getThreadLocal() {
        return CONTEXT_HOLDER;
    }

    private EncryptContextHolder() {
    }

    public static EncryptContextHolder getInstance() {
        return INSTANCE;
    }
}
