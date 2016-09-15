package edu.phonebook.web;
import java.io.IOException;
import java.util.List;

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("accountId") == null)
            response.sendRedirect("login");
        else {
            long accountId = (long) session.getAttribute("accountId");
            ServletContext ctx = getServletContext();
            String dbUrl = ctx.getInitParameter("DatabaseURL");
            String dbUser = ctx.getInitParameter("DatabaseUser");
            String dbPass = ctx.getInitParameter("DatabasePassword");
            DatabaseController controller = DatabaseService.getController(dbUrl, dbUser, dbPass);
            List<Record> records = controller.getAllRecords(accountId);
            request.setAttribute("records", records);
            if (request.getParameter("actstatus") != null) {
                request.setAttribute("statusmsg", getStatusMessage(request.getParameter("actstatus")));
            }
            RequestDispatcher disp = ctx.getRequestDispatcher("/view.jsp");
            try {
                disp.forward(request, response);
            } catch (ServletException e) {
                response.getWriter().println(e);
                // redirect to an error page
            }
        }
    }

    private String getStatusMessage(String status) {
        // replace with template + parameter
        if (status.equals("addsucc"))
            return "Successfully added record";
        else if (status.equals("addfail"))
            return "Failed to add record";
        else if (status.equals("editsucc"))
            return "Successfully edited record";
        else if (status.equals("editfail"))
            return "Failed to edit record";
        else if (status.equals("deletesucc"))
            return "Successfully deleted record";
        else if (status.equals("deletefail"))
            return "Failed to delete record";
        else
            return "";
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
