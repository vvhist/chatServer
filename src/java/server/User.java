package server;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {

    private String username;
    private char[] password;
    private PrintWriter out;
    private Queue<String> unsentMessages = new ConcurrentLinkedQueue<>();

    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasCorrectPassword(char[] suggestedPassword) {
        return password.length == suggestedPassword.length
                && Arrays.equals(password, suggestedPassword);
    }

    public void connect(PrintWriter out) {
        this.out = out;
        while (!unsentMessages.isEmpty()) {
            out.println(unsentMessages.poll());
        }
    }

    public void disconnect() {
        out = null;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        } else {
            unsentMessages.add(message);
        }
    }
}