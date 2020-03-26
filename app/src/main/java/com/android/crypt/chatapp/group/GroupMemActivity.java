package com.android.crypt.chatapp.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.group.adaptor.GroupMemAdaptor;
import com.android.crypt.chatapp.group.model.GroupMember;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.android.crypt.chatapp.widget.swipexlistview.XListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupMemActivity extends BaseActivity  implements XListView.IXListViewListener, AdapterView.OnItemClickListener{


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.group_mem_list)
    XListView groupMemList;

    private String token;
    private ContactModel mMap;
    private ArrayList<GroupMember> groupMembersArr;
    private GroupMemAdaptor adapterFavor;
    private boolean isWebSuccess = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_mem);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_groups_mem);
        setSupportActionBar(toolbar);

        token = RunningData.getInstance().getToken();
        addListener();
        initData();
        onRefresh();
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

    private void addListener() {
        groupMemList.setXListViewListener(this);
        groupMemList.setPullRefreshEnable(true);
        groupMemList.setPullLoadEnable(false);
        groupMemList.setOnItemClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        this.mMap = (ContactModel) intent.getSerializableExtra("friendInfo");

        groupMembersArr = new ArrayList();
        adapterFavor = new GroupMemAdaptor(this, groupMembersArr);
        groupMemList.setAdapter(adapterFavor);
    }

    @Override
    public void onRefresh() {
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "group/v2/getGroupMemberList")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("groupId", mMap.account)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        groupMemList.stopRefresh();
                        isWebSuccess = true;
                        if (response.body().code == 1) {
                            try {
                                JSONArray mList = Convert.formatToJson(response.body().data).getJSONArray("memberList");
                                if (mList.length() > 0){
                                    initView(mList);
                                }else{
                                    makeSnake(groupMemList, "没有结果", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                                }

                            } catch (Exception ex) {
                                makeSnake(groupMemList, "出错", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                            }
                        } else if (response.body().code == -1) {
                            RunningData.getInstance().reLogInMethod();
                        } else {
                            makeSnake(groupMemList, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if(isWebSuccess == false){
                            Gson gson = new Gson();
                            String cacheString = CacheTool.getInstance().getCacheObject(ObjectCacheType.group_members);
                            ArrayList<GroupMember> cacheList = gson.fromJson(cacheString, new TypeToken<ArrayList<GroupMember>>() {
                            }.getType());
                            groupMembersArr.clear();
                            if (cacheList != null && cacheList.size() >= 0){
                                int total_number = cacheList.size();
                                for (int i = 0; i < total_number; i++){
                                    groupMembersArr.add(cacheList.get(i));
                                }
                                freshListMsthod();
                            }
                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                        groupMemList.stopRefresh();
                    }
                });

    }

    @Override
    public void onLoadMore() {
        groupMemList.stopLoadMore();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        //查看资料








    }

    private void initView(JSONArray list) {
        Gson gson = new Gson();
        groupMembersArr.clear();
        ArrayList<GroupMember> tempArr = new ArrayList();
        try {
            for (int i = 0; i < list.length(); i++) {
                String valueString = list.getJSONObject(i).toString();//gson.toJson(list.get(i));
                GroupMember groupMember = gson.fromJson(valueString, GroupMember.class);
                groupMembersArr.add(groupMember);
            }
            freshListMsthod();
            if (tempArr.size() > 0) {
                String contactsString = gson.toJson(groupMembersArr);
                CacheTool.getInstance().cacheObject(ObjectCacheType.group_members, contactsString);
            }
        } catch (Exception ex) {
            makeSnake(groupMemList, "搜索出错", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
        }
    }

    private void freshListMsthod() {
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapterFavor.notifyDataSetChanged();
            }
        });
    }


}
