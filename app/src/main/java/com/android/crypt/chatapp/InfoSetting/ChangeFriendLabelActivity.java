package com.android.crypt.chatapp.InfoSetting;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangeFriendLabelActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_value)
    EditText inputValue;
    @BindView(R.id.finish)
    Button finish;

    private String token;
    private boolean okGoFlag = false;
    private ContactModel mMap;


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
        this.mMap = (ContactModel) intent.getSerializableExtra("friendInfo");
        if (this.mMap != null) {
            //根据类型判断
            toolbar.setTitle(R.string.title_bar_change_label);
            setSupportActionBar(toolbar);
            inputValue.setHint("输入备注名");
            finish.setText("修改备注");
        } else {
            finish();
            overridePendingTransition(R.anim.in_from_left,
                    R.anim.out_to_right);
        }
    }


    @OnClick(R.id.finish)
    public void onViewClicked() {
        String textVaule = inputValue.getText().toString();

        if (textVaule.equals("")){
            return;
        }
        if (okGoFlag == true){
            return;
        }
        okGoFlag = true;
        createDialog("修改中...");

        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/setFriendLabel")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("user_id", this.mMap.friend_id)
                .params("label", textVaule)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        okGoFlag = false;
                        if (dialog != null) {
                            dialog.dismiss();
                        }

                        Logger.d("response.body()" + response.body());
                        if (response.body().code == 1) {
                            process();
                        } else {
                            makeSnake(toolbar, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
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
        finish();
        overridePendingTransition(R.anim.in_from_left,  R.anim.out_to_right);
    }

}
