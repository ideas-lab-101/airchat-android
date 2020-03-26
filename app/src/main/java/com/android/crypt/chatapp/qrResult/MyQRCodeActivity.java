package com.android.crypt.chatapp.qrResult;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Common.WRKShareUtil;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.baoyz.actionsheet.ActionSheet;
import com.bumptech.glide.Glide;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import butterknife.BindView;
import butterknife.ButterKnife;
import static com.android.crypt.chatapp.utility.Common.ParameterUtil.imagePath;

public class MyQRCodeActivity extends BaseActivity implements View.OnClickListener,  ActionSheet.ActionSheetListener{

    @BindView(R.id.contentIvWithLogo)
    ImageView contentIvWithLogo;
    @BindView(R.id.head_icon)
    RoundImageView headIcon;
    @BindView(R.id.inner_head_icon)
    RoundImageView innerHeadIcon;
    @BindView(R.id.user_name_code)
    TextView userName;
    @BindView(R.id.user_intro)
    TextView userIntro;
    @BindView(R.id.qrcode_tips)
    TextView qrcodeTips;

    @BindView(R.id.code_bg)
    LinearLayout codeBg;
    @BindView(R.id.choose_limit)
    LinearLayout chooseLimit;
    @BindView(R.id.code_bg_content)
    RelativeLayout relative;
    @BindView(R.id.gap_line)
    View gapLine;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.quit_msg_ac)
    ImageButton quitMsgAc;

    @BindView(R.id.scan_f)
    ImageButton scanF;
    /**
     * 生成带logo的二维码
     */
    private String contentEtString;
    private  ContactModel mMap;
    private Bitmap bitmap;
    private Handler mHandler = new Handler();
    private long timeGap =  24 * 3600;
    private int actionKind = -1;
    private boolean isMine = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qrcode);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.title_bar_qrCode);
        cosTextTitle.setText("");
        gapLine.setVisibility(View.GONE);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(Color.parseColor("#F5F5F4"));
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

        Intent intent = getIntent();
        this.mMap = (ContactModel) intent.getSerializableExtra("friendInfo");
        isMine = (boolean) intent.getSerializableExtra("isMine");

        quitMsgAc.setOnClickListener(this);
        chooseLimit.setOnClickListener(this);

        int shareDays = 100;
        if (!isMine){
            shareDays = 1;
            qrcodeTips.setText("分享好友二维码只有24小时有效期");
        }

        long time = System.currentTimeMillis();
        long timeLimit = shareDays * timeGap;
        String tokenValue = "2#" + String.valueOf(time) + "&" + timeLimit + "&" + this.mMap.account ;
        CryTool tool = new CryTool();
        String codeValue = "http://airchat.ideas-lab.cn/index?token=" + tool.aesEnWith(tokenValue, RunningData.getInstance().getInnerAESKey());


        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels - DensityUtil.dip2px(this, 70);

        ViewGroup.LayoutParams params = relative.getLayoutParams();
        params.height = width;
        relative.setLayoutParams(params);

        bitmap = null;
        contentEtString = codeValue;

        bitmap = CodeCreator.createQRCode(contentEtString, width, width, null);
        if (bitmap != null) {
            contentIvWithLogo.setImageBitmap(bitmap);
        }

        String user_name = this.mMap.username;
        if (!this.mMap.label.equals("")){
            user_name = this.mMap.label;
        }
        userName.setText(user_name);

        String intro = this.mMap.introduction;
        if (intro != null && !intro.equals("")) {
            userIntro.setVisibility(View.VISIBLE);
            userIntro.setText(intro);
        }else{
            userIntro.setVisibility(View.GONE);

        }

        Glide.with(this)
                .load(RunningData.getInstance().echoMainPicUrl() + this.mMap.avatar_url)
                .into(headIcon);
        Glide.with(this)
                .load(RunningData.getInstance().echoMainPicUrl() + this.mMap.avatar_url)
                .into(innerHeadIcon);

        relative.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareMethod();
                return true;
            }
        });

        scanF.setVisibility(View.VISIBLE);
        scanF.setBackgroundResource(R.mipmap.three_dot);
        scanF.setOnClickListener(this);

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (actionKind == 1){
            if (index == 0) {
                saveImage();
            }else if (index == 1) {
                shareToWeChat(SendMessageToWX.Req.WXSceneSession);
            }else if (index == 2) {
                shareToWeChat(SendMessageToWX.Req.WXSceneTimeline);
            }
        }else if(actionKind == 2){
            if (index == 0) {
                changeCodeText(1);
            }else if (index == 1) {
                changeCodeText(7);
            }else if (index == 2) {
                changeCodeText(30);
            }else if (index == 3) {
                changeCodeText(100);
            }
        }
    }



    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quit_msg_ac:
                finish();
                overridePendingTransition(R.anim.in_from_left,
                        R.anim.out_to_right);
                break;
            case R.id.choose_limit:
                if (isMine){
                    changeCodeMethod();
                }
                break;
            case R.id.scan_f:
                shareMethod();
            default:
                break;
        }
    }

    private void shareMethod(){
        actionKind  =1;
        ActionSheet.createBuilder(MyQRCodeActivity.this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("保存图片", "分享到微信联系人", "分享到朋友圈")
                .setCancelableOnTouchOutside(true)
                .setListener(MyQRCodeActivity.this).show();
    }

    private void saveImage(){
        // 获取图片某布局
        codeBg.setDrawingCacheEnabled(true);
        codeBg.buildDrawingCache();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 要在运行在子线程中
                try {
                    final Bitmap image_map = codeBg.getDrawingCache(); // 获取图片
                    saveToLocal(image_map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                codeBg.destroyDrawingCache(); // 保存过后释放资源
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
        makeSnake(codeBg, "保存成功", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
    }

    private void changeCodeMethod(){
        actionKind  =2;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("24小时", "7天", "30天", "100天")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }



    private void changeCodeText(int days){
        long time = System.currentTimeMillis();
        long timeLimit = days * timeGap;
        String tokenValue = "2#" + String.valueOf(time) + "&" + timeLimit + "&" + this.mMap.account ;
        CryTool tool = new CryTool();
        String codeValue =  "http://airchat.ideas-lab.cn/index?token=" + tool.aesEnWith(tokenValue, RunningData.getInstance().getInnerAESKey());

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels - DensityUtil.dip2px(this, 70);

        bitmap.recycle();
        contentEtString = codeValue;

        bitmap = CodeCreator.createQRCode(contentEtString, width, width, null);
        if (bitmap != null) {
            contentIvWithLogo.setImageBitmap(bitmap);
        }
    }

    private void shareToWeChat(final int wxSceneSession){
        final String title = "分享你一个安全的通讯工具";
        final String sub = "AirChat -- 基于密码学的安全通讯工具";
        // 获取图片某布局
        codeBg.setDrawingCacheEnabled(true);
        codeBg.buildDrawingCache();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 要在运行在子线程中
                try {
                    final Bitmap image_map = codeBg.getDrawingCache(); // 获取图片
                    WRKShareUtil.getInstance().shareImageToWx(image_map, title, sub, wxSceneSession);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                codeBg.destroyDrawingCache(); // 保存过后释放资源
            }
        },1000);

    }
}
