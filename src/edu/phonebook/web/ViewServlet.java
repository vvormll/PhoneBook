package edu.phonebook.web;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.phonebook.persistence.DatabaseController;
import edu.phonebook.persistence.DatabaseService;
import edu.phonebook.persistence.Record;

public class ViewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("accountId") == null) {
            response.sendRedirect("login");
        } else {
            long accountId = (long) session.getAttribute("accountId");
            ServletContext ctx = getServletContext();
            DatabaseController controller = DatabaseService.getControllerFromServletContext(ctx);

            List<Record> records = null;
            try {
                if (request.getParameter("act") == null) {
                    records = controller.getAllRecords(accountId);
                } else if (request.getParameter("act").equals("find")) {
                    Map<String, String> fields = parseRequestParameters(request);
                    records = controller.getRecordsByFields(accountId, fields);
                }
            } catch (SQLException e) {
                throw new IOException("There was a problem in fetching records from the database", e);
            }

            request.setAttribute("records", records);
            RequestDispatcher disp = ctx.getRequestDispatcher("/view.jsp");
            disp.forward(request, response);
        }
    }

    private Map<String, String> parseRequestParameters(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String paramName = en.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue == null || paramValue.isEmpty())
                continue;

            String dbParamName;
            try {
                dbParamName = translateParamToDb(paramName);
                fields.put(dbParamName, paramValue);
            } catch (IllegalArgumentException e) {
                // ignore invalid fields
                continue;
            }
        }
        return fields;
    }

    private String translateParamToDb(String param) {
        if (param.equals("name")) {
            return "contact_name";
        } else if (param.equals("number")) {
            return "number";
        } else if (param.equals("addr")) {
            return "address";
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
}
