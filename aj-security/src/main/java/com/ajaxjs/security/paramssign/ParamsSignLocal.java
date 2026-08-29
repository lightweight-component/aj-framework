package com.ajaxjs.security.paramssign;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the params sign local component.
 */
public class ParamsSignLocal extends ParamsSign {
    Set<String> usedNonce = ConcurrentHashMap.newKeySet();

    /**
     * Executes the params sign local operation.
     */
    public ParamsSignLocal() {
        setSetNonceUsed(usedNonce::add);
        setContainsUsedNonce(usedNonce::contains);
    }
}
