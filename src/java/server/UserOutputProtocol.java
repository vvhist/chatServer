package server;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UserOutputProtocol {

    private PrintWriter out;
    private String username;
    private ZoneId timeZone;
    public static final String DELIMITER = " ";

    public void setWriter(PrintWriter out) {
        this.out = out;
    }

    public String getUsername() {
        return username;
    }

    public boolean process(String inputLine) throws SQLException {
        String[] input = inputLine.split(DELIMITER, 3);
        switch (InCmd.get(input[0])) {
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

    public void sendMessage(Message message) {
        message.setTimeZone(timeZone);
        out.println(OutCmd.MESSAGE + DELIMITER + message);
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
            out.println(OutCmd.ALLOWED_REGISTRATION);
        } else {
            out.println(OutCmd.DENIED_REGISTRATION);
        }
    }

    private void sendHashIfExists(String username) throws SQLException {
        if (Database.containsUser(username)) {
            out.println(OutCmd.PASSWORD_HASH + DELIMITER + Database.getPasswordHash(username));
        } else {
            out.println(OutCmd.DENIED_LOGIN);
        }
    }

    private void logIn(String username) {
        this.username = username;
        Server.add(username, this);
    }

    private void sendHistory() throws SQLException {
        List<Message> history = Database.getHistory(username);
        history.forEach(this::sendMessage);
    }

    private void addContactIfExists(String contact) throws SQLException {
        if (Database.containsUser(contact)) {
            out.println(OutCmd.FOUND_USER + DELIMITER + contact);
        } else {
            out.println(OutCmd.NOT_FOUND_USER + DELIMITER + contact);
        }
    }


    private enum InCmd {
        REGISTRATION("reg"),
        HASH_REQUEST("hash"),
        LOGIN       ("log"),
        TIMEZONE    ("zone"),
        NEW_MESSAGE ("msg"),
        NEW_CONTACT ("add"),
        EXIT        ("exit");

        private final String keyword;
        private static final Map<String, InCmd> COMMANDS;

        InCmd(String keyword) {
            this.keyword = keyword;
        }

        static {
            COMMANDS = new HashMap<>();
            for (InCmd command : InCmd.values()) {
                COMMANDS.put(command.keyword, command);
            }
        }

        public static InCmd get(String keyword) {
            return COMMANDS.get(keyword);
        }
    }


    private enum OutCmd {
        ALLOWED_REGISTRATION("allowReg"),
        DENIED_REGISTRATION ("denyReg"),
        DENIED_LOGIN        ("denyLog"),
        PASSWORD_HASH       ("hash"),
        MESSAGE             ("msg"),
        FOUND_USER          ("newChat"),
        NOT_FOUND_USER      ("notFound");

        private final String keyword;

        OutCmd(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public String toString() {
            return this.keyword;
        }
    }
}