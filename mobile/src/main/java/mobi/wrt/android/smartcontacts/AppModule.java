package mobi.wrt.android.smartcontacts;

import android.content.Context;

import by.istin.android.xcore.XCoreHelper;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;

/**
 * Created by IstiN on 31.01.2015.
 */
public class AppModule extends XCoreHelper.BaseModule {

    @Override
    protected void onCreate(Context context) {
        registerAppService(new ContactHelper());
    }

}
