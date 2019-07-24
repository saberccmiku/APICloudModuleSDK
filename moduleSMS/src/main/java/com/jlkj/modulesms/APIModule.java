package com.jlkj.modulesms;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class APIModule extends UZModule {

    public APIModule(UZWebView webView) {
        super(webView);
    }
    private JSONObject ret = new JSONObject();

    /**
     * <strong>函数</strong><br><br>
     * 该函数映射至Javascript中moduleDemo对象的showAlert函数<br><br>
     * <strong>JS Example：</strong><br>
     * moduleDemo.showAlert(argument);
     *
     * @param moduleContext (Required)
     */

    public void jsmethod_getSMS(final UZModuleContext moduleContext) {
        getSMS(moduleContext);
    }


//    public void jsmethod_registerSMS(final UZModuleContext moduleContext) {
//        new MyHandler(moduleContext);
//    }



    /**
     * 动态申请读取权限
     */
    private void getSMS(final UZModuleContext moduleContext) {
        String[] permissions = {"android.permission.READ_SMS",
                "android.permission.RECEIVE_SMS"};
        final PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
                setBackcall(moduleContext,true);
                SharedPreferences sharedPreferences = moduleContext.getContext().getSharedPreferences("appSetting", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (!sharedPreferences.getBoolean("IsFirstView",false)) {
                    if (MiuiUtils.isMIUI()) {
                        Toast.makeText(moduleContext.getContext(), "你当前使用的小米手机，请在权限管理中设置通知类短信权限", Toast.LENGTH_LONG).show();
                        MiuiUtils.goPermissionSettings((Activity) moduleContext.getContext());
                        editor.putBoolean("IsFirstView", true);
                        editor.apply();//先提交到内存，然后异步提交到磁盘，效率更高，但没有返回消息。
                    }
                }
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                setBackcall(moduleContext,false);
            }
        };

        PermissionsUtil.requestPermission(moduleContext.getContext(), permissionListener, permissions);

    }

    private void setBackcall(UZModuleContext moduleContext, boolean isGranted){
        List<SMS> smsInPhone = getSmsInPhone(moduleContext);
        try {
            ret.put("isGranted", isGranted);
            ret.put("sms",smsInPhone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }

    /**
     * 查询手机短信
     */
    private List<SMS> getSmsInPhone(UZModuleContext moduleContext) {
        String where = moduleContext.optString("where");//为null默认查询所有的
        final String SMS_URI_ALL = "content://sms/"; // 所有短信
        final String SMS_URI_INBOX = "content://sms/inbox"; // 收件箱
        final String SMS_URI_SEND = "content://sms/sent"; // 已发送
        final String SMS_URI_DRAFT = "content://sms/draft"; // 草稿
        final String SMS_URI_OUTBOX = "content://sms/outbox"; // 发件箱
        final String SMS_URI_FAILED = "content://sms/failed"; // 发送失败
        final String SMS_URI_QUEUED = "content://sms/queued"; // 待发送列表

        List<SMS> smsList = new ArrayList<>();

        try {
            Uri uri = Uri.parse(SMS_URI_ALL);
            String[] projection = new String[]{"_id", "address", "person",
                    "body", "date", "type",};
            Cursor cur = moduleContext.getContext().getContentResolver().query(uri, projection, where,
                    null, "date desc"); // 获取手机内部短信
            // 获取短信中最新的未读短信
            // Cursor cur = getContentResolver().query(uri, projection,
            // "read = ?", new String[]{"0"}, "date desc");
            assert cur != null;
            if (cur.moveToFirst()) {
                int index_id = cur.getColumnIndex("_id");
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");

                do {
                    int _id = cur.getInt(index_id);
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String body = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    int intType = cur.getInt(index_Type);

                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    Date d = new Date(longDate);
                    String strDate = dateFormat.format(d);

                    String strType;
                    if (intType == 1) {
                        strType = "接收";
                    } else if (intType == 2) {
                        strType = "发送";
                    } else if (intType == 3) {
                        strType = "草稿";
                    } else if (intType == 4) {
                        strType = "发件箱";
                    } else if (intType == 5) {
                        strType = "发送失败";
                    } else if (intType == 6) {
                        strType = "待发送列表";
                    } else if (intType == 0) {
                        strType = "所有短信";
                    } else {
                        strType = "null";
                    }

                    SMS sms = new SMS();
                    sms.set_id(_id);
                    sms.setAddress(strAddress);
                    sms.setPerson(intPerson);
                    sms.setBody(body);
                    sms.setDate(strDate);
                    sms.setType(strType);

                    smsList.add(sms);

                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            }

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return smsList;
    }


    private static class MyHandler extends Handler {

        // SoftReference<Activity> 也可以使用软应用 只有在内存不足的时候才会被回收
        private final WeakReference<UZModuleContext> mActivity;

        private MyHandler(UZModuleContext activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UZModuleContext activity = mActivity.get();
            if (activity != null){
                SmsMessage smsMessage = (SmsMessage) msg.obj;
                JSONObject jsonObject = null;
                try {
                    jsonObject = activity.get().put("newSMS",smsMessage.getMessageBody());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("-----------------------------------------");
                System.out.println(smsMessage.getMessageBody());
                System.out.println("-----------------------------------------");
                activity.success(jsonObject,true);
            }
            super.handleMessage(msg);
        }
    }

}
