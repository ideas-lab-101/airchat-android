package com.android.crypt.chatapp.InfoSetting;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;


import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.baoyz.actionsheet.ActionSheet;

import butterknife.BindView;
import butterknife.ButterKnife;



public class WatchMyPrivateStringActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener, ActionSheet.ActionSheetListener {

    @BindView(R.id.pri_key_string)
    EditText priKeyString;
    @BindView(R.id.show_bg)
    LinearLayout showBg;
    @BindView(R.id.txt_password)
    EditText txtPassword;
    @BindView(R.id.tv_login)
    Button tvLogin;
    @BindView(R.id.check_bg)
    LinearLayout checkBg;


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_my_private_string);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_keys_watch);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initView() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();

        String pri_key = RunningData.getInstance().getMyPrikeyEnWith();

        priKeyString.setText(pri_key);
        tvLogin.setOnClickListener(this);
        priKeyString.setOnTouchListener(touchListener);
        priKeyString.setKeyListener(null);
    }

    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        hideInput();
        String pwd = txtPassword.getText().toString();
        String userPwd = RunningData.getInstance().getCurrentPwd();
        if (pwd.equals(userPwd)) {
            checkBg.setVisibility(View.GONE);
            showBg.setVisibility(View.VISIBLE);
        } else {
            showAlert();
        }

    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("密码错误")
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    }
                });
        builder.create().show();

    }


    private void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
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

        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {}




}
