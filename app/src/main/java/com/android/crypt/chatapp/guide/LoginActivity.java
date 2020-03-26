package com.android.crypt.chatapp.guide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.InfoSetting.LoadPrivateKeyActivity;
import com.android.crypt.chatapp.InfoSetting.ReCalKeysActivity;
import com.android.crypt.chatapp.MainActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.ParameterUtil;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Common.ServiceUtils;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends BaseActivity {


    @BindView(R.id.txt_account)
    EditText txtAccount;
    @BindView(R.id.txt_password)
    EditText txtPassword;


    @BindView(R.id.tv_login)
    Button tvLogin;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @BindView(R.id.tv_findPwd)
    Button tvFindPwd;
    @BindView(R.id.big_title)
    TextView bigTitle;


    private String accountStr = "", pwdStr = "";
    private int directLogin = 0; //是否直接登陆

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_login_activity);
        ButterKnife.bind(this);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        accountStr = RunningData.getInstance().getCurrentAccount();
        pwdStr = RunningData.getInstance().getCurrentPwd();

        initView();
        directLogin = getIntent().getIntExtra("directLogin", 0);
        if (directLogin == 1) {
            accountLogin();
        }
        String curRegNumber = RunningData.getInstance().getCurRegNumber();
        if (curRegNumber != null && !curRegNumber.equals("")){
            bigTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            bigTitle.setText("新注册Air号: " + curRegNumber);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理ActionBar的菜单
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 绑定事件
     */
    private void initView() {

        txtAccount.setText(accountStr);
        txtPassword.setText(pwdStr);
        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

    }

    @OnClick({R.id.tv_login, R.id.tv_findPwd})
    public void onViewClicked(View view) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.tv_login:
                attemptLogin();
                break;
            case R.id.tv_findPwd:
                toFindPwdAc();
                break;
            default:
                break;
        }
    }

    private void toFindPwdAc() {
        Intent intent = new Intent(this, RefindPwordActivity.class);
        startActivity(intent);
    }


    private void attemptLogin() {
        // Reset errors.
        txtAccount.setError(null);
        txtPassword.setError(null);

        // Store values at the time of the login attempt.
        accountStr = txtAccount.getText().toString();
        pwdStr = txtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(pwdStr)) {
            txtPassword.setError(getString(R.string.error_invalid_password));
            focusView = txtPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(accountStr)) {
            txtAccount.setError(getString(R.string.error_account_required));
            focusView = txtAccount;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            createDialog("登录系统,请稍后...");
            accountLogin();
        }
    }

    private void accountLogin() {
        JSONObject deviceInfo = new JSONObject();
        try {
            String deviceToken = RunningData.getInstance().getPushAlias();

            deviceInfo.put("uniqueId", ServiceUtils.getUniqueID());
            deviceInfo.put("deviceToken", deviceToken);
            deviceInfo.put("osType", "Android");
            deviceInfo.put("osVersion", Build.VERSION.RELEASE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String baseurl = RunningData.getInstance().server_url();
        OkGo.<CodeResponse>post(baseurl + "system/v2/userLogin")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("account", accountStr)
                .params("password", pwdStr)
                .params("deviceInfo", deviceInfo.toString())
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onFinish() {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (response.body().code == 1) {
                            try {
                                RunningData.getInstance().setCurrentAccount(accountStr);
                                RunningData.getInstance().setCurrentPwd(pwdStr);

                                JSONObject data = Convert.formatToJson(response.body().data);
                                String token = data.getString("token");
                                CacheTool.getInstance().cacheObject(ObjectCacheType.cur_token, token);
                                CacheTool.getInstance().cacheObject(ObjectCacheType.cur_account, accountStr);

                                CryTool tool = new CryTool();
                                String pwd_en = tool.aesEnWith(pwdStr, RunningData.getInstance().getInnerAESKey());
                                CacheTool.getInstance().cacheObject(ObjectCacheType.cur_pwd, pwd_en);

                                //***缓存user info
                                String userInfoString = data.getJSONObject("userInfo").toString();
                                CacheTool.getInstance().cacheObject(ObjectCacheType.user_info, userInfoString);
                                whetherCanLogIn();

                                Logger.d("userInfoString - " + userInfoString);
                            } catch (Exception ex) {

                            }
                        } else {
                            makeSnake(tvLogin, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        response.getException().printStackTrace();
                    }
                });

    }

    private void whetherCanLogIn() {
        String pri_key = RunningData.getInstance().getMyRSAPriKeyWith(accountStr);
        Logger.d("获取的私钥是 =" + pri_key + "---" + pri_key.equals("") + "--");
        if (pri_key.equals("")) {
            showAlert();
        } else {
            setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "loginState", "1");// 0未登录,1已登录
            //导向到主页面
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            // 设置切换动画，从右边进入，左边退出
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }

    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("没有解密私钥")
                .setMessage("没有私钥你无法解密消息")
                .setPositiveButton("导入历史私钥", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(LoginActivity.this, LoadPrivateKeyActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("重新计算密钥", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(LoginActivity.this, ReCalKeysActivity.class);
                        intent.putExtra("is_reg", false);
                        startActivity(intent);
                    }
                });
        builder.create().show();
    }

//    private void setupUI() {
//        //更改字体
//        Typeface customFont = Typeface.createFromAsset(this.getAssets(), "fonts/Days.otf");
//        if (customFont != null) {
//            logTitle.setTypeface(customFont);
//        }
//    }

//    private void othersOp() {
//        ActionSheet.createBuilder(this, getSupportFragmentManager())
//                .setCancelButtonTitle("取消")
//                .setOtherButtonTitles("新用户注册", "导入历史私钥")
//                .setCancelableOnTouchOutside(true)
//                .setListener(this).show();
//    }

//    @Override
//    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
//        if (index == 0) {
//            Intent intent = new Intent(this, RegTipsActivity.class);
//            startActivity(intent);
//        } else if (index == 1) {
//            Intent intent = new Intent(this, LoadPrivateKeyActivity.class);
//            startActivity(intent);
//        }
//    }

//    @Override
//    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {
//
//
//    }

}
