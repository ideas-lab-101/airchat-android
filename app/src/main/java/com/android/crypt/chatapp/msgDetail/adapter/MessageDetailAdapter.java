package com.android.crypt.chatapp.msgDetail.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.msgDetail.IMTools.HttpTextView;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Common.TimeStrings;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageBody;
import com.android.crypt.chatapp.widget.swipexlistview.RListView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;


import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;

/**
 * Created by White on 2019/4/23.
 */

public class MessageDetailAdapter extends BaseAdapter implements HttpTextView.URLClickListener {
    private Context context;
    private ArrayList <SendMessageBody> mListAll;
    private LayoutInflater layoutInflater;
    private ViewHolder viewHolder;
    private long lastTime = 0;  //用来计算显示不显示时间
    private SendMessageBody curBody;


    private ItemContentClickListener innerListener = null;
    public void setItemContentClickListener(ItemContentClickListener listener) {
        innerListener = listener;
    }

    public interface ItemContentClickListener {
        void innerItemClick(final int position);
        void longPressListener(final int position);
        void urlClickMethod(final String text);

    }


    public MessageDetailAdapter(Context context, ArrayList<SendMessageBody> mListAll) {
        this.context = context;
        this.mListAll = mListAll;
    }


    @Override
    public int getCount() {
        return mListAll.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAll.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        SendMessageBody mMap = mListAll.get(position);
        if (mMap.getMessageTag() == false){
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View leftView = null;
        View rightView = null;
        viewHolder = null;

        int currentType = getItemViewType(position);
        if (currentType == 1) {
            if (convertView == null) {
                leftView = LayoutInflater.from(context).inflate(R.layout.msg_item_left, null);
                viewHolder = new ViewHolder(leftView);
                leftView.setTag(viewHolder);
                convertView = leftView;
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
        } else if (currentType == 0) {
            if (convertView == null) {
                rightView = LayoutInflater.from(context).inflate(R.layout.msg_item_right, null);
                viewHolder = new ViewHolder(rightView);
                rightView.setTag(viewHolder);
                convertView = rightView;
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
        }
        SendMessageBody mMap = mListAll.get(position);
        this.curBody = mMap;
        freshViewContent(mMap, position);
        return convertView;
    }



    private void freshViewContent(SendMessageBody mMap, final int position) {
        // 1    *//文字
        // 2    *//默认表情
        // 3    *//照片信息
        // 4    *//语音消息
        // 5    *//表情包
        // 6    *//位置
        // 7    *//推荐联系人
        // 8    *//加密图片


        addListener(position);
        switch (mMap.getMsgType()) {
            case 1:
                freshTextView(mMap, position);
                break;
            case 2:
                freshDefaultImageView(mMap, position);
                break;
            case 3:
                freshPhotoView(mMap, position, true);
                break;
            case 4:
                freshVoiceImageView(mMap, position);
                break;
            case 5:
                freshPhotoView(mMap, position, false);
                break;
            case 6:
                freshPositionImageView(mMap, position);
                break;
            case 7:
                freshFriendCardImageView(mMap, position);
                break;
            case 8:
                freshEncodeImageView(mMap, position);
                break;
            case -1:
                freshBlankView();
                break;
            default:
                freshTipsView(mMap, position);
                break;
        }
    }

    private void addListener(final int position){
        viewHolder.imDefaultImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (innerListener != null) {
                    innerListener.innerItemClick(position);
                }
            }
        });

        viewHolder.imPhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (innerListener != null) {
                    innerListener.innerItemClick(position);
                }
            }
        });

        viewHolder.voiceBgVolorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (innerListener != null) {
                    innerListener.innerItemClick(position);
                }
            }
        });

        viewHolder.imPositionContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (innerListener != null) {
                    innerListener.innerItemClick(position);
                }
            }
        });


        viewHolder.imFriendContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (innerListener != null) {
                    innerListener.innerItemClick(position);
                }
            }
        });


        viewHolder.imEnPhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (innerListener != null) {
                    innerListener.innerItemClick(position);
                }
            }
        });

        viewHolder.allConBg.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });

        viewHolder.imTextContent.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });

        viewHolder.imDefaultImage.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });

        viewHolder.imPhotoImage.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });

        viewHolder.voiceBgVolorView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });

        viewHolder.imPositionContent.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });


        viewHolder.imFriendContent.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });


        viewHolder.imEnPhotoImage.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if (innerListener != null) {
                    innerListener.longPressListener(position);
                }
                return true;
            }
        });
    }

    //****最后的空白行，用于键盘抬起时候确认 item 位置
    private void freshBlankView (){
        viewHolder.imTextContent.setVisibility(View.GONE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);
        viewHolder.bottomBg.setVisibility(View.GONE);
        viewHolder.gapLine.setVisibility(View.GONE);

        viewHolder.imTime.setVisibility(View.GONE);
        viewHolder.imTimeBg.setVisibility(View.GONE);

        //****是否显示debug的空白表格
        if (RunningData.getInstance().IsApkInDebug()){
            viewHolder.debugShow.setVisibility(View.VISIBLE);
        }else{
            viewHolder.debugShow.setVisibility(View.GONE);
        }
    }


    private void freshTipsView(SendMessageBody mMap,int position){
        viewHolder.imTextContent.setVisibility(View.VISIBLE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }

        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);
            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);

        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);
            changeGaoline(true);
        }
        viewHolder.allConBg.setAlpha(1);
        viewHolder.imText.setText("当前版本不支持此消息");
        viewHolder.imText.setTextColor(0xffAC1F25);
        viewHolder.imText.setTextSize(TypedValue.COMPLEX_UNIT_SP, RunningData.getInstance().getCurTextSize());
    }



    private void freshTextView(SendMessageBody mMap, final int position){
        viewHolder.imTextContent.setVisibility(View.VISIBLE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }

        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);

            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);

        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);
            changeGaoline(true);
        }

        if (mMap.getIsSendSuccess() == false){
            viewHolder.allConBg.setAlpha((float)0.5);
        }else{
            viewHolder.allConBg.setAlpha(1);
        }

        viewHolder.imText.setTextColor(0xff0f0f0f);
        int textSize = RunningData.getInstance().getCurTextSize();
        if (mMap.getContent().length() <= 6){
            if (mMap.getContent().length() == 1){
                textSize = 35;
            }else if(bigTextSize(mMap.getContent()) == true){
                textSize = 35;
            }else if(isEmoji(mMap.getContent()) == true){
                textSize = 35;
            }
        }
        viewHolder.imText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        ViewGroup.LayoutParams lp;
        lp = viewHolder.imText.getLayoutParams();
        lp.width = WRAP_CONTENT;
        lp.height = WRAP_CONTENT;
        viewHolder.imText.setLayoutParams(lp);

        try{ //防止emoji崩溃
//            viewHolder.imText.setText(mMap.getContent());
            viewHolder.imText.setUrlText(mMap.getContent(), this);

        }catch (Exception e){
            viewHolder.imText.setText("内容含有无法解析的字符");
            Logger.d("文字渲染错误");
        }
    }

    private void freshDefaultImageView(SendMessageBody mMap, final int position){
        viewHolder.imTextContent.setVisibility(View.GONE);
        viewHolder.imDefaultImageBg.setVisibility(View.VISIBLE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }
        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);
            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);
        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);

            changeGaoline(true);
        }

        //  发送状态
        if (mMap.getIsSendSuccess() == false){
            viewHolder.allConBg.setAlpha((float)0.5);
        }else{
            viewHolder.allConBg.setAlpha(1);
        }

        String image = RunningData.getInstance().echoEmojiPicUrl() + mMap.getFileUrl();
        viewHolder.imDefaultImage.setImageDrawable(null);
        Glide.with(context)
                .load(image)
                .into(viewHolder.imDefaultImage);

    }

    private void freshPhotoView(final SendMessageBody mMap, final int position, boolean isPhoto){
        viewHolder.imTextContent.setVisibility(View.GONE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.VISIBLE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }

        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);
            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);
        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);
            changeGaoline(true);
        }

        //  发送状态
        if (mMap.getIsSendSuccess() == false){
            viewHolder.allConBg.setAlpha((float)0.5);
        }else{
            viewHolder.allConBg.setAlpha(1);
        }

        String excessInfo = mMap.getExcessInfo();
        try{
            String[] image_info = excessInfo.split("_");
            String widthStr = excessInfo.split("_")[0];
            String heightStr = excessInfo.split("_")[image_info.length - 1];

            int widthImage = string2int(widthStr);
            int heightImage = string2int(heightStr);

            double scale = 1.0;
            if (widthImage != 0){
                scale = (double)heightImage / (double)widthImage;
            }
            String image = RunningData.getInstance().echoIMPicUrl() + mMap.getFileUrl() + "?imageslim";
            if (isPhoto){
                int imageWidth = RunningData.getInstance().getScreenWidth() / 2;
                int imageHeight = (int)(imageWidth * scale);
                ViewGroup.LayoutParams lp;
                lp = viewHolder.imPhotoImage.getLayoutParams();
                lp.width = imageWidth;
                lp.height = imageHeight;
                viewHolder.imPhotoImage.setLayoutParams(lp);
                viewHolder.imPhotoImage.setBackground(new ColorDrawable(0xffffffff));
                if (widthImage > 300){
                    image = RunningData.getInstance().echoIMPicUrl() + mMap.getFileUrl() + "?imageView2/2/w/300";
                }
//                Logger.d("照片 = " + widthImage);
            }else{
                //表情包的image大小做一些调整
                int imageMinWidth = RunningData.getInstance().getScreenWidth() / 5;
                int imageMaxWidth = RunningData.getInstance().getScreenWidth() / 4;
                if (widthImage > imageMinWidth){
                    imageMinWidth =  imageMinWidth * (widthImage / 100);
                    if (imageMinWidth > imageMaxWidth){
                        imageMinWidth = imageMaxWidth;
                    }
                }

                ViewGroup.LayoutParams lp;
                lp = viewHolder.imPhotoImage.getLayoutParams();
                lp.width = imageMinWidth;
                lp.height = (int)(imageMinWidth * scale);
                viewHolder.imPhotoImage.setLayoutParams(lp);
                viewHolder.imPhotoImage.setBackground(new ColorDrawable(0x00ffffff));

                if (widthImage > 200){
                    image = RunningData.getInstance().echoIMPicUrl() + mMap.getFileUrl() + "?imageView2/2/w/200";
                }
            }

            viewHolder.imPhotoImage.setImageDrawable(null);
            Glide.with(context).load(image).into(viewHolder.imPhotoImage);
            showCacheImage(mMap.image_value);

        }catch (Exception e){
            Logger.d("info_arr3 解析错误");
        }
    }



    private void freshEncodeImageView(SendMessageBody mMap, final int position){
        viewHolder.imTextContent.setVisibility(View.GONE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.VISIBLE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }
        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);
            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);
        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);
            changeGaoline(true);
        }
        if (mMap.getIsSendSuccess() == false){
            viewHolder.allConBg.setAlpha((float)0.5);
        }else{
            viewHolder.allConBg.setAlpha(1);
        }
        try{
            String deKey = mMap.getExcessInfo();
            String allurl= mMap.getFileUrl();

            //本地路径 斜线替换成下划线  / to -
            String[] fileNames = allurl.replace("/", "_").split("imModelImages_");
            final String fileName = fileNames[fileNames.length - 1];
            final String imagePath =  RunningData.getInstance().getEncodeImageUrl();
            final String fileLocal = imagePath + fileName;
            if (fileIsExists(fileLocal)){
                showImage(viewHolder.imEnPhotoImage, fileLocal, deKey);
                return;
            }
        }catch (Exception e){
            Logger.d("info_arr encode 解析错误");
        }
    }


    private void freshFriendCardImageView(SendMessageBody mMap, final int position){
        viewHolder.imTextContent.setVisibility(View.GONE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.VISIBLE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }
        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);
            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);

        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);
            changeGaoline(true);
        }
        if (mMap.getIsSendSuccess() == false){
            viewHolder.allConBg.setAlpha((float)0.5);
        }else{
            viewHolder.allConBg.setAlpha(1);
        }

        String excessInfo = mMap.getExcessInfo();

        try{
            String[] info_arr = excessInfo.split("&");
            String label = info_arr[info_arr.length - 1].substring(6);
            viewHolder.imFriendDetail.setText(label);

            String headIcom = info_arr[0].substring(11);
//            Logger.d("info_arr[0].substring(11) = " + info_arr[0].substring(11));
            if (headIcom != null && !headIcom.equalsIgnoreCase("")) {
                String headIcon = RunningData.getInstance().echoMainPicUrl() + info_arr[0].substring(11) + "?imageView2/1/w/150/h/150";
                RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.default_head);
                Glide.with(context)
                        .load(headIcon)
                        .apply(requestOptions)
                        .into(viewHolder.imFriendHead);
            }else{
                viewHolder.imFriendHead.setImageDrawable(context.getResources().getDrawable(R.mipmap.default_head));
            }

        }catch (Exception e){
            Logger.d("info_arr 解析错误");

        }

    }

    private void freshPositionImageView(SendMessageBody mMap, final int position){
        viewHolder.imTextContent.setVisibility(View.GONE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.GONE);
        viewHolder.imPositionContent.setVisibility(View.VISIBLE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }
        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);
            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);

        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);
            changeGaoline(true);
        }
        if (mMap.getIsSendSuccess() == false){
            viewHolder.allConBg.setAlpha((float)0.5);
        }else{
            viewHolder.allConBg.setAlpha(1);
        }

        String excessInfo = mMap.getExcessInfo();

        try{
            String[] info_arr = excessInfo.split("&");
            String title = info_arr[0];
            String subTitle = info_arr[1];
            viewHolder.imPositionMain.setText(title);
            viewHolder.imPositionDetail.setText(subTitle);
        }catch (Exception e){
            Logger.d("info_arr2 解析错误");
        }
    }

    private void freshVoiceImageView(SendMessageBody mMap, final int position){
        viewHolder.imTextContent.setVisibility(View.GONE);
        viewHolder.imDefaultImageBg.setVisibility(View.GONE);
        viewHolder.imPhotoImageBg.setVisibility(View.GONE);
        viewHolder.imVoiceContent.setVisibility(View.VISIBLE);
        viewHolder.imPositionContent.setVisibility(View.GONE);
        viewHolder.imFriendContent.setVisibility(View.GONE);
        viewHolder.imEncodeImage.setVisibility(View.GONE);

        //*** 不是blank的都要显示
        viewHolder.bottomBg.setVisibility(View.VISIBLE);
        viewHolder.gapLine.setVisibility(View.VISIBLE);

        if (mMap.getMessageSecretType() == 1){
            viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_secret_shape));
        }else{
            if (mMap.getMessageTag() == false){
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_right_shape));
            }else{
                viewHolder.gapLine.setBackground(context.getResources().getDrawable(R.drawable.msg_left_shape));
            }
        }
        if (showTimeTextView(position) == true){
            //time
            viewHolder.imTime.setVisibility(View.VISIBLE);
            viewHolder.imTimeBg.setVisibility(View.VISIBLE);
            if (mMap.getMessageSendTime().length() <= 13){
                long time = Long.parseLong(mMap.getMessageSendTime());
                viewHolder.imTime.setText(TimeStrings.getNewChatTime(time));
            }else{
                viewHolder.imTime.setText("");
            }
            changeGaoline(false);

        }else{
            viewHolder.imTime.setVisibility(View.GONE);
            viewHolder.imTimeBg.setVisibility(View.GONE);
            changeGaoline(true);
        }
        if (mMap.getIsSendSuccess() == false){
            viewHolder.allConBg.setAlpha((float)0.5);
        }else{
            viewHolder.allConBg.setAlpha(1);
        }

        //***截取时间
        String fileInfo = mMap.getFileUrl();

        try{
            String[] info_arr = fileInfo.split("_");
            String timeLength = info_arr[info_arr.length - 2];
            viewHolder.voiceLength.setText(timeLength+ "s");

            int voiceWidth = string2int(timeLength);
            int bgLength = 120 + (int)((80 / 30.0) * voiceWidth);

            ViewGroup.LayoutParams lp;
            lp = viewHolder.voiceBgVolorView.getLayoutParams();
            lp.width =  DensityUtil.dip2px(context, bgLength);
            lp.height = DensityUtil.dip2px(context, 35);
            viewHolder.voiceBgVolorView.setLayoutParams(lp);

            if (!mMap.message_other_process_info.equals("")){
                viewHolder.imVoiceText.setVisibility(View.VISIBLE);
                viewHolder.imVoiceText.setText(mMap.message_other_process_info);
            }else{
                viewHolder.imVoiceText.setVisibility(View.GONE);
                viewHolder.imVoiceText.setText(mMap.message_other_process_info);
            }


        }catch (Exception e){
            Logger.d("info_arr voice 解析错误");
        }
    }



    public  void updateItem(RListView listView, SendMessageBody itemBody, int showStyle) {
        if (listView != null){
            int start = listView.getFirstVisiblePosition();
            int end = listView.getLastVisiblePosition() - 1; //末尾有一个空白行

            for (int k = start; k < end; k++){
                SendMessageBody mMap = mListAll.get(k);

                if (mMap.getMsgType() == 4){
                    View convertView = listView.getChildAt(k - start + 1);
                    ViewHolder viewHolderTemp = (ViewHolder) convertView.getTag();

                    viewHolderTemp.voidPlayingTips.setVisibility(View.GONE);
                    viewHolderTemp.voidLoadingTips.setVisibility(View.GONE);
                    String msgId = itemBody.getMessageIdClient();

                    if (msgId.equalsIgnoreCase(mMap.getMessageIdClient())){
                        viewHolder = viewHolderTemp;
                    }
                }
            }

            if (viewHolder != null){
                if (showStyle == 0){
                    viewHolder.voidPlayingTips.setVisibility(View.GONE);
                    viewHolder.voidLoadingTips.setVisibility(View.GONE);
                }else if(showStyle == 1){  //加载中
                    viewHolder.voidPlayingTips.setVisibility(View.GONE);
                    viewHolder.voidLoadingTips.setVisibility(View.VISIBLE);
                }else if(showStyle == 2){   //播放中
                    viewHolder.voidPlayingTips.setVisibility(View.VISIBLE);
                    viewHolder.voidLoadingTips.setVisibility(View.GONE);
                }
            }
        }

    }


    public  void updateVoiceItem(RListView listView, String msgId, String value) {
        if (listView != null){
            int start = listView.getFirstVisiblePosition();
            int end = listView.getLastVisiblePosition() - 1; //末尾有一个空白行
            for (int k = start; k < end; k++){
                SendMessageBody mMap = mListAll.get(k);
                if (mMap.getMsgType() == 4){
                    View convertView = listView.getChildAt(k - start + 1);
                    ViewHolder viewHolderTemp = (ViewHolder) convertView.getTag();

                    if (msgId.equalsIgnoreCase(mMap.getMessageIdClient())){
                        viewHolder = viewHolderTemp;
                    }
                }
            }
            viewHolder.imVoiceText.setVisibility(View.VISIBLE);
            viewHolder.imVoiceText.setText(value);
        }

    }


    static class ViewHolder {
        @BindView(R.id.all_con_bg)
        LinearLayout allConBg;
        @BindView(R.id.debug_show)
        TextView debugShow;
        @BindView(R.id.gap_line)
        View gapLine;
        @BindView(R.id.im_time)
        TextView imTime;
        @BindView(R.id.im_text)
        HttpTextView imText;
        @BindView(R.id.im_text_content)
        LinearLayout imTextContent;
        @BindView(R.id.im_default_image_bg)
        LinearLayout imDefaultImageBg;
        @BindView(R.id.im_default_image)
        ImageView imDefaultImage;
        @BindView(R.id.im_friend_head)
        ImageView imFriendHead;
        @BindView(R.id.im_photo_image_bg)
        LinearLayout imPhotoImageBg;
        @BindView(R.id.im_photo_image)
        ImageView imPhotoImage;
        @BindView(R.id.default_image_tips)
        AVLoadingIndicatorView defaultImageTips;
        @BindView(R.id.void_playing_tips)
        AVLoadingIndicatorView voidPlayingTips;
        @BindView(R.id.voice_length)
        TextView voiceLength;
        @BindView(R.id.voice_bg_color_view)
        RelativeLayout voiceBgVolorView;
        @BindView(R.id.void_loading_tips)
        AVLoadingIndicatorView voidLoadingTips;
        @BindView(R.id.im_voice_content)
        LinearLayout imVoiceContent;
        @BindView(R.id.im_position_main)
        TextView imPositionMain;
        @BindView(R.id.im_position_detail)
        TextView imPositionDetail;
        @BindView(R.id.im_voice_text)
        TextView imVoiceText;
        @BindView(R.id.im_position_content)
        LinearLayout imPositionContent;
        @BindView(R.id.im_friend_detail)
        TextView imFriendDetail;
        @BindView(R.id.im_friend_content)
        LinearLayout imFriendContent;
        @BindView(R.id.bottom_bg)
        LinearLayout bottomBg;
        @BindView(R.id.im_time_bg)
        LinearLayout imTimeBg;

        @BindView(R.id.im_encode_image)
        LinearLayout imEncodeImage;
        @BindView(R.id.im_en_photo_image)
        ImageView imEnPhotoImage;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private boolean showTimeTextView(int position){
        SendMessageBody mMap = mListAll.get(position);
        long curtime = Long.parseLong(mMap.getMessageSendTime());

        if (position == 0 || position == mListAll.size() - 2){
            lastTime = curtime;
            return true;
        }

        if ((curtime - lastTime) > 90000){
            lastTime = curtime;
            return true;
        }

        lastTime = curtime;
        return false;
    }

    private int string2int(String value){
        if (value == null || value.equals("")){
            return 0;
        }
        int result = 0;
        try{
            result = Float.valueOf(value).intValue();
        }catch (Exception e) {}
        return result;
    }

    private boolean bigTextSize(String substring){
        if (substring.endsWith("!") || substring.endsWith("?") || substring.endsWith("！") || substring.endsWith("？")){
            return  true;
        }

        if (substring.toLowerCase().equals("ok")||  substring.toLowerCase().equals("yes")|| substring.toLowerCase().equals("no")|| substring.toLowerCase().equals("hi")|| substring.toLowerCase().equals("ojbk")|| substring.toLowerCase().equals("hell0")|| substring.toLowerCase().equals("what")){
            return true;
        }
        return false;

    }

    private boolean isEmoji(String source) {
        int len = source.length();
        if (len != 2){
            return false;
        }
        try{
            for (int i = 0; i < len; i++) {
                if (isEmojiCharacter(source.charAt(i))) {
                    return true;
                }
            }
        }catch(Exception e){
            Logger.d("isEmoji  错误");
        }
        return false;
    }
    private boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));
    }




    private void changeGaoline(boolean hide){


    }

    //*****加密图
    private void showImage(final ImageView photo, final String imagePath, final String deKey){
        if (fileIsExists(imagePath)){
            new Thread(){
                @Override
                public void run() {
                    try{
                        byte[] data = getContent(imagePath);
                        if (data != null){
                            CryTool tool = new CryTool();
                            byte[] date_de = tool.aesImageDeWith(data, deKey);
                            if (date_de != null){

                            }
                        }
                    }catch (Exception e){
                    }
                }
            }.start();
        }
    }


    private byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        fi.close();
        return buffer;
    }


    public boolean fileIsExists(String strFile) {
        try {
            File f=new File(strFile);
            if(!f.exists()) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    private void showCacheImage(final String image_value){
        try{
            Timer check = new Timer();
            check.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (viewHolder != null){
                        if (viewHolder.imPhotoImage != null) {
                            if (viewHolder.imPhotoImage.getDrawable() == null){
                                //***先加载本地图片
                                if (image_value != null){
                                    final File file = new File(image_value);
                                    if (file.exists()){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Glide.with(context).load(image_value).into(viewHolder.imPhotoImage);
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }

                }
            },1000);
        }catch (Exception e){}

    }

    @Override
    public void urlItemClick(String text) {
        if (innerListener != null) {
            innerListener.urlClickMethod(text);
        }
    }
}