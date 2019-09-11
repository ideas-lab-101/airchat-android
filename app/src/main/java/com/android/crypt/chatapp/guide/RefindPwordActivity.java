package com.android.crypt.chatapp.guide;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RefindPwordActivity extends BaseActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.finda_account)
    EditText findaAccount;
    @BindView(R.id.send_sms)
    Button sendSms;
    @BindView(R.id.sms_code)
    EditText smsCode;
    @BindView(R.id.new_pword)
    EditText newPword;
    @BindView(R.id.reset_pword)
    Button resetPword;

    private int totalTime = 30;
    private int curTime = totalTime;
    private boolean canSendSms = true;
    private boolean canStartFindpwd = true;

    private Timer sendSmsTimer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_refind_pword);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.title_bar_find_pwd);
        setSupportActionBar(toolbar);

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

    @OnClick({R.id.send_sms, R.id.reset_pword})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.send_sms:
                sendSmsMethod();
                break;
            case R.id.reset_pword:
                startChangePwd();
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
        String account = findaAccount.getText().toString();
        if (account == null || account.length() != 11){
            makeSnake(resetPword, "请输入正确的手机号", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }

        createDialog("正在发送验证码...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/retrievePassword")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("account", account)
                .params("accountType", 1)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Logger.d("response.body()" + response.body());
                        makeSnake(resetPword, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        makeSnake(resetPword, "网络错误", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                    }
                });
    }

    private void startChangePwd(){
        String account = findaAccount.getText().toString();
        String sms = smsCode.getText().toString();
        String pwd = newPword.getText().toString();
        if (account == null || account.length() != 11){
            makeSnake(resetPword, "请输入正确的手机号", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }
        if (sms == null || sms.length() < 4){
            makeSnake(resetPword, "请输入验证码", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }
        if (pwd == null || pwd.length() < 6){
            makeSnake(resetPword, "密码过短", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }

        if (canStartFindpwd == true){
            canStartFindpwd = false;

            createDialog("正在重置密码...");
            OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/changePassword")
                    .tag(this)
                    .cacheMode(CacheMode.NO_CACHE)
                    .params("account", account)
                    .params("verifyCode", sms)
                    .params("password", pwd)

                    .execute(new JsonCallback<CodeResponse>() {
                        @Override
                        public void onSuccess(Response<CodeResponse> response) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            makeSnake(resetPword, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                            if (response.body().code == 1){
                                finish();
                            }else{
                                Logger.d("response.body()" + response.body());
                            }
                        }
                        @Override
                        public void onError(Response<CodeResponse> response) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            makeSnake(resetPword, "网络错误", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    });


        }

    }

}
