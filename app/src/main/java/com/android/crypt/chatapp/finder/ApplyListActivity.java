package com.android.crypt.chatapp.finder;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.finder.adaptor.Model.ApplyModel;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.android.crypt.chatapp.widget.swipexlistview.XListView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.finder.adaptor.ApplyListAdapter;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ApplyListActivity extends BaseActivity implements XListView.IXListViewListener, AdapterView.OnItemClickListener, View.OnClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.apply_list)
    XListView applyList;
    @BindView(R.id.iv_avatar)
    RoundImageView ivAvatar;
    @BindView(R.id.cancel_apply)
    Button cancelApply;
    @BindView(R.id.confirm_apply)
    Button confirmApply;
    @BindView(R.id.bg_choose)
    RelativeLayout bgChoose;
    private String token;
    private ApplyListAdapter adapterFavor;
    private ArrayList<ApplyModel> applyArr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_list);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_apply_friend);
        setSupportActionBar(toolbar);

        token = RunningData.getInstance().getToken();
        addListener();
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
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addListener() {
        bgChoose.setOnClickListener(this);
        cancelApply.setOnClickListener(this);
        confirmApply.setOnClickListener(this);

        applyList.setXListViewListener(this);
        applyList.setPullRefreshEnable(true);
        applyList.setPullLoadEnable(false);
        applyList.setOnItemClickListener(this);

        applyArr = new ArrayList();
        adapterFavor = new ApplyListAdapter(this, applyArr);
        applyList.setAdapter(adapterFavor);
        freshListMsthod();
    }

    @Override
    public void onRefresh() {
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/v2/getFriendApplyList")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        applyList.stopRefresh();
                        if (response.body().code == 1) {
                            try {
                                JSONArray mList = Convert.formatToJson(response.body().data).getJSONArray("list");
                                if (mList.length() > 0){
                                    initView(mList);
                                }else{
                                    makeSnake(applyList, "没有结果", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                                }

                            } catch (Exception ex) {
                                makeSnake(applyList, "搜索出错", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                            }
                        } else if (response.body().code == -1) {
                            RunningData.getInstance().reLogInMethod();
                        } else {
                            makeSnake(applyList, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                        applyList.stopRefresh();
                    }
                });

    }

    @Override
    public void onLoadMore() {
        applyList.stopLoadMore();
    }

    private void initView(JSONArray list) {
        Gson gson = new Gson();
        ArrayList<ApplyModel> tempArr = new ArrayList();
        try {
            for (int i = 0; i < list.length(); i++) {
                String valueString = list.getJSONObject(i).toString();//gson.toJson(list.get(i));
                ApplyModel applyModel = gson.fromJson(valueString, ApplyModel.class);
                tempArr.add(applyModel);
            }
            if (tempArr.size() > 0) {
                applyArr.clear();
                applyArr.addAll(tempArr);
                freshListMsthod();
            }
        } catch (Exception ex) {
            makeSnake(applyList, "搜索出错", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
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

    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.bg_choose:
                bgChoose.setVisibility(View.GONE);
                break;
            case R.id.cancel_apply:
                cancelFriendApply();
                break;

            case R.id.confirm_apply:
                confirmFriendApply();
                break;

        }
    }

    private ApplyModel modelCur = null;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        modelCur = applyArr.get(position - 1);
        if (modelCur != null){
            bgChoose.setVisibility(View.VISIBLE);
            String headImage = RunningData.getInstance().echoMainPicUrl() + modelCur.avatar_url + "?imageView2/1/w/150/h/150";
            RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.ic_icon);
            Glide.with(this).load(headImage).apply(requestOptions).into(ivAvatar);
        }

    }

    private void cancelFriendApply(){
        startServer(-1);
    }

    private void confirmFriendApply(){
        startServer(1);
    }

    private void startServer(int op_type){
        createDialog("正在处理..");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/v2/confirmFriend")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("data_id", modelCur.id)
                .params("user_id", modelCur.apply_user_id)
                .params("op_type", op_type)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (response.body().code == 1){
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                            finish();
                        }else if (response.body().code == -1){
                            RunningData.getInstance().reLogInMethod();
                        }else{
                            makeSnake(applyList, response.body().msg, R.mipmap.toast_alarm
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
