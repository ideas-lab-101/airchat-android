package com.android.crypt.chatapp.InfoSetting;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.user.Model.UserInfo;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangeMyInfoActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_value)
    EditText inputValue;
    @BindView(R.id.finish)
    Button finish;

    private String token;
    private String ac_tips;
    private boolean okGoFlag = false;
    private String subKey = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_my_info);
        ButterKnife.bind(this);

        token = RunningData.getInstance().getToken();
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
                overridePendingTransition(R.anim.in_from_left,
                        R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        Intent intent = getIntent();//获取相关的intent
        String change_kind = intent.getStringExtra("change_kind");
        if (change_kind.equalsIgnoreCase("username")) {
            //根据类型判断
            toolbar.setTitle(R.string.title_bar_change_name);
            setSupportActionBar(toolbar);
            inputValue.setHint("请输入姓名");
            finish.setText("修改姓名");
            subKey = "username";
        } else if (change_kind.equalsIgnoreCase("introduction")) {
            //根据类型判断
            toolbar.setTitle(R.string.title_bar_change_intro);
            setSupportActionBar(toolbar);
            inputValue.setHint("请输入个人简介");
            finish.setText("修改个人简介");
            subKey = "introduction";
        }
//        else if (change_kind.equalsIgnoreCase("friend_label")) {
//            //根据类型判断
//            toolbar.setTitle(R.string.title_bar_change_label);
//            setSupportActionBar(toolbar);
//            inputValue.setHint("输入备注名");
//            finish.setText("修改备注");
//            subKey = "introduction";
//            subUrl = "contact/setFriendLabel";
//        }
        else {
            finish();
            overridePendingTransition(R.anim.in_from_left,
                    R.anim.out_to_right);
        }
    }


    @OnClick(R.id.finish)
    public void onViewClicked() {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        String textVaule = inputValue.getText().toString();

        if (subKey.equals("") || textVaule.equals("")){
            return;
        }
        if (okGoFlag == true){
            return;
        }

        okGoFlag = true;
        createDialog("修改中...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "user/v2/updateUserInfo")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params(subKey, textVaule)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onFinish() {
                        okGoFlag = false;
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    @Override
                    public void onCacheSuccess(Response<CodeResponse> response) {
                        onSuccess(response);
                    }

                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        okGoFlag = false;
                        if (dialog != null) {
                            dialog.dismiss();
                        }
//                        Logger.d("response.body()" + response.body());
                        if (response.body().code == 1) {
                            process();
                        } else {
                            makeSnake(toolbar, response.body().msg, R.mipmap.toast_alarm
                                    , Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                        okGoFlag = false;
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    private void process(){
        Gson gson = new Gson();
        String userInfoString = CacheTool.getInstance().getCacheObject(ObjectCacheType.user_info);
        if (userInfoString != "") {
            try {
                UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class);
                if (userInfo != null){
                    if (subKey.equals("introduction")){
                        userInfo.introduction = inputValue.getText().toString();
                    }else if(subKey.equals("username")){
                        userInfo.username = inputValue.getText().toString();
                    }

                    String userInfoNew = gson.toJson(userInfo);
                    CacheTool.getInstance().cacheObject(ObjectCacheType.user_info, userInfoNew);
                }

            }catch (Exception e){}
        }

        finish();
        overridePendingTransition(R.anim.in_from_left,  R.anim.out_to_right);
    }

}
