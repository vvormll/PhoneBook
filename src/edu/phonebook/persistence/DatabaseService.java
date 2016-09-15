package edu.phonebook.persistence;
import java.io.IOException;

public class DatabaseService {

    //enable parametrization?

    public static DatabaseController getController(String dbUrl, String user, String pass) throws IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        return new MySQLController(dbUrl, user, pass);
    }
}
