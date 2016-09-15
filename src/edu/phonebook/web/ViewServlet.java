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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("accountId") == null) {
            response.sendRedirect("login");
        } else {
            long accountId = (long) session.getAttribute("accountId");
            ServletContext ctx = getServletContext();
            DatabaseController controller = DatabaseService.getControllerFromServletContext(ctx);
            List<Record> records = controller.getAllRecords(accountId);
            request.setAttribute("records", records);
            RequestDispatcher disp = ctx.getRequestDispatcher("/view.jsp");
            disp.forward(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
}
