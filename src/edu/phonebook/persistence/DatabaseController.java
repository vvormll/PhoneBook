package edu.phonebook.persistence;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DatabaseController {
    List<Record> getAllRecords(long accountId) throws IOException;
    List<Record> getRecordsByFields(long accountId, Map<String, String> fields) throws IOException;
    Record getRecordById(long accountId, long recordId) throws IOException;
    void addRecord(long accountId, Record record) throws IOException;
    void editRecord(long recordId, Record edited) throws IOException;
    void deleteRecord(long recordId) throws IOException;
    long getAccountId(String user, String pass) throws IOException;
}