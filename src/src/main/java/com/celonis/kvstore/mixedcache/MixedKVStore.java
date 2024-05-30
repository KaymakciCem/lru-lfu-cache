package com.celonis.kvstore.mixedcache;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.celonis.kvstore.KVPair;
import com.celonis.kvstore.interfaces.KVStoreCache;
import com.celonis.kvstore.persistence.KVPersistentStorage;

@Component
public class MixedKVStore implements KVStoreCache {
    private long maxSize;

    public MixedKVStore(@Value("${cache.maxSize}") long maxSize,
                        KVPersistentStorage kvPersistentStorage) {
    }

    @Override
    public void put(KVPair kvPair) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<KVPair> get(String key) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}