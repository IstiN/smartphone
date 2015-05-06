/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package mobi.wrt.smartphone.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.images.ImagesServiceFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mobi.wrt.smartphone.backend.auth.SecurityUtils;

public class ConfigServlet extends HttpServlet {

    private JSONObject mJSONObject;

    private final Object mLock = new Object();

    private Entity mEntity;

    public ConfigServlet() {
        super();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        final Query query = new Query("config");
        PreparedQuery pq = datastore.prepare(query);
        Iterable<Entity> entities = pq.asIterable();
        Iterator<Entity> iterator = entities.iterator();
        if (iterator.hasNext()) {
            mEntity = iterator.next();
            try {
                mJSONObject = new JSONObject((String)(mEntity.getProperties().get("value")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mJSONObject = new JSONObject();
            mEntity = new Entity("config");
            mEntity.setProperty("value", mJSONObject.toString());
            datastore.put(mEntity);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.getWriter().print(mJSONObject.toString());
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (SecurityUtils.hasPermission(req, resp)) {
            //TODO update config
            String value = req.getParameter("value");
            resp.setContentType("application/json");
            try {
                mJSONObject = new JSONObject(value);
                mEntity.setProperty("value", mJSONObject.toString());
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                datastore.put(mEntity);
                resp.getWriter().write(new JSONObject().put("success", true).toString());
            } catch (JSONException e) {
                try {
                    resp.getWriter().write(new JSONObject().put("error", e.toString()).toString());
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
