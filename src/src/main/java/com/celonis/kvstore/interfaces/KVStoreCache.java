package com.celonis.kvstore.interfaces;

import java.util.Optional;

import com.celonis.kvstore.KVPair;

public interface KVStoreCache {
    void put(KVPair kvPair);
    void delete(String key);
    Optional<KVPair> get(String key);
}
