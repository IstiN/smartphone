package mobi.wrt.android.smartcontacts.ads;

import android.content.Context;

import java.io.InputStream;
import java.io.Serializable;

import by.istin.android.xcore.processor.impl.AbstractGsonProcessor;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.IDataSource;

public class AdsProcessor extends AbstractGsonProcessor<AdsProcessor.AdsItem[]> {

    public static class AdsItem implements Serializable {

        private String i;

        private String icon;

        private String title;

        private String link;

        private String descr;

        private String pack;

        public String getInformation() {
            return i;
        }

        public String getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public String getDescription() {
            return descr;
        }

        public String getPackage() {
            return pack;
        }
    }

    public static final String APP_SERVICE_KEY = "wrt:ads:processor";

    public AdsProcessor() {
        super(AdsItem[].class);
    }

    @Override
    public AdsItem[] execute(DataSourceRequest dataSourceRequest, IDataSource<InputStream> dataSource, InputStream inputStream) throws Exception {
        return super.execute(dataSourceRequest, dataSource, inputStream);
    }

    @Override
    public void cache(Context context, DataSourceRequest dataSourceRequest, AdsItem[] adsItem) throws Exception {

    }

    @Override
    public String getAppServiceKey() {
        return APP_SERVICE_KEY;
    }
}
