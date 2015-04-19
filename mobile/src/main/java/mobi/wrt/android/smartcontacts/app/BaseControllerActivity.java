package mobi.wrt.android.smartcontacts.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import java.util.List;

import by.istin.android.xcore.callable.ISuccess;
import by.istin.android.xcore.ui.DialogBuilder;
import by.istin.android.xcore.utils.StringUtil;
import mobi.wrt.android.smartcontacts.R;
import mobi.wrt.android.smartcontacts.helper.ContactHelper;

/**
 * Created by IstiN on 18.02.2015.
 */
public class BaseControllerActivity extends ActionBarActivity {

    public void onContactMoreClick(View view) {
        final Object tag = view.getTag();
        if (tag instanceof String) {
            String phone = (String) tag;
            addContact(this, phone);
            return;
        }
        Long id = (Long) tag;
        openContact(id);
    }

    public static void addContact(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        context.startActivity(intent);
    }

    private void openContact(Long id) {
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onRecentContactClick(View view) {
        Object tag = view.getTag();
        if (tag instanceof Long) {
            openContact((Long)tag);
            return;
        }
        makeCall(BaseControllerActivity.this, (String)tag);
    }

    public void onContactClick(final View view) {
        final Long id = (Long) view.getTag();
        new Thread(new Runnable() {
            @Override
            public void run() {
                initPhoneNumbers(id, new ISuccess<List<ContentValues>>() {
                    @Override
                    public void success(List<ContentValues> contentValueses) {
                        final String[] numbers = new String[contentValueses.size()];
                        for (int i = 0; i < contentValueses.size(); i++) {
                            numbers[i] = contentValueses.get(i).getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        }
                        DialogBuilder.options(BaseControllerActivity.this, R.string.title_choose_number, numbers, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                makeCall(BaseControllerActivity.this, numbers[which]);
                            }
                        });
                    }

                }, new ISuccess<String>() {
                    @Override
                    public void success(String s) {
                        makeCall(BaseControllerActivity.this, s);
                    }
                });
            }
        }).start();
    }

    public void initPhoneNumbers(Long id, final ISuccess<List<ContentValues>> result, final ISuccess<String> success) {
        final List<ContentValues> entities = ContactHelper.get(this).getPhonesById(id);
        if (entities != null && !entities.isEmpty()) {
            if (entities.size() == 1) {
                final String phoneNumber = entities.get(0).getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (!StringUtil.isEmpty(phoneNumber)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            success.success(phoneNumber);
                        }
                    });
                }
            } else {
                for (ContentValues phoneValues : entities) {
                    if (phoneValues.getAsInteger(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY) > 0) {
                        final String phone = phoneValues.getAsString(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (!StringUtil.isEmpty(phone)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    success.success(phone);
                                }
                            });
                        }
                        return;
                    }
                }
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                result.success(entities);
                            }
                        }
                );
            }
        }
    }

    public static void makeCall(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
        context.startActivity(intent);
    }

    public static void sendSms(Context context, String phone) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + phone));
        context.startActivity(intent);
    }

}
