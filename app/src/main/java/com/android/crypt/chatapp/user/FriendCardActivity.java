package com.android.crypt.chatapp.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.InfoSetting.ChangeFriendLabelActivity;
import com.android.crypt.chatapp.PhotoViewer.Model.ImageModel;
import com.android.crypt.chatapp.PhotoViewer.PhotoViewerActivity;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.msgDetail.MsgDetailActivity;
import com.android.crypt.chatapp.qrResult.MyQRCodeActivity;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.baoyz.actionsheet.ActionSheet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FriendCardActivity extends BaseActivity implements ActionSheet.ActionSheetListener{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.gap_line)
    View gapLine;
    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.iv_avatar)
    RoundImageView ivAvatar;
    @BindView(R.id.tv_profileName)
    TextView tvProfileName;
    @BindView(R.id.iv_qrCode)
    ImageView ivQrCode;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.account)
    TextView account;
    @BindView(R.id.change_name_f)
    LinearLayout changeNameF;
    @BindView(R.id.send_sm_f)
    LinearLayout sendSmF;
    @BindView(R.id.scan_f)
    ImageButton scanF;


    private ContactModel mMap;
    private String pushDirect;
    private int selectKind = -1;
    private int actionKind = -1;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_card);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.title_bar_userInfo);
        cosTextTitle.setText("");
        gapLine.setVisibility(View.GONE);
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
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        Intent intent = getIntent();
        this.mMap = (ContactModel) intent.getSerializableExtra("friendInfo");
        this.pushDirect = intent.getStringExtra("pushDirect");
        RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.default_head);

        Glide.with(this)
                .load(RunningData.getInstance().echoMainPicUrl() + this.mMap.avatar_url)
                .apply(requestOptions)
                .into(ivAvatar);

        tvProfileName.setText(this.mMap.label);
        userName.setText(this.mMap.username);
        account.setText(changePhoneNum(this.mMap.account));

        scanF.setVisibility(View.VISIBLE);
        scanF.setBackgroundResource(R.mipmap.three_dot);

//        ivAvatar.add
    }

    private String changePhoneNum(String account) {
        if (account.startsWith("AC")){
            return account;
        }else{
            if (account.length() <= 8) {
                return account;
            } else {
                return account.substring(0, 3) + "****" + account.substring(7, account.length());
            }
        }
    }



    @OnClick({R.id.iv_avatar, R.id.iv_qrCode, R.id.change_name_f, R.id.send_sm_f, R.id.scan_f, R.id.change_bg_image})
    public void onViewClicked(View view) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Intent intent;
        switch (view.getId()) {
            case R.id.iv_avatar:
                Gson gson = new Gson();
                String image_url = RunningData.getInstance().echoMainPicUrl() + this.mMap.avatar_url;
                intent = new Intent(this, PhotoViewerActivity.class);
                ArrayList<ImageModel> image_url_list = new ArrayList<ImageModel>();
                ImageModel model = new ImageModel(image_url, "");
                image_url_list.add(model);
                intent.putExtra("image_url", image_url_list);
                intent.putExtra("cur_index", 0);
                intent.putExtra("is_encode", false);
                startActivity(intent);
                overridePendingTransition(0,0);

                break;
            case R.id.iv_qrCode:
                myQrCodeAc();
                break;
            case R.id.change_name_f:
                intent = new Intent(this, ChangeFriendLabelActivity.class);
                intent.putExtra("friendInfo", this.mMap);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.send_sm_f:
                if (pushDirect.equalsIgnoreCase("finish")) {
                    finish();
                    overridePendingTransition(R.anim.in_from_left,
                            R.anim.out_to_right);
                    break;
                } else {
                    intent = new Intent(this, MsgDetailActivity.class);
                    intent.putExtra("msgReceiver", this.mMap);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right,
                            R.anim.out_to_left);
                }
                break;

            case R.id.scan_f:
                showChoosePage();
                break;
            case R.id.change_bg_image:
                choosePicture();
                break;
            default:
                break;
        }
    }



    private void myQrCodeAc() {
        Intent intent = new Intent(this, MyQRCodeActivity.class);
        intent.putExtra("friendInfo", this.mMap);
        intent.putExtra("isMine", false);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right,
                R.anim.out_to_left);

    }

    private void showChoosePage(){
        actionKind = 1;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("删除好友", "加入黑名单", "清空聊天记录")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (actionKind == 1){
            if (index == 0) {
                Logger.d("删除好友");
                selectKind = 0;
                showDeleteMsgAlert();
            } else if (index == 1) {
                Logger.d("加入黑名单");
                selectKind = 1;
                showDeleteMsgAlert();
            }else if(index == 2){
                Logger.d("清空聊天记录");
                selectKind = 2;
                showDeleteMsgAlert();
            }
        }else if(actionKind == 2){
            if (index == 0) {
               openPhoto(false);
            } else if (index == 1) {
                openPhoto(true);
            }
        }


    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {
    }

    private void showDeleteMsgAlert() {
        if(selectKind == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("确定删除好友 "+ this.mMap.label +" ？")
                    .setPositiveButton("删除好友", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteFriend();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    });
            builder.create().show();
        }else if(selectKind == 1){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("确定加入 "+ this.mMap.label +" 到黑名单？")
                    .setPositiveButton("清空消息记录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMsg();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            builder.create().show();
        }else if(selectKind == 2){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("确定清空 "+ this.mMap.label +" 的聊天消息？")
                    .setPositiveButton("清空消息记录", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMsg();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            builder.create().show();
        }
    }

    private void deleteMsg(){
        CacheTool.getInstance().deleteFriendMsg(this.mMap.account);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                makeSnake(scanF, "正在后台删除", R.mipmap.toast_alarm
                        , Snackbar.LENGTH_LONG);
            }
        });
    }

    private void choosePicture(){
        actionKind = 2;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("选择照片", "删除当前图片")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }


    private void openPhoto(boolean isdelete){
        if (isdelete == false){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("设置个人聊天背景")
                    .setMessage("如果设置了基础聊天背景\n优先显示此背景")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setPositiveButton("选择图片", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent   intent = new Intent(FriendCardActivity.this, BgImageActivity.class);
                            intent.putExtra("is_global", false);
                            intent.putExtra("account", mMap.account);
                            startActivity(intent);
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }
                    });
            builder.create().show();
        }else{
            deleteBgImage( this.mMap.account);
        }
    }

    private void deleteBgImage(String account){
        try{
            String speBgImage = RunningData.getInstance().getBgImageUrl();
            //查找特定的图
            File fileRoot = new File(speBgImage);
            if (fileRoot.exists()){
                File[] files = fileRoot.listFiles();
                for (int i = 0; i < files.length; i++){
                    File file = files[i];
                    String imageName = file.getName().split("\\.")[0];
                    if (imageName.equals(account)){
                        file.delete();
                        makeSnake(scanF, "操作成功", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                        break;
                    }
                }
            }
        }catch (Exception e){

        }
    }

    private void deleteFriend(){
        createDialog("正在删除中..");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/v2/delFriend")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("friend_id", this.mMap.friend_id)
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
                            makeSnake(ivAvatar, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
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

