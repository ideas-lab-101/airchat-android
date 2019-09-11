package com.android.crypt.chatapp.finder;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.user.Model.UserInfo;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFriendActivity extends BaseActivity implements View.OnClickListener{
    @BindView(R.id.input_account)
    EditText inputAccount;
    @BindView(R.id.start_search)
    Button startSearch;
    @BindView(R.id.my_apply_list)
    Button myApplyList;
    private String token;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private boolean okGoFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_search_friend);
        setSupportActionBar(toolbar);

        token = RunningData.getInstance().getToken();
        okGoFlag = false;
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
        startSearch.setOnClickListener(this);
        myApplyList.setOnClickListener(this);

        Intent intent = getIntent();
        String account = (String) intent.getSerializableExtra("account");
        Logger.d("account = " + account);
        if (account != null && !account.equals("")){
            startResearch(account);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_apply_list:
                Intent intent = new Intent(this, ApplyListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
                break;
            case R.id.start_search:
                getInputValue();
                break;

        }
    }
    private void getInputValue(){
        if (inputAccount.getText().toString().equals("")){
            return;
        }
        String textVaule = inputAccount.getText().toString();
        startResearch(textVaule);
    }

    private void startResearch(String textVaule){
        if (okGoFlag == true){
            return;
        }
        okGoFlag = true;
        createDialog("正在查找...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/searchUser")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("key", textVaule)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        okGoFlag = false;
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Logger.d("response.body()" + response.body());
                        if (response.body().code == 1){
                            if (response.body().list.size() > 0){
                                Gson gson = new Gson();
                                String valueString = gson.toJson(response.body().list.get(0));
                                final UserInfo info = gson.fromJson(valueString, UserInfo.class);
                                showFriendCard(info);
                            }else{
                                makeSnake(myApplyList, "没有结果", R.mipmap.toast_alarm
                                        , Snackbar.LENGTH_LONG);
                            }

                        }else if (response.body().code == -1){
                            RunningData.getInstance().reLogInMethod();
                        }else{
                            makeSnake(myApplyList, response.body().msg, R.mipmap.toast_alarm
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

    private void showFriendCard(UserInfo friendInfo){
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra("friendInfo", friendInfo);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }
}
