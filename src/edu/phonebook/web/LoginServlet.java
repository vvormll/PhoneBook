package edu.phonebook.web;
import java.io.IOException;
import java.io.PrintWriter;

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (request.getParameter("act") == null) {
            if (session != null && session.getAttribute("accountId") != null)
                response.sendRedirect("view");
            else {
                RequestDispatcher disp = getServletContext().getRequestDispatcher("/loginform.jsp");
                try {
                    disp.forward(request, response);
                } catch (ServletException e) {
                    response.getWriter().println(e);
                    // sendRedirect to an error page
                }
            }
        }
        else if (request.getParameter("act").equals("logout")) {
            if (session != null) {
                if (session.getAttribute("accountId") != null)
                    session.removeAttribute("accountId");
                session.invalidate();
                response.sendRedirect("index.html");
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
        DatabaseController controller = null;
        long accountId;
        try {
            ServletContext ctx = getServletContext();
            String dbUrl = ctx.getInitParameter("DatabaseURL");
            String dbUser = ctx.getInitParameter("DatabaseUser");
            String dbPass = ctx.getInitParameter("DatabasePassword");
            controller = DatabaseService.getController(dbUrl, dbUser, dbPass);
        } catch (IOException e) {
            // redirect to an error page
            PrintWriter out = response.getWriter();
            out.println("Couldn't connect to the database");
            return;
        }
        try {
            accountId = controller.getAccountId(user, pass);
        } catch (IOException e) {
            // redirect to an error page
            PrintWriter out = response.getWriter();
            out.println(e.getMessage()); 
            out.println("Account not found");
            return;
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("accountId", accountId);
        response.sendRedirect("view");
    }

}
