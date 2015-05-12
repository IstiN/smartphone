package mobi.wrt.android.smartcontacts.config;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import by.istin.android.xcore.preference.PreferenceHelper;
import by.istin.android.xcore.processor.impl.AbstractGsonProcessor;
import by.istin.android.xcore.source.DataSourceRequest;
import by.istin.android.xcore.source.IDataSource;
import by.istin.android.xcore.utils.StringUtil;
import mobi.wrt.android.smartcontacts.BuildConfig;

public class ConfigProcessor extends AbstractGsonProcessor<ConfigProcessor.Config> {

    public static final String CONFIG_KEY = "config_key";

    public static class Config implements Serializable {

        private String supportEmail;

        private String fbLikeUrl;

        private String plusOneUrl;

        private String githubUrl;

        private String proUrl;

        private String market;

        private String shareUrl;

        private Integer version;

        private String versionName;

        private String aboutAppUrl;

        private String rateAppUrl;

        public static class Group implements Serializable {

            @SerializedName("n")
            private String name;

            @SerializedName("v")
            private String value;

            public String getName() {
                return name;
            }

            public String getValue() {
                return value;
            }
        }

        private List<Group> groups;

        public List<Group> getGroups() {
            return groups;
        }

        @SerializedName("c")
        private List<Config> configs;

        public String getSupportEmail() {
            return supportEmail;
        }

        public String getFbLikeUrl() {
            return fbLikeUrl;
        }

        public String getPlusOneUrl() {
            return plusOneUrl;
        }

        public String getGithubUrl() {
            return githubUrl;
        }

        public String getProUrl() {
            return proUrl;
        }

        public String getShareUrl() {
            return shareUrl;
        }

        public Integer getVersion() {
            return version;
        }

        public String getVersionName() {
            return versionName;
        }

        public String getAboutAppUrl() {
            return aboutAppUrl;
        }

        public String getRateAppUrl() {
            return rateAppUrl;
        }
    }

    public static final String APP_SERVICE_KEY = "wrt:ads:processor";

    public ConfigProcessor() {
        super(Config.class);
    }

    @Override
    protected Config process(DataSourceRequest dataSourceRequest, Gson gson, BufferedReader bufferedReader) {
        Config config = super.process(dataSourceRequest, gson, bufferedReader);
        List<Config> configs = config.configs;
        if (configs != null) {
            for (Config c : configs) {
                if (c.market.equals(BuildConfig.FLAVOR_market)) {
                    if (!StringUtil.isEmpty(c.supportEmail)) {
                        config.supportEmail = c.supportEmail;
                    }
                    if (!StringUtil.isEmpty(c.fbLikeUrl)) {
                        config.fbLikeUrl = c.fbLikeUrl;
                    }
                    if (!StringUtil.isEmpty(c.plusOneUrl)) {
                        config.plusOneUrl = c.plusOneUrl;
                    }
                    if (!StringUtil.isEmpty(c.githubUrl)) {
                        config.githubUrl = c.githubUrl;
                    }
                    if (!StringUtil.isEmpty(c.proUrl)) {
                        config.proUrl = c.proUrl;
                    }
                    if (!StringUtil.isEmpty(c.shareUrl)) {
                        config.shareUrl = c.shareUrl;
                    }
                    if (c.version != null) {
                        config.version = c.version;
                    }
                    if (!StringUtil.isEmpty(c.versionName)) {
                        config.versionName = c.versionName;
                    }
                    if (!StringUtil.isEmpty(c.aboutAppUrl)) {
                        config.aboutAppUrl = c.aboutAppUrl;
                    }
                    if (!StringUtil.isEmpty(c.rateAppUrl)) {
                        config.rateAppUrl = c.rateAppUrl;
                    }
                    break;
                }
            }
            config.configs = null;
        }
        return config;
    }

    @Override
    public void cache(Context context, DataSourceRequest dataSourceRequest, Config config) throws Exception {
        String value = new Gson().toJson(config);
        PreferenceHelper.set(CONFIG_KEY, value);
    }

    public static Config getFromCache() {
        String value = PreferenceHelper.getString(CONFIG_KEY, null);
        return new Gson().fromJson(value, Config.class);
    }

    @Override
    public String getAppServiceKey() {
        return APP_SERVICE_KEY;
    }
}
