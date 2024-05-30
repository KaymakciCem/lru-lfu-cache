package com.celonis.kvstore;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.celonis.kvstore.lfucache.LFUKVStore;
import com.celonis.kvstore.persistence.KVPersistentStorage;

@ExtendWith(MockitoExtension.class)
class LFUKVStoreTest {

    @Mock
    private KVPersistentStorage kvPersistentStorage;
    private LFUKVStore lfuKVStore;

    @BeforeEach
    void setUp() {
        lfuKVStore = new LFUKVStore(3, kvPersistentStorage);
    }

    @Test
    void put_element_cache_size_zero() {
        lfuKVStore.put(new KVPair("node-1", "nodeVal-1"));
        verifyNoInteractions(kvPersistentStorage);
    }

    @Test
    void put_one_element() {
        lfuKVStore.put(new KVPair("node-1", "nodeVal-1"));

        final Optional<KVPair> kvPair = lfuKVStore.get("node-1");
        assertThat(kvPair.get().getValue()).isEqualTo("nodeVal-1");

        verifyNoInteractions(kvPersistentStorage);
    }

    @Test
    void put_multiple_elements() {
        lfuKVStore.put(new KVPair("node-1", "nodeVal-1"));
        lfuKVStore.put(new KVPair("node-2", "nodeVal-2"));

        final Optional<KVPair> kvPair = lfuKVStore.get("node-1");
        assertThat(kvPair.get().getValue()).isEqualTo("nodeVal-1");

        final Optional<KVPair> kvPair1 = lfuKVStore.get("node-2");
        assertThat(kvPair1.get().getValue()).isEqualTo("nodeVal-2");

        verifyNoInteractions(kvPersistentStorage);
    }

    @Test
    void multiple_Keys_Stored_And_LFUKey_ShouldBeRetrievedFromStorage() {
        lfuKVStore.put(new KVPair("node-1", "nodeVal-1"));
        lfuKVStore.put(new KVPair("node-2", "nodeVal-2"));
        lfuKVStore.put(new KVPair("node-3", "nodeVal-3"));
        lfuKVStore.put(new KVPair("node-4", "nodeVal-4"));

        when(kvPersistentStorage.retrieve("node-1")).thenReturn(Optional.of(new KVPair("node-1", "nodeVal-1")));

        for (int i = 1; i <= 4; i++) {
            final Optional<KVPair> result = lfuKVStore.get("node-" + i);
            assertThat(result.get().getValue()).isEqualTo("nodeVal-" + i);
        }

        verify(kvPersistentStorage, times(1)).store(new KVPair("node-1", "nodeVal-1"));
    }

    // "This test has been sourced from https://www.yegor256.com/2018/03/27/how-to-test-thread-safety.html"
    @Test
    void concurrent_requests_3_of_them_ShouldBeRetrievedFromStorage() throws ExecutionException, InterruptedException {
        int threads = 6;
        ExecutorService service = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();
        Collection<Future<Integer>> futures = new ArrayList<>(threads);
        for (int t = 0; t < threads; ++t) {
            final String keyId = String.format("%d", t+1);
            futures.add(
                    service.submit(
                            () -> {
                                latch.await();
                                if (running.get()) {
                                    overlaps.incrementAndGet();
                                    System.out.println("Overlapping: " + keyId);
                                }
                                running.set(true);
                                lfuKVStore.put(new KVPair(keyId, "nodeVal-" + keyId));
                                Optional<KVPair> kvPair = lfuKVStore.get(keyId);
                                running.set(false);
                                return kvPair.map(pair -> Integer.valueOf(pair.getKey()))
                                             .orElse(-1);
                            }
                    )
            );
        }
        latch.countDown();
        List<Integer> ids = new ArrayList<>();
        for (Future<Integer> f : futures) {
            ids.add(f.get());
        }

        // this tells us there are multiple threads that are trying to access the same resource
        assertThat(overlaps.get()).isPositive();

        for (int i = 0; i < ids.size(); i++) {
            final Optional<KVPair> kvPair = lfuKVStore.get(ids.get(i).toString());
            if (kvPair.isPresent()) {
                assertThat(kvPair.get().getValue()).isEqualTo("nodeVal-" + ids.get(i));
            }
        }

        verify(kvPersistentStorage, times(3))
                .store(any());
    }

    @Test
    void the_Key_ShouldBeRetrievedFromCache() {
        lfuKVStore.put(new KVPair("node-1", "nodeVal-1"));

        final Optional<KVPair> kvPair = lfuKVStore.get("node-1");
        assertThat(kvPair).isNotEmpty();
        assertThat(kvPair.get().getValue()).isEqualTo("nodeVal-1");

        verifyNoInteractions(kvPersistentStorage);
    }


    @Test
    void get_from_storage() {
        lfuKVStore.put(new KVPair("node-1", "nodeVal-1"));
        lfuKVStore.get("node-1");
        lfuKVStore.put(new KVPair("node-2", "nodeVal-2"));
        lfuKVStore.get("node-2");
        lfuKVStore.put(new KVPair("node-3", "nodeVal-3"));
        lfuKVStore.put(new KVPair("node-4", "nodeVal-4"));

        when(kvPersistentStorage.retrieve(any()))
                .thenReturn(Optional.of(new KVPair("node-3", "nodeVal-3")));

        final Optional<KVPair> kvPair = lfuKVStore.get("node-3");
        assertThat(kvPair).isNotEmpty();
        assertThat(kvPair.get().getKey()).isEqualTo("node-3");
        assertThat(kvPair.get().getValue()).isEqualTo("nodeVal-3");
        verify(kvPersistentStorage, times(1)).store(new KVPair("node-3", "nodeVal-3"));
    }

}