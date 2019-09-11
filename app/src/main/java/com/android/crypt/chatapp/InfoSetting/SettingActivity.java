package com.android.crypt.chatapp.InfoSetting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.InfoSetting.Config.ConfigDic;
import com.android.crypt.chatapp.guide.LoginActivity;
import com.android.crypt.chatapp.user.BgImageActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.baoyz.actionsheet.ActionSheet;
import com.google.gson.Gson;
import com.kyleduo.switchbutton.SwitchButton;
import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.GlideCacheUtils;
import com.android.crypt.chatapp.utility.Common.ParameterUtil;
import com.android.crypt.chatapp.utility.Common.ServiceUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity implements ActionSheet.ActionSheetListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.auto_check)
    SwitchButton autoCheck;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.ll_version_check)
    LinearLayout llVersionCheck;
    @BindView(R.id.tv_cacheSize)
    TextView tvCacheSize;
    @BindView(R.id.ll_clear_cache)
    LinearLayout llClearCache;
    @BindView(R.id.btn_exit)
    TextView btnExit;
    @BindView(R.id.push_voice)
    TextView pushVoice;

    private String token;
    private String versionName;
    private int actionKind = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_setting);
        setSupportActionBar(toolbar);

        token = RunningData.getInstance().getToken();
        //getPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "token", "");// 用户token
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
        if ("1".equals(getPrivateXml(ParameterUtil.SYS_SETTING_XML, "auto_check", "1"))) {
            autoCheck.setChecked(true);
        } else {
            autoCheck.setChecked(false);
        }
        autoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    setPrivateXml(ParameterUtil.SYS_SETTING_XML, "auto_check", "1");
                } else
                    setPrivateXml(ParameterUtil.SYS_SETTING_XML, "auto_check", "0");
            }
        });

        versionName = ServiceUtils.getCurVersionName(getApplication());
        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        tv_version.setText("【" + versionName + "】");
        tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(this));


        String voice_setting = RunningData.getInstance().getCurPushVoice();
        changeTips(voice_setting);
    }

    private void changeTips(String defaultVoice) {
        if (defaultVoice.equals("dog.mp3")) {
            pushVoice.setText("汪星人");
        } else if (defaultVoice.equals("dev.mp3")) {
            pushVoice.setText("系统音");
        } else if (defaultVoice.equals("blank.mp3")) {
            pushVoice.setText("静音");
        } else {
            pushVoice.setText("喵星人");
        }
    }

    @OnClick({R.id.ll_version_check, R.id.ll_clear_cache, R.id.btn_exit, R.id.ll_account_info, R.id.ll_chat_global_Image, R.id.ll_chat_text_size, R.id.ll_server_setting, R.id.log_out_and_delete_key})
    public void onViewClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.ll_version_check:
//				createDialog(getString(R.string.str_waiting_for_loading));
//				ServiceUtils.checkAppUpdate(this, pid, true);
//				setPrivateXml(ParameterUtil.SYS_SETTING_XML, "last_check", DateUtils.formatDateTime(new Date(), DateUtils.DATETIME_FORMAT_SS));
                break;
            case R.id.ll_clear_cache:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("确认清除应用缓存?");
                builder.setTitle(getResources().getString(
                        R.string.dialog_alert_tip));
                builder.setPositiveButton(
                        getResources().getString(R.string.dialog_alert_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //执行删除缓存目录的操作
                                GlideCacheUtils.getInstance().clearImageAllCache(SettingActivity.this);
                            }
                        });
                builder.setNegativeButton(
                        getResources().getString(R.string.dialog_alert_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                break;

            case R.id.ll_account_info:
                intent = new Intent(this, InfoDataActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.btn_exit:
                logOut(false);
                break;
            case R.id.log_out_and_delete_key:
                logOut(true);
                break;
            case R.id.ll_chat_global_Image:
                choosePicture();
                break;
            case R.id.ll_chat_text_size:
                intent = new Intent(this, TextSizeSetActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_server_setting:
                intent = new Intent(this, ChangePushVoiceActivity.class);
                startActivityForResult(intent, 1);
                break;
            default:
                break;
        }
    }

    private int logoutkind = 0;

    private void logOut(boolean deleteKey) {
        if (deleteKey == true) {
            logoutkind = 2;  //删除本地私钥
        } else {
            logoutkind = 1;  //不删除本地私钥
        }
        actionKind = 1;
        ActionSheet.createBuilder(this, getSupportFragmentManager()).setCancelButtonTitle("取消").setOtherButtonTitles("退出登录").setCancelableOnTouchOutside(true).setListener(this).show();
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (actionKind == 1) {
            if (index == 0) {
                if (logoutkind == 2) {
                    CacheTool.getInstance().clearPri_key(RunningData.getInstance().getCurrentAccount());
                }
                setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML,
                        "loginState", "0");
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                ChatAppApplication.getDBcApplication().exit();
            }
        } else if (actionKind == 2) {
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


    private void choosePicture() {
        actionKind = 2;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("选择照片", "删除当前图片")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }


    private void openPhoto(boolean isdelete) {
        if (isdelete == false) {
            Intent intent = new Intent(this, BgImageActivity.class);
            intent.putExtra("is_global", true);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            deleteBgImage("global_bg_image");
        }
    }

    private void deleteBgImage(String account) {
        try {
            String speBgImage = RunningData.getInstance().getBgImageUrl();
            //查找特定的图
            File fileRoot = new File(speBgImage);
            if (fileRoot.exists()) {
                File[] files = fileRoot.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    String imageName = file.getName().split("\\.")[0];
                    if (imageName.equals(account)) {
                        file.delete();
                        makeSnake(btnExit, "操作成功", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                        break;
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1){
                String voice = (String)data.getSerializableExtra("voice");
                changeTips(voice);
            }
        }
    }
}