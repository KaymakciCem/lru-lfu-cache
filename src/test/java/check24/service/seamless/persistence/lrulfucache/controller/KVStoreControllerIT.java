package com.celonis.kvstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.celonis.kvstore.KVPair;
import com.celonis.kvstore.dto.KVStoreDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebClient
class KVStoreControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient.put()
                     .uri("/api/keyvaluestore")
                     .bodyValue(new KVStoreDto("key1", "value1"))
                     .exchange()
                     .expectStatus()
                     .isCreated();
    }

    @Test
    void getKey_no_key_available() {
        webTestClient.get()
                     .uri("/api/keyvaluestore/key99")
                     .exchange()
                     .expectStatus()
                     .is4xxClientError();
    }

    @Test
    void getKey_success() {
        webTestClient.get()
                     .uri("/api/keyvaluestore/key1")
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(KVStoreDto.class)
                     .consumeWith(response -> {
                         var responseBody = response.getResponseBody();
                         assertThat(responseBody.getKey()).isEqualTo("key1");
                         assertThat(responseBody.getValue()).isEqualTo("value1");
                     });
    }

    @Test
    void addDataToCache() {
        webTestClient.put()
                     .uri("/api/keyvaluestore")
                     .bodyValue(new KVStoreDto("key112", "value112"))
                     .exchange()
                     .expectStatus()
                     .isCreated();

        webTestClient.get()
                     .uri("/api/keyvaluestore/key112")
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(KVStoreDto.class)
                     .consumeWith(response -> {
                         var responseBody = response.getResponseBody();
                         assertThat(responseBody.getKey()).isEqualTo("key112");
                         assertThat(responseBody.getValue()).isEqualTo("value112");
                     });
    }

    @Test
    void deleteKey() {
        webTestClient.delete()
                     .uri("/api/keyvaluestore/key1")
                     .exchange()
                     .expectStatus()
                     .isNoContent();
    }
}