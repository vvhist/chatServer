package server;

import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {

    private String username;
    private char[] password;
    private PrintWriter out;
    private ZoneId timeZone;
    private Queue<String> unsentMessages = new ConcurrentLinkedQueue<>();

    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
        Server.addUser(username, this);
    }

    public String getUsername() {
        return username;
    }

    public boolean hasCorrectPassword(char[] suggestedPassword) {
        return password.length == suggestedPassword.length
                && Arrays.equals(password, suggestedPassword);
    }

    public void connect(PrintWriter out, ZoneId timeZone) {
        this.out = out;
        this.timeZone = timeZone;
        while (!unsentMessages.isEmpty()) {
            String message = unsentMessages.poll();
            String time = message.substring(0, message.indexOf(' '));
            message = message.substring(message.indexOf(' ') + 1);
            ZonedDateTime clientTime = ZonedDateTime.parse(time);
            out.println(clientTime.toLocalTime() + " " + message);
        }
    }

    public void disconnect() {
        out = null;
    }

    public void sendMessage(String message) {
        ZonedDateTime serverTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        if (out != null) {
            ZonedDateTime clientTime = serverTime.withZoneSameInstant(timeZone);
            out.println(clientTime.toLocalTime() + " " + message);
        } else {
            unsentMessages.add(serverTime + " " + message);
        }
    }
}