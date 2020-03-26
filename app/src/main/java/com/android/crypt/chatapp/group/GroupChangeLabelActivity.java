package com.android.crypt.chatapp.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.group.model.GroupChatMsg;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupChangeLabelActivity extends BaseActivity {

    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_value)
    EditText inputValue;
    @BindView(R.id.finish)
    Button finish;

    private String token;
    private ContactModel mMap;
    private boolean okGoFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_my_info);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_groups_label);
        setSupportActionBar(toolbar);

        token = RunningData.getInstance().getToken();
        initData();
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

    private void initData() {
        Intent intent = getIntent();
        this.mMap = (ContactModel) intent.getSerializableExtra("groupInfo");

        inputValue.setHint("输入群备注名");
        finish.setText("修改群内备注");
    }
    @OnClick(R.id.finish)
    public void onViewClicked(View view) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.finish:
                changeLabel();
                break;
        }
    }


    public void changeLabel() {
        final String textVaule = inputValue.getText().toString();
        if (textVaule.equals("")) {
            return;
        }
        if (okGoFlag == true) {
            return;
        }
        okGoFlag = true;
        createDialog("修改中...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "group/v2/setMemberLabel")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("groupId", this.mMap.account)
                .params("label", textVaule)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        okGoFlag = false;
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (response.body().code == 1) {
                            process(textVaule);
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

    private void process(final String label) {
        GroupChatMsg cur_model = RunningData.getInstance().getGroupChatMsg(mMap.account);
        if (cur_model == null) {
            cur_model = new GroupChatMsg();
        }
        cur_model.groupLabel = label;
        RunningData.getInstance().cacheGroupChatMsg(mMap.account, cur_model);

        Intent resendIntent = new Intent();
        resendIntent.putExtra("label", label);
        setResult(100, resendIntent);
        finish();
    }


}
