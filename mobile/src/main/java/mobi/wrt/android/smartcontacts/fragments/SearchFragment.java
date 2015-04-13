package mobi.wrt.android.smartcontacts.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;

import by.istin.android.xcore.fragment.AbstractFragment;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.view.DrawerArrowDrawable;

/**
 * Created by IstiN on 31.01.2015.
 */
public class SearchFragment extends AbstractFragment {

    @Override
    public int getViewLayout() {
        return R.layout.fragment_search;
    }

    private View mShadowView;

    private EditText mEditText;

    private DrawerArrowDrawable mArrowDrawable;

    @Override
    public void onViewCreated(final View view) {
        super.onViewCreated(view);
        mEditText = (EditText) view.findViewById(R.id.search_edit);
        mShadowView = view.findViewById(R.id.shadow);
        mEditText.setVisibility(View.VISIBLE);
        mArrowDrawable = new DrawerArrowDrawable(getActivity(), getActivity());
        ImageView topButton = (ImageView) view.findViewById(R.id.arrow);
        topButton.setImageDrawable(mArrowDrawable);
        topButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearch();
            }
        });

        animate(0, 1, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.findViewById(R.id.search_input).setVisibility(View.INVISIBLE);
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
        mEditText.requestFocus();
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                UiUtil.showKeyboard(mEditText);
            }
        });
    }

    private void animate(int startValue, int endvalue, Animator.AnimatorListener listener) {
        ValueAnimator anim = ValueAnimator.ofFloat(startValue, endvalue);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                mArrowDrawable.setProgress(slideOffset);
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
        animate(1, 0, new Animator.AnimatorListener() {
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
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        UiUtil.hideKeyboard(mEditText);
    }
}
