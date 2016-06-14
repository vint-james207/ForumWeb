package com.james;

import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by jamesyburr on 6/14/16.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        User user = Main.selectUser(conn, "Alice");
        conn.close();
        assertTrue(user != null);
    }
    public static void insertMessage(Connection conn, int userId, int replyId, String text) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO messages VALUES (NULL, ?, ?, ?)");
        stmt.setInt(1, userId);
        stmt.setInt(2, replyId);
        stmt.setString(3, text);
        stmt.execute();
    }

    public static Message selectMessage(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM messages INNER JOIN users ON messages.user_id = users.id WHERE messages.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int replyId = results.getInt("messages.reply_id");
            String name = results.getString("users.name");
            String text = results.getString("messages.text");
            return new Message(id, replyId, name, text);
        }
        return null;
    }

    @Test
    public void testMessage() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        User user = Main.selectUser(conn, "Alice");
        Main.insertMessage(conn, -1, "Hello, world!", user.id);
        Message msg = Main.selectMessage(conn, 1);
        conn.close();
        assertTrue(msg != null);
        assertTrue(msg.author.equals("Alice"));
    }
    @Test
    public void testReplies() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertUser(conn, "Bob", "");
        User alice = Main.selectUser(conn, "Alice");
        User bob = Main.selectUser(conn, "Bob");
        Main.insertMessage(conn, -1, "Hello, world!", alice.id);
        Main.insertMessage(conn, 1, "This is a reply!", bob.id);
        Main.insertMessage(conn, 1, "This is another reply!", bob.id);
        ArrayList<Message> replies = Main.selectReplies(conn, 1);
        conn.close();
        assertTrue(replies.size() == 2);
    }

    @Test
    public void testDeleteMessage() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Alice", "");
        Main.insertMessage(conn, -1, "Hello, world!", 1);
        Main.deleteMessage(conn, 1);
        Message msg = Main.selectMessage(conn, 1);
        conn.close();
        assertTrue(msg == null);
    }
}