package edu.phonebook.web;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.phonebook.persistence.DatabaseController;
import edu.phonebook.persistence.DatabaseService;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (isRequestForLogout(request)) {
            logoutSession(session);
            response.sendRedirect("index.html");
            return;
        }

        if (isSessionActive(session)) {
            response.sendRedirect("view");
        } else {
            RequestDispatcher disp = getServletContext().getRequestDispatcher("/loginform.jsp");
            disp.forward(request, response);
        }
    }

    private boolean isRequestForLogout(HttpServletRequest request) {
        String action = request.getParameter("act");
        return action != null && action.equals("logout");
    }

    private boolean isSessionActive(HttpSession session) {
        return session != null && session.getAttribute("accountId") != null;
    }

    private void logoutSession(HttpSession session) {
        if (session == null) {
            return;
        }

        if (session.getAttribute("accountId") != null) {
            session.removeAttribute("accountId");
        }
        session.invalidate();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletContext ctx = getServletContext();
        DatabaseController controller = DatabaseService.getControllerFromServletContext(ctx);
        try {
            HttpSession session = initSessionWithAccount(request, controller);
        } catch (SQLException e) {
            throw new IOException("There was a problem in accessing the database", e);
        }
        response.sendRedirect("view");
    }

    private HttpSession initSessionWithAccount(HttpServletRequest request, DatabaseController controller)
            throws IOException, SQLException {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
        long accountId = controller.getAccountId(user, pass);

        HttpSession session = request.getSession();
        session.setAttribute("accountId", accountId);
        return session;
    }

}
