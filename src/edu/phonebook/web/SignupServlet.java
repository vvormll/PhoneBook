package edu.phonebook.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.phonebook.persistence.DatabaseController;
import edu.phonebook.persistence.DatabaseService;

public class SignupServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (isActiveSession(session))
            throw new IOException("You already have an account");
        checkAndAttachErrMsg(request);
        RequestDispatcher disp = getServletContext().getRequestDispatcher("/signupform.jsp");
        disp.forward(request, response);
    }

    private boolean isActiveSession(HttpSession session) {
        return session != null && session.getAttribute("accountId") != null;
    }

    private void checkAndAttachErrMsg(HttpServletRequest request) {
        String err = request.getParameter("err");
        if (err != null) {
            if (err.equals("exists"))
                request.setAttribute("errmsg", "Account with this username already exists");
            else if (err.equals("baduser"))
                request.setAttribute("errmsg", "Incorrect input: username");
            else if (err.equals("badpass"))
                request.setAttribute("errmsg", "Incorrect input: password");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
        if (user != null && pass != null) {
            DatabaseController controller = DatabaseService.getControllerFromServletContext(getServletContext());
            try {
                controller.addAccount(user, pass);
            } catch (SQLException e) {
                response.sendRedirect("signup?err=exists");
            }
            response.sendRedirect("login?msg=supsucc");
        }
    }

}
