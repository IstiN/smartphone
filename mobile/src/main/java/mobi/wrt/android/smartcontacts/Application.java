package mobi.wrt.android.smartcontacts;

import java.util.ArrayList;
import java.util.List;

import by.istin.android.xcore.CoreApplication;
import by.istin.android.xcore.XCoreHelper;

/**
 * Created by IstiN on 31.01.2015.
 */
public class Application extends CoreApplication {

    public static List<Class<? extends XCoreHelper.Module>> MODULES;

    static {
        MODULES = new ArrayList<>();
        MODULES.add(AppModule.class);
    }

    @Override
    public List<Class<? extends XCoreHelper.Module>> getModules() {
        return MODULES;
    }

}
