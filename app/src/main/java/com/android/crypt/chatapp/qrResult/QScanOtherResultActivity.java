package com.android.crypt.chatapp.qrResult;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QScanOtherResultActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.qr_value)
    EditText qrValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qscan_other_result);

        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_code_other_result);
        setSupportActionBar(toolbar);
        initGlobalData();
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


    private void initGlobalData() {
        Intent intent = getIntent();
        String keyValue = (String) intent.getSerializableExtra("qr_result");
        if (keyValue != null){
            qrValue.setText(keyValue);
        }
    }
}
