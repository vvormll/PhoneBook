package edu.phonebook.web;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

public class ModifyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        if (session.getAttribute("accountId") == null) {
            response.sendRedirect("login");
        }
        long accountId = (long) session.getAttribute("accountId");
        String action = request.getParameter("act");
        if (action.equals("add")) {
            RequestDispatcher disp = getServletContext().getRequestDispatcher("/addform.jsp");
            try {
                disp.forward(request, response);
            } catch (ServletException e) {
                // redirect to an error page
            }
        }
        else {
            long contactId = Long.parseLong(request.getParameter("contactId"));
            ServletContext ctx = getServletContext();
            String dbUrl = ctx.getInitParameter("DatabaseURL");
            String dbUser = ctx.getInitParameter("DatabaseUser");
            String dbPass = ctx.getInitParameter("DatabasePassword");
            DatabaseController controller = DatabaseService.getController(dbUrl, dbUser, dbPass);
            Record rec = null;
            try {
                rec = controller.getRecordById(accountId, contactId);
            } catch (IOException e) {
                throw new IOException("Record not found or you don't have the rights to modify it", e);
            }
            if (action.equals("edit")) {
                request.setAttribute("record", rec);
                RequestDispatcher disp = ctx.getRequestDispatcher("/editform.jsp");
                try {
                    disp.forward(request, response);
                } catch (ServletException e) {
                    response.getWriter().println(e);
                }
            } else if (action.equals("delete")) {
                request.setAttribute("record", rec);
                RequestDispatcher disp = ctx.getRequestDispatcher("/deleteform.jsp");
                try {
                    disp.forward(request, response);
                } catch (ServletException e) {
                    response.getWriter().println(e);
                }
            } else {
                // redirect to an error page
            }
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        if (session.getAttribute("accountId") == null) {
            response.sendRedirect("login");
        }
        else {
            ServletContext ctx = getServletContext();
            String dbUrl = ctx.getInitParameter("DatabaseURL");
            String dbUser = ctx.getInitParameter("DatabaseUser");
            String dbPass = ctx.getInitParameter("DatabasePassword");
            DatabaseController controller = DatabaseService.getController(dbUrl, dbUser, dbPass);
            long accountId = (long) session.getAttribute("accountId");
            String action = request.getParameter("act");
            if (action.equals("add")) {
                String name = request.getParameter("contactName");
                String address = request.getParameter("address");
                String additional = request.getParameter("additional");
                String[] numbers = request.getParameter("numbers").split(" ");
                Set<String> numbersSet = new HashSet<>(Arrays.asList(numbers));
                Record rec = new Record(-1, name, numbersSet);
                if (additional.length() > 0)
                    rec.setAdditionalInfo(additional);
                if (address.length() > 0)
                    rec.setAddress(address);

                controller.addRecord(accountId, rec);
                response.sendRedirect("view?actstatus=addsucc");
            } 
            else if (action.equals("edit")) {
                if (request.getParameter("contactId") == null) {
                    // replace with smth meaningful
                    response.sendRedirect("view");
                }
                String name = request.getParameter("contactName");
                String address = request.getParameter("address");
                String additional = request.getParameter("additional");
                String[] numbers = request.getParameter("numbers").split(" ");
                Set<String> numbersSet = new HashSet<>(Arrays.asList(numbers));
                long contactId = Long.parseLong(request.getParameter("contactId"));
                Record rec = new Record(contactId, name, numbersSet);
                if (additional.length() > 0)
                    rec.setAdditionalInfo(additional);
                if (address.length() > 0)
                    rec.setAddress(address);
                controller.editRecord(contactId, rec);
                response.sendRedirect("view?actstatus=editsucc");
            }
            else if (action.equals("delete")) {
                if (request.getParameter("contactId") == null) {
                    // replace with smth meaningful
                    response.sendRedirect("view");
                }
                long contactId = Long.parseLong(request.getParameter("contactId"));
                controller.deleteRecord(contactId);
                response.sendRedirect("view?actstatus=deletesucc");
            }
            else {
                // replace with smth meaningful
                response.sendRedirect("view");
            }
        }
    }
}
