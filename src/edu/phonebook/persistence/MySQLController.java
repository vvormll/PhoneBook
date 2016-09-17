package edu.phonebook.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MySQLController implements DatabaseController {

    public final String dbUrl;
    public final String dbUser;
    public final String dbPass;

    public MySQLController(String dbUrl, String user, String pass) throws SQLException {
        this.dbUrl = dbUrl;
        this.dbUser = user;
        this.dbPass = pass;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, user, pass);
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }
    }

    @Override
    public List<Record> getAllRecords(long accountId) throws SQLException {
        List<Record> records = new LinkedList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            stmt = conn.prepareStatement("select c.contact_id, c.contact_name, c.address, c.add_info, p.number"
                    + " from contacts as c inner join ph_numbers as p on c.contact_id = p.contact_id where account_id = ?");
            stmt.setLong(1, accountId);
            result = stmt.executeQuery();
            Map<Long, Record> processed = new HashMap<>();
            while (result.next()) {
                long contactId = result.getLong("contact_id");
                if (processed.containsKey(contactId)) {
                    Record rec = processed.get(contactId);
                    String number = result.getString("number");
                    rec.addPhoneNumber(number);
                }
                else {
                    String name = result.getString("contact_name");
                    String address = result.getString("address");
                    String additional = result.getString("add_info");
                    String number = result.getString("number");
                    Record rec = new Record(contactId, name, number);
                    if (address != null)
                        rec.setAddress(address);
                    if (additional != null)
                        rec.setAdditionalInfo(additional);
                    processed.put(contactId, rec);
                }
            }
            for (Record rec : processed.values())
                records.add(rec);
        } finally {
            try {
                if (result != null)
                    result.close();
            } catch (SQLException e) {}

            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {}

            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }
        return records;
    }

    @Override
    public List<Record> getRecordsByFields(long accountId, Map<String, String> fields) throws SQLException {
        List<Record> records = new LinkedList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select c.contact_id, c.contact_name, c.address, c.add_info, p.number"
                + " from contacts as c inner join ph_numbers as p on c.contact_id = p.contact_id where account_id = ?");
        LinkedList<String> linkedFields = new LinkedList<>();
        if (fields.size() > 0) {
            for (String field : fields.keySet()) {
                queryBuilder.append(" and ");
                queryBuilder.append(field);
                queryBuilder.append(" like ?");
                linkedFields.add(field);
            }
        }
        String query = queryBuilder.toString();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, accountId);
            int counter = 2;
            for (String field : linkedFields) {
                stmt.setString(counter, "%" + fields.get(field) + "%");
                counter++;
            }
            result = stmt.executeQuery();
            Map<Long, Record> processed = new HashMap<>();
            while (result.next()) {
                long contactId = result.getLong("contact_id");
                if (processed.containsKey(contactId)) {
                    Record rec = processed.get(contactId);
                    String number = result.getString("number");
                    rec.addPhoneNumber(number);
                }
                else {
                    String name = result.getString("contact_name");
                    String address = result.getString("address");
                    String additional = result.getString("add_info");
                    String number = result.getString("number");
                    Record rec = new Record(contactId, name, number);
                    if (address != null)
                        rec.setAddress(address);
                    if (additional != null)
                        rec.setAdditionalInfo(additional);
                    processed.put(contactId, rec);
                }
            }
            for (Record rec : processed.values())
                records.add(rec);
        } finally {
            try {
                if (result != null)
                    result.close();
            } catch (SQLException e) {}

            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {}

            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }

        return records;
    }

    @Override
    public void addRecord(long accountId, Record record) throws SQLException {
        String name = record.getContactName();
        String address = record.getAddress();
        String additional = record.getAdditionalInfo();
        Set<String> numbers = record.getPhoneNumbers();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("insert into contacts values (NULL, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            if (address != null)
                stmt.setString(2, address);
            else
                stmt.setNull(2, Types.VARCHAR);
            if (additional != null)
                stmt.setString(3, additional);
            else
                stmt.setNull(3, Types.VARCHAR);
            stmt.setLong(4, accountId);
            stmt.setNull(5, Types.INTEGER);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            rs.next();
            long contactId = rs.getLong(1);
            stmt.close();
            stmt = conn.prepareStatement("insert into ph_numbers values (NULL, ?, ?)");
            try {
                for (String number : numbers) {
                    stmt.setLong(1, contactId);
                    stmt.setString(2, number);
                    stmt.addBatch();
                }
                stmt.executeBatch();	
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            conn.commit();
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {}

            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {}

            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }
    }

    @Override
    public void editRecord(long recordId, Record edited) throws SQLException {
        String name = edited.getContactName();
        String address = edited.getAddress();
        String additional = edited.getAdditionalInfo();
        Set<String> numbers = edited.getPhoneNumbers();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("update contacts as c set c.contact_name = ?, c.address = ?, c.add_info = ? where c.contact_id = ?");
            stmt.setString(1, name);
            if (address != null)
                stmt.setString(2, address);
            else
                stmt.setNull(2, Types.VARCHAR);
            if (additional != null)
                stmt.setString(3, additional);
            else
                stmt.setNull(3, Types.VARCHAR);
            stmt.setLong(4, recordId);
            stmt.executeUpdate();
            stmt.close();
            stmt = conn.prepareStatement("delete from ph_numbers where contact_id = ?");
            stmt.setLong(1, recordId);
            stmt.executeUpdate();
            stmt.close();
            stmt = conn.prepareStatement("insert into ph_numbers values (NULL, ?, ?)");
            try {
                for (String number : numbers) {
                    stmt.setLong(1, recordId);
                    stmt.setString(2, number);
                    stmt.addBatch();
                }
                stmt.executeBatch();	
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            conn.commit();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {}

            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }
    }

    @Override
    public void deleteRecord(long recordId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("delete from ph_numbers where contact_id = ?");
            stmt.setLong(1, recordId);
            stmt.executeUpdate();
            stmt.close();
            try {
                stmt = conn.prepareStatement("delete from contacts where contact_id = ?");
                stmt.setLong(1, recordId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            conn.commit();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {}

            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }
    }

    @Override
    public long getAccountId(String user, String pass) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        long id = -1;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            stmt = conn.prepareStatement("select account_id from accounts where username = ? and password = password(?)");
            stmt.setString(1, user);
            stmt.setString(2, pass);
            rs = stmt.executeQuery();
            rs.next();
            id = rs.getLong(1);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {}

            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }
        return id;
    }

    @Override
    public Record getRecordById(long accountId, long recordId) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from contacts where account_id = " + accountId + " and contact_id = " + recordId);
            if (!rs.isBeforeFirst())
                throw new SQLException("No record found");
            rs.next();
            String name = rs.getString("contact_name");
            String address = rs.getString("address");
            String additional = rs.getString("add_info");
            long contactId = rs.getLong("contact_id");
            rs.close();
            rs = stmt.executeQuery("select * from ph_numbers where contact_id = " + recordId);
            Set<String> numbers = new HashSet<>();
            while (rs.next()) {
                numbers.add(rs.getString("number"));
            }
            Record rec = new Record(contactId, name, numbers);
            if (address != null)
                rec.setAddress(address);
            if (additional != null)
                rec.setAdditionalInfo(additional);
            return rec;
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {}

            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {}

            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {}
        }
    }

}
