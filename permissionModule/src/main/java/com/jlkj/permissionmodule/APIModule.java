package com.jlkj.permissionmodule;

import android.support.annotation.NonNull;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class APIModule extends UZModule {

    public APIModule(UZWebView webView) {
        super(webView);
    }

    /**
     * <strong>函数</strong><br><br>
     * 该函数映射至Javascript中moduleDemo对象的showAlert函数<br><br>
     * <strong>JS Example：</strong><br>
     * moduleDemo.showAlert(argument);
     *
     * @param moduleContext (Required)
     */

    public void jsmethod_getPermission(final UZModuleContext moduleContext) {
        checkPermission(moduleContext);
    }

    /**
     * 动态申请读取权限
     */
    private void checkPermission(final UZModuleContext moduleContext) {
        List<String> permissionList = moduleContext.optArray("permission");
        Object[] objList = permissionList.toArray();
        List<String> tempList = new ArrayList<>();
        assert objList != null;
        String[] permissions = new String[objList.length];
        for (Object permission : objList) {
            tempList.add((String) permission);
        }
        tempList.toArray(permissions);
        final PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
                setBackcall(moduleContext,true);
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                setBackcall(moduleContext,false);
            }
        };

        PermissionsUtil.requestPermission(moduleContext.getContext(), permissionListener, permissions);

    }

    public void setBackcall(UZModuleContext moduleContext,boolean isGranted){
        JSONObject ret = new JSONObject();
        try {
            ret.put("isGranted", isGranted);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }

}
