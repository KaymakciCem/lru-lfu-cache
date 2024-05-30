package com.celonis.kvstore.lrucache;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.celonis.kvstore.KVPair;
import com.celonis.kvstore.persistence.KVPersistentStorage;
import com.celonis.kvstore.interfaces.KVStoreCache;

@Component
public class LRUKVStore implements KVStoreCache {
    private static Logger logger = LoggerFactory.getLogger(LRUKVStore.class);
    private ConcurrentHashMap<String, KVPair> cache;


    private LruData doublyList;
    private long maxSize;
    private final KVPersistentStorage kvPersistentStorage;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LRUKVStore(@Value("${cache.maxSize}") long maxSize,
                      KVPersistentStorage kvPersistentStorage) {
        this.maxSize = maxSize;
        this.kvPersistentStorage = kvPersistentStorage;

        doublyList = new LruData();
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public void put(KVPair kvPair) {
        if (maxSize == 0) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (cache.size() < maxSize) {
                if (cache.containsKey(kvPair.getKey())) {
                    doublyList.removeNode(cache.get(kvPair.getKey()));
                    doublyList.addNode(kvPair);
                } else {
                    doublyList.addNode(kvPair);
                }

                cache.put(kvPair.getKey(), kvPair);
            } else {
                logger.info("Cache is full. LRU value will be removed");

                KVPair removedElement = cache.remove(doublyList.getTail()
                                                               .getPrevious()
                                                               .getKey());

                doublyList.removeNode(doublyList.getTail().getPrevious());
                doublyList.addNode(kvPair);
                cache.put(kvPair.getKey(), kvPair);

                kvPersistentStorage.store(removedElement);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(String key) {
        lock.writeLock().lock();
        logger.info("Removing the key {} from the cache.", key);
        doublyList.removeNode(cache.remove(key));
    }

    @Override
    public Optional<KVPair> get(String key) {

        try {
            final KVPair kvPair = cache.get(key);
            if (kvPair != null) {
                logger.info("Data retrieved from cache. Id {}, value {}", kvPair.getKey(), kvPair.getValue());
            }

            if (kvPair == null) {
                Optional<KVPair> dataRetrieved = kvPersistentStorage.retrieve(key);
                if (dataRetrieved.isPresent()) {
                    logger.info("Data retrieved from persistent storage. Id {}, value {}", dataRetrieved.get().getKey(), dataRetrieved.get().getValue());
                    cache.put(key, kvPair);
                    return dataRetrieved;
                }

                return Optional.empty();
            }

            lock.writeLock().lock();
            doublyList.removeNode(kvPair);
            doublyList.addNode(kvPair);
            return Optional.of(kvPair);
        } finally {
            lock.readLock().unlock();
        }
    }
}
