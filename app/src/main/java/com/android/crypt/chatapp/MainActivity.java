package com.android.crypt.chatapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.crypt.chatapp.contact.ContactFragment;
import com.android.crypt.chatapp.user.UserFragment;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.utility.Common.ParameterUtil;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.android.crypt.chatapp.widget.StateTabHost;
import com.android.crypt.chatapp.msgList.MsgFragment;

public class MainActivity extends BaseActivity
        implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        BaseFragment.OnFragmentInteractionListener {

    private Handler handler;
    private boolean isClose = false;
    private CoordinatorLayout rootView = null;
    private View setting_view = null;
    private View bottomView = null;
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
    private int bottonTabbarHeight = 0;
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
        rootView = (CoordinatorLayout) findViewById(R.id.root_view);
        //主页tab页
        mTabHost = (StateTabHost) findViewById(R.id.tab_host);
        bottomView = (View) findViewById(R.id.bottom_view);
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
        checkIsShowSettingView();
    }



    private View getTabItemView(int index){
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_view, null);
        ImageView imageView = (ImageView) ((LinearLayout) view.findViewById(R.id.tabContainer)).getChildAt(0);
        imageView.setImageResource(mImageViewArray[index]);
//        TextView textView = (TextView) ((LinearLayout) view.findViewById(R.id.tabContainer)).getChildAt(1);
//        textView.setText(mTextViewArray[index]);
        return view;
    }

    private void checkIsShowSettingView(){
        SeekBar seekBar = null;
        Button dismiss = null;
        Button finish = null;

        String initTabbar = getPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "initTabbar", "0");
        if (initTabbar.equalsIgnoreCase("0")){
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            setting_view = LayoutInflater.from(this).inflate(R.layout.app_main_setting, null);
            setting_view.setLayoutParams(lp);
            if (rootView != null && setting_view != null){
                rootView.addView(setting_view);
                dismiss = (Button) setting_view.findViewById(R.id.dismiss);
                finish = (Button) setting_view.findViewById(R.id.finish);
                seekBar = (SeekBar) setting_view.findViewById(R.id.seek_bar);
                dismiss.setOnClickListener(this);
                finish.setOnClickListener(this);
                seekBar.setOnSeekBarChangeListener(this);
            }
        }

        String tabbar_height = getPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "tabbar_height", null);// 0未登录,1已登录
        changeTabItemView(string2int(tabbar_height));
        if (seekBar != null){
            seekBar.setProgress(string2int(tabbar_height));
        }
    }

    private void finishSetTabHeight(){
        setting_view.setVisibility(View.GONE);
        rootView.removeView(setting_view);
        setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "tabbar_height", bottonTabbarHeight + "");
        setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "initTabbar", "1");

    }

    private void changeTabItemView(int height){
        int heigthAll = DensityUtil.dip2px(this, height);
        ViewGroup.LayoutParams lp;
        lp = bottomView.getLayoutParams();
        lp.height = heigthAll;
        bottomView.setLayoutParams(lp);
    }

    private int string2int(String value){
        int result = 0;
        try{
            result = Float.valueOf(value).intValue();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.dismiss:
                setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "tabbar_height", "0");
                setPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "initTabbar", "1");
                setting_view.setVisibility(View.GONE);
                rootView.removeView(setting_view);
                break;
            case R.id.finish:
                finishSetTabHeight();
                break;
            default:
                break;
        }
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


    /**
     * 当进度条发生变化时调用该方法
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int count = fragmentArray.length;
        bottonTabbarHeight = seekBar.getProgress();
        changeTabItemView(bottonTabbarHeight);
    }
}
