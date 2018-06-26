package server;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;

public final class OnlineUser {

    private String username;
    private ZoneId timeZone;
    private PrintWriter out;

    public OnlineUser(String username, ZoneId timeZone, PrintWriter out) throws SQLException {
        this.username = username;
        this.timeZone = timeZone;
        this.out = out;

        List<Message> history = Database.getHistory(username);
        history.forEach(this::sendMessage);
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(Message message) {
        message.setTimeZone(timeZone);
        out.println(message);
    }
}