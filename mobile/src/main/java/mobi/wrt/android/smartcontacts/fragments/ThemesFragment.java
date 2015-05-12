package mobi.wrt.android.smartcontacts.fragments;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.fragment.AbstractFragment;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.app.ThemesActivity;
import mobi.wrt.android.smartcontacts.utils.ThemeUtils;

/**
 * Created by uladzimir_klyshevich on 4/30/15.
 */
public class ThemesFragment extends AbstractFragment {

    @Override
    public int getViewLayout() {
        return R.layout.fragment_themes;
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        GridView gridView = (GridView) view.findViewById(android.R.id.list);
        final ThemeUtils.ThemeValue[] values = ThemeUtils.ThemeValue.values();
        int columnWidth = UiUtil.getDisplayWidth() / 2;
        if (columnWidth < 240) {
            columnWidth = 160;
        } else if (columnWidth < 320) {
            columnWidth = 240;
        } else if (columnWidth < 480) {
            columnWidth = 320;
        } else if (columnWidth < 720) {
            columnWidth = 480;
        } else if (columnWidth < 1080) {
            columnWidth = 720;
        } else {
            columnWidth = 1080;
        }

        ITracker.Impl.get(getActivity()).track("themes:width:"+ columnWidth);

        final int finalImageWidth = columnWidth;

        gridView.setAdapter(new ArrayAdapter<ThemeUtils.ThemeValue>(getActivity(),
                R.layout.adapter_theme,
                android.R.id.text1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView imageView = (ImageView) view.findViewById(R.id.icon);
                String path = "http://robust-window-94306.appspot.com/themes/" + finalImageWidth + "/" + values[position].name() + ".jpeg";
                Picasso.with(getActivity()).load(path).into(imageView);
                return view;
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(ThemesActivity.EXTRA_THEME_ORDINAL_KEY, position);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        });
    }
}
