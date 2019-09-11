package com.android.crypt.chatapp.finder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.user.Model.UserInfo;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.widget.RoundImageView;


import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_avatar)
    RoundImageView ivAvatar;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.account)
    TextView account;
    @BindView(R.id.apply_text)
    EditText applyText;
    @BindView(R.id.apply_text_bg)
    LinearLayout applyTextBg;
    @BindView(R.id.apply_send)
    LinearLayout applySend;
    @BindView(R.id.apply_event)
    LinearLayout applyEvent;

    private String token;
    private UserInfo mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_search_friend_result);
        setSupportActionBar(toolbar);

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
        applyEvent.setOnClickListener(this);
        applySend.setOnClickListener(this);


        Intent intent = getIntent();
        this.mMap = (UserInfo) intent.getSerializableExtra("friendInfo");
        userName.setText(this.mMap.username);
        account.setText(changePhoneNum(this.mMap.login_name));

        RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.default_head);
        Glide.with(this).load(RunningData.getInstance().echoMainPicUrl() + this.mMap.avatar_url).apply(requestOptions).into(ivAvatar);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.apply_event:
                if (this.mMap.login_name.equals(RunningData.getInstance().getCurrentAccount())){
                    makeSnake(applyText, "不能添加自己为好友", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                    return;
                }
                applyEvent.setVisibility(View.GONE);
                applyTextBg.setVisibility(View.VISIBLE);
                applySend.setVisibility(View.VISIBLE);

                break;
            case R.id.apply_send:
                startSendApply();
                break;

        }
    }


    private String changePhoneNum(String account) {
        if (account.length() != 11) {
            return "*****";
        } else {
            return account.substring(0, 3) + "****" + account.substring(7, account.length());
        }
    }


    private void startSendApply(){
        String app_info = applyText.getText().toString();
        if (app_info == null){
            app_info = "";
        }
        Logger.d("applyText = " + app_info);
        if (this.mMap.login_name.equals(RunningData.getInstance().getCurrentAccount())){
            makeSnake(applyText, "不能添加自己为好友", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }

        createDialog("正在发送...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/applyFriend")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("user_id", this.mMap.user_id)
                .params("apply_msg", app_info)

                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Logger.d("response.body()" + response.body());
                        if (response.body().code == 1){
                            makeSnake(applyText, response.body().msg, R.mipmap.toast_alarm
                                    , Snackbar.LENGTH_LONG);
                        }else if (response.body().code == -1){
                            RunningData.getInstance().reLogInMethod();
                        }else{
                            makeSnake(applyText, response.body().msg, R.mipmap.toast_alarm
                                    , Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
    }
}
