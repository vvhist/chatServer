package server;

import java.util.Arrays;

public class User {

    private String username;
    private char[] password;
    private boolean online;

    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean hasCorrectPassword(char[] suggestedPassword) {
        return password.length == suggestedPassword.length
                && Arrays.equals(password, suggestedPassword);
    }
}