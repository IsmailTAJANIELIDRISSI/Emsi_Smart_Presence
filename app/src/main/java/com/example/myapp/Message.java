package com.example.myapp;

// Message Model Class
public class Message {
    public String content;
    public String sender;
    public String timestamp;

    public Message(String content, String sender, String timestamp) {
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
    }
}
