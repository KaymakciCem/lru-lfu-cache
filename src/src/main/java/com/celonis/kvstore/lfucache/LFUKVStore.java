package com.celonis.kvstore.lfucache;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.celonis.kvstore.KVPair;
import com.celonis.kvstore.persistence.KVPersistentStorage;
import com.celonis.kvstore.interfaces.KVStoreCache;

@Component
public class LFUKVStore implements KVStoreCache {
    private static Logger logger = LoggerFactory.getLogger(LFUKVStore.class);
    private ConcurrentHashMap<String, KVPair> mapNodes;
    private ConcurrentHashMap<Integer, LinkedHashSet<String>> frequencyListMap;
    private ConcurrentHashMap<String, KeyFrequency> frequencyCounterMap;

    private long maxSize;
    private int minFrequency;

    private final KVPersistentStorage kvPersistentStorage;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LFUKVStore(@Value("${cache.maxSize}") long maxSize,
                      KVPersistentStorage kvPersistentStorage) {
        this.maxSize = maxSize;
        this.kvPersistentStorage = kvPersistentStorage;

        mapNodes = new ConcurrentHashMap<>();
        frequencyListMap = new ConcurrentHashMap<>();
        frequencyCounterMap = new ConcurrentHashMap<>();
    }

    @Override
    public void put(KVPair kvPair) {
        lock.writeLock().lock();
        try {
            if (maxSize == 0) {
                return;
            }

            if (mapNodes.size() < maxSize) {
                logger.info("Cache is full. LRU value will be removed");
                mapNodes.put(kvPair.getKey(), kvPair);
            } else {
                if (mapNodes.get(kvPair.getKey()) != null) {
                    incrementFrequency(kvPair);
                    incrementFrequencyCounter(kvPair);
                    return;
                }

                String lfuKey = frequencyListMap.get(minFrequency).iterator().next();
                removeLFUKey(lfuKey);
                mapNodes.remove(lfuKey);
                frequencyCounterMap.remove(lfuKey);

                mapNodes.put(kvPair.getKey(), kvPair);
            }

            updateKeyFrequency(kvPair);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(String key) {
        lock.writeLock().lock();
        try {
            logger.info("Removing the key {} from the cache.", key);

            mapNodes.remove(key);
            removeFrequencies(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<KVPair> get(String key) {
        lock.readLock().lock();
        try {
            final KVPair kvPair = mapNodes.compute(key, (k, v) -> {
                if (Objects.nonNull(v)) {
                    updateKeyFrequency(v);
                }
                return v;
            });

            if (kvPair == null) {
                Optional<KVPair> dataRetrieved = kvPersistentStorage.retrieve(key);
                if (dataRetrieved.isPresent()) {
                    logger.info("Data retrieved from persistent storage.");
                    return dataRetrieved;
                }
                return Optional.empty();
            }

            return Optional.ofNullable(kvPair);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void incrementFrequencyCounter(KVPair kvPair) {
        // increment the frequency of the incoming key
        frequencyCounterMap.compute(kvPair.getKey(), (k, value) -> {
            if (Objects.isNull(value)) {
                value = new KeyFrequency();
            }
            value.frequency++;
            value.key = kvPair.getKey();
            return value;
        });
    }

    private void incrementFrequency(KVPair kvPair) {
        KeyFrequency keyFrequency = frequencyCounterMap.get(kvPair.getKey());

        //move key to next frequency
        frequencyListMap.compute(keyFrequency.frequency + 1, (k, value) -> {
            if (CollectionUtils.isEmpty(value)) {
                value = new LinkedHashSet<>();
            }
            value.add(kvPair.getKey());
            return value;
        });

        // remove the key from the current frequency
        frequencyListMap.compute(keyFrequency.frequency, (k, value) -> {
            value.remove(kvPair.getKey());
            if (CollectionUtils.isEmpty(value)) {
                minFrequency = keyFrequency.frequency + 1;
            }
            return value;
        });
    }

    private void removeLFUKey(final String lfuKey) {
        frequencyListMap.compute(minFrequency, (k, value) -> {
            if (!CollectionUtils.isEmpty(value)) {
                value.remove(lfuKey);
            }
            kvPersistentStorage.store(mapNodes.get(lfuKey));
            return value;
        });
    }

    private void updateKeyFrequency(KVPair kvPair) {

        // moving the key into the next frequency
        if (frequencyCounterMap.containsKey(kvPair.getKey())) {
            incrementFrequency(kvPair);
        } else {
            //add the key to the frequency 1
            frequencyListMap.compute(1, (k, value) -> {
                if (CollectionUtils.isEmpty(value)) {
                    value = new LinkedHashSet<>();
                }
                value.add(kvPair.getKey());
                return value;
            });
            minFrequency = 1;
        }

        incrementFrequencyCounter(kvPair);
    }

    private void removeFrequencies(String key) {
        KeyFrequency removedKey = frequencyCounterMap.remove(key);
        frequencyListMap.compute(removedKey.frequency, (k, value) -> {
            if (Objects.nonNull(value)) {
                value.remove(key);
            }
            return value;
        });
    }

    public static class KeyFrequency {
        private int frequency;
        private String key;
    }
}
