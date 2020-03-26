package com.android.crypt.chatapp.msgDetail.IMTools;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.group.model.GroupMember;
import com.android.crypt.chatapp.msgDetail.model.CollectEmoji;
import com.android.crypt.chatapp.utility.Cache.CacheClass.ObjectCacheType;
import com.android.crypt.chatapp.utility.Cache.CacheTool;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Crypt.CryTool;
import com.android.crypt.chatapp.utility.okgo.callback.JsonCallback;
import com.android.crypt.chatapp.utility.okgo.model.CodeResponse;
import com.baoyz.actionsheet.ActionSheet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.msgDetail.adapter.EmojiAdapter;
import com.qiniu.android.common.AutoZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.devio.takephoto.model.TImage;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddEmojiActivity extends BaseActivity implements AdapterView.OnItemClickListener, ActionSheet.ActionSheetListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.emoji_content)
    GridView emojiContent;

    private ArrayList<CollectEmoji> show_EmojiList;
    private EmojiAdapter emojiAdapter;
    String qiniuToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emoji);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.title_bar_emoji);
        setSupportActionBar(toolbar);
        initData();
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

    private void initData() {
        if (show_EmojiList == null) {
            show_EmojiList = new ArrayList<CollectEmoji>();
        }
        show_EmojiList.clear();
        show_EmojiList.add(new CollectEmoji());

        Gson gson = new Gson();
        String cacheString = CacheTool.getInstance().getCacheObject(ObjectCacheType.collect_emoji);
        if (cacheString != null && cacheString != "") {
            ArrayList<CollectEmoji> cacheModel = gson.fromJson(cacheString, new TypeToken<ArrayList<CollectEmoji>>() {
            }.getType());
            if(cacheModel != null && cacheModel.size() > 0){
                show_EmojiList.addAll(cacheModel);
            }
        }

    }

    private void initView() {
        emojiContent.setOnItemClickListener(this);
        emojiAdapter = new EmojiAdapter(this, show_EmojiList, 1);
        emojiContent.setNumColumns(4);
        emojiContent.setAdapter(emojiAdapter);
    }

    private int tap_index = 0;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < show_EmojiList.size()) {
            if (position == 0) {
                if (show_EmojiList.size() <= 100) {
                    Intent intent = new Intent(this, ChooseEmojiActivity.class);
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else {
                    makeSnake(emojiContent, "最多添加100个表情", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                }
            } else {
                tap_index = position;
                ActionSheet.createBuilder(this, getSupportFragmentManager()).setCancelButtonTitle("取消").setOtherButtonTitles("移动到最前", "删除当前图片").setCancelableOnTouchOutside(true).setListener(this).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 100) {
                ArrayList<TImage> imagesUrl = (ArrayList<TImage>) data.getSerializableExtra("images");
                dealCollectEMoji(imagesUrl);
            }
        }
    }


    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            if (tap_index > 0) {
                moveImageToTop(tap_index);
                tap_index = 0;
            }
        } else if (index == 1) {
            if (tap_index > 0) {
                deleteEmojiFile(tap_index);
                tap_index = 0;
            }
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {
        tap_index = 0;
    }

    private void dealCollectEMoji(ArrayList<TImage> imagesUrl) {
        String imgLocalUrl = imagesUrl.get(0).getCompressPath();
        String uploadKey = getQiniuKey(imgLocalUrl);
        getUploadToken(uploadKey, imgLocalUrl);
    }


    private void getUploadToken(final String uploadKey, final String imgLocalUrl) {
        if (qiniuToken == "") {
            String token = RunningData.getInstance().getToken();
            createDialog("上传表情包...");
            OkGo.<CodeResponse>post(RunningData.getInstance().server_url() + "system/v2/getUploadToken")
                    .tag(this)
                    .cacheMode(CacheMode.NO_CACHE)
                    .params("token", token)
                    .params("type", 3)
                    .execute(new JsonCallback<CodeResponse>() {
                        @Override
                        public void onSuccess(Response<CodeResponse> response) {
                            qiniuToken = response.body().data.toString();
                            startUpload(uploadKey, imgLocalUrl);
                        }

                        @Override
                        public void onError(Response<CodeResponse> response) {
                            response.getException().printStackTrace();
                        }
                    });
        } else {
            startUpload(uploadKey, imgLocalUrl);
        }

    }

    private void startUpload(final String uploadKey, final String imgLocalUrl) {
        Configuration config = new Configuration.Builder().zone(AutoZone.autoZone).build();
        UploadManager uploadManager = new UploadManager(config);
        String token = qiniuToken;
        uploadManager.put(imgLocalUrl, uploadKey, token, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject res) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (info.isOK()) {
                    Logger.d("Upload Success");
                    processEmoji(uploadKey, imgLocalUrl);

                } else {
                    Logger.d("Upload Fail");
                }
            }
        }, null);
    }


    private void processEmoji(final String uploadKey, final String imgLocalUrl) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        op.inSampleSize = 1;
        BitmapFactory.decodeFile(imgLocalUrl, op);

        CollectEmoji model = new CollectEmoji();
        model.emojiUrl = uploadKey;
        model.emojiBaseUrl = "http://engramuserfile.paralzone.com/";
        model.emojiName = "[动画表情]";
        model.excessInfo = op.outWidth + "_" +op.outHeight;

        show_EmojiList.add(model);
        freshListMsthod();
        cacheCollectEmoji();
    }


    private String getQiniuKey(final String imageUrl) {
        String curAccount = RunningData.getInstance().getCurrentAccount();

        int randNumber = (int) (Math.random() * 1000);
        long time = System.currentTimeMillis();
        String timeNumberList = String.valueOf(time);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String dateString = simpleDateFormat.format(date);

        String surName = "png";
        String[] surNmaeArray = imageUrl.toString().split("\\.");
        if (surNmaeArray.length >= 1) {
            surName = surNmaeArray[surNmaeArray.length - 1];
        }
        String key = "collectEmoji/" + curAccount + "/" + dateString + "/" + timeNumberList + "/" + randNumber + "0." + surName;

        return key;
    }


    private void freshListMsthod() {
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emojiAdapter.notifyDataSetChanged();
            }
        });
    }

    private void cacheCollectEmoji(){
        if(show_EmojiList != null && show_EmojiList.size() >= 1){
            ArrayList<CollectEmoji> cacheModel = new  ArrayList<CollectEmoji>();
            for(int i = 0; i < show_EmojiList.size(); i++){
                if(i > 0){
                    cacheModel.add(show_EmojiList.get(i));
                }
            }



            Gson gson = new Gson();
            String cacheString = gson.toJson(cacheModel);
            CacheTool.getInstance().cacheObject(ObjectCacheType.collect_emoji, cacheString);
        }
    }


    //****处理选中的图片
    private void deleteEmojiFile(int index){
        if (index > 0 && index < show_EmojiList.size()){
            show_EmojiList.remove(index);
            freshListMsthod();
            cacheCollectEmoji();
        }
    }

    private void moveImageToTop(int index){
        if (index > 0 && index < show_EmojiList.size()){
            CollectEmoji model = show_EmojiList.get(index);
            show_EmojiList.remove(index);
            show_EmojiList.add(1, model);
            freshListMsthod();
            cacheCollectEmoji();
        }

//        Logger.d("index = " + index + " size = " + true_EmojiList.size());
//        if (index > 0 && index < true_EmojiList.size()){
//            try{
//                String oldname = true_EmojiList.get(index);
//                Logger.d("oldname = " + oldname);
//                File fileRoot = new File(oldname);
//                if (fileRoot.exists()){
//                    String[] surNmaeArray = oldname.toString().split("\\.");
//                    if (surNmaeArray.length >= 1){
//                        String surNameS = surNmaeArray[surNmaeArray.length - 1];
//                        long time = System.currentTimeMillis();
//                        String newName = mFile + time  + "." + surNameS;
//                        Logger.d("newName = " + newName);
//                        fileRoot.renameTo(new File(newName));
//
//                        true_EmojiList.remove(index);
//                        true_EmojiList.add(1, oldname);
//
//                        String oldname_show = show_EmojiList.get(index);
//                        show_EmojiList.remove(index);
//                        show_EmojiList.add(1, oldname_show);
//
//
//                        freshListMsthod();
//                    }
//
//
//                }
//
//            }catch (Exception e){}
//        }
    }


//    public void saveBgImage(final String imageUrl,final long i) {
//        try{
//            new Thread(new Runnable() {
//                @Override
//                public void run(){
//                    mFile = RunningData.getInstance().getCollectImage();
//                    File file = new File(mFile);
//                    if (!file.exists()){
//                        file.mkdir();
//                    }
//                    if (mFile != null){
//                        String[] surNmaeArray = imageUrl.toString().split("\\.");
//                        if (surNmaeArray.length >= 1){
//                            String surNameS = surNmaeArray[surNmaeArray.length - 1];
//                            long time = System.currentTimeMillis() + i;
//                            String savefile = mFile + time  + "." + surNameS;
//                            copyFile(imageUrl, savefile);
//                        }
//                    }
//                }
//            }).start();
//
//        }catch (Exception e){}
//    }

//    public boolean copyFile(String oldPath$Name, String newPath$Name) {
//        try {
//            Logger.d("oldPath$Name = " + oldPath$Name + "\n newPath$Name" + newPath$Name);
//            File oldFile = new File(oldPath$Name);
//            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
//                return false;
//            }
//            File file = new File(mFile);
//            long fileSize = file.length();
//            if (!file.exists()){
//                file.mkdir();
//            }
//            //删除旧的图
//            File newFile = new File(newPath$Name);
//            if (newFile.exists()){
//                newFile.delete();
//            }
//
//            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
//            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
//            byte[] buffer = new byte[(int)fileSize];
//            int byteRead;
//            while (-1 != (byteRead = fileInputStream.read(buffer))) {
//                fileOutputStream.write(buffer, 0, byteRead);
//            }
//            fileInputStream.close();
//            fileOutputStream.flush();
//            fileOutputStream.close();
//
//            show_EmojiList.add(oldPath$Name);
//            true_EmojiList.add(newPath$Name);
//            freshListMsthod();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }


//}
}