package com.android.crypt.chatapp.InfoSetting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.baoyz.actionsheet.ActionSheet;
import com.android.crypt.chatapp.R;
import com.yzq.zxinglibrary.encode.CodeCreator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.android.crypt.chatapp.utility.Common.ParameterUtil.imagePath;


public class WatchMyPrivateKeyActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener, ActionSheet.ActionSheetListener {
    @BindView(R.id.contentIvWithLogo)
    ImageView contentIvWithLogo;
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
    @BindView(R.id.code_tips)
    TextView codeTips;
    @BindView(R.id.pri_key_bg)
    LinearLayout priKeyBg;


    private RelativeLayout relative;
    private Bitmap bitmap;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_my_private_key);
        ButterKnife.bind(this);

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
        bitmap.recycle();
        bitmap = null;
    }

    private void initView() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.widthPixels;

        relative = (RelativeLayout) findViewById(R.id.code_bg);
        ViewGroup.LayoutParams params = relative.getLayoutParams();
        params.height = height;
        relative.setLayoutParams(params);

        String pri_key = RunningData.getInstance().getMyPrikeyEnWith();
        String result = "1#" + pri_key;

        bitmap = null;
        bitmap = CodeCreator.createQRCode(result, width, width, null);

        if (bitmap != null) {
            contentIvWithLogo.setImageBitmap(bitmap);
        }

        priKeyString.setText(pri_key);
        tvLogin.setOnClickListener(this);
        priKeyString.setOnTouchListener(touchListener);
        priKeyString.setKeyListener(null);
        priKeyBg.setOnLongClickListener(this);


        String account = RunningData.getInstance().getCurrentAccount();
        if (account.equals("")) {
            account = "x-";
        } else {
            account = account.substring(account.length() - 1, account.length()) + "-";
        }
        String tips = " 标记(助记):" + account + (int)(1 + Math.random() * (10 - 1 + 1)) + " - 建议用摄像头扫描";
        codeTips.setText(tips);

    }

    @Override
    public void onClick(View v) {
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
                        hideInput();
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    }
                });
        builder.create().show();

    }


    private void hideInput() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(txtPassword.getWindowToken(), 0);
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
            saveImage();
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {}


    private Handler mHandler = new Handler();
    private void saveImage(){
        // 获取图片某布局
        priKeyBg.setDrawingCacheEnabled(true);
        priKeyBg.buildDrawingCache();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 要在运行在子线程中
                try {
                    final Bitmap image_map = priKeyBg.getDrawingCache(); // 获取图片
                    saveToLocal(image_map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                priKeyBg.destroyDrawingCache(); // 保存过后释放资源
            }
        },1000);
    }
    private void saveToLocal(Bitmap bmp) throws IOException {
        // 首先保存图片
        File appDir = new File(imagePath);
        if (!appDir.exists()) {
            appDir.getParentFile().mkdirs();
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            bmp.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        Uri uri = Uri.fromFile(file);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        makeSnake(toolbar, "保存成功", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
    }

}
