package com.android.crypt.chatapp.PhotoViewer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.PhotoViewer.Model.ImageModel;
import com.android.crypt.chatapp.PhotoViewer.adaptor.PhotoPagerAdapter;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.baoyz.actionsheet.ActionSheet;

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
import uk.co.senab.photoview.PhotoView;

import static com.android.crypt.chatapp.utility.Common.ParameterUtil.imagePath;


public class PhotoViewerActivity extends BaseActivity implements ActionSheet.ActionSheetListener, View.OnClickListener, PhotoPagerAdapter.AddLongTapCallback {



    private LayoutInflater inflater;
    private PhotoView longPressImage = null;
    private PhotoPagerAdapter adapter;
    private List<MyDragPhotoView> viewList = null;


    int mOriginLeft;
    int mOriginTop;
    int mOriginHeight;
    int mOriginWidth;
    int mOriginCenterX;
    int mOriginCenterY;
    private float mTargetHeight;
    private float mTargetWidth;
    private float mScaleX;
    private float mScaleY;
    private float mTranslationX;
    private float mTranslationY;


    @BindView(R.id.quit_photo_viewer)
    ImageButton quitPhotoViewer;
    @BindView(R.id.three_dot)
    ImageButton threeDot;

    @BindView(R.id.page_view)
    ViewPager pageView;
    @BindView(R.id.all_bg_view)
    ConstraintLayout allBgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //禁止截屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_contain);
        ButterKnife.bind(this);
        inflater = LayoutInflater.from(this);
        initView();
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black_text));
        }

        Intent intent = getIntent();//获取相关的intent
        quitPhotoViewer.setBackground(getResources().getDrawable(R.mipmap.navi_quit_w));
        try {
            ArrayList<ImageModel> image_url_list = (ArrayList) intent.getSerializableExtra("image_url");
            if (viewList == null) {
                viewList = new ArrayList<MyDragPhotoView>();
            }
            if (image_url_list != null) {
                for (int i = 0; i < image_url_list.size(); i++) {
                    ConstraintLayout view = (ConstraintLayout) inflater.inflate(R.layout.activity_photo_viewer, null);
                    MyDragPhotoView photo = (MyDragPhotoView) view.findViewById(R.id.big_image_viewer);
                    viewList.add(photo);
                }
                adapter = new PhotoPagerAdapter(image_url_list, this, this);
                pageView.setAdapter(adapter);
                try {
                    int index = (int) intent.getSerializableExtra("cur_index");
                    pageView.setCurrentItem(index);

                } catch (Exception e) {
                    Logger.d("index 缺失");
                }
            }


            pageView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            pageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            mOriginLeft = RunningData.getInstance().getScreenWidth() / 8 * 3;//getIntent().getIntExtra("left", 0);
                            mOriginTop = RunningData.getInstance().getScreenHeight() /  8 * 3;//getIntent().getIntExtra("top", 0);
                            mOriginHeight = RunningData.getInstance().getScreenHeight() / 4;//getIntent().getIntExtra("height", 0);
                            mOriginWidth = RunningData.getInstance().getScreenWidth() / 4;//getIntent().getIntExtra("width", 0);
                            mOriginCenterX = mOriginLeft + mOriginWidth / 2;
                            mOriginCenterY = mOriginTop + mOriginHeight / 2;

                            int[] location = new int[2];

                            final MyDragPhotoView photoView = (MyDragPhotoView) viewList.get(0);
                            photoView.getLocationOnScreen(location);

                            mTargetHeight = (float) photoView.getHeight();
                            mTargetWidth = (float) photoView.getWidth();

                            if (mTargetHeight <= 10){
                                mTargetHeight = RunningData.getInstance().getScreenHeight();
                            }

                            if (mTargetWidth <= 10){
                                mTargetWidth = RunningData.getInstance().getScreenWidth();
                            }

                            mScaleX = (float) mOriginWidth / mTargetWidth;
                            mScaleY = (float) mOriginHeight / mTargetHeight;

                            float targetCenterX = location[0] + mTargetWidth / 2;
                            float targetCenterY = location[1] + mTargetHeight / 2;

                            mTranslationX = mOriginCenterX - targetCenterX;
                            mTranslationY = mOriginCenterY - targetCenterY;
                            photoView.setTranslationX(mTranslationX);
                            photoView.setTranslationY(mTranslationY);

                            photoView.setScaleX(mScaleX);
                            photoView.setScaleY(mScaleY);

                            performEnterAnimation();

                            for (int i = 0; i < viewList.size(); i++) {
                                MyDragPhotoView photo = (MyDragPhotoView) viewList.get(0);
                                photo.setMinScale(mScaleX);
                            }
                        }
                    });

        } catch (Exception e) {
            Logger.d("url参数错误");
        }
        quitPhotoViewer.setOnClickListener(this);
        threeDot.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewList.clear();
    }


    @Override
    public void addLongTapMethod(MyDragPhotoView photo, int position) {
        if (photo != null){
            photo.setOnExitListener(new MyDragPhotoView.OnExitListener() {
                @Override
                public void onExit(MyDragPhotoView view, float x, float y, float w, float h) {
                    performExitAnimation(view, x, y, w, h);
                }
            });
            photo.setOnTapListener(new MyDragPhotoView.OnTapListener() {
                @Override
                public void onTap(MyDragPhotoView view) {
                    finish();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    overridePendingTransition(0, 0);
                }
            });
            viewList.set(position, photo);
        }
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
                            if (image_map != null){
                                saveImageToGallery(image_map);
                            }else {
                                makeSnake(pageView, "保存失败", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                            }
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



    @Override
    public void onClick(View v) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        switch (v.getId()){
            case R.id.quit_photo_viewer:
                finish();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                overridePendingTransition(0, 0);
                break;
            case R.id.three_dot:
                longPressImage = viewList.get(pageView.getCurrentItem());
                ActionSheet.createBuilder(PhotoViewerActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle("取消")
                        .setOtherButtonTitles("保存图片")
                        .setCancelableOnTouchOutside(true)
                        .setListener(PhotoViewerActivity.this).show();
                break;
        }
    }

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



    private void performExitAnimation(final MyDragPhotoView view, float x, float y, float w, float h) {
        view.finishAnimationCallBack();
        float viewX = mTargetWidth / 2 + x - mTargetWidth * mScaleX / 2;
        float viewY = mTargetHeight / 2 + y - mTargetHeight * mScaleY / 2;
        view.setX(viewX);
        view.setY(viewY);

        float centerX = view.getX() + mOriginWidth / 2;
        float centerY = view.getY() + mOriginHeight / 2;

        float translateX = mOriginCenterX - centerX;
        float translateY = mOriginCenterY - centerY;


        ValueAnimator translateXAnimator = ValueAnimator.ofFloat(view.getX(), view.getX() + translateX);
        translateXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setX((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateXAnimator.setDuration(100);
        translateXAnimator.start();
        ValueAnimator translateYAnimator = ValueAnimator.ofFloat(view.getY(), view.getY() + translateY);
        translateYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                view.setY((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateYAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animator.removeAllListeners();
                finish();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        translateYAnimator.setDuration(100);
        translateYAnimator.start();
    }

    private void performEnterAnimation() {
        final MyDragPhotoView photoView = (MyDragPhotoView) viewList.get(0);
//        final DragPhotoView photoView = mPhotoViews[0];
        ValueAnimator translateXAnimator = ValueAnimator.ofFloat(photoView.getX(), 0);
        translateXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                photoView.setX((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateXAnimator.setDuration(100);
        translateXAnimator.start();

        ValueAnimator translateYAnimator = ValueAnimator.ofFloat(photoView.getY(), 0);
        translateYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                photoView.setY((Float) valueAnimator.getAnimatedValue());
            }
        });
        translateYAnimator.setDuration(100);
        translateYAnimator.start();

        ValueAnimator scaleYAnimator = ValueAnimator.ofFloat(mScaleY, 1);
        scaleYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                photoView.setScaleY((Float) valueAnimator.getAnimatedValue());
            }
        });
        scaleYAnimator.setDuration(100);
        scaleYAnimator.start();

        ValueAnimator scaleXAnimator = ValueAnimator.ofFloat(mScaleX, 1);
        scaleXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                photoView.setScaleX((Float) valueAnimator.getAnimatedValue());
            }
        });
        scaleXAnimator.setDuration(100);
        scaleXAnimator.start();
    }

}
