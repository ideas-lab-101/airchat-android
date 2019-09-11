package com.android.crypt.chatapp.msgDetail;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.InfoSetting.ChangeFriendLabelActivity;
import com.android.crypt.chatapp.Map.MapShowActivity;
import com.android.crypt.chatapp.PhotoViewer.Model.ImageModel;
import com.android.crypt.chatapp.finder.SearchResultActivity;
import com.android.crypt.chatapp.msgDetail.IMTools.ChatUiHelper;
import com.android.crypt.chatapp.msgDetail.IMTools.ChoosePhotoActivity;
import com.android.crypt.chatapp.msgDetail.IMTools.StateButton;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.Websocket.MessageCallbacks;
import com.android.crypt.chatapp.utility.Websocket.Model.MessageSendState;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveRecalledInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.SendIsTypingBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageEnBody;
import com.android.crypt.chatapp.utility.Websocket.WsChatService;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.android.crypt.chatapp.widget.swipexlistview.RListView;
import com.baoyz.actionsheet.ActionSheet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.Map.MapMainActivity;
import com.android.crypt.chatapp.Map.model.PositionModel;
import com.android.crypt.chatapp.PhotoViewer.PhotoViewerActivity;
import com.android.crypt.chatapp.WebView.WebActivity;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.msgDetail.IMTools.AddEmojiActivity;
import com.android.crypt.chatapp.msgDetail.IMTools.DownLoadVoice;
import com.android.crypt.chatapp.msgDetail.IMTools.MsgBgView;
import com.android.crypt.chatapp.msgDetail.IMTools.RecordButton;
import com.android.crypt.chatapp.msgDetail.Interface.ChatUiCallback;
import com.android.crypt.chatapp.msgDetail.adapter.MessageDetailAdapter;
import com.android.crypt.chatapp.user.FriendCardActivity;
import com.android.crypt.chatapp.user.Model.UserInfo;
import com.android.crypt.chatapp.utility.Websocket.Link.WsStatus;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveSpecialInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiverIsTypingBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageBody;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.qiniu.android.common.AutoZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.android.crypt.chatapp.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.devio.takephoto.model.TImage;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MsgDetailActivity extends BaseActivity implements View.OnClickListener, MessageDetailAdapter.ItemContentClickListener, RListView.IRListViewListener, ChatUiCallback, ActionSheet.ActionSheetListener, ServiceConnection, MessageCallbacks, DownLoadVoice.VoiceProcessCallback {
    @BindView(R.id.msg_bg_view)
    MsgBgView msgBgView;

    private ContactModel mMap;
    private String pubKey;
    private String messageCreator;
    private String messageReceiver;

    private ViewHolderInfo holder_info;
    private ViewHolderList holder_list;
    private ArrayList<SendMessageBody> mListContact;
    private ArrayList<String> secretMsgIdList = new ArrayList<>();

    private MessageDetailAdapter adapterFavor;
    private boolean onceMsg = false;
    private ChatUiHelper mUiHelper;
    private int _messageSecretType = 0;
    private String qiniuToken = null;
    private MediaPlayer mp = new MediaPlayer();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_msg_detail);
        ButterKnife.bind(this);
        initGlobalData();
        initView();
        bindWsService();
    }

    private void initGlobalData() {
        Intent intent = getIntent();
        this.mMap = (ContactModel) intent.getSerializableExtra("msgReceiver");
        this.pubKey = this.mMap.public_key;
        this.messageCreator = RunningData.getInstance().getCurrentAccount();
        this.messageReceiver = this.mMap.account;
        if (this.pubKey == null || this.messageCreator == null || this.messageReceiver == null || this.pubKey.equals("") || this.messageCreator.equals("") || this.messageReceiver.equals("")) {
            showAlert();
        }
        mListContact = new ArrayList<>();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        ConstraintLayout viewL = (ConstraintLayout) inflater.inflate(R.layout.activity_msg_detail_list, null);
        msgBgView.addPage(viewL);
        freshListView(viewL);
        ConstraintLayout viewR = (ConstraintLayout) inflater.inflate(R.layout.activity_msg_detail_info, null);
        msgBgView.addPage(viewR);
        freshInfoView(viewR);
        onLoadMore();
        initChatUi();
        freshPriKey();
    }

    @Override
    protected void onStop() {
        super.onStop();
        otherMsgTipsShow(false);
    }

    //***更新私钥
    private void freshPriKey(){
        Logger.d("login_name = " + this.messageReceiver);
        OkGo.<CodeResponse>get(RunningData.getInstance().server_url() + "contact/getUserPublicKey")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("login_name", this.messageReceiver)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if(response.body().data != null){
                            String cur_pubKey = response.body().data.toString();
                            pubKey = cur_pubKey;
                            Logger.d("cur_pubKey= " + cur_pubKey);
                        }
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {
                        Gson gson = new Gson();
                        Logger.d("2 response = " + gson.toJson(response.body()));
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("无法给 " + this.mMap.label + " 发消息")
                .setMessage("未获取必要的通讯数据")
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情
                        dialogInterface.dismiss();
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    }
                });
        builder.create().show();
    }


    private void initChatUi() {
        mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(holder_list.mLlContent)
                .bindttToSendButton(holder_list.mBtnSend)
                .bindEditText(holder_list.mEtContent)
                .bindEmojiLayout(holder_list.mLlEmotion)
                .bindBottomLayout(holder_list.mRlBottomLayout)
                .bindToAddButton(holder_list.mIvAdd)
                .bindToEmojiButton(holder_list.mIvEmo)
                .bindAudioBtn(holder_list.mBtnAudio)
                .bindAudioIv(holder_list.mIvAudio)
                .bindEmojiContent(holder_list.emojiContent)
                .bindMessageList(holder_list.messageList)
                .bindBgimage(holder_list.chatBgImage)
                .bindCallback(this);
        holder_list.mBtnAudio.setFatherLayout(msgBgView);
        mUiHelper.addBgImage(this.messageReceiver);
        addAcListener();
    }

    private void addAcListener(){
        holder_list.messageList.setOnCreateContextMenuListener(this);
        holder_list.mEtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    startSendIsTyping();
                }
            }
        });
        //底部布局弹出,聊天列表上滑
        holder_list.messageList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    holder_list.messageList.setSelection(holder_list.messageList.getBottom());
                    mUiHelper.isShowKb = true;
                }
            }
        });
        //点击空白区域关闭键盘
        holder_list.messageList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                holder_list.mEtContent.clearFocus();
                holder_list.mIvEmo.setImageResource(R.mipmap.emojikb);
                return false;
            }
        });
        ((RecordButton) holder_list.mBtnAudio).setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                Logger.d("录音结束回调");
                File file = new File(audioPath);
                if (file.exists()) {
                    sendAudioMessage(audioPath,time);
                } else {
                    createDialog("录音失败");
                }
            }
        });
    }

    private void freshListView(ConstraintLayout view) {
        holder_list = new ViewHolderList(view);
        holder_list.toolbar.setTitle("");
        holder_list.cosTextTitle.setText(this.mMap.label);
        holder_list.quitMsgAc.setOnClickListener(this);
        holder_list.msgShowF.setOnClickListener(this);
        holder_list.cameraF.setOnClickListener(this);
        holder_list.collectF.setOnClickListener(this);
        holder_list.dogF.setOnClickListener(this);
        holder_list.catF.setOnClickListener(this);
        holder_list.defaultF.setOnClickListener(this);
        holder_list.mBtnSend.setOnClickListener(this);
        holder_list.messageList.setRListViewListener(this);
        holder_list.messageList.setPullLoadEnable(true);

        holder_list.messageList.setOnCreateContextMenuListener(this);

        adapterFavor = new MessageDetailAdapter(this, mListContact);
        adapterFavor.setItemContentClickListener(this);
        holder_list.messageList.setAdapter(adapterFavor);

    }

    private void freshInfoView(ConstraintLayout view) {
        holder_info = new ViewHolderInfo(view);
        holder_info.userName.setText(this.mMap.username);
        //头像
        String headImage = RunningData.getInstance().echoMainPicUrl() + this.mMap.avatar_url;
        RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.default_head);

        Glide.with(this).load(headImage).apply(requestOptions).into(holder_info.ivAvator);
        holder_info.ivAvator.setOnClickListener(this);
        holder_info.friendCard.setOnClickListener(this);
        holder_info.onceMsg.setOnClickListener(this);
        holder_info.changeName.setOnClickListener(this);
        holder_info.onceMsgHelp.setOnClickListener(this);
        holder_info.coverView.setOnClickListener(this);
    }

    @Override
    public void innerItemClick(int position) {
        if (mUiHelper.isShowKb == false){
            if (position >= 0 && position < mListContact.size()){
                SendMessageBody itemBody = mListContact.get(position);
                //***图片点击时间
                if(itemBody.getMsgType() == 8){
                    encodeImageItemClick(itemBody);
                }else if (itemBody.getMsgType() == 3 || itemBody.getMsgType() == 5){
                    imageItemClick(itemBody);
                }else if(itemBody.getMsgType() == 4){
                    voiceItemClick(itemBody, position);
                }else if(itemBody.getMsgType() == 6){
                    positionItemClick(itemBody);
                }else if(itemBody.getMsgType() == 7){
                    friendCardItemClick(itemBody);
                }
            }
        }else{
            mUiHelper.isShowKb = true;
        }
    }

    private void friendCardItemClick(SendMessageBody itemBody){
        String excessInfo = itemBody.getExcessInfo();
        try{
            String[] info_arr = excessInfo.split("&");
            String avatar_url = info_arr[0].substring(11);
            String login_name = info_arr[1].substring(8);
            String username = info_arr[3].substring(6);

            double value = Double.valueOf(info_arr[2].substring(10));
            int user_id = (int)value;
            if (user_id >= 0){
                UserInfo friendInfo = new UserInfo(avatar_url, "", username, login_name, "", user_id, 0);
                Gson gson = new Gson();
                Logger.d("friendInfo = " + gson.toJson(friendInfo));
                Intent intent = new Intent(this, SearchResultActivity.class);
                intent.putExtra("friendInfo", friendInfo);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        }catch (Exception e){
            Logger.d("info_arr 解析错误");

        }
    }

    private void positionItemClick(SendMessageBody itemBody){
        Intent intent = new Intent(this, MapShowActivity.class);
        intent.putExtra("body", itemBody);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }
    //*****item 点击事件
    private void encodeImageItemClick(SendMessageBody itemBody){
        int index = 0;
        ArrayList<ImageModel> image_url_list = new ArrayList<ImageModel>();
        ImageModel model = new ImageModel(RunningData.getInstance().echoIMPicUrl() + itemBody.getFileUrl(),  itemBody.getExcessInfo());
        image_url_list.add(model);
        if (image_url_list.size() > 0){
            Intent intent = new Intent(this, PhotoViewerActivity.class);
            intent.putExtra("image_url", image_url_list);
            intent.putExtra("cur_index", index);
            intent.putExtra("is_encode", true);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }
    private void imageItemClick(SendMessageBody itemBody){
        int index = 0;
        ArrayList<ImageModel> image_url_list = new ArrayList<ImageModel>();
        if (itemBody.getMsgType() == 3 || itemBody.getMsgType() == 5){
            int temperIndex = 0;
            for (int i = 0; i < mListContact.size(); i++){
                SendMessageBody innerBody = mListContact.get(i);
                if (innerBody.getMsgType() == 3 || innerBody.getMsgType() == 5){
                    ImageModel model = new ImageModel(RunningData.getInstance().echoIMPicUrl() + innerBody.getFileUrl(), "");
                    image_url_list.add(model);
                    if (innerBody.getMessageIdClient().equals(itemBody.getMessageIdClient())){
                        index = temperIndex;
                    }else{
                        temperIndex++;
                    }
                }
            }
        }
        if (image_url_list.size() > 0){
            Intent intent = new Intent(this, PhotoViewerActivity.class);
            intent.putExtra("image_url", image_url_list);
            intent.putExtra("cur_index", index);
            intent.putExtra("is_encode", false);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    private SendMessageBody curVoiceBody = null;
    private void voiceItemClick(SendMessageBody itemBody, int position){
        curVoiceBody = itemBody;
        DownLoadVoice loadVoice = DownLoadVoice.getInstance(this, this);
        loadVoice.loadFile(itemBody, 0);
    }

    @Override
    public void downLoadStart() {
        if (curVoiceBody != null){
            adapterFavor.updateItem(holder_list.messageList, curVoiceBody, 1);
        }
    }

    @Override
    public void downLoadEnd(boolean haserror) {
        if (haserror == true){
            if (curVoiceBody != null){
                adapterFavor.updateItem(holder_list.messageList, curVoiceBody, 0);
            }
            makeSnake(holder_list.messageList,"录音文件被删除", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void playStart() {
        if (curVoiceBody != null){
            adapterFavor.updateItem(holder_list.messageList, curVoiceBody, 2);
        }
    }

    @Override
    public void playFinish() {
        if (curVoiceBody != null){
            adapterFavor.updateItem(holder_list.messageList, curVoiceBody, 0);
        }
    }

    @Override
    public void transStart(SendMessageBody file) {
        String msgId = file.getMessageIdClient();
        Logger.d("正在转化");
        adapterFavor.updateVoiceItem(holder_list.messageList, msgId, "正在转化...");
    }

    @Override
    public void transEnd(SendMessageBody file, String result, int has_error) {
        String msgId = file.getMessageIdClient();
        if (result.equals("")){
            result = "没有识别到语音";
        }
        if (has_error  == 0){
            adapterFavor.updateVoiceItem(holder_list.messageList, msgId, result);
            CacheTool.getInstance().uploadTranslate(this.messageCreator, msgId, result);
            for (int i = 0; i < mListContact.size(); i++){
                SendMessageBody body = mListContact.get(i);
                String msgIdInner = body.getMessageIdClient();
                if (msgIdInner.equalsIgnoreCase(msgId)){
                    body.message_other_process_info = result;
                    break;
                }
            }
        }else if (has_error  == 1){
            makeSnake(holder_list.messageList, "转化中, 请稍后", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
        }else if (has_error  == 3){
            adapterFavor.updateVoiceItem(holder_list.messageList, msgId, result);
            CacheTool.getInstance().uploadTranslate(this.messageCreator, msgId, result);
        }else{
            adapterFavor.updateVoiceItem(holder_list.messageList, msgId, result);
            makeSnake(holder_list.messageList, result, R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void onLoadMore() {
        getCacheData();
    }

    private void stopFresh() {
        globalPage++;
        holder_list.messageList.stopLoadMore();
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.iv_avatar:
                String image_url = RunningData.getInstance().echoMainPicUrl() + MsgDetailActivity.this.mMap.avatar_url;
                intent = new Intent(MsgDetailActivity.this, PhotoViewerActivity.class);
                ArrayList<ImageModel> image_url_list = new ArrayList<ImageModel>();
                ImageModel model = new ImageModel(image_url, "");
                image_url_list.add(model);
                intent.putExtra("image_url", image_url_list);
                intent.putExtra("cur_index", 0);
                intent.putExtra("is_encode", false);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.quit_msg_ac:
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                break;
            case R.id.friend_card:
                intent = new Intent(this, FriendCardActivity.class);
                intent.putExtra("friendInfo", this.mMap);
                intent.putExtra("pushDirect", "finish");
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.change_name:
                intent = new Intent(this, ChangeFriendLabelActivity.class);
                intent.putExtra("friendInfo", this.mMap);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.msg_show_f:
                changeListPos();
                break;
            case R.id.once_msg_help:
                holder_info.coverView.setVisibility(View.VISIBLE);
                break;
            case R.id.once_msg:
                setOnceMsg();
                break;
            case R.id.cover_view:
                holder_info.coverView.setVisibility(View.INVISIBLE);
                break;
            case R.id.default_f:
                mUiHelper.bindEmojiData(0);
                RunningData.getInstance().keyKind = 0;
                break;
            case R.id.dog_f:
                mUiHelper.bindEmojiData(1);
                RunningData.getInstance().keyKind = 1;
                break;
            case R.id.cat_f:
                mUiHelper.bindEmojiData(2);
                RunningData.getInstance().keyKind = 2;
                break;
            case R.id.collect_f:
                mUiHelper.bindEmojiData(3);
                RunningData.getInstance().keyKind = 3;
                break;
            case R.id.btn_send:
                sendText();
                break;
            default:
                break;
        }
    }

    private void changeListPos() {
        if (msgBgView.pageFlag == true) {
            msgBgView.toFirstPage();
        } else {
            msgBgView.toSecondPage();
        }
    }

    private void setOnceMsg() {
        if (onceMsg == false) {
            holder_list.inputNg.setBackground(new ColorDrawable(0xff0f0f0f));
            holder_info.onceMsg.setBackground(new ColorDrawable(0xff0f0f0f));
            holder_info.onceMsgTitle.setText("🕙");
            onceMsg = true;
            Vibrator vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            _messageSecretType = 1;
        } else {
            holder_list.inputNg.setBackground(new ColorDrawable(0xffffffff));
            holder_info.onceMsg.setBackground(new ColorDrawable(0xfffbfbfb));
            holder_info.onceMsgTitle.setText("一次性消息");
            onceMsg = false;
            _messageSecretType = 0;
        }
    }

    //***长按菜单
    @Override
    public void longPressListener(int position) {
        pressBodyPosition = position;
        holder_list.messageList.showContextMenu();
    }
    private int pressBodyPosition = -1;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Logger.d(" onCreateContextMenu onCreateContextMenu onCreateContextMenu");
        if (pressBodyPosition >= 0 && pressBodyPosition < mListContact.size()){
            SendMessageBody pressBody = mListContact.get(pressBodyPosition);
            if (pressBody.has_send_error == true){
                getMenuInflater().inflate(R.menu.msg_error_op,menu);
                return;
            }
            switch (pressBody.getMsgType()){
                case 1:
                    getMenuInflater().inflate(R.menu.msg_text_op,menu);
                    break;
                case 4:
                    if (pressBody.getMessageTag() == true){
                        getMenuInflater().inflate(R.menu.msg_voice_op,menu);
                    }else{
                        getMenuInflater().inflate(R.menu.msg_default_emoji_op,menu);
                    }
                    break;
                case 2:
                case 6:
                case 7:
                case 8:
                    getMenuInflater().inflate(R.menu.msg_default_emoji_op,menu);
                    break;
                case 3:
                case 5:
                    getMenuInflater().inflate(R.menu.msg_photo_op,menu);
                    break;
                default:
                    break;
            }
        }
    }

    //选中菜单Item后触发
    public boolean onContextItemSelected(MenuItem item){
        //关键代码在这里
        switch (item.getItemId()) {
            case R.id.delete:
                deleteMethod();
                break;
            case R.id.copy:
                copyMethod();
                break;
            case R.id.transend:
                resendMsg();
                break;
            case R.id.addEmoji:
                addEmojiMethod();
                break;
            case R.id.toString:
                voiceToString();
                break;
            default:
                break;
        }
        //输出position
        return super.onContextItemSelected(item);
    }
    private void voiceToString(){
        if (pressBodyPosition >= 0 && pressBodyPosition < mListContact.size()){
            SendMessageBody resendBody = mListContact.get(pressBodyPosition);
            if (resendBody.getMessageTag() == true && resendBody.message_other_process_info.equals("")){
                DownLoadVoice loadVoice = DownLoadVoice.getInstance(this, this);
                loadVoice.loadFile(resendBody, 1);
            }
        }
    }

    private void resendMsg(){
        if (pressBodyPosition >= 0 && pressBodyPosition < mListContact.size()){
            SendMessageBody resendBody = mListContact.get(pressBodyPosition);
            Intent intent = new Intent(MsgDetailActivity.this, ResendMsgActivity.class);
            intent.putExtra("resend_body", resendBody);
            intent.putExtra("kind",1);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    private void addEmojiMethod(){
        if (pressBodyPosition >= 0 && pressBodyPosition < mListContact.size()){
            SendMessageBody resendBody = mListContact.get(pressBodyPosition);
            mUiHelper.addEmoji(resendBody.getFileUrl());
        }
    }

    private void deleteMethod(){
        if (pressBodyPosition >= 0 && pressBodyPosition < mListContact.size()){
            SendMessageBody pressBody = mListContact.get(pressBodyPosition);
            String messageId = pressBody.getMessageIdClient();
            mListContact.remove(pressBodyPosition);
            adapterFavor.notifyDataSetChanged();
            CacheTool.getInstance().deleteOneSpeMsg(this.messageReceiver ,messageId);
        }
    }
    private void copyMethod(){
        if (pressBodyPosition >= 0 && pressBodyPosition < mListContact.size()){
            SendMessageBody pressBody = mListContact.get(pressBodyPosition);
            String str = pressBody.getContent();
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("content", str); //Label是任意文字标签
            cm.setPrimaryClip(mClipData);
        }
    }

    @Override
    public void urlClickMethod(String text) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("url_value", text);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        startActivity(intent);

    }

    static class ViewHolderList {
        @BindView(R.id.blur_layout)
        View blurLayout;
        @BindView(R.id.cos_text_title)
        TextView cosTextTitle;
        @BindView(R.id.toolbar)
        Toolbar toolbar;
        @BindView(R.id.quit_msg_ac)
        ImageButton quitMsgAc;
        @BindView(R.id.msg_show_f)
        ImageButton msgShowF;
        @BindView(R.id.message_list)
        RListView messageList;
        @BindView(R.id.llContent)
        LinearLayout mLlContent;
        @BindView(R.id.et_content)
        EditText mEtContent;
        @BindView(R.id.bottom_layout)
        RelativeLayout mRlBottomLayout;//表情,添加底部布局
        @BindView(R.id.ivAdd)
        ImageView mIvAdd;
        @BindView(R.id.ivEmo)
        ImageView mIvEmo;
        @BindView(R.id.ivAudio)
        ImageView mIvAudio;//录音图片
        @BindView(R.id.btn_send)
        StateButton mBtnSend;//发送按钮
        @BindView(R.id.btnAudio)
        RecordButton mBtnAudio;//录音按钮
        @BindView(R.id.rlEmotion)
        LinearLayout mLlEmotion;//表情布局
        @BindView(R.id.input_bg)
        LinearLayout inputNg;//表情布局
        @BindView(R.id.emoji_content)
        GridView emojiContent;
        @BindView(R.id.camera_f)
        ImageView cameraF;
        @BindView(R.id.collect_f)
        ImageView collectF;
        @BindView(R.id.cat_f)
        ImageView catF;
        @BindView(R.id.dog_f)
        ImageView dogF;
        @BindView(R.id.default_f)
        ImageView defaultF;
        @BindView(R.id.chat_bg_image)
        ImageView chatBgImage;

        @BindView(R.id.connect_tips)
        AVLoadingIndicatorView connectTips;
        @BindView(R.id.other_msg_tips)
        TextView otherMsgTips;
        ViewHolderList(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ViewHolderInfo {
        @BindView(R.id.iv_avatar)
        RoundImageView ivAvator;
        @BindView(R.id.user_name)
        TextView userName;
        @BindView(R.id.once_msg_help)
        Button onceMsgHelp;
        @BindView(R.id.cover_view)
        LinearLayout coverView;
        @BindView(R.id.once_msg)
        LinearLayout onceMsg;
        @BindView(R.id.once_msg_title)
        TextView onceMsgTitle;
        @BindView(R.id.friend_card)
        LinearLayout friendCard;
        @BindView(R.id.change_name)
        LinearLayout changeName;
        ViewHolderInfo(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void showActionsheets() {
        ActionSheet.createBuilder(this, getSupportFragmentManager()).setCancelButtonTitle("取消").setOtherButtonTitles("发送图片", "加密图片", "位置", "推荐联系人").setCancelableOnTouchOutside(true).setListener(this).show();
    }

    @Override
    public void addCollectEmji() {
        Intent intent = new Intent(this, AddEmojiActivity.class);
        startActivityForResult(intent, 4);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0){
            Intent intent = new Intent(this, ChoosePhotoActivity.class);
            intent.putExtra("image_kind", false);
            startActivityForResult(intent, 2);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }else if(index == 1){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("图片经高强度加密后发送")
                    .setMessage("加密图具有极强的安全性\n图片密钥随机生成\n每张图加密密钥均不同\n收信人外都无法知晓图片内容\n适合发送私密性强的图片\n")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(MsgDetailActivity.this, ChoosePhotoActivity.class);
                            intent.putExtra("image_kind", true);
                            startActivityForResult(intent, 2);
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }
                    });
            builder.create().show();
        }else if(index == 2){
            Intent intent = new Intent(MsgDetailActivity.this, MapMainActivity.class);
            startActivityForResult(intent, 3);
        }else if(index == 3){
            Intent intent = new Intent(MsgDetailActivity.this, ResendMsgActivity.class);
            intent.putExtra("kind",2);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {}

    private void startSendIsTyping(){
        final Timer isTypingTimer = new Timer();
        isTypingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String textContent = holder_list.mEtContent.getText().toString();
                if (textContent.length() > 0){
                    sendIsTyping();
                }
                isTypingTimer.cancel();
            }
        }, 5000);
    }
    private void sendAudioMessage(String audioPath, int time){
        String excessInfo = time + "_s";
        String key = getQiniuKey(audioPath, 1, time, 0);
        getUploadToken(audioPath, key, excessInfo,2);

    }

    private void sendCommonPhoto(ArrayList<TImage> imagesUrl){
        for (int i = 0; i < imagesUrl.size(); i++){
            String imgLocalUrl = imagesUrl.get(i).getOriginalPath();
            String key = getQiniuKey(imgLocalUrl, 0, 0, i);
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;
            op.inSampleSize = 1;
            BitmapFactory.decodeFile(imgLocalUrl, op);
            String excessInfo = op.outWidth + "_" + op.outHeight;
            getUploadToken(imgLocalUrl, key, excessInfo, 1);
        }
    }

    private void sendEncodePhoto(ArrayList<TImage> imagesUrl){
        String imgLocalUrl = imagesUrl.get(0).getOriginalPath();
        String key = getQiniuKey(imgLocalUrl, 2, 0, 0);
        getUploadToken(imgLocalUrl, key, "", 4);
    }

    @Override
    public void sendCollectEmji(String imageString) {
        String key = getQiniuKey(imageString, 0, 0, 0);
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        op.inSampleSize = 1;
        BitmapFactory.decodeFile(imageString, op);
        String excessInfo = op.outWidth + "_" +  op.outHeight;
        getUploadToken(imageString, key, excessInfo, 3);
    }

    //*****语音 照片的 发送处理
    private void getUploadToken(final String fileUrl, final String key, final String excessInfo, final int uploadType){
        String token = RunningData.getInstance().getToken();
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/getUploadToken")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .params("type", 2)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        qiniuToken = response.body().data.toString();
                        if (uploadType == 4){
                            startUploadByte(fileUrl, key, excessInfo, uploadType);
                        }else{
                            startUpload(fileUrl, key, excessInfo, uploadType);
                        }
                    }

                    @Override
                    public void onError(Response<CodeResponse> response) {
                        response.getException().printStackTrace();
                    }
                });
    }
//****根据文件路径上传
    private void startUpload(final String fileUrl , final String key, final String excessInfo, final int uploadType){
        Configuration config = new Configuration.Builder().zone(AutoZone.autoZone).build();
        UploadManager uploadManager = new UploadManager(config);
        String token = qiniuToken;
        uploadManager.put(fileUrl, key, token, new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        if(info.isOK()) {
                            Logger.d("Upload Success");
                            if (uploadType == 1){
                                sendImageMethod(fileUrl, key , excessInfo);
                            }else if(uploadType == 2){
                                sendVoiceMethod(fileUrl, key , excessInfo);
                            }else if(uploadType == 3){
                                sendCollectEmojiMethod(fileUrl, key , excessInfo);
                            }
                        }else {
                            Logger.d("Upload Fail");
                        }
                    }
                }, null);
    }
    //****上传byte
    private void startUploadByte(final String fileUrl , final String key, final String excessInfo, final int uploadType){
        try{
            byte[] data = getContent(fileUrl);
            if (data != null){
                Configuration config = new Configuration.Builder().zone(AutoZone.autoZone).build();
                UploadManager uploadManager = new UploadManager(config);
                String token = qiniuToken;
                uploadManager.put(data, key, token, new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        if(info.isOK()) {
                            Logger.d("Upload Success");
                            sendEncodeImageMethod(fileUrl, key , excessInfo);
                        }else {
                            Logger.d("Upload Fail");
                        }
                    }
                }, null);
            }else {
                makeSnake(holder_list.messageList, "图片获取失败", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
            }
        }catch (Exception e){}
    }

    //*************************  IM ********************* //
    private Intent serviceIntent;
    private WsChatService wsChatService = null;

    private void bindWsService() {
        serviceIntent = new Intent(this, WsChatService.class);
        //启动并绑定service
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }

    private void unbindWsService() {
        if (wsChatService != null) {
            wsChatService.setMessageDetailCallBacks(null);
            unbindService(this);
            wsChatService = null;
        }
    }

    /**
     * 绑定 Service  WsChatService
     **/
    @Override
    protected void onDestroy() {
        deleteSecretMsg();
        unbindWsService();
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        wsChatService = ((WsChatService.WsChatServiceBinder) binder).getService();
        //绑定后，需要实现MessageCallbacks接口，并且调用setMessageListCallBacks方法
        wsChatService.setMessageDetailCallBacks(this);
        if (wsChatService.getWsState() == WsStatus.AUTH_SUCCESS || wsChatService.getWsState() == WsStatus.CONNECT_SUCCESS) {
            showOrHide(false);
        } else {
            showOrHide(true);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
//        Logger.d("detail 解除绑定了 Ws_chat_service");
    }

    /**
     * WsService 发送方法*
     **/

    private void sendIsTyping() {
        SendIsTypingBody body = new SendIsTypingBody(this.messageCreator, this.messageReceiver);
        wsChatService.sendUserIsTyping(body);
    }


    private void sendText() {
        String content = holder_list.mEtContent.getText().toString();
        if (!content.equals("")) {
            SendMessageBody body = getSendModel(1, content, "", "", "");
            SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
            holder_list.mEtContent.setText("");
            freshList(body);
            wsChatService.sendImMessageBody(body_en);
        }
    }

    @Override
    public void sendDefaultEmoji(String imageString) {
        String content = "[表情]";
        SendMessageBody body = getSendModel(2, content, imageString, "","");
        SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
        freshList(body);
        wsChatService.sendImMessageBody(body_en);
    }

    //**发送照片
    private void sendImageMethod(String fileUrl , String key, String excessInfo){
        String content = "[图片]";
        SendMessageBody body = getSendModel(3, content, key, excessInfo, fileUrl);
        SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
        freshList(body);
        wsChatService.sendImMessageBody(body_en);
    }
    //**发送照片
    private void sendEncodeImageMethod(String fileUrl , String key, String excessInfo){
        String content = "[加密图片]";
        SendMessageBody body = getSendModel(8, content, key, excessInfo, fileUrl);
        SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
        freshList(body);
        wsChatService.sendImMessageBody(body_en);
    }
    //***发送表情包
    private void sendCollectEmojiMethod(String fileUrl , String key, String excessInfo){
        String content = "[动画表情]";
        SendMessageBody body = getSendModel(5, content, key, excessInfo, fileUrl);
        SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
        freshList(body);
        wsChatService.sendImMessageBody(body_en);
    }
    //**发送语音
    private void sendVoiceMethod(String fileUrl , String key, String excessInfo){
        mp = MediaPlayer.create(this, R.raw.send_voice);
        mp.start();
        String content = "[语音]";
        SendMessageBody body = getSendModel(4, content, key, excessInfo, fileUrl);
        SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
        freshList(body);
        wsChatService.sendImMessageBody(body_en);
    }
    //**发送位置
    private void sendPositionMethod(String fileUrl , String key, String excessInfo){
        String content = "[位置]";
        SendMessageBody body = getSendModel(6, content, key, excessInfo, fileUrl);
        SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
        freshList(body);
        wsChatService.sendImMessageBody(body_en);
    }
    //**推荐联系人
    private void sendFriendCardMethod(String fileUrl , String key, String excessInfo){
        String content = "[推荐联系人]";
        SendMessageBody body = getSendModel(7, content, key, excessInfo, fileUrl);
        SendMessageEnBody body_en = new SendMessageEnBody(body, this.pubKey);
        freshList(body);
        wsChatService.sendImMessageBody(body_en);
    }
    /**
     * WsService  的回掉通知
     * 根据回掉处理数据
     **/
    private void showOrHide(boolean show) {
        if (show) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder_list.connectTips.smoothToShow();
                    holder_list.connectTips.setVisibility(View.VISIBLE);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder_list.connectTips.smoothToHide();
                    holder_list.connectTips.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void chatConnected() {
        // 连接成功，更改UI
        showOrHide(false);
    }


    @Override
    public void chatDisconnected() {
        // 连接断开，更改UI
        showOrHide(true);
    }


    @Override
    public void comeANewIMMessage(SendMessageEnBody body) {
        //来了一个私信
        //1.0 更改消息列表
        //如果是我的消息就渲染UI
        Logger.d("body.getMessageReceiver() = " + body.getMessageReceiver());
        Logger.d("body.getMessageCreator() = " + body.getMessageCreator());
        Logger.d("this.messageCreator = " + this.messageCreator);
        Logger.d("this.messageReceiver = " + this.messageReceiver);

        if (body.getMessageCreator().equals(this.messageReceiver)) {
            CryTool tool = new CryTool();
            String body_en = body.getBody_en();
            String key_en = body.getKey();
            String key = tool.rsaDecrypt(key_en, RunningData.getInstance().getMyRSAPriKeyWith(this.messageCreator));
            String bodyString = tool.aesDeWith(body_en, key);
//            Logger.d("解密后body = " + bodyString);
            if (!bodyString.equals("")) {
                Gson gson = new Gson();
                SendMessageBody sendBody = gson.fromJson(bodyString, SendMessageBody.class);
                if (sendBody != null) {
                    sendBody.setIsSendSuccess(true);
                    sendBody.setMessageTag(true);
                    sendBody.set_has_send_error(false);
                    if (existAlready(sendBody) == false) {
                        int index = (mListContact.size() - 1) <= 0 ? 0 : (mListContact.size() - 1);
                        mListContact.add(index, sendBody);
                    }
                    freshListMsthod();
                    return;
                }
            }
            SendMessageBody sendBody = new SendMessageBody();
            sendBody.setMessageCreator(body.getMessageCreator());
            sendBody.setMessageReceiver(body.getMessageReceiver());
            sendBody.setMessageSendTime(body.getMessageSendTime());
            sendBody.setMessageIdClient(body.getMessageIdClient());
            sendBody.setMsgType(1);
            sendBody.setContent("解密错误，具体查看帮助");
            sendBody.setIsSendSuccess(true);
            sendBody.setMessageTag(true);
            sendBody.set_has_send_error(false);
            int index = (mListContact.size() - 1) <= 0 ? 0 : (mListContact.size() - 1);

            mListContact.add(index, sendBody);
            freshListMsthod();
        }else{
            otherMsgTipsShow(true);
        }
        //2.0 缓存消息到本地，（缓存一律到service做）
    }

    private void freshListMsthod() {
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapterFavor.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void messageSendSuccess(MessageSendState body) {
        //消息发送成功
        //1.0改变UI状态（可选，iOS做法是发送成功后才更新最外面消息列表）
        String messageIdClient = body.getMessageIdClientt();
        String messageSendTime = body.getMessageSendTime();
        for (int i = 0; i < mListContact.size(); i++) {
            SendMessageBody bodyInner = mListContact.get(i);
            if (bodyInner.getMessageIdClient() != null) {
                if (bodyInner.getMessageIdClient().equals(messageIdClient)) {
                    bodyInner.setIsSendSuccess(true);
                    bodyInner.setMessageSendTime(messageSendTime);
                    mListContact.set(i, bodyInner);
                    break;
                }
            }
        }
        freshListMsthod();
    }

    @Override
    public void messageSendFailed(MessageSendState body) {
        //消息发送失败
        //1.0改变UI状态（可选，iOS做法是发送成功后才更新最外面消息列表）
        //2.0 更新数据库 has_send_error 字段
    }
    @Override
    public void userIsTyping(ReceiverIsTypingBody body) {
        //应该在消息详情页面实现，这里不实现。
        if (body.getWhoIsTyping().equals(this.messageReceiver)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder_list.cosTextTitle.setText("对方正在输入...");
                }
            });
            final Timer changeTimer = new Timer();
            final String title = this.mMap.label;
            changeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder_list.cosTextTitle.setText(title);
                        }
                    });
                    changeTimer.cancel();
                }
            }, 3000);
        }
    }

    //***下面方法这里暂不实现
    @Override
    public void userRecalledAMessage(ReceiveRecalledInfo body) {
        // 消息撤回，可以在这里删除消息
        // 暂时不实现
    }

    //***下面方法这里不必实现
    @Override
    public void applyForFriend(ReceiveSpecialInfo body) {
        //不实现
    }
    @Override
    public void allowMeASFriend(ReceiveSpecialInfo body) {}//不实现
    @Override
    public void someOneDeleteMe(ReceiveSpecialInfo body) {}//不实现
    @Override
    public void freshMsgListView() {}//不实现

    @Override
    public void connectSucTips() {
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 连接成功，更改UI
                showOrHide(false);
            }
        });
    }
    @Override
    public void noOfflineMessage() {
        //没有离线消息，改变UI， "获取离线消息" -->  "对话"
    }

    /**
     * IM 工具方法
     */
    private void otherMsgTipsShow(boolean show){
        if (show == true){
            final int number = string2int( holder_list.otherMsgTips.getText().toString()) + 1;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder_list.otherMsgTips.setText("" + number);
                    holder_list.otherMsgTips.setVisibility(View.VISIBLE);
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder_list.otherMsgTips.setText("0");
                    holder_list.otherMsgTips.setVisibility(View.GONE);
                }
            });
        }
    }

    private String getNowTimeTimestamp() {
        long time = System.currentTimeMillis();
        String timeTimestamp = String.valueOf(time);
        return timeTimestamp;
    }

    private String getMessageId() {
        String messageId = this.messageCreator + getNowTimeTimestamp() + (int) (1 + Math.random() * (1000));
        CryTool tool = new CryTool();
        String result = tool.shaEncrypt(messageId);

        return result;
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

    private SendMessageBody getSendModel(int msgType, String content, String fileUrl, String excessInfo, String localFileUrl) {
        // 1    //文字
        // 2    //默认表情
        // 3    //照片信息
        // 4    //语音消息
        // 5    //表情包
        // 6    //位置
        // 7    //推荐联系人
        // 8    //加密图
        SendMessageBody body = new SendMessageBody();
        body.setMsgType(msgType);
        body.setContent(content);
        body.setFileUrl(fileUrl);
        body.setExcessInfo(excessInfo);

        body.setMessageCreator(this.messageCreator);
        body.setMessageReceiver(this.messageReceiver);
        body.setMessageIdClient(getMessageId());
        body.setMessageIdServer("");

        body.setMessageSendTime(getNowTimeTimestamp());
        body.setIsSendSuccess(false);
        body.setMessageTag(false);
        body.setMessageSecretType(_messageSecretType);
        body.image_value = localFileUrl;
        return body;
    }


    private void freshList(SendMessageBody body) {
        int index = (mListContact.size() - 1) <= 0 ? 0 : (mListContact.size() - 1);
        mListContact.add(index, body);
        adapterFavor.notifyDataSetChanged();
    }


    /**
     * IMCache 工具方法
     */
    //读取缓存聊天记录
    private int globalPage = 0;

    private void getCacheData() {
        if (globalPage == 0) {
            SendMessageBody lastBlank = new SendMessageBody();//加个空item，便于listview 滑动到底部检索
            lastBlank.setMsgType(-1);
            mListContact.add(0, lastBlank);
        }

        ArrayList<SendMessageBody> cacheList = CacheTool.getInstance().queryAll(this.messageReceiver, this.messageCreator, globalPage);
        if (cacheList.size() > 0) {
            for (int i = 0; i < cacheList.size(); i++) {
                SendMessageBody body = cacheList.get(i);
                if (existAlready(body) == false) {
                    mListContact.add(0, body);
                }
                if (body.getMessageSecretType() == 1) {
                    secretMsgIdList.add(body.getMessageIdClient());
                }
            }
            adapterFavor.notifyDataSetChanged();
        }
        stopFresh();
    }

    private void deleteSecretMsg() {
        if (secretMsgIdList.size() > 0) {
            CacheTool.getInstance().deleteSecretMsg(this.messageReceiver, secretMsgIdList);
        }
    }

    //*** 去重
    private boolean existAlready(SendMessageBody body) {
        boolean exist = false;
        int endIndex = (mListContact.size() - 15) >= 0 ? (mListContact.size() - 15) : 0;
        if (mListContact.size() - 2 > 0) {
            for (int i = mListContact.size() - 2; i >= endIndex; i--) {
                SendMessageBody bodyInnder = mListContact.get(i);
                if (bodyInnder.getMessageIdClient() != null) {
                    if (bodyInnder.getMessageIdClient().equals(body.getMessageIdClient())) {
                        exist = true;
                        break;
                    }
                }
            }
        }

        return exist;
    }

    //****获取byte
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

    //***上传key
    private String getQiniuKey(String file_url, int type, int length, int index){
        CryTool tool = new CryTool();
        String phoneNum = tool.shaEncrypt(RunningData.getInstance().getCurrentAccount()).substring(0, 24);

        int randNumber = (int) (Math.random() * 1000);
        long time = System.currentTimeMillis();
        String timeNumberList = String.valueOf(time);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String dateString = simpleDateFormat.format(date);

        //图片后缀
        String surNmae = "png";
        String[] surNmaeArray = file_url.toString().split("\\.");
        if (surNmaeArray.length >= 1){
            surNmae = surNmaeArray[surNmaeArray.length - 1];
        }

        String key = "";

        if (type == 0){ // 照片
            key = "imModelImages/" + phoneNum + "/" + dateString + "/"+ timeNumberList + randNumber + index + "." + surNmae;
        }else if (type == 1){  //语音
            key = "imModelImages/" + phoneNum + "/" + dateString + "/"+ timeNumberList + randNumber + index + "_" + length + "_" + "." + surNmae;
        }else if (type == 2){  //语音
            key = "imModelImages/" + phoneNum + "/" + dateString + "/"+ timeNumberList + randNumber + index + "." + surNmae + ".crypt";
        }

        return key;
    }

    //*****转发
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {   //    重发  ***  推荐联系人
            if (resultCode == 100){
                SendMessageBody body = (SendMessageBody)data.getSerializableExtra("body");
                SendMessageEnBody body_en = (SendMessageEnBody)data.getSerializableExtra("body_en");
                if(body.getMessageReceiver().equals(this.messageReceiver)){
                    freshList(body);
                }
                wsChatService.sendImMessageBody(body_en);
            }else if(resultCode == 101){
                String excessInfo = (String)data.getSerializableExtra("excessInfo");
                sendFriendCardMethod("", "", excessInfo);
            }
        }else if(requestCode == 2){
            if (resultCode == 100){//普通照片
                ArrayList<TImage> imagesUrl = (ArrayList<TImage>)data.getSerializableExtra("images");
                if (imagesUrl.size() > 0){
                    sendCommonPhoto(imagesUrl);
                }
            }else if (resultCode == 200){
                //加密照片
                ArrayList<TImage> imagesUrl = (ArrayList<TImage>)data.getSerializableExtra("images");
                if (imagesUrl.size() > 0){
                    sendEncodePhoto(imagesUrl);
                }
            }

        }else if(requestCode == 3 && resultCode == 100){ //地图
            PositionModel model = (PositionModel)data.getSerializableExtra("position");
            String excessInfo = model.title + "&" + model.detail + "&" + model.lat + "_" + model.lon;
            sendPositionMethod("", "", excessInfo);
        }else if(requestCode == 4){ //添加表情
            mUiHelper.bindEmojiData(3);
        }
    }


}
