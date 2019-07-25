package com.jlkj.modulesms;

import android.support.annotation.NonNull;

import java.util.List;

public interface SmsListener {

    /**
     * 监听变化
     */
    void onChanged(@NonNull List<SMS> smsList);
}
