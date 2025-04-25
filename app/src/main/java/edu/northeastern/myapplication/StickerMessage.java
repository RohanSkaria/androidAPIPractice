package edu.northeastern.myapplication;

public class StickerMessage {
    private String sender;
    private String recipient;
    private String stickerId;
    private long timestamp;

    public StickerMessage() {

    }

    public StickerMessage(String sender, String recipient, String stickerId, long timestamp) {
        this.sender = sender;
        this.recipient = recipient;
        this.stickerId = stickerId;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}