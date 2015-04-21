package mobi.wrt.android.smartcontacts.ads;

import android.app.Activity;

/**
 * Created by uladzimir_klyshevich on 4/21/15.
 */
public interface IAdsProvider {

    void onCreate(Activity activity);

    void onBackPressed(Activity activity);

    void onFabClick(Activity activity);

    void onPhoneClosed(Activity activity);
}
