package com.android.crypt.chatapp.InfoSetting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.BaseActivity;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoadPrivateKeyActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.save_pri_key)
    Button savePriKey;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.load_pri_key)
    EditText loadPriKey;
    @BindView(R.id.pri_key_account)
    EditText priKeyAccount;
    @BindView(R.id.scan_f)
    ImageButton scanF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_private_key);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.title_private_key);
        cosTextTitle.setText("");
        setSupportActionBar(toolbar);
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
        priKeyAccount.setText(RunningData.getInstance().getCurrentAccount());
        scanF.setVisibility(View.VISIBLE);
        scanF.setOnClickListener(this);
        savePriKey.setOnClickListener(this);
        initGlobalData();
    }

    private void initGlobalData() {
        Intent intent = getIntent();
        String keyValue = (String) intent.getSerializableExtra("qr_result");
        if (keyValue != null){
            loadPriKey.setText(keyValue);
        }
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.scan_f:
                startQrCode();
                break;
            case R.id.save_pri_key:
                savePriKeyMethod();
                break;
            default:
                    break;
        }
    }

    private void savePriKeyMethod(){
        showAlert();
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("更新密钥后请重启app")
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情
                        dialogInterface.dismiss();
                        saveKeys();
                    }
                });
        builder.create().show();
    }

    private void saveKeys(){
        String pri_key = loadPriKey.getText().toString();
        String phone_num = priKeyAccount.getText().toString();
        if (pri_key.equals("") || phone_num.equals("")){
            makeSnake(savePriKey, "请输入私钥和对应的账号", R.mipmap.toast_alarm
                    , Snackbar.LENGTH_LONG);
        }else{
            Logger.d("导入的私钥是：" + pri_key);
            CacheTool.getInstance().cacheObject(ObjectCacheType.pri_key, pri_key);
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }



    private void startQrCode(){
        Intent intent = new Intent(this, CaptureActivity.class);
        /*ZxingConfig是配置类
        *可以设置是否显示底部布局，闪光灯，相册，
        * 是否播放提示音  震动
        * 设置扫描框颜色等
        * 也可以不传这个参数
        * */
        ZxingConfig config = new ZxingConfig();
        config.setPlayBeep(true);//是否播放扫描声音 默认为true
        config.setShake(true);//是否震动  默认为true
        config.setDecodeBarCode(true);//是否扫描条形码 默认为true
        config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
        config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
        config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
        config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
        startActivityForResult(intent, 1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                if (content.startsWith("1#")){
                    Logger.d("长度 = " + content.length());
                    loadPriKey.setText(content.substring(2));
                }else{
                    makeSnake(scanF, "密钥较长，对准扫描会增加识别成功概率", R.mipmap.toast_alarm , Snackbar.LENGTH_LONG);
                }
            }else{
                makeSnake(scanF, "没有识别到二维码", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            }
        }
    }

}
