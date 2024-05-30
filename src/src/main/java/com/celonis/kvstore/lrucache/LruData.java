package com.celonis.kvstore.lrucache;

import java.util.Objects;

import com.celonis.kvstore.KVPair;

public class LruData {
    private final KVPair head;
    private final KVPair tail;

    public KVPair getHead() {
        return head;
    }

    public KVPair getTail() {
        return tail;
    }

    public LruData() {
        head = new KVPair("head", "headVal");
        tail = new KVPair("tail", "tailVal");
        head.setNext(tail);
        tail.setPrevious(head);
    }

    public void addNode(KVPair kvPair) {
        KVPair tempNode = head.getNext();
        tempNode.setPrevious(kvPair);

        kvPair.setPrevious(head);
        kvPair.setNext(tempNode);

        head.setNext(kvPair);
    }

    public void removeNode(KVPair kvPair) {
        if (Objects.isNull(kvPair)) {
            return;
        }

        KVPair previous = kvPair.getPrevious();
        KVPair next = kvPair.getNext();

        previous.setNext(next);
        next.setPrevious(previous);
    }
}
