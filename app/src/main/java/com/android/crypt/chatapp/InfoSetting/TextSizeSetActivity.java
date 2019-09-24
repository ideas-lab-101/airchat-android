package com.android.crypt.chatapp.InfoSetting;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.InfoSetting.Config.ConfigDic;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextSizeSetActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    @BindView(R.id.text_1)
    TextView text1;
    @BindView(R.id.text_2)
    TextView text2;
    @BindView(R.id.text_3)
    TextView text3;
    @BindView(R.id.finish)
    Button finish;

    private ConfigDic config = null;
    private int curTextSize = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_size_set);
        ButterKnife.bind(this);

        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_text_size);
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
                overridePendingTransition(R.anim.in_from_left,
                        R.anim.out_to_right);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        curTextSize = RunningData.getInstance().getCurTextSize();
        text1.setTextSize(curTextSize);
        text2.setTextSize(curTextSize);
        text3.setTextSize(curTextSize);

        seekBar.setProgress(curTextSize - 15);
        seekBar.setOnSeekBarChangeListener(this);
        finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Gson gson = new Gson();
        String configDicString = CacheTool.getInstance().getCacheObject(ObjectCacheType.config_dic);
        if (configDicString != "") {
            try {
                ConfigDic config = gson.fromJson(configDicString, ConfigDic.class);
                if (config != null){
                    config.msgTextSize = curTextSize;
                    RunningData.getInstance().setCurTextSize(curTextSize);
                    CacheTool.getInstance().cacheObject(ObjectCacheType.config_dic, gson.toJson(config));

                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
            }catch (Exception e){
                Logger.d("getCurTextSize 错误");
            }
        }else{
            ConfigDic config = new  ConfigDic();
            config.msgTextSize = curTextSize;
            RunningData.getInstance().setCurTextSize(curTextSize);
            CacheTool.getInstance().cacheObject(ObjectCacheType.config_dic, gson.toJson(config));
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    /**
     * 当进度条发生变化时调用该方法
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //设置文本框的值
        curTextSize = progress + 15;
        int value = progress;
        text1.setTextSize(value + 15);
        text2.setTextSize(value + 15);
        text3.setTextSize(value + 15);
    }

    /**
     * 开始滑动时调用该方法
     *
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 结束滑动时调用该方法
     *
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
