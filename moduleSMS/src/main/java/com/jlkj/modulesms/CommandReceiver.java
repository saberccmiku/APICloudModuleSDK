package com.jlkj.modulesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.SmsMessage;

public class CommandReceiver extends BroadcastReceiver {

    private Handler mHandler; // 更新UI
    protected static final int MSG_INBOX = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        //pdus短信单位pdu
        //解析短信内容
        if (intent.getExtras()!=null){
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            if (pdus!=null) {
                for (Object pdu : pdus) {
                    //封装短信参数的对象
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
//            String number = sms.getOriginatingAddress();
//            String body = sms.getMessageBody();
                    System.out.println("------------------封装短信参数的对象----------------------------");
                    System.out.println(sms.getMessageBody());
                    System.out.println("-----------------封装短信参数的对象-----------------------------");
                    //写自己的处理逻辑
                    mHandler.obtainMessage(MSG_INBOX, sms);
                }
            }
        }
    }
}
