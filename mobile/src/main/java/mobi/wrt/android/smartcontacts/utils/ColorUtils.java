package mobi.wrt.android.smartcontacts.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.support.v7.graphics.Palette;

import java.util.concurrent.ConcurrentHashMap;

import by.istin.android.xcore.utils.StringUtil;

/**
 * Created by IstiN on 18.02.2015.
 */
public class ColorUtils {

    private static ConcurrentHashMap<String, Drawable> sCache = new ConcurrentHashMap<>();

    public static Drawable getColorCircle(int size, String value) {
        if (value == null) {
            value = StringUtil.EMPTY;
        }
        Drawable drawable = sCache.get(value);
        if (drawable != null) {
            return drawable;
        }
        int color = calculateColor(value);
        drawable = createStateListDrawable(size, color);
        sCache.put(value, drawable);
        return drawable;
    }

    private static Drawable createStateListDrawable(int size, int color) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    };

    public static int calculateColorBase(String value) {
        if (value == null) {
            value = StringUtil.EMPTY;
        }
        String opacity = "#ff"; //opacity between 00-ff
        String hexColor = String.format(
                opacity + "%06X", (0xeeeeee & value.hashCode()));

        return Color.parseColor(hexColor);
    }

    public static int calculateColor(String value) {
        ShapeDrawable drawable = new ShapeDrawable(new RectShape());

        drawable.getPaint().setColor(calculateColorBase(value));
        drawable.setIntrinsicHeight(2);
        drawable.setIntrinsicWidth(2);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        Palette palette = Palette.generate(bitmap);
        bitmap.recycle();
        return palette.getVibrantColor(0xffcccccc);
    }

}
