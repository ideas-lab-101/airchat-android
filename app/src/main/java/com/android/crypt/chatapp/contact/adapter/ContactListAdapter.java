package com.android.crypt.chatapp.contact.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.contact.cn.CNPinyin;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.contact.Model.ContactModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<CNPinyin<ContactModel>> mListAll;
    private LayoutInflater layoutInflater;

    public ContactListAdapter(Context context, ArrayList<CNPinyin<ContactModel>> mListAll) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.contact_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ContactModel mMap = mListAll.get(position).data;

        //姓名
        String username = mMap.username;
        String label = mMap.label;
        if (label != null && label != "") {
            username = label;
        }
        viewHolder.tvCaption.setText(username);

        //介绍 introduction
        String introduction = mMap.introduction;
        if (introduction != null && introduction != "") {
            viewHolder.tvInfo.setVisibility(View.VISIBLE);
            viewHolder.tvInfo.setText(introduction);
        } else {
            viewHolder.tvInfo.setVisibility(View.GONE);
        }

        //头像
        if ( mMap.avatar_url != null && !mMap.avatar_url.equalsIgnoreCase("")) {
            String headImage = RunningData.getInstance().echoMainPicUrl() + mMap.avatar_url + "?imageView2/1/w/150/h/150";
            RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.ic_icon);
            Glide.with(context)
                    .load(headImage)
                    .apply(requestOptions)
                    .into(viewHolder.ivAvatar);
        }else{
            viewHolder.ivAvatar.setImageDrawable(context.getResources().getDrawable(R.mipmap.default_head));
        }


        //标记
        char catalog = mListAll.get(position).getFirstChar();
        int posCur = getSectionForPosition(catalog);

        if (position == getSectionForPosition(catalog)){
            String groupChat = String.valueOf(catalog);
            viewHolder.catalog.setText(groupChat);
            viewHolder.catalog.setVisibility(View.VISIBLE);
        }else{
            viewHolder.catalog.setVisibility(View.GONE);
        }

        if (mMap.public_key.equalsIgnoreCase("")){
            viewHolder.contactTips.setVisibility(View.VISIBLE);
            viewHolder.itemBg.setAlpha((float)0.5);
        }else{
            viewHolder.contactTips.setVisibility(View.GONE);
            viewHolder.itemBg.setAlpha((float)1.0);

        }

        return convertView;
    }

    public int getSectionForPosition(char c) {
        int pos = -1;
        for (int i = 0; i < mListAll.size(); i++){
            if (c == mListAll.get(i).getFirstChar()){
                pos = i;
                break;
            }
        }
        return pos;
    }


    static class ViewHolder {
        @BindView(R.id.item_bg)
        LinearLayout itemBg;

        @BindView(R.id.iv_avatar)
        RoundImageView ivAvatar;
        @BindView(R.id.tv_caption)
        TextView tvCaption;
        @BindView(R.id.tv_info)
        TextView tvInfo;
        @BindView(R.id.catalog)
        TextView catalog;
        @BindView(R.id.contact_tips)
        ImageView contactTips;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
