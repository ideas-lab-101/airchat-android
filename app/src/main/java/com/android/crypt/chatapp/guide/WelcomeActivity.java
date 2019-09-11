package com.android.crypt.chatapp.guide;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.MainActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.guide.WelcomeActivityPermissionsDispatcher;
import com.android.crypt.chatapp.utility.Common.ParameterUtil;
import com.android.crypt.chatapp.utility.Common.ServiceUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

//import com.android.amusement.user.LoginActivity;

/**
 * @author mula
 */
@RuntimePermissions
public class WelcomeActivity extends BaseActivity {

    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.root_view)
    FrameLayout rootView;
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
        rootView.setBackgroundResource(R.mipmap.welcome);
        tvVersion.setText(getString(R.string.version_set, ServiceUtils.getCurVersionName(this)));
    }

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    public void initApp() {
        //判断是否首次登入
        String loginState = getPrivateXml(
                ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "loginState", "0");// 0未登录,1已登录

        Intent intent = null;

        blnFirstOpen = "1".equals(getPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "blnFirstOpen_" + ServiceUtils.getCurVersionName(getApplication()), "1")) ? true : false;

        if (blnFirstOpen) {
            intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        } else {
            intent = "0".equals(loginState) ? new Intent(WelcomeActivity.this, LoginActivity.class) :
                    new Intent(WelcomeActivity.this, MainActivity.class);
        }

        startActivity(intent);
        // 设置切换动画，从右边进入，左边退出
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        WelcomeActivity.this.finish();
        //写入使用状态, 1:首次打开应用; 0:非首次
        setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "blnFirstOpen_" + ServiceUtils.getCurVersionName(getApplication()), "0");
    }


    @OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    void showRationaleForApp(PermissionRequest request) {
        showRationaleDialog("此APP必要的权相关权限", request);
    }

    @OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    void showPermissionDenied() {
        makeToast(this, "拒绝权限将无法进行APP", R.mipmap.toast_alarm, Toast.LENGTH_SHORT);
    }

    @OnNeverAskAgain({Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO})
    void showNeverAskAgain() {
        makeToast(this, "已选择不再询问，请至设置中打开APP权限", R.mipmap.toast_alarm, Toast.LENGTH_SHORT);
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
