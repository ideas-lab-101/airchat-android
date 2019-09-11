package com.android.crypt.chatapp.InfoSetting;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.baoyz.actionsheet.ActionSheet;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.guide.RegInputAccountActivity;
import com.android.crypt.chatapp.utility.Crypt.CryToolCallbacks;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.android.crypt.chatapp.utility.Common.ParameterUtil.imagePath;


public class ReCalKeysActivity extends BaseActivity implements View.OnClickListener, CryToolCallbacks,View.OnLongClickListener, ActionSheet.ActionSheetListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.show_recal)
    Button showRecal;
    @BindView(R.id.re_cal_tips)
    LinearLayout reCalTips;
    @BindView(R.id.recal_contant)
    LinearLayout recalContant;
    @BindView(R.id.start_re_cal)
    Button startReCal;
    @BindView(R.id.upload_keys)
    Button uploadKeys;

    @BindView(R.id.start_reg)
    Button startReg;
    @BindView(R.id.pub_key)
    EditText pubKey;
    @BindView(R.id.pri_key)
    EditText priKey;
    @BindView(R.id.contentIvWithLogo)
    ImageView contentIvWithLogo;
    @BindView(R.id.pri_key_bg)
    LinearLayout priKeyBg;
    @BindView(R.id.keys_contant)
    LinearLayout keysContant;
    @BindView(R.id.code_tips)
    TextView codeTips;


    private RelativeLayout relative;
    private int width, height;
    private String prikeyEnString;
    private String pubKeySting;
    private Bitmap bitmap;
    private boolean isrReg = false;

    private String mpub_key = "";
    private String mpri_key = "";
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_cal_keys);

        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_keys_re_cal);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null){
            bitmap.recycle();
            bitmap = null;
        }
    }

    private void initView() {
        try{
            Intent intent = getIntent();
            isrReg = (boolean) intent.getSerializableExtra("is_reg");
        }catch (Exception e){}


        showRecal.setOnClickListener(this);
        startReCal.setOnClickListener(this);
        startReg.setOnClickListener(this);
        uploadKeys.setOnClickListener(this);


        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.widthPixels;

        relative = (RelativeLayout) findViewById(R.id.code_bg);
        ViewGroup.LayoutParams params = relative.getLayoutParams();
        params.height = height;
        relative.setLayoutParams(params);

        pubKey.setOnTouchListener(touchListener);
        priKey.setOnTouchListener(touchListener);
        pubKey.setKeyListener(null);
        pubKey.setKeyListener(null);
        priKeyBg.setOnLongClickListener(this);


        String account = RunningData.getInstance().getCurrentAccount();
        if (account.equals("")){
            account = "x-";
        }else{
            account = account.substring(account.length() - 1, account.length()) + "-";
        }
        String tips = " 标记(助记):" + account + (int)(1+Math.random()*(10-1+1)) + " - 建议用摄像头扫描";
        codeTips.setText(tips);

        if (isrReg == true){
            reCalTips.setVisibility(View.GONE);
            recalContant.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_recal:
                reCalTips.setVisibility(View.GONE);
                recalContant.setVisibility(View.VISIBLE);
                break;
            case R.id.start_re_cal:
                startRecalculate();
                break;
            case R.id.start_reg:
                startRegMethod();
                break;
            case R.id.upload_keys:
                startUploadMethod();
                break;

            default:
                break;
        }
    }

    private void startRecalculate() {
        createDialog("正在计算密钥,请稍后...");
        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                showKeys();
            }
        }, 3000);
    }

    private void startRegMethod(){
        Intent intent = new Intent(this, RegInputAccountActivity.class);
        intent.putExtra("pub_key", mpub_key);
        intent.putExtra("pri_key", mpri_key);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void showKeys() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (isrReg == true){
                    uploadKeys.setVisibility(View.GONE);
                    startReg.setVisibility(View.VISIBLE);
                }else{
                    uploadKeys.setVisibility(View.VISIBLE);
                    startReg.setVisibility(View.GONE);
                }
            }
        });
        CryTool tool = new CryTool();
        tool.genPubAndPriKey(this);
    }


    @Override
    public void calRSAKeysFinish(final String pub_key, String pri_key, final String pri_key_en) {
        //更新主线程UI
        prikeyEnString = pri_key_en;
        pubKeySting = pub_key;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 连接断开，更改UI
                mpub_key = pub_key;
                mpri_key = pri_key_en;
                pubKey.setText(pub_key);
                priKey.setText(pri_key_en);
                keysContant.setVisibility(View.VISIBLE);

                String result = "1#" + pri_key_en;
                bitmap = null;
                bitmap = CodeCreator.createQRCode(result, width, width, null);

                if (bitmap != null) {
                    contentIvWithLogo.setImageBitmap(bitmap);
                }
            }
        });

    }

    /**
     * 设置触摸事件，由于EditView与TextView都处于ScollView中，
     * 所以需要在OnTouch事件中通知父控件不拦截子控件事件
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE) {
                //按下或滑动时请求父节点不拦截子节点
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //抬起时请求父节点拦截子节点
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        }
    };


    private void startUploadMethod(){
        Logger.d("公钥 = " + mpub_key);
        Logger.d("私钥 = " + mpri_key);

        createDialog("修改中...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "user/updateUserInfo")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("public_key", mpub_key)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onFinish() {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                    @Override
                    public void onCacheSuccess(Response<CodeResponse> response) {
                        onSuccess(response);
                    }

                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (response.body().code == 1) {
                            process();
                        } else {
                            makeSnake(toolbar, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
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

    private void process(){
        String account = RunningData.getInstance().getCurrentAccount();
        CacheTool.getInstance().cacheObjectWithKey(ObjectCacheType.pri_key, mpri_key, account);
        finish();
    }

    public boolean onLongClick(View v) {
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("保存图片")
                .setCancelableOnTouchOutside(true)
                .setListener(this)
                .show();
        return true;
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            saveImage();
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {}


    private Handler mHandler = new Handler();
    private void saveImage(){
        // 获取图片某布局
        priKeyBg.setDrawingCacheEnabled(true);
        priKeyBg.buildDrawingCache();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 要在运行在子线程中
                try {
                    final Bitmap image_map = priKeyBg.getDrawingCache(); // 获取图片
                    saveToLocal(image_map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                priKeyBg.destroyDrawingCache(); // 保存过后释放资源
            }
        },1000);
    }
    private void saveToLocal(Bitmap bmp) throws IOException {
        // 首先保存图片
        File appDir = new File(imagePath);
        if (!appDir.exists()) {
            appDir.getParentFile().mkdirs();
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            bmp.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        Uri uri = Uri.fromFile(file);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        makeSnake(toolbar, "保存成功", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
    }
}
