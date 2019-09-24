package com.android.crypt.chatapp.msgDetail.IMTools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.widget.swipexlistview.RListView;
import com.android.crypt.chatapp.widget.swipexlistview.XListView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.msgDetail.Interface.ChatUiCallback;
import com.android.crypt.chatapp.msgDetail.adapter.EmojiAdapter;

import java.io.File;
import java.util.ArrayList;


public class ChatUiHelper implements AdapterView.OnItemClickListener, SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
    private static final String SHARE_PREFERENCE_NAME = "com.chat.ui";
    private static final String SHARE_PREFERENCE_TAG = "soft_input_height";
    private Activity mActivity;
    private LinearLayout mContentLayout;//整体界面布局
    private RelativeLayout mBottomLayout;//底部布局
    private Button mSendBtn;//发送按钮
    private View mAddButton;//加号按钮
    private Button mAudioButton;//录音按钮
    private ImageView mAudioIv;//录音图片
    private ChatUiCallback callback;
    private LinearLayout mEmojiLayout;//表情布局
    private GridView emojiContent;;//表情布局

    private EditText mEditText;
    private InputMethodManager mInputManager;
    private SharedPreferences mSp;
    private ImageView mIvEmoji;
    private RListView messageList;
    private ImageView bgImageView;
    private int emoji_kind = -1;
    private SoftKeyBoardListener kbListener;
    private boolean isShowEmoji = false;
    private boolean iskbShow = false;
    public ChatUiHelper() {

    }

    public static ChatUiHelper with(Activity activity) {
        ChatUiHelper mChatUiHelper = new ChatUiHelper();

        mChatUiHelper.mActivity = activity;
        mChatUiHelper.mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mChatUiHelper.mSp = activity.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);

        mChatUiHelper.kbListener.setListener(activity.getWindow().getDecorView(), mChatUiHelper);
        return mChatUiHelper;
    }

    public static final int EVERY_PAGE_SIZE = 21;

    private ArrayList<String> listItems;
    public  ChatUiHelper bindEmojiData(int btnIndex){
        emoji_kind = 0;
        if (btnIndex == 0){
            listItems = getDefault();
            emojiContent.setNumColumns(5);
        }else if(btnIndex == 1){
            listItems = getDog();
            emojiContent.setNumColumns(4);
        }else if(btnIndex == 2){
            listItems = getCat();
            emojiContent.setNumColumns(4);
        }else if(btnIndex == 3){
            listItems = getCollectEmeoji();
            emojiContent.setNumColumns(4);
            emoji_kind = 1;
        }
        EmojiAdapter emojiAdapter = new EmojiAdapter(mActivity, listItems, emoji_kind);
        emojiContent.setAdapter(emojiAdapter);
        return this;
    }

    private ArrayList<String> getDefault(){
        ArrayList<String> emojiBeanList = new ArrayList<String>(){{
            add("qxDefaultEmoji/qxFace_1.png");
            add("qxDefaultEmoji/qxFace_2.png");
            add("qxDefaultEmoji/qxFace_3.png");
            add("qxDefaultEmoji/qxFace_4.png");
            add("qxDefaultEmoji/qxFace_5.png");
            add("qxDefaultEmoji/qxFace_6.png");
            add("qxDefaultEmoji/qxFace_7.png");
            add("qxDefaultEmoji/qxFace_8.png");
            add("qxDefaultEmoji/qxFace_9.png");
            add("qxDefaultEmoji/qxFace_10.png");
            add("qxDefaultEmoji/qxFace_11.png");
            add("qxDefaultEmoji/qxFace_12.png");
            add("qxDefaultEmoji/qxFace_13.png");
            add("qxDefaultEmoji/qxFace_14.png");
            add("qxDefaultEmoji/qxFace_15.png");
            add("qxDefaultEmoji/qxFace_16.png");
            add("qxDefaultEmoji/qxFace_17.png");
            add("qxDefaultEmoji/qxFace_18.png");
            add("qxDefaultEmoji/qxFace_19.png");
            add("qxDefaultEmoji/qxFace_20.png");
            add("qxDefaultEmoji/qxFace_21.png");
            add("qxDefaultEmoji/qxFace_22.png");
            add("qxDefaultEmoji/qxFace_23.png");
            add("qxDefaultEmoji/qxFace_24.png");
            add("qxDefaultEmoji/qxFace_25.png");
            add("qxDefaultEmoji/qxFace_26.png");
            add("qxDefaultEmoji/qxFace_27.png");
            add("qxDefaultEmoji/qxFace_28.png");
            add("qxDefaultEmoji/qxFace_29.png");
            add("qxDefaultEmoji/qxFace_30.png");
            add("qxDefaultEmoji/qxFace_31.png");
            add("qxDefaultEmoji/qxFace_32.png");
            add("qxDefaultEmoji/qxFace_33.png");
            add("qxDefaultEmoji/qxFace_34.png");
            add("qxDefaultEmoji/qxFace_35.png");
            add("qxDefaultEmoji/qxFace_36.png");
            add("qxDefaultEmoji/qxFace_37.png");
            add("qxDefaultEmoji/qxFace_38.png");
            add("qxDefaultEmoji/qxFace_39.png");
            add("qxDefaultEmoji/qxFace_40.png");}};
        return emojiBeanList;
    }

    private ArrayList<String> getDog(){
        ArrayList<String> emojiBeanList = new ArrayList<String>(){{
            add("qxDefaultEmoji/qxDog_1.png");
            add("qxDefaultEmoji/qxDog_2.png");
            add("qxDefaultEmoji/qxDog_3.png");
            add("qxDefaultEmoji/qxDog_4.png");
            add("qxDefaultEmoji/qxDog_5.png");
            add("qxDefaultEmoji/qxDog_6.png");
            add("qxDefaultEmoji/qxDog_7.png");
            add("qxDefaultEmoji/qxDog_8.png");
            add("qxDefaultEmoji/qxDog_9.png");
            add("qxDefaultEmoji/qxDog_10.png");
            add("qxDefaultEmoji/qxDog_11.png");
            add("qxDefaultEmoji/qxDog_12.png");
            add("qxDefaultEmoji/qxDog_13.png");
            add("qxDefaultEmoji/qxDog_14.png");
            add("qxDefaultEmoji/qxDog_15.png");
            add("qxDefaultEmoji/qxDog_16.png");
        }};
        return emojiBeanList;
    }

    private ArrayList<String> getCat(){
        ArrayList<String> emojiBeanList = new ArrayList<String>(){{
            add("qxDefaultEmoji/qxCat_1.png");
            add("qxDefaultEmoji/qxCat_2.png");
            add("qxDefaultEmoji/qxCat_3.png");
            add("qxDefaultEmoji/qxCat_4.png");
            add("qxDefaultEmoji/qxCat_5.png");
            add("qxDefaultEmoji/qxCat_6.png");
            add("qxDefaultEmoji/qxCat_7.png");
            add("qxDefaultEmoji/qxCat_8.png");
            add("qxDefaultEmoji/qxCat_9.png");
            add("qxDefaultEmoji/qxCat_10.png");
            add("qxDefaultEmoji/qxCat_11.png");
            add("qxDefaultEmoji/qxCat_12.png");
            add("qxDefaultEmoji/qxCat_13.png");
            add("qxDefaultEmoji/qxCat_14.png");
            add("qxDefaultEmoji/qxCat_15.png");
            add("qxDefaultEmoji/qxCat_16.png");
        }};
        return emojiBeanList;
    }

    private ArrayList<String> getCollectEmeoji(){
        ArrayList<String> emojiBeanList = new ArrayList<String>();
        emojiBeanList.add("");

        try{
            String speBgImage = RunningData.getInstance().getCollectImage();
            //查找特定的图
            File fileRoot = new File(speBgImage);
            if (fileRoot.exists()){
                File[] files = fileRoot.listFiles();
                for (int i = 0; i < files.length; i++){
                    File file = files[i];
                    String imageName = file.getName();
                    long outTime = string2int(imageName.split("\\.")[0]);
                    if (emojiBeanList.size() <= 1){
                        emojiBeanList.add(speBgImage + imageName);
                    }else{
                        int insert_index = emojiBeanList.size() - 1;
                        for(int j = 1; j < emojiBeanList.size(); j++){
                            String[] names = emojiBeanList.get(j).split("\\/");
                            String name = names[names.length - 1];
                            long innerTime = string2int(name.split("\\.")[0]);
                            if (outTime >= innerTime){
                                insert_index = j;
                                break;
                            }
                        }
                        emojiBeanList.add(insert_index, speBgImage + imageName);
                    }
                }
            }
        }catch (Exception e){}

        return emojiBeanList;
    }
    //绑定整体界面布局
    public ChatUiHelper  bindContentLayout(LinearLayout bottomLayout) {
        mContentLayout = bottomLayout;
        return this;
    }

    public ChatUiHelper  bindMessageList(RListView mesList) {
        messageList = mesList;
        return this;
    }

    public ChatUiHelper  bindBgimage(ImageView imgView) {
        bgImageView = imgView;
        return this;
    }


    //绑定输入框
    public ChatUiHelper  bindEditText(EditText editText) {
        mEditText = editText;
        mEditText.requestFocus();
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && mBottomLayout.isShown()) {
                    hideBottomLayout();//隐藏表情布局，显示软件盘
                    mIvEmoji.setImageResource(R.mipmap.emojikb);
                }
                return false;
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mEditText.getText().toString().trim().length() > 0) {
                    mSendBtn.setVisibility(View.VISIBLE);
                    mAddButton.setVisibility(View.GONE);
                 } else {
                    mSendBtn.setVisibility(View.GONE);
                    mAddButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return this;
    }

    //绑定底部布局
    public ChatUiHelper  bindEmojiContent(GridView emoji) {
        int kind = RunningData.getInstance().keyKind;
        emojiContent = emoji;
        bindEmojiData(kind);
        emojiContent.setOnItemClickListener(this);
        return this;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (emoji_kind == 0){
            if (position < listItems.size()){
                String imageString = listItems.get(position);
                if (callback != null){
                    callback.sendDefaultEmoji(imageString);
                }
            }
        }else if(emoji_kind == 1){ // 收藏表情
            if (position == 0){
                if (callback != null){
                    callback.addCollectEmji();
                }
            }else{
                String imageString = listItems.get(position);
                if (callback != null){
                    callback.sendCollectEmji(imageString);
                }
            }

        }

    }

    //绑定底部布局
    public ChatUiHelper  bindBottomLayout(RelativeLayout bottomLayout) {
        mBottomLayout = bottomLayout;
        return this;
    }

    public ChatUiHelper bindCallback(ChatUiCallback callb){
        callback = callb;
        return this;
    }


    //绑定表情布局
    public ChatUiHelper  bindEmojiLayout(LinearLayout emojiLayout) {
        mEmojiLayout = emojiLayout;
        return this;
    }


    //绑定发送按钮
    public ChatUiHelper  bindttToSendButton(Button sendbtn) {
        mSendBtn=sendbtn;
        return this;
    }



    //绑定语音按钮点击事件
    public ChatUiHelper  bindAudioBtn(RecordButton audioBtn) {
        mAudioButton=audioBtn;
         return this;
    }

    //绑定语音图片点击事件
    public ChatUiHelper  bindAudioIv(ImageView audioIv) {
        mAudioIv=audioIv;
        audioIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //如果录音按钮显示
                if (mAudioButton.isShown()) {
                    hideAudioButton();
                    mEditText.requestFocus();
                    showSoftInput();
                } else {
                    mEditText.clearFocus();
                    showAudioButton();
                }
            }
        });

        return this;
    }

    private void hideAudioButton() {
        mAudioButton.setVisibility(View.GONE);
        mEditText.setVisibility(View.VISIBLE);
        mAudioIv.setImageResource(R.mipmap.voicebtn);
    }



    private void showAudioButton() {
        mAudioButton.setVisibility(View.VISIBLE);
        mEditText.setVisibility(View.GONE);
        mAudioIv.setImageResource(R.mipmap.ic_keyboard);
        if (mBottomLayout.isShown()) {
               hideBottomLayout();
        } else {
            isShowEmoji = false;
            hideSoftInput();
        }
    }

    //绑定表情按钮点击事件
    public ChatUiHelper bindToEmojiButton(ImageView emojiBtn){
        mIvEmoji=emojiBtn;
        emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.clearFocus();
                if (!mBottomLayout.isShown()) {
                    if (isSoftInputShown()){
                        hideAudioButton();
                        isShowEmoji = true;
                        hideSoftInput();
                    }else{
                        showBottomLayout();
                    }
                } else {
                    hideBottomLayout();//隐藏表情布局，显示软件盘
                    showSoftInput();
                    mIvEmoji.setImageResource(R.mipmap.emojikb);
                }
            }
        });
        return this;
    }

   //绑定底部加号按钮
    public ChatUiHelper bindToAddButton(View addButton) {
       mAddButton = addButton;
       addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSoftInputShown()) {//同上
                    isShowEmoji = false;
                    hideSoftInput();
                }
                hideAudioButton();
                hideBottomLayout();//隐藏表情布局，显示软件盘
                if(callback != null){
                    callback.showActionsheets();
                }
            }
        });
        return this;
    }



    /**
     * 隐藏底部布局
     *
     * @param showSoftInput 是否显示软件盘
     */
    public void hideBottomLayout() {
        mIvEmoji.setImageResource(R.mipmap.emojikb);
        mBottomLayout.setVisibility(View.GONE);
    }

    private void showBottomLayout() {
        mBottomLayout.setVisibility(View.VISIBLE);
        mIvEmoji.setImageResource(R.mipmap.ic_keyboard);
        listViewToBottom();
    }

    /**
     * 是否显示软件盘
     *
     * @return
     */
    public boolean isSoftInputShown() {
        return iskbShow;
//        return getSupportSoftInputHeight() != 0;
    }

    public int dip2Px(int dip) {
        float density = mActivity.getApplicationContext().getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + 0.5f);
        return px;
    }

    /**
     * 隐藏软件盘
     */
    public void hideSoftInput() {
        mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }


    /**
     * 获取软件盘的高度
     *
     * @return
     */
    private int getSupportSoftInputHeight() {
        Rect r = new Rect();
        /**
         * decorView是window中的最顶层view，可以从window中通过getDecorView获取到decorView。
         * 通过decorView获取到程序显示的区域，包括标题栏，但不包括状态栏。
         */
        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        //获取屏幕的高度
        int screenHeight = mActivity.getWindow().getDecorView().getRootView().getHeight();
        //计算软件盘的高度
        int softInputHeight = screenHeight - r.bottom;
        /**
         * 某些Android版本下，没有显示软键盘时减出来的高度总是144，而不是零，
         * 这是因为高度是包括了虚拟按键栏的(例如华为系列)，所以在API Level高于20时，
         * 我们需要减去底部虚拟按键栏的高度（如果有的话）
         */
        if (Build.VERSION.SDK_INT >= 20) {
            // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
            softInputHeight = softInputHeight - getSoftButtonsBarHeight();
        }
        if (softInputHeight < 0) {
         }
        //存一份到本地
        if (softInputHeight > 0) {
            mSp.edit().putInt(SHARE_PREFERENCE_TAG, softInputHeight).apply();
        }
        return softInputHeight;
    }


    public void showSoftInput() {
        mEditText.requestFocus();
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                mInputManager.showSoftInput(mEditText, 0);
            }
        });
    }

    /**
     * 锁定内容高度，防止跳闪
     */
    private void lockContentHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentLayout.getLayoutParams();
         params.height = mContentLayout.getHeight();
        params.weight = 0.0F;
    }

    /**
     * 释放被锁定的内容高度
     */
    public void unlockContentHeightDelayed() {
        mEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((LinearLayout.LayoutParams) mContentLayout.getLayoutParams()).weight = 1.0F;
            }
        }, 200);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    public void listViewToBottom(){
        messageList.setSelection(messageList.getBottom());
    }

    public void addBgImage(final String account){
        try{
            bgImageView.setImageURI(null);
            String speBgImage = RunningData.getInstance().getBgImageUrl();
            String imageUrl = "";
            //查找特定的图
            File fileRoot = new File(speBgImage);

            if (fileRoot.exists()){
                File[] files = fileRoot.listFiles();
                for (int i = 0; i < files.length; i++){
                    File file = files[i];
                    String imageName = file.getName().split("\\.")[0];
                    if (imageName.equals("global_bg_image")){
                        imageUrl = file.getName();
                    }else if(imageName.equals(account)){
                        imageUrl = file.getName();
                        break;
                    }
                }
                if (!imageUrl.equals("")){
                    String fileBgImage = speBgImage + imageUrl;
                    RequestOptions requestOptions = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
                    Glide.with(mActivity).load(fileBgImage).apply(requestOptions).into(bgImageView);
                }
            }
        }catch (Exception e){

        }
    }

    public void addEmoji(String imageUrl){
        try{
            String baseUrl = RunningData.getInstance().echoIMPicUrl();
            Glide.with(mActivity).downloadOnly().load(baseUrl + imageUrl).into(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(@NonNull File file, @Nullable Transition<? super File> transition) {
                            String filePath = file.getPath();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(filePath, options);

                            //outMimeType是以--”image/png”、”image/jpeg”、”image/gif”…….这样的方式返回的
                            String mimeType = options.outMimeType;
                            String surfix = mimeType.split("\\/")[1];

                            String mFile = RunningData.getInstance().getCollectImage();
                            long time = System.currentTimeMillis() + 1;
                            String savefile = mFile + time  + "." + surfix;
                            file.renameTo(new File(savefile));

                            bindEmojiData(3);
                        }
                    });


        }catch (Exception e){}

    }

    @Override
    public void keyBoardShow(int height) {
        iskbShow = true;
    }

    @Override
    public void keyBoardHide(int height) {
        iskbShow = false;
        if (isShowEmoji == true){
            showBottomLayout();
        }
    }

    private long string2int(String value){
        long result = 0;
        try{
            result = Float.valueOf(value).longValue();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
