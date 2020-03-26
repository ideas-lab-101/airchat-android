package com.android.crypt.chatapp.msgList;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.BaseFragment;
import com.android.crypt.chatapp.ChatAppApplication;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.finder.SearchLocalActivity;
import com.android.crypt.chatapp.msgDetail.MsgDetailActivity;
import com.android.crypt.chatapp.msgList.adapter.MessageListAdapter;
import com.android.crypt.chatapp.msgList.model.MessageListModel;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.AcNotifyCallbackManager;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.Websocket.MessageCallbacks;
import com.android.crypt.chatapp.utility.Websocket.Model.MessageSendState;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveRecalledInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiveSpecialInfo;
import com.android.crypt.chatapp.utility.Websocket.Model.ReceiverIsTypingBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageEnBody;
import com.android.crypt.chatapp.utility.Websocket.Model.SendToGetOffLineMessage;
import com.android.crypt.chatapp.utility.Websocket.Model.SendToResetPushNumber;
import com.android.crypt.chatapp.utility.Websocket.WsChatService;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.android.crypt.chatapp.widget.swipexlistview.SwipeXListView;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;


public class MsgFragment extends BaseFragment implements SwipeXListView.IXListViewListener, View.OnClickListener, AdapterView.OnItemClickListener, ServiceConnection, MessageCallbacks, MessageListAdapter.onRightItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.message_list)
    SwipeXListView messageList;

    Unbinder unbinder;
    @BindView(R.id.connect_tips)
    AVLoadingIndicatorView connectTips;

    @BindView(R.id.cos_text_title)
    TextView cosTextTitle;
    @BindView(R.id.gap_line)
    View gapLine;
    @BindView(R.id.list_bg_relative)
    RelativeLayout listBgRelative;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MessageListAdapter adapterFavor = null;
    private ArrayList<MessageListModel> mListContact = null;
    private ViewHolder holder = null;
    private LinearLayout listHeadView;
    private int maxOffsetValue = 1000;
    private String token = "";
    public MsgFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayCenterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MsgFragment newInstance(String param1, String param2) {
        MsgFragment fragment = new MsgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        toolbar.setVisibility(View.GONE);
        cosTextTitle.setText(R.string.tab_msg);
        //重新设置菜单
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_msg, container, false);
        unbinder = ButterKnife.bind(this, view);
        listHeadView = (LinearLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.fragment_msg_list_header, null);
        messageList.addHeaderView(listHeadView);

        holder = new ViewHolder(listHeadView);
        holder.searchAcF.setOnClickListener(this);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        initPush();
        startWsService();
    }

    //登录成功后才初始化push服务
    private void initPush() {
        CryTool tool = new CryTool();
        String accountSha256 = tool.shaAccount(RunningData.getInstance().getCurrentAccount());
        ChatAppApplication.getInstances().setPushAlias(accountSha256);
        loginAuto();
    }

    private void initView() {
        messageList.setOnItemClickListener(this);
        messageList.setXListViewListener(this);
        messageList.setPullRefreshEnable(false);
        messageList.setPullLoadEnable(false);
        messageList.hideHeadViewLayout();
        gapLine.setVisibility(View.INVISIBLE);
       // listGapVlaue();
    }

    private void initData() {
        RunningData.getInstance().initContactList();
        if (mListContact == null) {
            mListContact = RunningData.getInstance().getMsgList();
        }
        if (adapterFavor == null) {
            int heightLlistView = RunningData.getInstance().getAppShowHeight() - RunningData.getInstance().getActionBarHeight() - DensityUtil.dip2px(getContext(), 75) - DensityUtil.dip2px(getContext(), 80);
            adapterFavor = new MessageListAdapter(getActivity(), mListContact, heightLlistView, DensityUtil.dip2px(getContext(), 80), true);
            messageList.setAdapter(adapterFavor);
            adapterFavor.setOnRightItemClickListener(this);
            freshListMsthod();
        }
        maxOffsetValue = DensityUtil.dip2px(getContext(), 40);
    }

    @Override
    public void onRightItemClick(View v, int position) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        deleteOneList(v, position);
    }

    @Override
    public void onRefresh() {
        stopFresh();
    }

    @Override
    public void onLoadMore() {
        stopFresh();
    }


    private void stopFresh() {
        messageList.stopRefresh();
        messageList.stopLoadMore();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        if ((position - 3) < (mListContact.size() - 1)) {
            MessageListModel listModel = mListContact.get(position - 3);

            if(listModel.isGroupMessage == true){
                ContactModel model = new ContactModel(listModel.avatar_url, listModel.username, listModel.label, listModel.account, listModel.introduction, "", "", true);
                Intent intent = new Intent(getActivity(), MsgDetailActivity.class);
                intent.putExtra("msgReceiver", model);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);

                listModel.isreaded = false;

            }else{
                ContactModel model = new ContactModel(listModel.avatar_url, listModel.username, listModel.label, listModel.account, listModel.introduction, listModel.public_key, listModel.friend_id, false);
                Intent intent = new Intent(getActivity(), MsgDetailActivity.class);
                intent.putExtra("msgReceiver", model);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);

                listModel.isreaded = false;
            }


        }
    }


    public void onClick(View view) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.search_ac_f:
                Intent intent = new Intent(getActivity(), SearchLocalActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            default:
                break;
        }
    }

    static class ViewHolder {
        @BindView(R.id.search_ac_f)
        LinearLayout searchAcF;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    //*************************  IM ********************* //
    private Intent serviceIntent = null;
    private WsChatService wsChatService = null;

    private void startWsService() {
        Logger.d("开始连接");
        serviceIntent = new Intent(getActivity(), WsChatService.class);
        //启动并绑定service
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }

    private void stopWsService() {
        if (wsChatService != null) {
            wsChatService = null;
            getActivity().unbindService(this);
        }
        getActivity().stopService(serviceIntent);
    }

    /**
     * 绑定 Service  WsChatService
     **/
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        wsChatService = ((WsChatService.WsChatServiceBinder) binder).getService();
        wsChatService.setMessageListCallBacks(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Logger.d("list 解除绑定了 Ws_chat_service");
    }
    /**
     *  WsService 发送方法*
     * **/

    /**
     * WsService  的回掉通知
     * 根据回掉处理数据
     **/
    @Override
    public void chatConnected() {

        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 连接成功，更改UI
                connectTips.setVisibility(View.INVISIBLE);
                cosTextTitle.setText("接收中...");
            }
        });
        getOfflineMsg();
    }

    private void getOfflineMsg() {
        SendToGetOffLineMessage body = new SendToGetOffLineMessage(RunningData.getInstance().getCurrentAccount());
        wsChatService.getOfflineMsg(body);
    }

    @Override
    public void connectSucTips() {
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 连接成功，更改UI
                connectTips.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void chatDisconnected() {
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 连接断开，更改UI
                connectTips.setVisibility(View.VISIBLE);
                connectTips.show();
                cosTextTitle.setText(R.string.tab_msg);
            }
        });
    }

    @Override
    public void freshMsgListView() {
        //来了新消息
        freshListMsthod();
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
    public void noOfflineMessage() {
        //没有离线消息，改变UI， "获取离线消息" -->  "对话"
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectTips.setVisibility(View.INVISIBLE);
                cosTextTitle.setText(R.string.tab_msg);
            }
        });
    }

    @Override
    public void applyForFriend(ReceiveSpecialInfo body) {
        AcNotifyCallbackManager.getInstance().contact_callback_apply();
    }

    @Override
    public void allowMeASFriend(ReceiveSpecialInfo body) {
        startFreshContact(body);
    }

    @Override
    public void someOneDeleteMe(ReceiveSpecialInfo body) {
        AcNotifyCallbackManager.getInstance().contact_callback_fresh();
        int index = -1;
        for (int i = 0; i < mListContact.size(); i++){
            String account = mListContact.get(i).account;
            if (account.equalsIgnoreCase(body.MessageReceive)){
                index = i;
                break;
            }
        }
        if (index >= 0) {
            mListContact.remove(index);

            freshListMsthod();
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
    public void messageSendSuccess(MessageSendState body) {
        //不做处理
    }

    @Override
    public void messageSendFailed(MessageSendState body) {
        //不做处理
    }

    @Override
    public void comeANewIMMessage(SendMessageEnBody body) {
        //来了一个私信
        //在 freshMsgListView 处理了
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cosTextTitle.setText(R.string.tab_msg);
            }
        });
    }

    @Override
    public void userIsTyping(ReceiverIsTypingBody body) {
        //不做处理
    }


    //********list cache
    private void startTimerCache() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                cacheAllData();
            }
        }, 30000, 60000);
    }

    private void cacheAllData() {
        RunningData.getInstance().cacheMsgListData();
    }

    private void deleteOneList(View v, int position) {
        if (position >= 0 && position < (mListContact.size() - 1)) {
            mListContact.remove(position);
        }else{
            mListContact.remove(mListContact.size() - 1);
        }
        messageList.hiddenRight();
        freshListMsthod();
    }


    @Override
    public void onStop() {
        super.onStop();
        freshListMsthod();
        cacheAllData();
        if (!isAppOnForeground()) {
            resetPushNumber();
        }
    }

    private void resetPushNumber() {
        SendToResetPushNumber resetbody = new SendToResetPushNumber(RunningData.getInstance().getCurrentAccount());
        wsChatService.resetPushNumber(resetbody);
    }

    public boolean isAppOnForeground() {

        // / Returns a list of application processes that are running on the
        ActivityManager activityManager = (ActivityManager) getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getActivity().getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        stopWsService();
        cacheAllData();
        RunningData.getInstance().clearmsgList();
        Logger.d("++++++++++++++++ onDestroy ++++++++++++");
    }

    private void loginAuto() {
        //自动重新登录
        Timer loginTimer = new Timer();
        loginTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                RunningData.getInstance().reLogInMethod();
            }
        }, 10000);
    }


    private void startFreshContact(final ReceiveSpecialInfo body){
        token = RunningData.getInstance().getToken();
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/v2/getFriendList")
                .tag(this)
                .cacheMode(CacheMode.NO_CACHE)
                .params("token", token)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onFinish() {
                    }
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        if (response.body().code == 1) {
                            try {
                                JSONArray mList = Convert.formatToJson(response.body().data).getJSONArray("list");
                                if (mList != null) {
                                    processResult(mList, body);
                                }
                            } catch (Exception ex) {}
                        }
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {}
                });
    }
    private void processResult(JSONArray mList, final ReceiveSpecialInfo body){
        if (mList != null && mList.length() > 0){
            //1。0 刷新消息列表
            freshMsgListToLatest(mList, body);

            //*** 2.0 刷新联系人列表
            AcNotifyCallbackManager.getInstance().contact_callback_apply_with_data(mList);
        }
        freshListMsthod();
    }

    private void freshMsgListToLatest(JSONArray mList, final ReceiveSpecialInfo body){
        Gson gson = new Gson();
        if (mListContact != null && mListContact.size() > 1){
            try{
                MessageListModel modeList = null;
                boolean isGetValue = false;
                for (int i = 0; i < mListContact.size() - 1; i++){
                    modeList = mListContact.get(i);
                    if (modeList.account.equalsIgnoreCase(body.MessageReceive)){
                        isGetValue = true;
                        break;
                    }
                }
                if (isGetValue && modeList != null){
                    for (int i = 0; i < mList.length(); i++){
                        String valueString = mList.getJSONObject(i).toString();
                        ContactModel info = gson.fromJson(valueString, ContactModel.class);
                        if (info.label == null || info.label.equals("")){
                            info.label = info.username;
                        }
                        if (modeList.account.equalsIgnoreCase(info.account)){
                            if (!info.public_key.equals("")){
                                modeList.public_key = info.public_key;
                                modeList.username = info.username;
                                modeList.label = info.label;
                                modeList.avatar_url = info.avatar_url;
                            }
                            break;
                        }
                    }
                }
            }catch (Exception ex) {
                Logger.d("freshMsgListToLatest 数据出错");
            }
        }
    }
}
