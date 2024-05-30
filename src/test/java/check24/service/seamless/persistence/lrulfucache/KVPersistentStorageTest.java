package com.celonis.kvstore;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celonis.kvstore.persistence.KVPersistentStorage;

class KVPersistentStorageTest {

    private KVPersistentStorage kvPersistentStorage;

    @BeforeEach
    void setUp() {
        kvPersistentStorage = new KVPersistentStorage();
    }

    @Test
    void retrieve_key_not_exists() {
        kvPersistentStorage.store(new KVPair("node-1", "nodeVal-1"));
        Optional<KVPair> result = kvPersistentStorage.retrieve("node-99");
        assertThat(result).isEmpty();
    }

    @Test
    void retrieve_key_exists() {
        kvPersistentStorage.store(new KVPair("node-1", "nodeVal-1"));
        Optional<KVPair> result = kvPersistentStorage.retrieve("node-1");
        assertThat(result.get().getValue()).isEqualTo("nodeVal-1");
    }
}