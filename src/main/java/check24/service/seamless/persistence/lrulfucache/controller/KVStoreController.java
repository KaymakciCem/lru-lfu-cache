package com.celonis.kvstore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.celonis.kvstore.dto.KVStoreDto;
import com.celonis.kvstore.service.KeyValueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/keyvaluestore")
public class KVStoreController {

    private final KeyValueService keyValueService;

    public KVStoreController(KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    @GetMapping(value = "{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KVStoreDto> getKey(@PathVariable("key") final String key) {
        return keyValueService.get(key)
                              .map(ResponseEntity::ok)
                              .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping
    public ResponseEntity<Void> addDataToCache(@RequestBody @Valid final KVStoreDto kvStoreRequest) {
        keyValueService.put(kvStoreRequest.getKey(), kvStoreRequest.getValue());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("{key}")
    public ResponseEntity<Void> deleteKey(@PathVariable("key") final String key) {
        keyValueService.delete(key);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
