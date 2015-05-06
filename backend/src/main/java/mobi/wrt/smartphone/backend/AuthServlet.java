/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package mobi.wrt.smartphone.backend;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        PrintWriter writer = resp.getWriter();
        if (user != null) {
            resp.sendRedirect("/index.html");
        } else {
            resp.setContentType("text/html");
            writer.println("<h2>GAE - Integrating Google user account</h2>");
            writer.println(
                    "Please <a href='"
                            + userService.createLoginURL(req.getRequestURI())
                            + "'> LogIn </a>");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        throw new UnsupportedOperationException("doPost is not supported");
    }
}
