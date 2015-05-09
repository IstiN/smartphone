package mobi.wrt.android.smartcontacts.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import by.istin.android.xcore.preference.PreferenceHelper;
import mobi.wrt.android.smartcontacts.R;

/**
 * Created by uladzimir_klyshevich on 4/28/15.
 */
public class ThemeUtils {

    public static final String CURRENT_THEME_KEY = "current_theme";

    public enum ThemeValue {
        RED_LIGHT(R.style.RedLightAppTheme),
        PINK_LIGHT(R.style.PinkLightAppTheme),
        PURPLE_LIGHT(R.style.PurpleLightAppTheme),
        DEEP_PURPLE_LIGHT(R.style.DeepPurpleLightAppTheme),
        INDIGO_LIGHT(R.style.IndigoLightAppTheme),
        BLUE_LIGHT(R.style.BlueLightAppTheme),
        LIGHT_BLUE_LIGHT(R.style.LightBlueLightAppTheme),
        CYAN_LIGHT(R.style.CyanLightAppTheme),
        TEAL_LIGHT(R.style.TealLightAppTheme),
        GREEN_LIGHT(R.style.GreenLightAppTheme),
        LIGHT_GREEN_LIGHT(R.style.LightGreenLightAppTheme),
        LIME_LIGHT(R.style.LimeLightAppTheme),
        YELLOW_LIGHT(R.style.YellowLightAppTheme),
        AMBER_LIGHT(R.style.AmberLightAppTheme),
        ORANGE_LIGHT(R.style.OrangeLightAppTheme),
        DEEP_ORANGE_LIGHT(R.style.DeepOrangeLightAppTheme),
        BROWN_LIGHT(R.style.BrownLightAppTheme),
        GRAY_LIGHT(R.style.GrayLightAppTheme),
        BLUE_GRAY_LIGHT(R.style.BlueGrayLightAppTheme),

        BLACK_GRAY_DARK(R.style.BlackDarkAppTheme),

        RED_DARK(R.style.RedDarkAppTheme),
        PINK_DARK(R.style.PinkDarkAppTheme),
        PURPLE_DARK(R.style.PurpleDarkAppTheme),
        DEEP_PURPLE_DARK(R.style.DeepPurpleDarkAppTheme),
        INDIGO_DARK(R.style.IndigoDarkAppTheme),
        BLUE_DARK(R.style.BlueDarkAppTheme),
        LIGHT_BLUE_DARK(R.style.LightBlueDarkAppTheme),
        CYAN_DARK(R.style.CyanDarkAppTheme),
        TEAL_DARK(R.style.TealDarkAppTheme),
        GREEN_DARK(R.style.GreenDarkAppTheme),
        LIGHT_GREEN_DARK(R.style.LightGreenDarkAppTheme),
        LIME_DARK(R.style.LimeDarkAppTheme),
        YELLOW_DARK(R.style.YellowDarkAppTheme),
        AMBER_DARK(R.style.AmberDarkAppTheme),
        ORANGE_DARK(R.style.OrangeDarkAppTheme),
        DEEP_ORANGE_DARK(R.style.DeepOrangeDarkAppTheme),
        BROWN_DARK(R.style.BrownDarkAppTheme),
        GRAY_DARK(R.style.GrayDarkAppTheme),
        BLUE_GRAY_DARK(R.style.BlueGrayDarkAppTheme)
        ;



        private int mThemeStyle;

        ThemeValue(int themeStyle) {
            this.mThemeStyle = themeStyle;
        }


        @Override
        public String toString() {
            return super.toString().replace("_", " ");
        }
    }

    public static int onContextCreate(Context context) {
        int themeOrdinal = PreferenceHelper.getInt(CURRENT_THEME_KEY, ThemeValue.LIGHT_BLUE_LIGHT.ordinal());
        int mThemeStyle = ThemeValue.values()[themeOrdinal].mThemeStyle;
        context.setTheme(mThemeStyle);
        return mThemeStyle;
    }

    public static void setTheme(Activity activity, ThemeValue themeValue) {
        PreferenceHelper.set(CURRENT_THEME_KEY, themeValue.ordinal());
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }

}
