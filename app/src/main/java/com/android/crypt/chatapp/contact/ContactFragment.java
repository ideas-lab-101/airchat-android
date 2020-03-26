package com.android.crypt.chatapp.contact;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.group.MyGroupActivity;
import com.android.crypt.chatapp.msgList.model.MessageListModel;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ACNotifyMessage;
import com.android.crypt.chatapp.utility.Common.AcNotifyCallbackManager;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.DensityUtil;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.baoyz.actionsheet.ActionSheet;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.okgo.utils.Convert;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.db.CacheManager;
import com.lzy.okgo.model.Response;
import com.android.crypt.chatapp.BaseFragment;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactCacheModel;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.contact.adapter.CharIndexView;
import com.android.crypt.chatapp.contact.adapter.ContactListAdapter;
import com.android.crypt.chatapp.contact.cn.CNPinyin;
import com.android.crypt.chatapp.contact.cn.CNPinyinFactory;
import com.android.crypt.chatapp.finder.ApplyListActivity;
import com.android.crypt.chatapp.finder.SearchLocalActivity;
import com.android.crypt.chatapp.user.FriendCardActivity;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.widget.swipexlistview.XListView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;


public class ContactFragment extends BaseFragment implements
        AdapterView.OnItemClickListener, XListView.IXListViewListener , View.OnClickListener, ActionSheet.ActionSheetListener, ACNotifyMessage {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.contactList_lv)
    XListView contactListLv;

    Unbinder unbinder;
    @BindView(R.id.iv_main)
    CharIndexView ivMain;
    @BindView(R.id.tv_index)
    TextView tvIndex;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<CNPinyin<ContactModel>> mContactContact;
    private Subscription subscription;
    private ContactListAdapter adapterFavor;
    private String token = "";
    private boolean canGetContact = true;
    private boolean isWebSuccess = false;
    private ViewHolder holder;
    public ContactFragment() {
        // Required empty public constructor
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
    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
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
        AcNotifyCallbackManager.getInstance().setContactCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        unbinder = ButterKnife.bind(this, view);
        //***添加表头
        LinearLayout headView = (LinearLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.fragment_contact_header, null);
        contactListLv.addHeaderView(headView);
//        View headView = onClick((LayoutInflater)getLayoutInflater()).inflate(R.layout.fragment_contact_header, container, false);
//        contactListLv.addHeaderView(headView); 这个是错的
        if (holder == null){
            holder = new ViewHolder(headView);
            holder.myApplyAcF.setOnClickListener(this);
            holder.searchAcF.setOnClickListener(this);
            holder.myGroup.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void freshFriendList() {
        getContactList();
    }

    @Override
    public void freshFriendListWithData(JSONArray mList) {
        processListData(mList);
    }

    @Override
    public void newFriendApply() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 连接断开，更改UI
                holder.messageTips.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 连接断开，更改UI
                holder.messageTips.setVisibility(View.GONE);
            }
        });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //找到activity的toolbar,重新设置title和menu
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle("");
        cosTitleTextView.setText(R.string.tab_contact);
        cosTitleTextView.setVisibility(View.VISIBLE);
        inflater.inflate(R.menu.menu_main, menu);
        //重新设置菜单
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan) {
            showActionSheet();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showActionSheet(){
        ActionSheet.createBuilder(this.getContext(), this.getFragmentManager())
                .setCancelButtonTitle("取消")
                .setOtherButtonTitles("刷新联系人列表")
                .setCancelableOnTouchOutside(true)
                .setListener(this).show();
    }
    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            getContactList();
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        token = RunningData.getInstance().getToken();
        initView();
        addIndexView();
        getContactList();
    }

    private void initView() {
        contactListLv.setOnItemClickListener(this);
        contactListLv.setXListViewListener(this);
        contactListLv.setPullRefreshEnable(true);
        contactListLv.setPullLoadEnable(false);

        if (mContactContact == null){
            mContactContact = RunningData.getInstance().getContactList();
        }
        int heightLlistView = RunningData.getInstance().getAppShowHeight() - RunningData.getInstance().getActionBarHeight() - DensityUtil.dip2px(getContext(), 180);
        adapterFavor = new ContactListAdapter(getActivity(), mContactContact, heightLlistView, DensityUtil.dip2px(getContext(), 80), true);
        contactListLv.setAdapter(adapterFavor);
        adapterFavor.notifyDataSetChanged();

    }



    private void addIndexView(){
        ivMain.setOnCharIndexChangedListener(new CharIndexView.OnCharIndexChangedListener() {
            @Override
            public void onCharIndexChanged(char currentIndex) {
                for (int i = 0; i < mContactContact.size() - 1; i++) {
                    if (mContactContact.get(i).getFirstChar() == currentIndex) {
                        contactListLv.smoothScrollToPositionFromTop(i, 200, 100);
                        return;
                    }
                }
            }

            @Override
            public void onCharIndexSelected(String currentIndex) {
                if (currentIndex == null) {
                    tvIndex.setVisibility(View.INVISIBLE);
                } else {
                    tvIndex.setVisibility(View.VISIBLE);
                    tvIndex.setText(currentIndex);
                }
            }
        });
    }


    private void getContactList() {
        if (canGetContact == false){
            return;
        }
        canGetContact = false;
        isWebSuccess = false;
        OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "contact/v2/getFriendList")
                .tag(this)
                .cacheKey("contact-getFriendList")
                .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                .params("token", token)
                .execute(new JsonCallback<CodeResponse>() {
                    @Override
                    public void onFinish() {
                        stopFresh();
                        canGetContact = true;
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (isWebSuccess == false){
                            CacheEntity<?> cacheEntity = CacheManager.getInstance().get("contact-getFriendList");
                            if (cacheEntity != null){
                                try {
                                    CodeResponse codeResponse = (CodeResponse)cacheEntity.getData();
                                    JSONArray mList = Convert.formatToJson(codeResponse.data).getJSONArray("list");
//                                    final List<Map<String, String>> mList = Convert.formatToListMap(dataList);
                                    if (mList != null) {
                                        processResult(mList);
                                    }
                                }catch (Exception ex) {}
                            }
                        }
                    }
                    @Override
                    public void onSuccess(Response<CodeResponse> response) {
                        stopFresh();
                        canGetContact = true;
                        if (response.body().code == 1) {
                            isWebSuccess = true;
                            try {
                                JSONArray mList = Convert.formatToJson(response.body().data).getJSONArray("list");
                                if (mList != null) {
                                    processResult(mList);
                                } else {
                                    makeSnake(toolbar, response.body().msg, R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                                }
                            } catch (Exception ex) {}
                        } else {
                            RunningData.getInstance().reLogInMethod();
                        }
                    }
                    @Override
                    public void onError(Response<CodeResponse> response) {
                        stopFresh();
                        canGetContact = true;
                        response.getException().printStackTrace();
                        makeSnake(toolbar, "网络出错", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
                    }
                });
    }

    private void processResult(JSONArray mList){
        if (mList.length() > 0){
            getPinyinList(mList);
        }else{
            //***添加一个空白item
            mContactContact.clear();;
            ContactModel data = new ContactModel();
            data.isBlank = true;
            CNPinyin<ContactModel> pinyin = new CNPinyin(data);
            mContactContact.add(pinyin);
            adapterFavor.notifyDataSetChanged();
        }
    }

    private void getPinyinList(final JSONArray mList) {
        subscription = Observable.create(new Observable.OnSubscribe<List<CNPinyin<ContactModel>>>() {
            @Override
            public void call(Subscriber<? super List<CNPinyin<ContactModel>>> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    List<CNPinyin<ContactModel>> contactList = CNPinyinFactory.createCNPinyinList(processListData(mList));
                    Collections.sort(contactList);
                    subscriber.onNext(contactList);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<CNPinyin<ContactModel>>>() {
                    @Override
                    public void onCompleted() {
                        reDrawIndexView();

                        //***添加一个空白item
                        ContactModel data = new ContactModel();
                        data.isBlank = true;
                        CNPinyin<ContactModel> pinyin = new CNPinyin(data);
                        mContactContact.add(pinyin);
                        adapterFavor.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<CNPinyin<ContactModel>> cnPinyins) {
                        mContactContact.clear();
                        mContactContact.addAll(cnPinyins);
                        adapterFavor.notifyDataSetChanged();
                        changePub_key(cnPinyins);
                    }
                });
    }

    private List<ContactModel> processListData(JSONArray mList) {
        List<ContactModel> contactList = new ArrayList<>();
        Gson gson = new Gson();
        try{
            for (int i = 0; i < mList.length(); i++) {

                String valueString = mList.getJSONObject(i).toString();
                ContactModel info = gson.fromJson(valueString, ContactModel.class);
                if (info.label == null || info.label.equals("")){
                    info.label = info.username;
                }
                contactList.add(info);
            }
            return contactList;
        }catch (Exception ex) {
            makeSnake(toolbar, "数据出错", R.mipmap.toast_alarm, Snackbar.LENGTH_SHORT);
            return contactList;
        }

    }

    private void changePub_key(List<CNPinyin<ContactModel>> cnPinyins){
        ArrayList<MessageListModel> mListContact = RunningData.getInstance().getMsgList();
        for (int i = 0; i < mListContact.size(); i++){
            MessageListModel modeList = mListContact.get(i);
            for (int j = 0; j < cnPinyins.size(); j++){
                ContactModel mMap = cnPinyins.get(j).data;
                if (modeList.account.equalsIgnoreCase(mMap.account)){
                    if (!mMap.public_key.equals("")){
                        modeList.public_key = mMap.public_key;
                        modeList.username = mMap.username;
                        modeList.label = mMap.label;
                        modeList.avatar_url = mMap.avatar_url;
                    }
                    break;
                }
            }
        }
    }

    private String objToString(Object obj) {
        if (obj == null) {
            return "";
        } else {
            return obj.toString();
        }
    }

    private void reDrawIndexView() {
        char[] chars = new char[28];
        int curIndex = 0;
        for (int i = 0; i < mContactContact.size() - 1; i++) {
            char catalog = mContactContact.get(i).getFirstChar();
            if (i == 0) {
                chars[0] = catalog;
            } else {
                if (chars[curIndex] != catalog) {
                    curIndex = curIndex + 1;
                    chars[curIndex] = catalog;
                }
            }
        }
        chars[curIndex + 1] = '#';

        char[] results = new char[curIndex + 2];
        for (int i = 0; i < curIndex + 2; i++) {
            results[i] = chars[i];
        }

        ivMain.CHARS = results;
        ivMain.requestLayout();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        if ((position - 2) < mContactContact.size() - 1) {
            ContactModel mMap = mContactContact.get(position - 2).data;
            Intent intent = new Intent(getActivity(), FriendCardActivity.class);
            intent.putExtra("friendInfo", mMap);
            intent.putExtra("pushDirect", "message");
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_right,
                    R.anim.out_to_left);
        }
    }

    @Override
    public void onRefresh() {
        getContactList();
        stopFresh();
    }

    @Override
    public void onLoadMore() {
        stopFresh();
    }

    private void stopFresh() {
        contactListLv.stopRefresh();
        contactListLv.stopLoadMore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        cacheFriendList();
    }

    public void onClick(View view) {
        if (!ClickUtils.isFastClick()) {
            return;
        }
        Intent intent;
        switch (view.getId()) {
            case R.id.my_apply_ac_f:
                intent = new Intent(getActivity(), ApplyListActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.search_ac_f:
                intent = new Intent(getActivity(), SearchLocalActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;

            case R.id.my_group:
                intent = new Intent(getActivity(), MyGroupActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            default:
                break;
        }
    }


    static class ViewHolder {
        @BindView(R.id.my_apply_ac_f)
        LinearLayout myApplyAcF;
        @BindView(R.id.my_group)
        LinearLayout myGroup;

        @BindView(R.id.message_tips)
        TextView messageTips;


        @BindView(R.id.search_ac_f)
        LinearLayout searchAcF;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!isAppOnForeground()) {
            //app 进入后台
            cacheFriendList();
        }
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

    public void  cacheFriendList(){
        try{
            ArrayList<ContactCacheModel> cacheModelList = new ArrayList<>();
            int totalSizeCache = mContactContact.size() - 1;
            if (totalSizeCache < 0){
                totalSizeCache = 0;
            }
            if (totalSizeCache > 100){
                totalSizeCache = 100;
            }
            for (int i = 0; i < totalSizeCache; i++){
                CNPinyin<ContactModel> pinyin = mContactContact.get(i);
                ContactModel data = pinyin.data;
                ContactCacheModel cacheModel = new ContactCacheModel();
                cacheModel.avatar_url = data.avatar_url;
                cacheModel.username  = data.username;
                cacheModel.label = data.label;
                cacheModel.account = data.account;
                cacheModel.introduction = data.introduction;
                cacheModel.public_key = data.public_key;
                cacheModel.friend_id = data.friend_id;

                cacheModel.firstChar = pinyin.firstChar;
                cacheModel.firstChars = pinyin.firstChars;
                cacheModel.pinyinsTotalLength = pinyin.pinyinsTotalLength;

                String[] pinyinstring = pinyin.pinyins;
                int pinyinsLength = pinyinstring.length;
                if (pinyinsLength > 3){
                    pinyinsLength = 3;
                }
                String pinsValue = pinyinstring[0];
                for (int j = 1; j < pinyinsLength; j++){
                    pinsValue = pinsValue + "_" + pinyinstring[j];
                }
                pinsValue = pinsValue + "_";
                cacheModel.pinyins = pinsValue;
                cacheModelList.add(cacheModel);
            }
            Gson gson = new Gson();
            String friendList = gson.toJson(cacheModelList);
            CacheTool.getInstance().cacheObject(ObjectCacheType.friend_list, friendList);
        }catch (Exception e){}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RunningData.getInstance().clearmListContact();
    }
}

