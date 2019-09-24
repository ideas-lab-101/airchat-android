package com.android.crypt.chatapp.InfoSetting;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.guide.RegNextActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.Crypt.CryToolCallbacks;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReCalKeysActivity extends BaseActivity implements View.OnClickListener, CryToolCallbacks {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.pub_key)
    EditText pubKey;
    @BindView(R.id.pri_key)
    EditText priKey;
    @BindView(R.id.start_re_cal)
    Button startReCal;
    @BindView(R.id.start_reg)
    Button startReg;
    @BindView(R.id.upload_keys)
    Button uploadKeys;
    @BindView(R.id.recal_contant)
    LinearLayout recalContant;
    @BindView(R.id.re_cal_centre)
    RoundImageView reCalCentre;
    @BindView(R.id.re_cal_process)
    LinearLayout reCalProcess;
    @BindView(R.id.show_recal)
    Button showRecal;
    @BindView(R.id.re_cal_tips)
    LinearLayout reCalTips;


    private String prikeyEnString;
    private String pubKeySting;

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
    }

    private void initView() {
        try {
            Intent intent = getIntent();
            isrReg = (boolean) intent.getSerializableExtra("is_reg");
        } catch (Exception e) {
        }


        showRecal.setOnClickListener(this);
        startReCal.setOnClickListener(this);
        startReg.setOnClickListener(this);
        uploadKeys.setOnClickListener(this);


        pubKey.setOnTouchListener(touchListener);
        priKey.setOnTouchListener(touchListener);
        pubKey.setKeyListener(null);
        pubKey.setKeyListener(null);

        if (isrReg == true) {
            reCalTips.setVisibility(View.GONE);
            recalContant.setVisibility(View.GONE);
            reCalProcess.setVisibility(View.VISIBLE);

            uploadKeys.setVisibility(View.GONE);
            startReg.setVisibility(View.VISIBLE);

            startRecalculate();

        } else {
            reCalTips.setVisibility(View.VISIBLE);
            recalContant.setVisibility(View.GONE);
            reCalProcess.setVisibility(View.GONE);

            uploadKeys.setVisibility(View.VISIBLE);
            startReg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.show_recal:
                reCalTips.setVisibility(View.GONE);
                recalContant.setVisibility(View.VISIBLE);
                reCalProcess.setVisibility(View.GONE);
                startRecalculate();
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
        reCalProcess.setVisibility(View.VISIBLE);
        reCalTips.setVisibility(View.GONE);
        recalContant.setVisibility(View.GONE);


        int resourceId = R.mipmap.code_cal;
        Glide.with(this)
                .load(resourceId)
                .into(reCalCentre);

        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                showKeys();
            }
        }, 3000);
    }

    private void startRegMethod() {
        Intent intent = new Intent(this, RegNextActivity.class);
        intent.putExtra("mpub_key", mpub_key);
        intent.putExtra("mpri_key", mpri_key);
        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void showKeys() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }

                reCalProcess.setVisibility(View.GONE);
                reCalTips.setVisibility(View.GONE);
                recalContant.setVisibility(View.VISIBLE);

                if (isrReg == true) {
                    uploadKeys.setVisibility(View.GONE);
                    startReg.setVisibility(View.VISIBLE);
                } else {
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


    private void startUploadMethod() {
//        Logger.d("公钥 = " + mpub_key);
//        Logger.d("私钥 = " + mpri_key);
        if (mpub_key.equalsIgnoreCase("") || mpri_key.equals("")) {
            makeSnake(toolbar, "数据为空", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            return;
        }
        createDialog("修改中...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "user/v2/updateUserInfo")
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

    private void process() {
        String account = RunningData.getInstance().getCurrentAccount();
        CacheTool.getInstance().cacheObjectWithKey(ObjectCacheType.pri_key, mpri_key, account);
        finish();
    }

//    public String getRandomString(int length) {
//        String str = "@#$%&*ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
//        Random random = new Random();
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < length; i++) {
//            int number = random.nextInt(str.length() - 1);
//            sb.append(str.charAt(number));
//        }
//        return sb.toString();
//    }

    //******注册返回
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 100) {
            String ac_id = data.getStringExtra("ac_id");
            Intent resultMethod = new Intent();
            resultMethod.putExtra("ac_id", ac_id);
            setResult(100, resultMethod);
            finish();
        }
    }

}
