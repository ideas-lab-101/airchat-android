package com.android.crypt.chatapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.internal.SnackbarContentLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.SnackbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.widget.swipeback.SwipeBackActivityInterface;
import com.android.crypt.chatapp.widget.swipeback.lib.Utils;
import com.android.crypt.chatapp.utility.Common.ParameterUtil;
import com.android.crypt.chatapp.utility.Common.StatusBarUtil;
import com.android.crypt.chatapp.widget.swipeback.SwipeBackActivityHelper;
import com.android.crypt.chatapp.widget.swipeback.lib.SwipeBackLayout;

import permissions.dispatcher.PermissionRequest;

/**
 * Created by mulaliu on 16/11/8.
 *
 * 基础Activity，用以承载基础方法
 */

public class BaseActivity extends AppCompatActivity implements SwipeBackActivityInterface {

    public Toolbar toolbar;
//    private SDKReceiver mReceiver;
    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        //地图
//        IntentFilter iFilter = new IntentFilter();
//        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
//        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
//        mReceiver = new SDKReceiver();
//        registerReceiver(mReceiver, iFilter);
        //Activity将自身注入List，在应用exit时集中销毁
        ChatAppApplication.getDBcApplication().addActivity(this);
        //状态栏
        StatusBarUtil.StatusBarLightMode(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //滑动
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

    public boolean checkLoginState(){
        boolean r = false;
        if (getPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "loginState", "0").equals("1")){
            r = true;
        }
        return r;
    }

    /**
     * 优化显示的toast
     * @param context
     * @param msg
     * @param imgRes
     * @param duration
     */
    public void makeToast(Context context, String msg, int imgRes, int duration){
        /**
         * 主线程中创建handler后会默认创建一个looper对象。但是子线程不会，需要手动创建。
         * 其ThreadLocal中没有设置过Looper，mLooper==null ,所以会抛出异常。
         */
        Looper.prepare();
        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.CENTER, 0, 100);
        LinearLayout toastView = (LinearLayout) toast.getView();
        ImageView imgToast = new ImageView(this);
        imgToast.setImageResource(imgRes);
        imgToast.setPadding(5,30,5,30);
        toastView.addView(imgToast, 0);
        toast.show();
        Looper.loop();
    }

    public Snackbar makeSnake(View view, String msg, int imgRes, int duration){
        Snackbar snackbar = Snackbar.make(view, msg, duration);
        SnackbarLayout snackView = (SnackbarLayout) snackbar.getView();
        ImageView imgSnack = new ImageView(this);
        imgSnack.setImageResource(imgRes);
        imgSnack.setPadding(5,30,5,30);
        ((SnackbarContentLayout)snackView.getChildAt(0)).addView(imgSnack, 0);
        snackbar.show();
        return snackbar;
    }

    /**
     * 根据键名在xml文件读取相应值
     *
     * @param fileName
     * @param key
     * @param defaultVal 默认值
     * @return
     */
    public String getPrivateXml(String fileName, String key, String defaultVal) {
        try {
            SharedPreferences sp = getSharedPreferences(fileName, MODE_PRIVATE);
            return sp.getString(key, defaultVal);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 在xml写入键值对
     *
     * @param fileName
     * @param key
     * @param val
     * @return
     */
    public boolean setPrivateXml(String fileName, String key, String val) {
        try {
            SharedPreferences sp = getSharedPreferences(fileName, MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putString(key, val);
            e.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Dialog dialog;
    public void createDialog(String msg) {
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_base,
                    null);
            LinearLayout dialogLL = (LinearLayout) view;
            TextView tv_msg = (TextView) dialogLL.findViewById(R.id.tv_msg);
            tv_msg.setText(msg);
            dialog = new Dialog(this, R.style.dialog_mass);
            Window win = dialog.getWindow();// 获取所在window
            android.view.WindowManager.LayoutParams params = win.getAttributes();// 获取LayoutParams
            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            win.setAttributes(params);// 设置生效
            dialog.setContentView(dialogLL);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public class SDKReceiver extends BroadcastReceiver {
//        public void onReceive(Context context, Intent intent) {
//            String s = intent.getAction();
//
//            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
//                makeToast(context, "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置", R.mipmap.toast_alarm, Toast.LENGTH_SHORT);
//            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
//                makeToast(context, "key 验证成功! 功能可以正常使用", R.mipmap.toast_ok, Toast.LENGTH_SHORT);
//            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
//                makeToast(context, "网络出错", R.mipmap.toast_alarm, Toast.LENGTH_SHORT);
//            }
//        }
//    }

    public void showRationaleDialog(String messageResId, final PermissionRequest request) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //地图
//        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }


}
