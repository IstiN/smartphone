package mobi.wrt.android.smartcontacts.gcm;

import android.content.Context;

import by.istin.android.xcore.processor.impl.AbstractGsonProcessor;
import by.istin.android.xcore.source.DataSourceRequest;

public class RegisterDeviceProcessor extends AbstractGsonProcessor<RegisterDeviceProcessor.Response> {

    public static final String APP_SERVICE_KEY = "core:registerdevice:gcm";

    public static class Response {

    }

    public RegisterDeviceProcessor() {
        super(Response.class);
    }

    @Override
    public void cache(Context context, DataSourceRequest dataSourceRequest, Response o) throws Exception {

    }

    @Override
    public String getAppServiceKey() {
        return APP_SERVICE_KEY;
    }
}
