package com.android.crypt.chatapp.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.crypt.chatapp.BaseFragment;
import com.android.crypt.chatapp.InfoSetting.LoadPrivateKeyActivity;
import com.android.crypt.chatapp.PhotoViewer.Model.ImageModel;
import com.android.crypt.chatapp.contact.Model.ContactModel;
import com.android.crypt.chatapp.qrResult.MyQRCodeActivity;
import com.android.crypt.chatapp.qrResult.QScanOtherResultActivity;
import com.android.crypt.chatapp.qrResult.ZBarScanActivity;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.ClickUtils;
import com.android.crypt.chatapp.utility.Common.NotifyUtils;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.android.crypt.chatapp.InfoSetting.InfoDataActivity;
import com.android.crypt.chatapp.InfoSetting.MyKeysOpActivity;
import com.android.crypt.chatapp.InfoSetting.SettingActivity;
import com.android.crypt.chatapp.PhotoViewer.PhotoViewerActivity;
import com.android.crypt.chatapp.finder.SearchFriendActivity;
import com.android.crypt.chatapp.guide.AppHelpActivity;
import com.android.crypt.chatapp.user.Model.UserInfo;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;


public class UserFragment extends BaseFragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.iv_avatar)
    ImageView ivAvatar;
    @BindView(R.id.tv_profileName)
    TextView tvProfileName;
    @BindView(R.id.iv_qrCode)
    ImageView ivQrCode;
    @BindView(R.id.ll_friend)
    LinearLayout llFriend;
    @BindView(R.id.ll_scan)
    LinearLayout llScan;
    @BindView(R.id.ll_sec)
    LinearLayout llSec;
    @BindView(R.id.ll_account)
    LinearLayout llAccount;
//    @BindView(R.id.blank_view)
//    LinearLayout blankView;

    @BindView(R.id.fresh_view)
    PullRefreshLayout freshView;
    Unbinder unbinder;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String token;


    public UserFragment() {
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
    public static UserFragment newInstance(String param1, String param2) {
        UserFragment fragment = new UserFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }



    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //找到activity的toolbar,重新设置title和menu
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle("");
        cosTitleTextView.setText("");
        cosTitleTextView.setVisibility(View.GONE);
        //重新设置菜单
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        token = RunningData.getInstance().getToken();
        //getPrivateXml(ParameterUtil.CUR_ACCOUNT_MANAGEMENT_XML, "token", "");// 用户id
        addListener();
        initView();
    }

    private void addListener(){
        freshView.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFresh();
            }
        });

    }

    public void initView() {
        Gson gson = new Gson();
        String userInfoString = CacheTool.getInstance().getCacheObject(ObjectCacheType.user_info);
        if (userInfoString != "") {
            try {
                UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class);
                if (userInfo != null){
                    RequestOptions requestOptions = new RequestOptions().centerCrop().placeholder(R.mipmap.default_head);

                    Glide.with(this)
                            .load(RunningData.getInstance().echoMainPicUrl() + userInfo.avatar_url)
                            .apply(requestOptions)
                            .into(ivAvatar);

                    tvProfileName.setText(userInfo.username);
                }
            }catch (Exception e){
                Logger.d("UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class) 错误");
            }
        }

        checkIsPushOpened();
    }

    private void checkIsPushOpened(){
        final Context mContext = getContext();
        if (NotifyUtils.isNotificationEnabled(mContext) == false){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle("推送未开启>.<")
                    .setMessage("建议打开推送可以及时获得消息")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    })
                    .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NotifyUtils.gotoSet(mContext);
                        }
                    });
            builder.create().show();
        }
    }

    public void getFresh() {
        freshView.setRefreshing(false);
        initView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.ll_friend, R.id.ll_scan, R.id.ll_sec, R.id.ll_account, R.id.iv_qrCode, R.id.iv_username, R.id.iv_avatar, R.id.ll_help, R.id.ll_share})
    public void onViewClicked(View view) {
        if (!ClickUtils.isFastClick()) {
           return;
        }
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.ll_friend:
                intent = new Intent(getActivity(), SearchFriendActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.ll_scan:
                startQrCode();
                break;
            case R.id.ll_sec:
                intent = new Intent(getActivity(), MyKeysOpActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.iv_qrCode:
            case R.id.iv_username:
                intent = new Intent(getActivity(), InfoDataActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
            case R.id.ll_account:
                intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;

            case R.id.iv_avatar:
                Gson gson = new Gson();
                String userInfoString = CacheTool.getInstance().getCacheObject(ObjectCacheType.user_info);
                UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class);
                String image_url = RunningData.getInstance().echoMainPicUrl() + userInfo.avatar_url;
                intent = new Intent(getActivity(), PhotoViewerActivity.class);

                ArrayList<ImageModel> image_url_list = new ArrayList<ImageModel>();
                ImageModel model = new ImageModel(image_url, "");
                image_url_list.add(model);
                intent.putExtra("image_url", image_url_list);
                intent.putExtra("cur_index", 0);
                intent.putExtra("is_encode", false);

                startActivity(intent);
                getActivity().overridePendingTransition(0,0);


                break;
            case  R.id.ll_help:
                intent = new Intent(getActivity(), AppHelpActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;

            case R.id.ll_share:
                shareMethod();
                break;
            default:
                break;
        }
    }

    private void shareMethod(){
        Gson gson = new Gson();
        String userInfoString = CacheTool.getInstance().getCacheObject(ObjectCacheType.user_info);
        if (userInfoString != "") {
            try {
                UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class);

                ContactModel mMap = new ContactModel(userInfo.avatar_url, userInfo.username, "", userInfo.login_name, userInfo.introduction, "", "", false);
                Intent intent  = new Intent(getActivity(), MyQRCodeActivity.class);
                intent.putExtra("friendInfo", mMap);
                intent.putExtra("isMine", true);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }catch (Exception e){
                Logger.d("UserInfo userInfo = gson.fromJson(userInfoString, UserInfo.class) 错误");
            }
        }

    }


    private void startQrCode(){

//        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        /*ZxingConfig是配置类
        *可以设置是否显示底部布局，闪光灯，相册，
        * 是否播放提示音  震动
        * 设置扫描框颜色等
        * 也可以不传这个参数
        * */
//        ZxingConfig config = new ZxingConfig();
//        config.setPlayBeep(true);//是否播放扫描声音 默认为true
//        config.setShake(true);//是否震动  默认为true
//        config.setDecodeBarCode(true);//是否扫描条形码 默认为true
//        config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
//        config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
//        config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
//        config.setFullScreenScan(true);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
//        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);

        //***Zxing太慢 使用Zbar
        Intent intent = new Intent(getActivity(), ZBarScanActivity.class);
        startActivityForResult(intent, 1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra("qr_code_string");//Constant.CODED_CONTENT
                if (content.startsWith("1#")){
                    Intent intent = new Intent(getActivity(), LoadPrivateKeyActivity.class);
                    intent.putExtra("qr_result", content.substring(2));
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }else if(content.startsWith("http")){
                    Logger.d("分支2 " + content);
                    int index = content.indexOf("?token=");
                    if (index > 0 && (index + 7) < content.length()){
                        String strResult_token_en = content.substring(index + 7);
                        CryTool tool = new CryTool();
                        Logger.d("aesDe 4");
                        String strResult_token = tool.aesDeWith(strResult_token_en, RunningData.getInstance().getInnerAESKey());
                        if (strResult_token != null && !strResult_token.equals("")){
                            if (strResult_token.startsWith("2#")){
                                String strResultValue = strResult_token.substring(2);
                                Logger.d("strResultValue = " + strResultValue);
                                String[] strResultArr = strResultValue.split("&");
                                String timeValue = strResultArr[0];
                                String account = strResultArr[strResultArr.length - 1];
                                String timeLimit = "";
                                if (strResultArr.length >= 3){
                                    timeLimit = strResultArr[1];
                                }
                                friendScan(timeValue, account, timeLimit);
                                return;

                            }
                        }
                    }
                    otherResult(content);
                }else{
                    otherResult(content);
                }

            }else{
                makeSnake(ivAvatar, "没有识别到二维码", R.mipmap.toast_alarm
                        , Snackbar.LENGTH_LONG);
            }
        }
    }

    private void otherResult(String content){
        Intent intent = new Intent(getActivity(), QScanOtherResultActivity.class);
        intent.putExtra("qr_result", content);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void friendScan(String timeValue, String account, String timeLimitTemp){
        long curtime = System.currentTimeMillis() / 1000;
        long codeTime = string2int(timeValue);
        long timeLimit = 100 * 24 * 3600;
        if (!timeLimitTemp.equals("")){
            long timeInner = string2int(timeLimitTemp);
            if (timeInner > 0){
                timeLimit = timeInner;
            }
        }
        long timegap =  (curtime  - codeTime) + 100;
        Logger.d("curtime " + curtime + " - codeTime " + codeTime + " = " + timegap + " timeLimit = " + timeLimit);

        if (timegap >= timeLimit){
            makeSnake(ivAvatar, "二维码过期", R.mipmap.toast_alarm
                    , Snackbar.LENGTH_LONG);
            return;
        }

        Intent intent = new Intent(getActivity(), SearchFriendActivity.class);
        intent.putExtra("account", account);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

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
