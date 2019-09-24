package com.android.crypt.chatapp.guide;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.InfoSetting.ReCalKeysActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.github.guilhe.views.CircularProgressView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegTipsActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.circular_progress_view)
    CircularProgressView circularProgressView;
    @BindView(R.id.register_tips)
    Button registerTips;
    @BindView(R.id.register)
    Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_tips);

        ButterKnife.bind(this);
        toolbar.setTitle("");
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
        circularProgressView.setProgress(50, true, 1500);
        register.setOnClickListener(this);
        registerTips.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Intent intent = null;
        switch (v.getId()) {
            case R.id.register:
                intent = new Intent(this, ReCalKeysActivity.class);
                intent.putExtra("is_reg", true);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;

            case R.id.register_tips:
                intent = new Intent(this, AppIntroActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
        }

    }


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

