package net.sourcedestination.codecafe;

import net.sourcedestination.codecafe.persistance.DBManager;

import java.util.List;
import java.util.Map;

public class InMemoryDBManager extends DBManager {

    public void recordSnippet(String username, String exercise, String code, String status, double completion) {

    }

    public void recordReset(String username, String exercise) {

    }

    public List<Map<String,String>> retrieveHistory(String username, String exercise) {
        return null;
    }


}
