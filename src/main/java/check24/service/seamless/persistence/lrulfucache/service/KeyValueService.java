package com.celonis.kvstore.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.celonis.kvstore.dto.KVStoreDto;
import com.celonis.kvstore.KVPair;
import com.celonis.kvstore.factory.CacheSelectorFactory;

@Service
public class KeyValueService {
    private final CacheSelectorFactory cacheSelectorFactory;

    public KeyValueService(CacheSelectorFactory cacheSelectorFactory) {
        this.cacheSelectorFactory = cacheSelectorFactory;
    }

    public void put(String key, String value) {
        cacheSelectorFactory.getCacheType()
                            .put(new KVPair(key, value));
    }

    public void delete(String key) {
        cacheSelectorFactory.getCacheType()
                            .delete(key);
    }

    public Optional<KVStoreDto> get(String key) {
        return cacheSelectorFactory.getCacheType()
                                   .get(key)
                                   .map(kvPair -> new KVStoreDto(kvPair.getKey(), kvPair.getValue()));
    }
}
