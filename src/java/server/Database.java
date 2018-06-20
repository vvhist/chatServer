package server;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;

public class Database {

    private static final JdbcConnectionPool POOL = JdbcConnectionPool.create(
            "jdbc:h2:./data", "user", "");

    static {
        try (Connection connection = POOL.getConnection();
             Statement stmt = connection.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS users("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "username     VARCHAR NOT NULL,"
                    + "passwordHash VARCHAR NOT NULL)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Database() {}

    public synchronized static void addUser(String username, String passwordHash)
            throws SQLException {
        try (Connection connection = POOL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     "INSERT INTO users (username, passwordHash) VALUES (?, ?)")) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
        }
    }

    public synchronized static boolean containsUser(String username) throws SQLException {
        try (Connection connection = POOL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT id FROM users WHERE username = ?")) {

            pstmt.setString(1, username);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.isBeforeFirst();
            }
        }
    }

    public synchronized static String getPasswordHash(String username) throws SQLException {
        try (Connection connection = POOL.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(
                     "SELECT passwordHash FROM users WHERE username = ?")) {

            pstmt.setString(1, username);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : "";
            }
        }
    }

    public static void close() {
        POOL.dispose();
    }
}