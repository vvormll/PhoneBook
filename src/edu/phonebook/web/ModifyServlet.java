package edu.phonebook.web;
import java.io.IOException;
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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        if (session.getAttribute("accountId") == null) {
            response.sendRedirect("login");
            return;
        }
        long accountId = (long) session.getAttribute("accountId");
        ServletContext ctx = getServletContext();
        if (request.getParameter("contactId") != null) {
            long contactId = Long.parseLong(request.getParameter("contactId"));
            DatabaseController controller = DatabaseService.getControllerFromServletContext(ctx);
            Record rec = null;
            try {
                rec = controller.getRecordById(accountId, contactId);
            } catch (IOException e) {
                throw new IOException("Record not found or you don't have the rights to modify it", e);
            }
            request.setAttribute("record", rec);
        }

        dispatchRequest(request, response, ctx);
    }

    private void dispatchRequest(HttpServletRequest request, HttpServletResponse response, ServletContext ctx)
            throws ServletException, IOException {
        String action = request.getParameter("act");
        if (action.equals("add")) {
            RequestDispatcher disp = getServletContext().getRequestDispatcher("/addform.jsp");
            disp.forward(request, response);
        } else if (action.equals("edit")) {
            RequestDispatcher disp = ctx.getRequestDispatcher("/editform.jsp");
            disp.forward(request, response);
        } else if (action.equals("delete")) {
            RequestDispatcher disp = ctx.getRequestDispatcher("/deleteform.jsp");
            disp.forward(request, response);
        } else {
            throw new IOException("Operation unknown or not specified");
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!isActiveSession(session)) {
            response.sendRedirect("login");
        } else {
            ServletContext ctx = getServletContext();
            DatabaseController controller = DatabaseService.getControllerFromServletContext(ctx);
            executeRequest(request, controller);
            response.sendRedirect("view");
        }
    }

    private boolean isActiveSession(HttpSession session) {
        return session != null && session.getAttribute("accountId") != null;
    }

    private void executeRequest(HttpServletRequest request, DatabaseController controller) throws IOException {
        HttpSession session = request.getSession(false);
        long accountId = (long) session.getAttribute("accountId");
        String action = request.getParameter("act");
        if (action.equals("add")) {
            Record rec = constructRecordFromRequest(request);
            controller.addRecord(accountId, rec);
        } else if (action.equals("edit")) {
            if (!isRecordSpecified(request)) {
                throw new IOException("Record not specified");
            }
            Record rec = constructRecordFromRequest(request);
            controller.editRecord(rec.getContactId(), rec);
        } else if (action.equals("delete")) {
            if (!isRecordSpecified(request)) {
                throw new IOException("Record not specified");
            }
            controller.deleteRecord(getLongContactId(request));
        } else {
            throw new IOException("Operation unknown or not specified");
        }
    }

    private boolean isRecordSpecified(HttpServletRequest request) {
        String contactIdString = request.getParameter("contactId");
        return contactIdString != null;
    }

    private long getLongContactId(HttpServletRequest request) {
        String contactIdString = request.getParameter("contactId");
        return Long.parseLong(contactIdString);
    }

    private Record constructRecordFromRequest(HttpServletRequest request) {
        String name = request.getParameter("contactName");
        String address = request.getParameter("address");
        String additional = request.getParameter("additional");
        String[] numbers = request.getParameter("numbers").split(" ");
        Set<String> numbersSet = new HashSet<>();
        for (String number : numbers) {
            if (!number.isEmpty()) {
                numbersSet.add(number);
            }
        }
        long contactId;
        if (request.getParameter("contactId") != null) {
            contactId = Long.parseLong(request.getParameter("contactId"));
        } else {
            contactId = -1;
        }
        Record rec = new Record(contactId, name, numbersSet);
        if (additional.length() > 0) {
            rec.setAdditionalInfo(additional);
        }
        if (address.length() > 0) {
            rec.setAddress(address);
        }
        return rec;
    }
}
