package server;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class UserOutputProtocol {

    private UserConnection connection;
    private String username = null;
    private ZoneId timeZone;

    public UserOutputProtocol(UserConnection connection) {
        this.connection = connection;
    }

    public boolean process(String inputLine) throws SQLException {
        String[] input = inputLine.split(Command.DELIMITER, 3);
        switch (Command.Input.get(input[0])) {
            case NEW_MESSAGE:
                processMessage(input[1], input[2]);
                break;
            case REGISTRATION:
                registerIfNew(input[1], input[2]);
                break;
            case HASH_REQUEST:
                sendHashIfExists(input[1]);
                break;
            case LOGIN:
                logIn(input[1]);
                sendHistory();
                break;
            case TIMEZONE:
                timeZone = ZoneId.of(input[1]);
                break;
            case NEW_CONTACT:
                addContactIfExists(input[1]);
                break;
            case EXIT:
                return false;
        }
        return true;
    }

    public void close() {
        if (username != null) {
            Server.removeOnlineUser(username);
        }
    }

    public void sendMessage(Message message) {
        message.setTimeZone(timeZone);
        connection.send(Command.Output.MESSAGE, message.toString());
    }

    private void processMessage(String recipient, String text) throws SQLException {
        LocalDateTime serverTime = LocalDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS);
        Message message = new Message(username, recipient, serverTime, text);

        Database.addMessage(message);
        Server.sendTo(recipient, message);
        sendMessage(message);
    }

    private void registerIfNew(String username, String passwordHash) throws SQLException {
        if (Database.addUser(username, passwordHash)) {
            logIn(username);
            connection.send(Command.Output.ALLOWED_REGISTRATION);
        } else {
            connection.send(Command.Output.DENIED_REGISTRATION);
        }
    }

    private void sendHashIfExists(String username) throws SQLException {
        if (Database.containsUser(username)) {
            connection.send(Command.Output.PASSWORD_HASH, Database.getPasswordHash(username));
        } else {
            connection.send(Command.Output.DENIED_LOGIN);
        }
    }

    private void logIn(String username) {
        this.username = username;
        Server.addOnlineUser(username, this);
    }

    private void sendHistory() throws SQLException {
        List<Message> history = Database.getHistory(username);
        history.forEach(this::sendMessage);
    }

    private void addContactIfExists(String contact) throws SQLException {
        if (Database.containsUser(contact)) {
            connection.send(Command.Output.FOUND_USER, contact);
        } else {
            connection.send(Command.Output.NOT_FOUND_USER, contact);
        }
    }
}