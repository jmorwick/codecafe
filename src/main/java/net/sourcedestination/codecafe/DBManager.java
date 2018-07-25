package net.sourcedestination.codecafe;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.sqlite.JDBC;

@Component
public class DBManager {
    private final Logger logger = Logger.getLogger(JShellExerciseTool.class.getCanonicalName());

    private Connection conn;

    public DBManager() {
        try {
            JDBC x = null;
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:codecafe.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");
        } catch(ClassNotFoundException e) {
            logger.info("Couldn't load DB driver: " + e);
        } catch(SQLException e) {
            logger.info("Couldn't connect to DB: " + e);
        }
    }

    public void recordSnippet(String username, String exercise, String code, boolean error) {
        try {
            var s = conn.prepareStatement(
                    "INSERT INTO snippets VALUES (NULL, CURRENT_TIMESTAMP, ?,?,?,?,FALSE);");
            s.setString(1, username);
            s.setString(2, exercise);
            s.setString(3, code);
            s.setBoolean(4, error);
            s.execute();
        } catch(SQLException e) {
            logger.info("ERROR logging code snippet: " + e);
        }
    }

    public void recordReset(String username, String exercise) {
        try {
            var s = conn.prepareStatement(
                    "INSERT INTO snippets VALUES (NULL, CURRENT_TIMESTAMP, ?,?,NULL,TRUE,TRUE);");
            s.setString(1, username);
            s.setString(2, exercise);
            s.execute();
        } catch(SQLException e) {
            logger.info("ERROR logging code snippet: " + e);
        }
    }

    public Stream<String> retrieveHistory(String username, String exercise) {
        try {
            var sql =
                    "SELECT snippet FROM snippets WHERE username == ? AND exercise == ? "+
                            "AND error == 0 " +
                            "AND id > COALESCE((SELECT MAX(id) FROM snippets WHERE username == ? "+
                            "AND exercise == ? AND reset == 1), 0);";
            var s = conn.prepareStatement(sql);
            s.setString(1, username);
            s.setString(2, exercise);
            s.setString(3, username);
            s.setString(4, exercise);
            var rs = s.executeQuery();
            logger.info("executed history query");
            var results = new ArrayList<String>();
            while(rs.next()) results.add(rs.getString("snippet"));
            return results.stream();
        } catch(SQLException e) {
            logger.info("ERROR logging code snippet: " + e);
        }
        return null;
    }

}
