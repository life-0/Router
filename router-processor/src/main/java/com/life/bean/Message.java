package com.life.bean;

public class Message<T> {
    private T data;

    public Message(T data) {
        this.data = data;
    }

    public T getContent() {
        return data;
    }
}
