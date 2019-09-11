package com.android.crypt.chatapp.InfoSetting;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.InfoSetting.Config.ConfigDic;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.android.crypt.chatapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChangePushVoiceActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cat_v)
    LinearLayout catV;
    @BindView(R.id.dog_v)
    LinearLayout dogV;
    @BindView(R.id.default_v)
    LinearLayout defaultV;
    @BindView(R.id.quite_v)
    LinearLayout quiteV;

    @BindView(R.id.cat_text)
    TextView catText;
    @BindView(R.id.dog_text)
    TextView dogText;
    @BindView(R.id.de_text)
    TextView deText;
    @BindView(R.id.quite_text)
    TextView quiteText;
    @BindView(R.id.finish)
    Button finishBtn;

    private String token;
    private String defaultVoice = "cat.mp3";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_push_voice);
        ButterKnife.bind(this);

        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_push_voice);
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
        catV.setOnClickListener(this);
        dogV.setOnClickListener(this);
        defaultV.setOnClickListener(this);
        quiteV.setOnClickListener(this);
        finishBtn.setOnClickListener(this);
        changeTips();

        defaultVoice = RunningData.getInstance().getCurPushVoice();
        changeTips();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dog_v:
                defaultVoice = "dog.mp3";
                playMusic();
                break;
            case R.id.cat_v:
                defaultVoice = "cat.mp3";
                playMusic();
                break;
            case R.id.default_v:
                defaultVoice = "dev.mp3";
                playMusic();
                break;
            case R.id.quite_v:
                defaultVoice = "blank.mp3";
                playMusic();
                break;

            case R.id.finish:
                startUpload();
                break;
        }
        changeTips();
    }



    private void changeTips() {
        if (defaultVoice.equals("dog.mp3")) {
            catText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            dogText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            deText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            quiteText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        } else if (defaultVoice.equals("dev.mp3")) {
            catText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            dogText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            deText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            quiteText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }else if (defaultVoice.equals("blank.mp3")) {
            catText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            dogText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            deText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            quiteText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else {
            catText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            dogText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            deText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            quiteText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
    }

    private void startUpload() {
        createDialog("修改中...");
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "user/updateUserInfo")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("voice_settings", defaultVoice)
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
        Gson gson = new Gson();
        String configDicString = CacheTool.getInstance().getCacheObject(ObjectCacheType.config_dic);
        if (configDicString != "") {
            try {
                ConfigDic config = gson.fromJson(configDicString, ConfigDic.class);
                if (config != null){
                    config.voiceSetting = defaultVoice;
                    CacheTool.getInstance().cacheObject(ObjectCacheType.config_dic, gson.toJson(config));

                    Intent intent = new Intent();
                    intent.putExtra("voice", defaultVoice);
                    setResult(1, intent);
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

                    RunningData.getInstance().setCurPushVoice(defaultVoice);
                }
            }catch (Exception e){}
        }else{
            ConfigDic config = new  ConfigDic();
            config.voiceSetting = defaultVoice;
            CacheTool.getInstance().cacheObject(ObjectCacheType.config_dic, gson.toJson(config));

            Intent intent = new Intent();
            intent.putExtra("voice", defaultVoice);
            setResult(1, intent);
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            RunningData.getInstance().setCurPushVoice(defaultVoice);

        }
    }
    private MediaPlayer mp = new MediaPlayer();
    private void playMusic(){
        if(mp.isPlaying()){
            return;
        }
        try{
            if (defaultVoice.equals("dog.mp3")) {
                mp = MediaPlayer.create(this, R.raw.dog);
                mp.start();
            } else if (defaultVoice.equals("dev.mp3")) {
                mp = MediaPlayer.create(this, R.raw.dev);
                mp.start();
            } else if (defaultVoice.equals("blank.mp3")) {
                mp = MediaPlayer.create(this, R.raw.blank);
                mp.start();
            } else {
                mp = MediaPlayer.create(this, R.raw.cat);
                mp.start();
            }
        }catch (Exception e){}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
        }
    }

}
