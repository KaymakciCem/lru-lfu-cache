package com.celonis.kvstore;

import java.util.Objects;

public class KVPair {
    private final String key;
    private final String value;
    private KVPair previous;
    private KVPair next;

    public KVPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public KVPair getPrevious() {
        return previous;
    }

    public void setPrevious(KVPair previous) {
        this.previous = previous;
    }

    public KVPair getNext() {
        return next;
    }

    public void setNext(KVPair next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KVPair kvPair = (KVPair) o;
        return Objects.equals(key, kvPair.key) && Objects.equals(value, kvPair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
