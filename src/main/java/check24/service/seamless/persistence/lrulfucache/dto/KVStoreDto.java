package com.celonis.kvstore.dto;

import jakarta.validation.constraints.NotEmpty;

public class KVStoreDto {
    @NotEmpty(message = "Key must not be empty")
    private String key;
    private String value;

    public KVStoreDto() { }

    public KVStoreDto(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
