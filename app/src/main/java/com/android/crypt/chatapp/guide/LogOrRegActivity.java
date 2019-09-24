package com.android.crypt.chatapp.guide;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.ClickUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogOrRegActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.login)
    Button login;
    @BindView(R.id.register)
    Button register;
    @BindView(R.id.register_tips)
    Button registerTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_or_reg);
        ButterKnife.bind(this);
        setSwipeBackEnable(false);

        initView();
    }

    private void initView() {
        register.setOnClickListener(this);
        registerTips.setOnClickListener(this);
        login.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Intent intent = null;
        switch (v.getId()){
            case R.id.login:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.register:
                toRegPage();
                break;
            case R.id.register_tips:
                intent = new Intent(this, AppIntroActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            default:
                break;
        }
    }


    private void toRegPage(){
        Intent intent = new Intent(this, RegTipsActivity.class);
        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 100) {
            String ac_id = data.getStringExtra("ac_id");
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("你的AirChat账号是：")
                    .setMessage(ac_id)
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            builder.create().show();
        }
    }
}
