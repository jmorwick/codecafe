package net.sourcedestination.codecafe.persistance;

import net.sourcedestination.codecafe.execution.JShellExerciseTool;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

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

    public void recordSnippet(String username, String exercise, String code, String status, double completion) {
        try {
            var s = conn.prepareStatement(
                    "INSERT INTO snippets VALUES (NULL, CURRENT_TIMESTAMP, ?,?,?,?,?,FALSE);");
            s.setString(1, username);
            s.setString(2, exercise);
            s.setString(3, code);
            s.setString(4, status);
            s.setDouble(5, completion);
            s.execute();
        } catch(SQLException e) {
            logger.info("ERROR logging code snippet: " + e);
        }
    }

    public void recordReset(String username, String exercise) {
        try {
            var s = conn.prepareStatement(
                    "INSERT INTO snippets VALUES (NULL, CURRENT_TIMESTAMP, ?,?,NULL,NULL,0,TRUE);");
            s.setString(1, username);
            s.setString(2, exercise);
            s.execute();
        } catch(SQLException e) {
            logger.info("ERROR logging code snippet: " + e);
        }
    }

    public List<Map<String,String>> retrieveHistory(String username, String exercise) {
        try {
            var sql =
                    "SELECT * FROM snippets WHERE username == ? AND exercise == ? "+
                            "AND status == 'VALID' " +
                            "AND id > COALESCE((SELECT MAX(id) FROM snippets WHERE username == ? "+
                            "AND exercise == ? AND reset == 1), 0);";
            var s = conn.prepareStatement(sql);
            s.setString(1, username);
            s.setString(2, exercise);
            s.setString(3, username);
            s.setString(4, exercise);
            var rs = s.executeQuery();
            logger.info("executed history query");
            var results = new ArrayList<Map<String,String>>();
            while(rs.next()) results.add(Map.of(
                    "time", rs.getString("time"),
                    "completion", (100*rs.getDouble("completion"))+"%",
                    "snippet", "<pre>"+rs.getString("snippet")+"</pre>", // nasty, fix this with css later
                    "status", rs.getString("status"),
                    "result", ""
            ));
            return results;
        } catch(SQLException e) {
            logger.info("ERROR retrieving code snippet: " + e);
        }
        return null;
    }

}
