package com.android.crypt.chatapp.guide;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.MainActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.ParameterUtil;
import com.android.crypt.chatapp.utility.Common.ServiceUtils;

import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


/**
 * @author mula
 */
@RuntimePermissions
public class WelcomeActivity extends BaseActivity {

    private boolean blnFirstOpen = false;
    private static WelcomeActivity context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        ButterKnife.bind(this);
        setSwipeBackEnable(false);

        initView();
        WelcomeActivityPermissionsDispatcher.initAppWithPermissionCheck(this);
    }

    public void initView() {
        context = this;
        //根据AppCode更改欢迎页面背景
//        rootView.setBackgroundResource(R.mipmap.default_head);
//        tvVersion.setText(getString(R.string.version_set, ServiceUtils.getCurVersionName(this)));
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})
    public void initApp() {
        //判断是否首次登入
        String loginState = getPrivateXml(
                ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "loginState", "0");// 0未登录,1已登录

        Intent intent = null;

        blnFirstOpen = "1".equals(getPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "blnFirstOpen_" + ServiceUtils.getCurVersionName(getApplication()), "1")) ? true : false;
        if (blnFirstOpen) {
            intent = new Intent(WelcomeActivity.this, LogOrRegActivity.class);
        } else {
            intent = "0".equals(loginState) ? new Intent(WelcomeActivity.this, LogOrRegActivity.class) :
                    new Intent(WelcomeActivity.this, MainActivity.class);
        }

        startActivity(intent);
        // 设置切换动画，从右边进入，左边退出
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        WelcomeActivity.this.finish();
        //写入使用状态, 1:首次打开应用; 0:非首次
        setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "blnFirstOpen_" + ServiceUtils.getCurVersionName(getApplication()), "0");
    }


    @OnShowRationale({ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})
    void showRationaleForApp(PermissionRequest request) {
        showRationaleDialog("AirChat需要的基础权限", request);
    }

    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})
    void showPermissionDenied() {
        makeToast(this, "基础权限将无法使用哦", R.mipmap.toast_alarm, Toast.LENGTH_SHORT);
    }

    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})
    void showNeverAskAgain() {
        makeToast(this, "请至设置中打开相关权限", R.mipmap.toast_alarm, Toast.LENGTH_SHORT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        WelcomeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public static WelcomeActivity getLauncher() {
        return context;
    }


}
