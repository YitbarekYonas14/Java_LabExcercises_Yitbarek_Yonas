package ChatApp.server;

import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/telegram_clone"
                                     + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public void initTables() {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS private_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "sender VARCHAR(50) NOT NULL," +
                "receiver VARCHAR(50) NOT NULL," +
                "message TEXT," +
                "file_path VARCHAR(255)," +
                "is_file BOOLEAN DEFAULT FALSE," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_conv (sender, receiver))"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS group_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "sender VARCHAR(50) NOT NULL," +
                "group_name VARCHAR(50) NOT NULL," +
                "message TEXT," +
                "file_path VARCHAR(255)," +
                "is_file BOOLEAN DEFAULT FALSE," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS group_members (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "group_name VARCHAR(50) NOT NULL," +
                "username VARCHAR(50) NOT NULL," +
                "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY uq_gm (group_name, username))"
            );

            st.executeUpdate("INSERT IGNORE INTO group_members (group_name, username) VALUES ('General', '__system__')");
            System.out.println("[DB] All tables ready.");
        } catch (SQLException e) {
            System.err.println("[DB ERROR] " + e.getMessage());
            System.err.println("Start XAMPP MySQL, then create database 'telegram_clone'.");
            System.exit(1);
        }
    }

    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.toLowerCase());
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean validateUser(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE LOWER(username)=? AND password=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.toLowerCase());
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public void savePrivateMessage(String sender, String receiver, String message, String filePath, boolean isFile) {
        String sql = "INSERT INTO private_messages (sender, receiver, message, file_path, is_file) VALUES (?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, message);
            ps.setString(4, filePath);
            ps.setBoolean(5, isFile);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String> getPrivateHistory(String user1, String user2) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT sender, message, is_file, file_path, DATE_FORMAT(timestamp,'%H:%i') AS time " +
                     "FROM private_messages WHERE (sender=? AND receiver=?) OR (sender=? AND receiver=?) " +
                     "ORDER BY id ASC LIMIT 50";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user1); ps.setString(2, user2);
            ps.setString(3, user2); ps.setString(4, user1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("sender") + "::" +
                         rs.getString("message") + "::" +
                         rs.getString("time") + "::" +
                         rs.getBoolean("is_file") + "::" +
                         (rs.getString("file_path") != null ? rs.getString("file_path") : ""));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void saveGroupMessage(String sender, String group, String message, String filePath, boolean isFile) {
        String sql = "INSERT INTO group_messages (sender, group_name, message, file_path, is_file) VALUES (?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sender);
            ps.setString(2, group);
            ps.setString(3, message);
            ps.setString(4, filePath);
            ps.setBoolean(5, isFile);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String> getGroupHistory(String groupName) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT sender, message, is_file, file_path, DATE_FORMAT(timestamp,'%H:%i') AS time " +
                     "FROM group_messages WHERE group_name=? ORDER BY id ASC LIMIT 50";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, groupName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("sender") + "::" +
                         rs.getString("message") + "::" +
                         rs.getString("time") + "::" +
                         rs.getBoolean("is_file") + "::" +
                         (rs.getString("file_path") != null ? rs.getString("file_path") : ""));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addMember(String group, String username) {
        String sql = "INSERT IGNORE INTO group_members (group_name, username) VALUES (?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, group);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void removeMember(String group, String username) {
        String sql = "DELETE FROM group_members WHERE group_name=? AND username=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, group);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String> getMembers(String group) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT username FROM group_members WHERE group_name=? AND username != '__system__'";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, group);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("username"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getAllGroups() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT group_name FROM group_members WHERE group_name != '__system__'";
        try (Connection c = connect(); Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(rs.getString("group_name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getUserGroups(String username) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT group_name FROM group_members WHERE username=? AND group_name != '__system__'";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("group_name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean groupExists(String group) {
        return getAllGroups().contains(group);
    }

    public boolean isMember(String group, String username) {
        String sql = "SELECT 1 FROM group_members WHERE group_name=? AND username=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, group);
            ps.setString(2, username);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }
}
