package com.android.crypt.chatapp.guide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegNextActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.reg_name)
    EditText regName;
    @BindView(R.id.reg_pwd)
    EditText regPwd;
    @BindView(R.id.sex_m)
    Button sexM;
    @BindView(R.id.sex_w)
    Button sexW;
    @BindView(R.id.sms_code)
    EditText smsCode;
    @BindView(R.id.send_sms)
    Button sendSms;
    @BindView(R.id.reg_start)
    Button regStart;

    private int totalTime = 30;
    private int curTime = totalTime;
    private boolean canSendSms = true;
    private boolean canStartFindpwd = true;

    private Timer sendSmsTimer = null;

    private String sexValue = "";
    private String mpub_key = "";
    private String mpri_key = "";
    private String account = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_next);

        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_reg);
        setSupportActionBar(toolbar);
        initView();
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
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initView() {
        Intent intent = getIntent();
        mpub_key = (String) intent.getSerializableExtra("pub_key");
        mpri_key = (String) intent.getSerializableExtra("pri_key");
        account = (String) intent.getSerializableExtra("account");

        sexM.setOnClickListener(this);
        sexW.setOnClickListener(this);
        sendSms.setOnClickListener(this);
        regStart.setOnClickListener(this);

        if(mpub_key == null || mpri_key == null || account == null || mpub_key.equals("") || mpri_key.equals("")|| account.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("数据传递错误").setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {finish();}
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_sms:
                sendSmsMethod();
                break;
            case R.id.sex_m:
                sexValue = "M";
                sexM.setBackground(getResources().getDrawable(R.drawable.btn_login_shape));
                sexW.setBackground(getResources().getDrawable(R.drawable.rig_tips_text_view));
                break;
            case R.id.sex_w:
                sexValue = "W";
                sexW.setBackground(getResources().getDrawable(R.drawable.btn_login_shape));
                sexM.setBackground(getResources().getDrawable(R.drawable.rig_tips_text_view));
                break;
            case R.id.reg_start:
                startReg();
                break;
        }
    }

    //****按钮定时器
    private void sendSmsMethod(){
        if (canSendSms == true){
            canSendSms = false;

            //发送验证码
            startSendSms();

            sendSms.setAlpha((float)0.2);
            sendSms.setText(curTime + "s");
            sendSmsTimer = new Timer();
            sendSmsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    curTime--;
                    changeBtnTips();
                }
            },0,1000);
        }
    }

    private void changeBtnTips(){
        if (curTime <= 0){
            sendSmsTimer.cancel();
            sendSmsTimer = null;
            canSendSms = true;
            curTime = totalTime;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendSms.setAlpha((float)1);
                    sendSms.setText("发送");
                }
            });

        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendSms.setText( curTime + "s");
                }
            });
        }
    }

    //发送请求
    private void startSendSms(){
        if (account == null || account.length() != 11){
            makeSnake(sendSms, "手机号不对", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }
        createDialog("发送验证码...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/checkAccount").tag(this).cacheMode(CacheMode.NO_CACHE).params("account", account).params("accountType", 1)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {dialog.dismiss();}
                        Logger.d("response.body()" + response.body());
                        makeSnake(sendSms, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {if (dialog != null) {dialog.dismiss();}makeSnake(sendSms, "网络错误", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);}
                });
    }

    private void startReg(){
        String username = regName.getText().toString();
        String smscode = smsCode.getText().toString();
        String pwd = regPwd.getText().toString();

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else locale = Locale.getDefault();
        String language = locale.getLanguage() + "-" + locale.getCountry();

        if(username == null || username.equals("")){
            makeSnake(sendSms, "请输入用户名", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }
        if(smscode == null || smscode.equals("")){
            makeSnake(sendSms, "请输入验证码", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }

        if (pwd == null || pwd.length() < 6){
            makeSnake(sendSms, "密码过短", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }

        createDialog("正在注册...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/registerAccount").tag(this).cacheMode(CacheMode.NO_CACHE)
                .params("name", username)
                .params("password", pwd)
                .params("registerCode", 1)
                .params("account", account)
                .params("sex", sexValue)
                .params("publicKey", mpub_key)
                .params("languageSettings", language)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {dialog.dismiss();}
                        Logger.d("response.body()" + response.body());
                        if(response.body().code == 1){
                            doSthAfterReg();
                        }else{
                            makeSnake(sendSms, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {if (dialog != null) {dialog.dismiss();}makeSnake(sendSms, "网络错误", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);}
                });
    }

    private void doSthAfterReg(){
        CacheTool.getInstance().cacheObjectWithKey(ObjectCacheType.pri_key, mpri_key, account);
        finish();
    }


}
