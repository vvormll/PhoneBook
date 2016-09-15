package edu.phonebook.persistence;
import java.io.IOException;

import javax.servlet.ServletContext;

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

    public static DatabaseController getControllerFromServletContext(ServletContext ctx) throws IOException {
        String dbUrl = ctx.getInitParameter("DatabaseURL");
        String dbUser = ctx.getInitParameter("DatabaseUser");
        String dbPass = ctx.getInitParameter("DatabasePassword");
        return getController(dbUrl, dbUser, dbPass);
    }
}
