package mobi.wrt.smartphone.backend.auth;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by uladzimir_klyshevich on 5/6/15.
 */
public class SecurityUtils {

    public static boolean hasPermission(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if (user != null &&  user.getEmail().equals("istin2007@gmail.com")) {
            return true;
        }
        resp.sendRedirect("/auth");
        return false;
    }

}
