package com.android.crypt.chatapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.crypt.chatapp.contact.ContactFragment;
import com.android.crypt.chatapp.user.UserFragment;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.android.crypt.chatapp.widget.StateTabHost;
import com.android.crypt.chatapp.msgList.MsgFragment;

public class MainActivity extends BaseActivity
        implements View.OnClickListener,
        BaseFragment.OnFragmentInteractionListener {

    private Handler handler;
    private boolean isClose = false;

    private StateTabHost mTabHost;
    private Class fragmentArray[] ;
    //定义数组来存放按钮图片
    private int mImageViewArray[] ;
    //Tab选项卡的文字
    private String mTextViewArray[] ;

    private TextView user_caption, user_sign;
    private RoundImageView avatarView;
    private TextView urCount;
    public static int intRefresh = 0;

    public static boolean isForeground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);
        setSwipeBackEnable(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
        handler = new Handler();
    }

    public void initView(){

        //***获取debug or release 版本
        RunningData.getInstance().setIsApkInDebug();

        //主页tab页
        mTabHost = (StateTabHost) findViewById(R.id.tab_host);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.tab_content);
        fragmentArray = new Class[] {MsgFragment.class, ContactFragment.class, UserFragment.class};
        int count = fragmentArray.length;
        mImageViewArray = new int[] {R.drawable.tab_msg, R.drawable.tab_contact, R.drawable.tab_setting};
        mTextViewArray = new String[] {getString(R.string.tab_msg), getString(R.string.tab_contact), getString(R.string.tab_user)};
        mTabHost.clearAllTabs();

        for(int i = 0; i < count; i++){
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextViewArray[i]).setIndicator(getTabItemView(i));

            mTabHost.addTab(tabSpec, fragmentArray[i], null);
        }
        mTabHost.setCurrentTab(0);
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            default:
                break;
        }
    }

    private View getTabItemView(int index){

        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) ((LinearLayout) view.findViewById(R.id.tabContainer)).getChildAt(0);
        imageView.setImageResource(mImageViewArray[index]);

//        TextView textView = (TextView) ((LinearLayout) view.findViewById(R.id.tabContainer)).getChildAt(1);
//        textView.setText(mTextViewArray[index]);

        return view;
    }

    @Override
    public void onResume() {
        //新增后的刷新
        if (intRefresh == 1) {

        }
        isForeground = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        toolbar.setTitle(R.string.app_name);
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        makeToast(this, "fragment", Toast.LENGTH_SHORT, R.mipmap.toast_ok);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()){
            case R.id.action_scan:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            if (!isClose) {
                isClose = true;
                makeSnake(toolbar, getString(R.string.str_exit_app), R.mipmap.toast_alarm, Snackbar.LENGTH_LONG).show();
                handler.postDelayed(runnable, 5000);
                return true;
            } else {
                handler.removeCallbacks(runnable);
                ChatAppApplication.getDBcApplication().exit();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            isClose = false;
        }

    };




}
