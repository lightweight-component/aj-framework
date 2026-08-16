package com.ajaxjs.security.paramssign;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ParamsSignLocal extends ParamsSign {
    Set<String> usedNonce = ConcurrentHashMap.newKeySet();

    public ParamsSignLocal() {
        setSetNonceUsed(usedNonce::add);
        setContainsUsedNonce(usedNonce::contains);
    }
}
