package edu.phonebook.persistence;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseController {
    List<Record> getAllRecords(long accountId) throws SQLException;

    List<Record> getRecordsByFields(long accountId, Map<String, String> fields) throws SQLException;

    Record getRecordById(long accountId, long recordId) throws SQLException;

    void addRecord(long accountId, Record record) throws SQLException;

    void editRecord(long recordId, Record edited) throws SQLException;

    void deleteRecord(long recordId) throws SQLException;

    long getAccountId(String user, String pass) throws SQLException;

    void addAccount(String user, String pass) throws SQLException;
}