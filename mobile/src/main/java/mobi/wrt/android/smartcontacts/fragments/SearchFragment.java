package mobi.wrt.android.smartcontacts.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;

import by.istin.android.xcore.fragment.AbstractFragment;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SearchFragment extends AbstractFragment {

    @Override
    public int getViewLayout() {
        return R.layout.fragment_search;
    }

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private View mShadowView;

    private EditText mEditText;

    @Override
    public void onViewCreated(final View view) {
        super.onViewCreated(view);
        mEditText = (EditText) view.findViewById(R.id.search_edit);
        mShadowView = view.findViewById(R.id.shadow);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout, mToolbar, R.string.search_hint,
                R.string.search_hint) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
            }
        };
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        animate(view, 0, 1, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.findViewById(R.id.search_input).setVisibility(View.INVISIBLE);
                mEditText = (EditText) view.findViewById(R.id.search_edit);
                mEditText.setVisibility(View.VISIBLE);
                UiUtil.showKeyboard(mEditText);
                mShadowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeSearch();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void animate(final View view, int startValue, int endvalue, Animator.AnimatorListener listener) {
        ValueAnimator anim = ValueAnimator.ofFloat(startValue, endvalue);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                mDrawerToggle.onDrawerSlide(mDrawerLayout, slideOffset);
                if (slideOffset < 0.57f) {
                    mShadowView.setAlpha(slideOffset);
                } else {
                    mShadowView.setAlpha(0.57f);
                }
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        // You can change this duration to more closely match that of the default animation.
        anim.setDuration(300);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    private boolean isCloseRunning = false;

    public void closeSearch() {
        if (isCloseRunning) {
            return;
        }
        isCloseRunning = true;
        animate(getView(), 1, 0, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.getSupportFragmentManager().popBackStackImmediate();
                UiUtil.hideKeyboard(mEditText);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
