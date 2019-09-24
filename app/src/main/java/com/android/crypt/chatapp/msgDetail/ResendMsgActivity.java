package com.android.crypt.chatapp.msgDetail;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.crypt.chatapp.contact.cn.CNPinyin;
import com.android.crypt.chatapp.msgDetail.adapter.ViewPagerAdapter;
import com.android.crypt.chatapp.msgList.model.MessageListModel;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageEnBody;
import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.contact.adapter.CharIndexView;
import com.android.crypt.chatapp.contact.adapter.ContactListAdapter;
import com.android.crypt.chatapp.msgList.adapter.MessageListAdapter;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageBody;
import com.android.crypt.chatapp.widget.swipexlistview.XListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResendMsgActivity extends BaseActivity implements  AdapterView.OnItemClickListener{

    private MsgViewHolder msgHolder;
    private ConViewHolder conHolder;
    private ArrayList<MessageListModel> mListMessage;
    private ArrayList<CNPinyin<ContactModel>> mListContact;

    private MessageListAdapter adapterMsgFavor;
    private ContactListAdapter adapterContactFavor;

    private SendMessageBody resendBody;
    private int kind;   // 1 转发文本；  2 推荐联系人
    private List<View> viewList = null;
    private ViewPagerAdapter adapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.page_view)
    ViewPager pageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resend_msg);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_resend_msg);
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
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void initView() {
        Intent intent = getIntent();
        this.resendBody = (SendMessageBody) intent.getSerializableExtra("resend_body");
        this.kind = (int)intent.getSerializableExtra("kind");

        if (viewList == null) {
            viewList = new ArrayList<View>();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        ConstraintLayout msgV = (ConstraintLayout) inflater.inflate(R.layout.resend_msg_list_layout, null);
        viewList.add(msgV);
        ConstraintLayout contact = (ConstraintLayout) inflater.inflate(R.layout.resend_contact_list_layout, null);
        viewList.add(contact);


        adapter = new ViewPagerAdapter(viewList);
        pageView.setAdapter(adapter);
        pageView.setCurrentItem(0);


        freshMsgListView(msgV);
        freshContactListView(contact);
    }

    private void freshMsgListView(View v){
        msgHolder = new MsgViewHolder(v);
        msgHolder.messageList.setOnItemClickListener(this);
        msgHolder.messageList.setPullRefreshEnable(false);
        msgHolder.messageList.setPullLoadEnable(false);
        msgHolder.messageList.setEmptyView(msgHolder.emptyView);
//        msgHolder.messageList.hideHeadViewLayout();
        int heightLlistView = RunningData.getInstance().getAppShowHeight() - RunningData.getInstance().getActionBarHeight() - DensityUtil.dip2px(this, 50);
        mListMessage = RunningData.getInstance().getMsgList();
        adapterMsgFavor = new MessageListAdapter(this, mListMessage, heightLlistView, DensityUtil.dip2px(this, 80), false);
        msgHolder.messageList.setAdapter(adapterMsgFavor);
    }

    private void freshContactListView(View v){
        conHolder = new ConViewHolder(v);
        conHolder.contactListLv.setOnItemClickListener(this);
        conHolder.contactListLv.setPullRefreshEnable(false);
        conHolder.contactListLv.setEmptyView(conHolder.emptyView);


        mListContact = RunningData.getInstance().getContactList();
        int heightLlistView = RunningData.getInstance().getAppShowHeight() - RunningData.getInstance().getActionBarHeight() - DensityUtil.dip2px(this, 20);
        adapterContactFavor = new ContactListAdapter(this, mListContact, heightLlistView, DensityUtil.dip2px(this, 80),false);
        conHolder.contactListLv.setAdapter(adapterContactFavor);

        conHolder.ivMain.setOnCharIndexChangedListener(new CharIndexView.OnCharIndexChangedListener() {
            @Override
            public void onCharIndexChanged(char currentIndex) {
                for (int i = 0; i < mListContact.size() - 1; i++) {
                    if (mListContact.get(i).getFirstChar() == currentIndex) {
                        conHolder.contactListLv.smoothScrollToPositionFromTop(i, 200, 100);
                        return;
                    }
                }
            }
            @Override
            public void onCharIndexSelected(String currentIndex) {
                if (currentIndex == null) {
                    conHolder.tvIndex.setVisibility(View.INVISIBLE);
                } else {
                    conHolder.tvIndex.setVisibility(View.VISIBLE);
                    conHolder.tvIndex.setText(currentIndex);
                }
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        String title = "转发给: ";
        if(this.kind == 2){
            title = "推荐联系人: ";
        }
        if (parent.equals(msgHolder.messageList)){
            position = position - 1;
            if (position >= 0 && position < mListMessage.size() - 1){
                final MessageListModel resendModel = mListMessage.get(position );
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(title + resendModel.label)
                        .setNegativeButton("发 送", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                resendMethod(resendModel, null,true);
                            }
                        });
                builder.create().show();
            }


        }else if (parent.equals(conHolder.contactListLv)){
            position = position - 1;
            if (position >= 0 && position < mListContact.size() - 1){
                final ContactModel resendModel = mListContact.get(position).data;
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(title + resendModel.label)
                        .setNegativeButton("发 送", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                resendMethod(null, resendModel, false);

                            }
                        });
                builder.create().show();
            }
        }
    }


    /**
     * IM 工具方法
     */

    private String getNowTimeTimestamp() {
        long time = System.currentTimeMillis();
        String timeTimestamp = String.valueOf(time);
        return timeTimestamp;
    }

    private String getMessageId() {
        String messageId = RunningData.getInstance().getCurrentAccount() + getNowTimeTimestamp() + (int) (1 + Math.random() * (1000));
        CryTool tool = new CryTool();
        String result = tool.shaEncrypt(messageId);

        return result;
    }

    private void resendMethod(MessageListModel listModel, ContactModel contactModel, boolean list){
        if (kind == 1){
            String creator = RunningData.getInstance().getCurrentAccount();
            this.resendBody.setMessageTag(false);
            this.resendBody.setMessageCreator(creator);
            this.resendBody.setMessageIdClient(getMessageId());
            this.resendBody.setMessageIdServer("");
            this.resendBody.setMessageSendTime(getNowTimeTimestamp());
            this.resendBody.setIsSendSuccess(false);
            String pubKey = "";
            if (list == true){
                this.resendBody.setMessageReceiver(listModel.account);
                for (int i = 0; i < mListMessage.size() - 1; i++){
                    MessageListModel resendModel = mListMessage.get(i);
                    if (resendModel.account.equals(listModel.account)){
                        pubKey = resendModel.public_key;
                        break;
                    }
                }
            }else{
                this.resendBody.setMessageReceiver(contactModel.account);
                for (int i = 0; i < mListContact.size() - 1; i++){
                    ContactModel resendModel = mListContact.get(i).data;
                    if (resendModel.account.equals(contactModel.account)){
                        pubKey = resendModel.public_key;
                        break;
                    }
                }
            }
            if (pubKey.equals("")){
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("无法转发")
                        .setMessage("没有获取到对方通讯公钥")
                        .setNegativeButton("转发", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
                return;
            }

            SendMessageEnBody body_en = new SendMessageEnBody(this.resendBody, pubKey);
            Intent resendIntent = new Intent();
            resendIntent.putExtra("body", this.resendBody);
            resendIntent.putExtra("body_en", body_en);
            setResult(100, resendIntent);
            finish();
        }else if (kind == 2){
            String excessInfo = "";
            if (list == true){
                String avatar_url = listModel.avatar_url;
                String account = listModel.account;
                String friend_id = listModel.friend_id;
                String label = listModel.label;
                excessInfo = "avatar_url:" + avatar_url + "&account:" + account + "&friend_id:" + friend_id + "&label:" + label;

            }else{
                String avatar_url = contactModel.avatar_url;
                String account = contactModel.account;
                String friend_id = contactModel.friend_id;
                String label = contactModel.username;
                excessInfo = "avatar_url:" + avatar_url + "&account:" + account + "&friend_id:" + friend_id + "&label:" + label;
            }
            Intent resendIntent = new Intent();
            resendIntent.putExtra("excessInfo", excessInfo);
            setResult(101, resendIntent);
            finish();

        }
    }


    static class MsgViewHolder {
        @BindView(R.id.message_list)
        XListView messageList;

        @BindView(R.id.empty_view)
        TextView emptyView;
        MsgViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ConViewHolder {
        @BindView(R.id.contactList_lv)
        XListView contactListLv;
        @BindView(R.id.empty_view)
        TextView emptyView;
        @BindView(R.id.iv_main)
        CharIndexView ivMain;
        @BindView(R.id.tv_index)
        TextView tvIndex;
        ConViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
