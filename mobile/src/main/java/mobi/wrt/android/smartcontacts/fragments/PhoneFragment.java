package mobi.wrt.android.smartcontacts.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.analytics.ITracker;
import by.istin.android.xcore.fragment.AbstractFragment;
import by.istin.android.xcore.utils.CursorUtils;
import by.istin.android.xcore.utils.Holder;
import by.istin.android.xcore.utils.Log;
import by.istin.android.xcore.utils.StringUtil;
import by.istin.android.xcore.utils.UiUtil;
import mobi.wrt.android.smartcontacts.Application;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.anim.SimpleAnimationListener;
import mobi.wrt.android.smartcontacts.app.BaseControllerActivity;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;
import mobi.wrt.android.smartcontacts.utils.ColorUtils;

/**
 * Created by IstiN on 31.01.2015.
 */
public class PhoneFragment extends AbstractFragment {

    public static final int NUMBER_STAR = 10;
    public static final int NUMBER_SHARP = 11;
    public static final String EXTRA_PHONE = "phone";
    private View mShadowView;

    private EditText mEditText;

    private View mBackspaceBtn;

    private View mPhone;

    private int mInitialBottomMargin = 0;

    private View mQuickPhoneCall;
    @Override
    public int getViewLayout() {
        return R.layout.fragment_phone;
    }

    @Override
    public void onViewCreated(final View view) {
        super.onViewCreated(view);
        final ITracker tracker = ITracker.Impl.get(getActivity());
        tracker.track("phone");
        mQuickPhoneCall = view.findViewById(R.id.quick_phone_call);
        mEditText = (EditText) view.findViewById(R.id.edit_phone);
        final String phone = getPhone();
        setNumber(phone);
        mPhone = view.findViewById(R.id.phone);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPhone.getLayoutParams();
        mInitialBottomMargin = layoutParams.bottomMargin;
        layoutParams.bottomMargin = -UiUtil.getDisplayHeight();
        mPhone.setLayoutParams(layoutParams);
        mPhone.setVisibility(View.INVISIBLE);
        final Holder<ViewTreeObserver> viewTreeObserverHolder = new Holder<>(mPhone.getViewTreeObserver());
        viewTreeObserverHolder.get().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPhone.getLayoutParams();
                int height = mPhone.getHeight();
                Log.xd(PhoneFragment.this, " height " + height);
                layoutParams.bottomMargin = -height + mInitialBottomMargin;
                mPhone.setLayoutParams(layoutParams);
                mPhone.setVisibility(View.VISIBLE);
                animate(0, 1, null);
                ViewTreeObserver viewTreeObserver = viewTreeObserverHolder.get();
                if (!viewTreeObserver.isAlive()) {
                    viewTreeObserver = mPhone.getViewTreeObserver();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this);
                } else {
                    viewTreeObserver.removeGlobalOnLayoutListener(this);
                }
            }

        });
        disableSoftInputFromAppearing(mEditText);
        mShadowView = view.findViewById(R.id.shadow);
        mShadowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePhone();
            }
        });
        mEditText.setVisibility(View.VISIBLE);
        mBackspaceBtn = view.findViewById(R.id.backspace);
        mBackspaceBtn.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.btn_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable phone = mEditText.getText();
                tracker.track("phone:makephone");
                if (phone.length() > 0) {
                    BaseControllerActivity.makeCall(getActivity(), phone.toString());
                }
            }
        });
        view.findViewById(R.id.btn_add_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable phone = mEditText.getText();
                tracker.track("phone:addcontact");
                if (phone.length() > 0) {
                    BaseControllerActivity.addContact(getActivity(), phone.toString());
                }
            }
        });
        view.findViewById(R.id.btn_send_sms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable phone = mEditText.getText();
                tracker.track("phone:sendsms");
                if (phone.length() > 0) {
                    BaseControllerActivity.sendSms(getActivity(), phone.toString());
                }
            }
        });
        view.findViewById(R.id.btn_voice_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.track("phone:voicemail");
                FragmentActivity activity = getActivity();
                TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
                BaseControllerActivity.makeCall(activity, telephonyManager.getVoiceMailNumber());
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    mBackspaceBtn.setVisibility(View.VISIBLE);
                    if (s.length() > 2) {
                        final FragmentActivity activity = getActivity();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Uri uri = ContactsContract.Data.CONTENT_URI;
                                String[] projection = new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                                String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " like ?";
                                String[] selectionArgs = { "%"+s.toString()+"%" };
                                Cursor cursor = activity.getContentResolver().query(uri, projection, selection, selectionArgs, ContactsContract.PhoneLookup._ID + " ASC limit 0,1");
                                if (!CursorUtils.isEmpty(cursor) && cursor.moveToFirst()) {
                                    final String phoneNumber = CursorUtils.getString(ContactsContract.CommonDataKinds.Phone.NUMBER, cursor);
                                    final String displayName = CursorUtils.getString(ContactsContract.PhoneLookup.DISPLAY_NAME, cursor);
                                    ContactHelper.get(activity).initPhotoAndContactIdUri(phoneNumber);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mQuickPhoneCall.findViewById(R.id.clickableView).setTag(phoneNumber);
                                            String title = StringUtil.isEmpty(displayName) ? phoneNumber : displayName;
                                            ((TextView)mQuickPhoneCall.findViewById(R.id.name)).setText(title);
                                            ((TextView)mQuickPhoneCall.findViewById(R.id.description)).setText(phoneNumber);
                                            ContactHelper contactHelper = ContactHelper.get(ContextHolder.get());
                                            Long contactId = contactHelper.getContactId(phoneNumber);
                                            ImageView imageView = (ImageView) mQuickPhoneCall.findViewById(R.id.icon);
                                            TextView characterView = (TextView) mQuickPhoneCall.findViewById(R.id.character);

                                            imageView.setTag(contactId);
                                            String contactPhotoUri = contactHelper.getContactPhotoUri(phoneNumber);
                                            if (contactPhotoUri == null) {
                                                if (contactId == null) {
                                                    mQuickPhoneCall.setVisibility(View.INVISIBLE);
                                                } else {
                                                    mQuickPhoneCall.setVisibility(View.VISIBLE);
                                                    if (StringUtil.isEmpty(displayName)) {
                                                        characterView.setText(StringUtil.EMPTY);
                                                    } else {
                                                        characterView.setText(displayName == null ? "?" : String.valueOf(Character.toUpperCase(displayName.charAt(0))));
                                                    }
                                                }
                                                UiUtil.setBackground(imageView, ColorUtils.getColorCircle(imageView.getHeight(), displayName));
                                            } else {
                                                mQuickPhoneCall.setVisibility(View.VISIBLE);
                                                characterView.setText(StringUtil.EMPTY);
                                                UiUtil.setBackground(imageView, null);
                                            }
                                            Picasso.with(activity).load(contactPhotoUri).transform(Application.ROUNDED_TRANSFORMATION).into(imageView);
                                        }
                                    });
                                } else {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mQuickPhoneCall.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                                CursorUtils.close(cursor);
                            }
                        }).start();
                    } else {
                        mQuickPhoneCall.setVisibility(View.INVISIBLE);
                    }
                } else {
                    mQuickPhoneCall.setVisibility(View.INVISIBLE);
                    mBackspaceBtn.setVisibility(View.INVISIBLE);
                }
            }
        });
        mBackspaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker.track("phone:backspace");
                Editable currentText = mEditText.getText();
                int selectionStart = mEditText.getSelectionStart();
                if (selectionStart != 0) {
                    currentText.delete(selectionStart - 1, selectionStart);
                }
            }
        });
        mBackspaceBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tracker.track("phone:backspace_long_tap");
                mEditText.getText().clear();
                return true;
            }
        });
        for (int i = 0; i < 12; i++) {
            View button = view.findViewWithTag("" + i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String) v.getTag();
                    int number = Integer.parseInt(tag);
                    if (number < 10) {
                        tracker.track("phone:number");
                        updateText(String.valueOf(number));
                    } else if (number == NUMBER_STAR) {
                        tracker.track("phone:star");
                        updateText("*");
                    } else if (number == NUMBER_SHARP) {
                        tracker.track("phone:sharp");
                        updateText("#");
                    }
                }
            });
            if (i == 0 || i == NUMBER_SHARP || i == NUMBER_STAR) {
                button.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String tag = (String) v.getTag();
                        int number = Integer.parseInt(tag);
                        if (number == 0) {
                            updateText("+");
                            tracker.track("phone:+");
                            return true;
                        } else if (mEditText.getText().length() > 0) {
                            if (number == NUMBER_STAR) {
                                updateText(",");
                                tracker.track("phone:,");
                                return true;
                            } else if (number == NUMBER_SHARP) {
                                updateText(";");
                                tracker.track("phone:;");
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }
        }
    }

    private String newPhone;

    private void setNumber(String phone) {
        if (isValidPhoneNumber(phone)) {
            if (mEditText == null) {
                newPhone = phone;
                return;
            }
            mEditText.setText(phone);
            mEditText.setSelection(phone.length());
        }
    }

    private String getPhone() {
        if (newPhone != null) {
            return newPhone;
        }
        Bundle arguments = getArguments();
        if (arguments != null) {
            return arguments.getString(EXTRA_PHONE);
        } else {
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final FragmentActivity activity = getActivity();
        final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CharSequence charSequence = readFromClipboard(clipboard);
                if (charSequence != null) {
                    final String phone = charSequence.toString().replace(" ", StringUtil.EMPTY);
                    if (isValidPhoneNumber(phone)) {
                        final View view = getView();
                        if (view == null) {
                            return;
                        }
                        view.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = (TextView) view.findViewById(R.id.paste);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(Html.fromHtml("<u>" + ContextHolder.get().getString(android.R.string.paste) + "</u>?"), TextView.BufferType.SPANNABLE);
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mEditText.setText(phone);
                                        mEditText.setSelection(phone.length());
                                    }
                                });
                            }
                        }, 300l);
                    }
                }
            }
        }).start();
    }

    public CharSequence readFromClipboard(ClipboardManager clipboard) {
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            ClipData.Item item = clip.getItemAt(0);
            CharSequence text = item.getText();
            Log.xd(this, "clipboard: " + text);
            if (StringUtil.isEmpty(text)) {
                return null;
            }
            return text;
        }
        return null;
    }

    public static final boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    private void updateText(String symbol) {
        Editable text = mEditText.getText();
        int selectionStart = mEditText.getSelectionStart();
        text.insert(selectionStart, symbol);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void animate(final int startValue, int endValue, Animator.AnimatorListener listener) {
        ValueAnimator anim = ValueAnimator.ofFloat(startValue, endValue);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                int height = mPhone.getHeight();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPhone.getLayoutParams();
                int bottomMargin = (int) (-height + mInitialBottomMargin + slideOffset * height);
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, bottomMargin);
                mPhone.setLayoutParams(layoutParams);
                if (slideOffset < 0.57f) {
                    mShadowView.setAlpha(slideOffset);
                } else {
                    mShadowView.setAlpha(0.57f);
                }
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    public static void disableSoftInputFromAppearing(EditText editText) {
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTextIsSelectable(true);
    }

    private boolean isClosePhone = false;

    public void closePhone() {
        if (isClosePhone) {
            return;
        }
        isClosePhone = true;
        animate(1, 0, new SimpleAnimationListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }
                if (activity.isFinishing()) {
                    return;
                }
                activity.getSupportFragmentManager().popBackStackImmediate();
            }

        });
    }

    public void updatePhone(String phoneNumber) {
        setNumber(phoneNumber);
    }
}
