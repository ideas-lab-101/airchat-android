package com.android.crypt.chatapp.guide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.RunningData;
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

public class RegInputAccountActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.send_sms)
    Button sendSms;

    private String mpub_key = "";
    private String mpri_key = "";
    private String regCode = "";
    private String regToken = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_input_account);

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

    private void initView(){
        Intent intent = getIntent();
        mpub_key = (String) intent.getSerializableExtra("pub_key");
        mpri_key = (String) intent.getSerializableExtra("pri_key");

        sendSms.setOnClickListener(this);
        if(mpub_key == null || mpri_key == null || mpub_key.equals("") || mpri_key.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("数据传递错误").setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {finish();}
                    });
        }
    }

    private void toNextPage(){
        //导向到主页面
        Intent intent = new Intent(this, RegNextActivity.class);
        intent.putExtra("mpub_key", mpub_key);
        intent.putExtra("mpri_key", mpri_key);
        intent.putExtra("regCode", regCode);
        intent.putExtra("regToken", regToken);

        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void onClick(View v) {
        startCheckAccount();
    }

    private void startCheckAccount(){
        createDialog("正在为你申请注册");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/v2/getRegCode")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (response.body().code == 1){
                            try {
                                JSONObject data = Convert.formatToJson(response.body().data);
                                regCode = data.getString("regCode");
                                regToken = data.getString("regToken");

                                toNextPage();
                            } catch (Exception ex) {
                                makeSnake(sendSms, "数据错误", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                            }
                        }else{
                            makeSnake(sendSms, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        makeSnake(sendSms, "网络错误", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                    }
                });
    }


    //******注册返回
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 100) {
            String ac_id = data.getStringExtra("ac_id");
            Intent resultMethod = new Intent();
            resultMethod.putExtra("ac_id", ac_id);
            setResult(100, resultMethod);
            finish();
        }
    }

}
