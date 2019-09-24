package com.android.crypt.chatapp.msgDetail.IMTools;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.crypt.chatapp.BaseActivity;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.baoyz.actionsheet.ActionSheet;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.msgDetail.adapter.EmojiAdapter;

import org.devio.takephoto.model.TImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddEmojiActivity extends BaseActivity implements AdapterView.OnItemClickListener, ActionSheet.ActionSheetListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.emoji_content)
    GridView emojiContent;

    private ArrayList<String> show_EmojiList;
    private ArrayList<String> true_EmojiList;  // 真是的数据

    private String mFile = RunningData.getInstance().getCollectImage();
    private EmojiAdapter emojiAdapter;

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

    private void initData(){
        if (show_EmojiList == null){
            show_EmojiList = new ArrayList<String>();
        }
        show_EmojiList.clear();
        show_EmojiList.add("");

        if (true_EmojiList == null){
            true_EmojiList = new ArrayList<String>();
        }
        true_EmojiList.add("");
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
                    if (true_EmojiList.size() <= 1){
                        show_EmojiList.add(speBgImage + imageName);
                        true_EmojiList.add(speBgImage + imageName);
                    }else{
                        int insert_index = true_EmojiList.size() - 1;
                        for(int j = 1; j < true_EmojiList.size(); j++){
                            String[] names = true_EmojiList.get(j).split("\\/");
                            String name = names[names.length - 1];
                            long innerTime = string2int(name.split("\\.")[0]);
                            if (outTime >= innerTime){
                                insert_index = j;
                                break;
                            }
                        }
                        show_EmojiList.add(insert_index, speBgImage + imageName);
                        true_EmojiList.add(insert_index, speBgImage + imageName);
                    }
                }
            }
        }catch (Exception e){}
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
        if (position < show_EmojiList.size()){
            if (position == 0){
                if (show_EmojiList.size() <= 100){
                    Intent intent = new Intent(this, ChooseEmojiActivity.class);
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }else{
                    makeSnake(emojiContent, "最多添加100个表情", R.mipmap.toast_alarm, Snackbar.LENGTH_LONG);
                }
            }else{
                tap_index = position;
                ActionSheet.createBuilder(this, getSupportFragmentManager()).setCancelButtonTitle("取消").setOtherButtonTitles("移动到最前", "删除当前图片").setCancelableOnTouchOutside(true).setListener(this).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 100){
                ArrayList<TImage> imagesUrl = (ArrayList<TImage>)data.getSerializableExtra("images");
                dealCollectEMoji(imagesUrl);
            }
        }
    }


    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            if (tap_index > 0){
                changeImageName(tap_index);
                tap_index = 0;
            }
        }else if(index == 1){
            if (tap_index > 0){
                deleteEmojiFile(tap_index);
                tap_index = 0;
            }
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancle) {
        tap_index = 0;
    }


    //****处理选中的图片
    private void deleteEmojiFile(int index){
        if (index > 0 && index < show_EmojiList.size()){
            try{
                String showImage = show_EmojiList.get(index);
                File fileRoot = new File(showImage);
                if (fileRoot.exists()){
                    fileRoot.delete();
                    show_EmojiList.remove(index);
                    freshListMsthod();
                }

                String emojiImage = true_EmojiList.get(index);
                File fileRoot2 = new File(emojiImage);
                if (fileRoot2.exists()){
                    fileRoot2.delete();
                    true_EmojiList.remove(index);
                    freshListMsthod();
                }
            }catch (Exception e){}
        }
    }

    private void changeImageName(int index){
        Logger.d("index = " + index + " size = " + true_EmojiList.size());
        if (index > 0 && index < true_EmojiList.size()){
            try{
                String oldname = true_EmojiList.get(index);
                Logger.d("oldname = " + oldname);
                File fileRoot = new File(oldname);
                if (fileRoot.exists()){
                    String[] surNmaeArray = oldname.toString().split("\\.");
                    if (surNmaeArray.length >= 1){
                        String surNameS = surNmaeArray[surNmaeArray.length - 1];
                        long time = System.currentTimeMillis();
                        String newName = mFile + time  + "." + surNameS;
                        Logger.d("newName = " + newName);
                        fileRoot.renameTo(new File(newName));

                        true_EmojiList.remove(index);
                        true_EmojiList.add(1, oldname);

                        String oldname_show = show_EmojiList.get(index);
                        show_EmojiList.remove(index);
                        show_EmojiList.add(1, oldname_show);


                        freshListMsthod();
                    }


                }

            }catch (Exception e){}
        }
    }


    private void dealCollectEMoji(ArrayList<TImage> imagesUrl){
        for (int i = 0; i < imagesUrl.size(); i++){
            String imgLocalUrl = imagesUrl.get(i).getCompressPath();
            saveBgImage(imgLocalUrl, (long)i);
        }
    }

    public void saveBgImage(final String imageUrl,final long i) {
        try{
            new Thread(new Runnable() {
                @Override
                public void run(){
                    mFile = RunningData.getInstance().getCollectImage();
                    File file = new File(mFile);
                    if (!file.exists()){
                        file.mkdir();
                    }
                    if (mFile != null){
                        String[] surNmaeArray = imageUrl.toString().split("\\.");
                        if (surNmaeArray.length >= 1){
                            String surNameS = surNmaeArray[surNmaeArray.length - 1];
                            long time = System.currentTimeMillis() + i;
                            String savefile = mFile + time  + "." + surNameS;
                            copyFile(imageUrl, savefile);
                        }
                    }
                }
            }).start();

        }catch (Exception e){}
    }

    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            Logger.d("oldPath$Name = " + oldPath$Name + "\n newPath$Name" + newPath$Name);
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            File file = new File(mFile);
            long fileSize = file.length();
            if (!file.exists()){
                file.mkdir();
            }
            //删除旧的图
            File newFile = new File(newPath$Name);
            if (newFile.exists()){
                newFile.delete();
            }

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[(int)fileSize];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();

            show_EmojiList.add(oldPath$Name);
            true_EmojiList.add(newPath$Name);
            freshListMsthod();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void freshListMsthod(){
        //更新主线程UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emojiAdapter.notifyDataSetChanged();
            }
        });
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
