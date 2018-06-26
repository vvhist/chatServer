package server;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private static final JdbcConnectionPool POOL = JdbcConnectionPool.create(
            "jdbc:h2:./data", "user", "");

    static {
        try (Connection connection = POOL.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users("
                    + "id            INT     AUTO_INCREMENT PRIMARY KEY,"
                    + "username      VARCHAR NOT NULL,"
                    + "password_hash VARCHAR NOT NULL)"
            );
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages("
                    + "id           INT          AUTO_INCREMENT PRIMARY KEY,"
                    + "sender_id    INT          NOT NULL,"
                    + "recipient_id INT          NOT NULL,"
                    + "timestamp    TIMESTAMP(0) NOT NULL,"
                    + "text         VARCHAR      NOT NULL)"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Database() {}

    public synchronized static boolean addUser(String username, String passwordHash)
            throws SQLException {
        if (containsUser(username)) return false;

        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection connection = POOL.getConnection();
             PreparedStatement preStatement = connection.prepareStatement(sql)) {

            preStatement.setString(1, username);
            preStatement.setString(2, passwordHash);
            preStatement.executeUpdate();
            return true;
        }
    }

    public static void addMessage(Message message) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, recipient_id, timestamp, text) "
                + "SELECT u1.id, u2.id, ?, ? "
                + "FROM users AS u1 "
                + "JOIN users AS u2 "
                + "ON  u1.username = ? "
                + "AND u2.username = ?";
        try (Connection connection = POOL.getConnection();
             PreparedStatement preStatement = connection.prepareStatement(sql)) {

            preStatement.setTimestamp(1, Timestamp.valueOf(message.getTimestamp()));
            preStatement.setString(2, message.getText());
            preStatement.setString(3, message.getSender());
            preStatement.setString(4, message.getRecipient());
            preStatement.executeUpdate();
        }
    }

    public static List<Message> getHistory(String username) throws SQLException {
        String sql = "SELECT u1.username, u2.username, timestamp, text "
                + "FROM messages "
                + "JOIN users AS u1 ON u1.id = messages.sender_id "
                + "JOIN users AS u2 ON u2.id = messages.recipient_id "
                + "WHERE ? IN (u1.username, u2.username) ORDER BY messages.id";
        try (Connection connection = POOL.getConnection();
             PreparedStatement preStatement = connection.prepareStatement(sql)) {

            preStatement.setString(1, username);
            try (ResultSet resultSet = preStatement.executeQuery()) {
                List<Message> history = new ArrayList<>();
                while (resultSet.next()) {
                    String sender = resultSet.getString(1);
                    String recipient = resultSet.getString(2);
                    LocalDateTime timestamp = resultSet.getTimestamp(3).toLocalDateTime();
                    String text = resultSet.getString(4);
                    history.add(new Message(sender, recipient, timestamp, text));
                }
                return history;
            }
        }
    }

    public static boolean containsUser(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection connection = POOL.getConnection();
             PreparedStatement preStatement = connection.prepareStatement(sql)) {

            preStatement.setString(1, username);
            try (ResultSet resultSet = preStatement.executeQuery()) {
                return resultSet.isBeforeFirst();
            }
        }
    }

    public static String getPasswordHash(String username) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection connection = POOL.getConnection();
             PreparedStatement preStatement = connection.prepareStatement(sql)) {

            preStatement.setString(1, username);
            try (ResultSet resultSet = preStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : "";
            }
        }
    }

    public static void close() {
        POOL.dispose();
    }
}