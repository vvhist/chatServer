package server;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class Message {

    private final String sender;
    private final String recipient;
    private LocalDateTime timestamp;
    private final String text;

    public Message(String sender, String recipient, LocalDateTime timestamp, String text) {
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = timestamp;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimeZone(ZoneId zone) {
        timestamp = timestamp.atZone(zone).toLocalDateTime();
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return sender + "/" + recipient + "/" + timestamp + "/" + text;
    }
}