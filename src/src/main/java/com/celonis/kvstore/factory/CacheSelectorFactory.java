package com.celonis.kvstore.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.celonis.kvstore.mixedcache.MixedKVStore;
import com.celonis.kvstore.interfaces.KVStoreCache;
import com.celonis.kvstore.lfucache.LFUKVStore;
import com.celonis.kvstore.lrucache.LRUKVStore;
import com.celonis.kvstore.persistence.KVPersistentStorage;

import jakarta.annotation.PostConstruct;

@Component
public class CacheSelectorFactory {

    @Value("${cache.maxSize}") long maxSize;

    @Value("${cache.strategy}") CacheStrategyEnum cacheStrategyType;

    @Autowired
    private KVPersistentStorage kvPersistentStorage;

    private KVStoreCache cacheType;

    @PostConstruct
    private void postConstruct() {
        switch (cacheStrategyType) {
        case LFU:
            cacheType = new LFUKVStore(maxSize, kvPersistentStorage);
            break;
        case LRU:
            cacheType = new LRUKVStore(maxSize, kvPersistentStorage);
            break;
        case MIXED:
            cacheType = new MixedKVStore(maxSize, kvPersistentStorage);
            break;
        }
    }

    public KVStoreCache getCacheType() {
        return cacheType;
    }
}
