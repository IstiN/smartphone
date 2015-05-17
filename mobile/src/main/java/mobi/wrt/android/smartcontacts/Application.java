package mobi.wrt.android.smartcontacts;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import java.util.ArrayList;
import java.util.List;

import by.istin.android.xcore.CoreApplication;
import by.istin.android.xcore.XCoreHelper;
import mobi.wrt.android.smartcontacts.utils.ThemeUtils;

/**
 * Created by IstiN on 31.01.2015.
 */
public class Application extends CoreApplication {

    public static List<Class<? extends XCoreHelper.Module>> MODULES;

    public static class RoundedTransformation implements com.squareup.picasso.Transformation {

        public RoundedTransformation() {

        }

        @Override
        public Bitmap transform(final Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());
            if (bitmap == null) {
                return source;
            }
            Canvas canvas = new Canvas(bitmap);

            Paint paint = new Paint();

            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size/2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;

        }

        @Override
        public String key() {
            return "rounded";
        }
    }

    public static RoundedTransformation ROUNDED_TRANSFORMATION = new RoundedTransformation();

    static {
        MODULES = new ArrayList<>();
        MODULES.add(AppModule.class);
    }

    @Override
    public List<Class<? extends XCoreHelper.Module>> getModules() {
        return MODULES;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeUtils.onContextCreate(this);
        if(BuildConfig.DEBUG){
            StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
            //        .detectAll()
                    .penaltyLog()
            //        .detectLeakedClosableObjects()
                    .build();
            StrictMode.setVmPolicy(policy);
            StrictMode.noteSlowCall("xxx_slow");
            StrictMode.ThreadPolicy.Builder threadPolicy = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().penaltyLog();
            StrictMode.setThreadPolicy(threadPolicy.build());
        } else {
            Crashlytics.start(this);
        }
    }

    @Override
    protected Class<?> getBuildConfigClass() {
        return BuildConfig.class;
    }
}
