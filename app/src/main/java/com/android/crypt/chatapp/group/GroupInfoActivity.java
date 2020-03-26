package com.android.crypt.chatapp.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.group.model.GroupChatMsg;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.baoyz.widget.PullRefreshLayout;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupInfoActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;

    @BindView(R.id.group_name)
    TextView groupName;
    @BindView(R.id.group_intro)
    TextView groupIntro;
    @BindView(R.id.fresh_view)
    PullRefreshLayout freshView;
    @BindView(R.id.invite)
    LinearLayout invite;
    @BindView(R.id.not_disturb)
    LinearLayout notDisturb;
    @BindView(R.id.change_label)
    LinearLayout changeLabel;
    @BindView(R.id.report_group)
    LinearLayout reportGroup;
    @BindView(R.id.delete_group_msg)
    LinearLayout deleteGroupMsg;
    @BindView(R.id.quit_group)
    LinearLayout quitGroup;

    private ContactModel mMap;
    private String token;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_groups_info);
        setSupportActionBar(toolbar);

        token = RunningData.getInstance().getToken();
        addListener();
        initData();
        getFresh();
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

    private void addListener() {
        freshView.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFresh();
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        this.mMap = (ContactModel) intent.getSerializableExtra("friendInfo");
        if (this.mMap != null) {
            groupName.setText(this.mMap.username);

            GroupChatMsg cur_model = RunningData.getInstance().getGroupChatMsg(mMap.account);
            groupIntro.setText(cur_model.groupIntro);
        }
    }

    private void getFresh() {
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "group/v2/getGroupInfo")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("groupId", mMap.account)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        freshView.setRefreshing(false);
                        if (response.body().code == 1) {
                            try {
                                JSONObject data = Convert.formatToJson(response.body().data);
                                freshData(data);
                            } catch (Exception ex) {
                            }
                        } else if (response.body().code == -1) {
                            RunningData.getInstance().reLogInMethod();
                        } else {
                            makeSnake(freshView, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                        freshView.setRefreshing(false);
                    }
                });
    }

    private void freshData(JSONObject data) {
        try {
            JSONObject groupInfo = data.getJSONObject("groupInfo");
            JSONObject memberInfo = data.getJSONObject("memberInfo");

            String groupNameStr = groupInfo.getString("groupName");
            String groupIntroStr = groupInfo.getString("groupIntro");
            String groupLabelStr = memberInfo.getString("groupLabel");
            String creator = groupInfo.getString("account");
            String groupIconStr = groupInfo.getString("groupIcon");

            boolean iaAdmin = false;
            String account = RunningData.getInstance().getCurrentAccount();
            if (account.equalsIgnoreCase(creator)) {
                iaAdmin = true;
            }
            groupName.setText(groupNameStr);
            groupIntro.setText(groupIntroStr);
            cacheGroupRunningData(groupNameStr, groupIntroStr, groupLabelStr, groupIconStr, iaAdmin);
        } catch (Exception ex) {
            makeSnake(freshView, "数据出错", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
        }

    }


    private void cacheGroupRunningData(String groupNameStr, String groupIntroStr, String groupLabelStr, String groupIconStr, boolean isAdmin) {
        GroupChatMsg cur_model = RunningData.getInstance().getGroupChatMsg(mMap.account);
        if (cur_model == null) {
            cur_model = new GroupChatMsg();
        }
        cur_model.groupLabel = groupLabelStr;
        cur_model.groupName = groupNameStr;
        cur_model.groupIcon = groupIconStr;
        cur_model.groupIntro = groupIntroStr;
        cur_model.isAdmin = isAdmin;

        RunningData.getInstance().cacheGroupChatMsg(mMap.account, cur_model);
    }


    @OnClick({R.id.invite, R.id.not_disturb, R.id.change_label, R.id.report_group, R.id.delete_group_msg, R.id.quit_group})
    public void onViewClicked(View view) {
        if (!ClickUtils.isFastClick()) {
            return;
        }

        switch (view.getId()) {
            case R.id.invite:
                alertNewFunc();
                break;
            case R.id.not_disturb:
                alertNewFunc();
                break;
            case R.id.change_label:
                toSettingPage();
                break;
            case R.id.report_group:
                reportThisGroup();
                break;
            case R.id.delete_group_msg:
                break;
            case R.id.quit_group:
                alertNewFunc();
                break;
        }
    }


    private void alertNewFunc(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("此功能敬请期待")
                .setMessage("开发者忙于考试，更新慢，敬请期待")
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(final DialogInterface dialog) {
                    }
                });
        builder.create().show();
    }

    private void reportThisGroup(){
        makeSnake(freshView, "举报成功", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
    }

    private void toSettingPage() {
        Intent intent = new Intent(this, GroupChangeLabelActivity.class);
        intent.putExtra("groupInfo", this.mMap);
        startActivityForResult(intent, 100);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 100) {
            String label = data.getStringExtra("label");
            if(label != null){
                Intent resendIntent = new Intent();
                resendIntent.putExtra("label", label);
                setResult(5, resendIntent);
                finish();
            }
        }
    }
}
