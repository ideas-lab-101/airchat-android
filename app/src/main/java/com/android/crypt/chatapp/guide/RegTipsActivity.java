package com.android.crypt.chatapp.guide;


import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.android.crypt.chatapp.InfoSetting.ReCalKeysActivity;
import com.github.guilhe.views.CircularProgressView;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.guide.View.PageView;

public class RegTipsActivity extends BaseActivity implements View.OnClickListener{
    private LayoutInflater inflater;
    private PageView mPageView;
    private CircularProgressView circleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_tips);

        inflater = LayoutInflater.from(this);
        mPageView = (PageView) findViewById(R.id.page_view);


        //这里就是个普通的xml布局文件
        ConstraintLayout viewL = (ConstraintLayout) inflater.inflate(R.layout.reg_tips_gen_layout, null);
        mPageView.addPage(viewL);

        circleView = (CircularProgressView)findViewById(R.id.circular_progress_view);
        circleView.setProgress(50, true, 1500);


        ConstraintLayout viewPP = (ConstraintLayout) inflater.inflate(R.layout.reg_tips_pp_layout, null);
        mPageView.addPage(viewPP);

        ConstraintLayout viewM = (ConstraintLayout) inflater.inflate(R.layout.reg_tips_crypt_layout, null);
        mPageView.addPage(viewM);

        ConstraintLayout viewR = (ConstraintLayout) inflater.inflate(R.layout.reg_tips_last_layout, null);
        mPageView.addPage(viewR);
        Button start = (Button)viewR.findViewById(R.id.start_click);
        start.setOnClickListener(this);
    }

    public void popThisPage(View view){
        finish();
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(RegTipsActivity.this, ReCalKeysActivity.class);
        intent.putExtra("is_reg", true);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


}

