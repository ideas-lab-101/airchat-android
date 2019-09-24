package com.android.crypt.chatapp.InfoSetting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.ClickUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyKeysOpActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recal_my_pri_key)
    LinearLayout recalMyPriKey;
    @BindView(R.id.my_pri_key)
    LinearLayout myPriKey;
    @BindView(R.id.my_pri_key_code)
    LinearLayout myPriKeyCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_keys_op);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_keys_op);
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
        recalMyPriKey.setOnClickListener(this);
        myPriKey.setOnClickListener(this);
        myPriKeyCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Intent intent = null;
        switch (v.getId()){
            case R.id.recal_my_pri_key:
                intent = new Intent(this, ReCalKeysActivity.class);
                intent.putExtra("is_reg", false);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.my_pri_key:
                intent = new Intent(this, WatchMyPrivateStringActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.my_pri_key_code:
                intent = new Intent(this, WatchMyPrivateKeyActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

                break;
            default:
                    break;
        }
    }
}
