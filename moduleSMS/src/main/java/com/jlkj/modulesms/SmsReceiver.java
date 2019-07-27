package com.jlkj.modulesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener mSmsListener;


    @Override
    public void onReceive(Context context, Intent intent) {
        //先测试广播能否执行成功
//        Toast.makeText(context, "短信到来了呢", Toast.LENGTH_SHORT).show();
//        System.out.println("短信到来了呢");

        //1 获得多条短信：
        Object[] objects = (Object[]) intent.getExtras().get("pdus");
        List<SMS> smsList = new ArrayList<>();
        // 2循环数组
        for (Object obj : objects) {
            //3 获得短信实例
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
            // 4 获得接收的短信的 号码 以及信息
            SMS sms = new SMS();
            sms.set_id(smsMessage.getIndexOnIcc());
            sms.setBody(smsMessage.getMessageBody());
            sms.setAddress(smsMessage.getOriginatingAddress());
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Date d = new Date(smsMessage.getTimestampMillis());
            String strDate = dateFormat.format(d);
            sms.setDate(strDate);
            sms.setPerson(0);
            sms.setType(smsMessage.getStatus());
            smsList.add(sms);
        }
        //Toast.makeText(context, smsList.get(0).getAddress()+smsList.get(0).getBody(), Toast.LENGTH_SHORT).show();
        if (mSmsListener!=null) {
            mSmsListener.onChanged(smsList);
        }


    }

    public static void setListener(SmsListener smsListener){
        mSmsListener = smsListener;
    }

}
