package com.android.crypt.chatapp.msgDetail.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.crypt.chatapp.utility.Common.RunningData;
import com.bumptech.glide.Glide;
import com.android.crypt.chatapp.R;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by White on 2019/4/23.
 */

public class EmojiAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> mListAll;
    private LayoutInflater layoutInflater;
    private  ViewHolder viewHolder;
    private int emoji_kind;
    public EmojiAdapter(Context context, ArrayList<String> mListAll, int kind) {
        this.context = context;
        this.mListAll = mListAll;
        this.emoji_kind = kind;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        viewHolder = null;
        if (convertView == null) {
            layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.msg_emoji_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (this.emoji_kind == 0){
            viewHolder.image.setBackground(new ColorDrawable(0xffffffff));
            String image = RunningData.getInstance().echoEmojiPicUrl() + mListAll.get(position);
            Glide.with(context).load(image).into(viewHolder.image);
        }else if(this.emoji_kind == 1){
            if (position == 0){
                viewHolder.image.setBackground(new ColorDrawable(0xffffffff));
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.add_emoji);
                viewHolder.image.setImageBitmap(bitmap);
            }else{
                viewHolder.image.setBackground(new ColorDrawable(0xfffafafa));
                String co_image_url = mListAll.get(position);
                File file = new File(co_image_url);
                //加载图片
                Glide.with(context).load(file).into(viewHolder.image);
            }
        }
        return convertView;
    }


    public  void updateItem(final int position, View convertView, ViewGroup parent) {



    }

    private void changeState(){
        viewHolder.image.setVisibility(View.VISIBLE);
        viewHolder.loadingImage.setVisibility(View.GONE);
    }

    static class ViewHolder {
        @BindView(R.id.image)
        ImageView image;

        @BindView(R.id.loading_image)
        AVLoadingIndicatorView loadingImage;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
