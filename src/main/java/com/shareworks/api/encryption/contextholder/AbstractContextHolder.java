package com.shareworks.api.encryption.contextholder;

/**
 * @author martin.peng
 */
public abstract class AbstractContextHolder<T> {

    /**
     * @return
     */
    public abstract ThreadLocal<T> getThreadLocal();

    public T get() {
        return getThreadLocal().get();
    }

    public void set(T t) {
        getThreadLocal().set(t);
    }

    public void remove() {
        getThreadLocal().remove();
    }
}
