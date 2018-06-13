package server;

import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class User {

    private String username;
    private char[] password;
    private PrintWriter out;
    private ZoneId timeZone;
    private Queue<Message> unsentMessages = new ConcurrentLinkedQueue<>();

    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
        Server.addUser(username, this);
    }

    public String getUsername() {
        return username;
    }

    public boolean hasCorrectPassword(char[] suggestedPassword) {
        return Arrays.equals(password, suggestedPassword);
    }

    public void connect(PrintWriter out, ZoneId timeZone) {
        this.out = out;
        this.timeZone = timeZone;
        unsentMessages.forEach(Message::print);
    }

    public void disconnect() {
        out = null;
    }

    public void sendMessage(String text) {
        ZonedDateTime serverTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Message message = new Message(serverTime, text);
        if (out != null) {
            message.print();
        } else {
            unsentMessages.add(message);
        }
    }


    private final class Message {

        ZonedDateTime timestamp;
        String text;

        Message(ZonedDateTime timestamp, String text) {
            this.timestamp = timestamp;
            this.text = text;
        }

        void print() {
            out.println(timestamp.withZoneSameInstant(timeZone).toLocalTime() + " " + text);
        }
    }
}