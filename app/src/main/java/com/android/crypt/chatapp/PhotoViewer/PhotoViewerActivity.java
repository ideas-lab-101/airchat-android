package com.android.crypt.chatapp.PhotoViewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.PhotoViewer.Model.ImageModel;
import com.android.crypt.chatapp.PhotoViewer.adaptor.PhotoPagerAdapter;
import com.android.crypt.chatapp.msgDetail.IMTools.DownLoadImage;
import com.baoyz.actionsheet.ActionSheet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoView;

import static com.android.crypt.chatapp.utility.Common.ParameterUtil.imagePath;


public class PhotoViewerActivity extends BaseActivity implements ActionSheet.ActionSheetListener,DownLoadImage.ImageCallback {

    private LayoutInflater inflater;
    private PhotoView longPressImage = null;
    private PhotoPagerAdapter adapter;
    private List<View> viewList = null;
    private List<Bitmap> bitMapList = null;

    @BindView(R.id.quit_photo_viewer)
    ImageButton quitPhotoViewer;
    @BindView(R.id.page_view)
    ViewPager pageView;
    @BindView(R.id.all_bg_view)
    ConstraintLayout allBgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_contain);
        ButterKnife.bind(this);
        inflater = LayoutInflater.from(this);
        initView();
    }

    private void initView() {
        Intent intent = getIntent();//获取相关的intent
        boolean isEncode = (boolean) intent.getSerializableExtra("is_encode");
        if (isEncode == false){
            allBgView.setBackground(new ColorDrawable(0x100F0F0F));
            quitPhotoViewer.setBackground(getResources().getDrawable(R.mipmap.navi_quit_b));
        }else{
            allBgView.setBackground(new ColorDrawable(0xff0f0f0f));
            quitPhotoViewer.setBackground(getResources().getDrawable(R.mipmap.navi_quit_w));
        }
        try {
            ArrayList<ImageModel> image_url_list = (ArrayList) intent.getSerializableExtra("image_url");
            if (viewList == null) {
                viewList = new ArrayList<View>();
            }
            if (bitMapList == null) {
                bitMapList = new ArrayList<Bitmap>();
            }
            if (image_url_list != null) {
                for (int i = 0; i < image_url_list.size(); i++) {
                    ConstraintLayout view = (ConstraintLayout) inflater.inflate(R.layout.activity_photo_viewer, null);
                    viewList.add(view);
                    String url = image_url_list.get(i).image_url;
                    loadImageWith(url, view, image_url_list.get(i).de_key);
                }
                adapter = new PhotoPagerAdapter(viewList);
                pageView.setAdapter(adapter);
                try {
                    int index = (int) intent.getSerializableExtra("cur_index");
                    pageView.setCurrentItem(index);

                } catch (Exception e) {
                    Logger.d("index 缺失");
                }
            }

        } catch (Exception e) {
            Logger.d("url参数错误");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewList.clear();
        for (int i = 0; i < bitMapList.size(); i++){
            Bitmap bitmap = bitMapList.get(i);
            if (bitmap != null){
                Logger.d("PHOTO onDestroy");
                bitmap.recycle();
            }
        }
        bitMapList.clear();
    }

    @Override
    public void showImageResult(Bitmap bitmap) {
        bitMapList.add(bitmap);
    }

    private void loadImageWith(String image_url, ConstraintLayout view, String deKey) {
        PhotoView photo = (PhotoView) view.findViewById(R.id.big_image_viewer);
        TextView text_Tips = (TextView) view.findViewById(R.id.text_se_tips);

        if (deKey == null || deKey.equals("")) {
            text_Tips.setVisibility(View.GONE);
            RequestOptions requestOptions = new RequestOptions().fitCenter().placeholder(R.mipmap.default_image);
            Glide.with(this).load(image_url).apply(requestOptions).into(photo);
            addLongTapMethod(photo);
        } else {
            text_Tips.setVisibility(View.VISIBLE);
            //加密图片
            DownLoadImage loadTool = new DownLoadImage(this, this);
            loadTool.loadFile(photo, image_url, deKey, R.mipmap.default_image);
        }
    }


    private void addLongTapMethod(PhotoView photo) {
        photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longPressImage = (PhotoView) v;
                Logger.d("view = " + v);
                ActionSheet.createBuilder(PhotoViewerActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle("取消")
                        .setOtherButtonTitles("保存图片")
                        .setCancelableOnTouchOutside(true)
                        .setListener(PhotoViewerActivity.this).show();
                return true;
            }
        });
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            if (longPressImage != null) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Bitmap image_map = Bitmap.createBitmap(longPressImage.getDrawingCache());
//                            long timeStamp = System.currentTimeMillis();
//                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                            String sd = sdf.format(new Date(timeStamp));
                            saveImageToGallery(image_map);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {


    }

    @OnClick(R.id.quit_photo_viewer)
    public void onViewClicked() {
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }


//    private void saveToLocal(Bitmap bitmap, String bitName) throws IOException {
//        File file = new File(imagePath + bitName + ".jpg");
//        if (!file.exists()) {
//            file.getParentFile().mkdirs();
//            file.createNewFile();
//        }
//        FileOutputStream out;
//        try {
//            out = new FileOutputStream(file);
//            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
//                out.flush();
//                out.close();
//                //保存图片后发送广播通知更新数据库
//                 Uri uri = Uri.fromFile(file);
//                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
////                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
////                intent.setData(uri);
////                this.sendBroadcast(intent);
//                makeSnake(pageView, "保存成功", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void saveImageToGallery(Bitmap bmp) throws IOException{
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
        makeSnake(pageView, "保存成功", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
    }
}
