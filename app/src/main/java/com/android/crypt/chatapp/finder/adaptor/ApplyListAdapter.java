package com.android.crypt.chatapp.finder.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.crypt.chatapp.finder.adaptor.Model.ApplyModel;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.android.crypt.chatapp.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by White on 2019/6/14.
 */

public class ApplyListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ApplyModel> mListAll;
    private LayoutInflater layoutInflater;

    public ApplyListAdapter(Context context, ArrayList<ApplyModel> mListAll) {
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
            convertView = layoutInflater.inflate(R.layout.my_apply_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ApplyModel model = mListAll.get(position);

        if (model.sex.equalsIgnoreCase("M")){
            viewHolder.tvCaption.setText(model.username + " (男)");
        }else{
            viewHolder.tvCaption.setText(model.username + " (女)");
        }
        viewHolder.tvInfo.setText(model.apply_msg);

        String headImage = RunningData.getInstance().echoMainPicUrl() + model.avatar_url + "?imageView2/1/w/150/h/150";
        RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.ic_icon);
        Glide.with(context).load(headImage).apply(requestOptions).into(viewHolder.ivAvatar);

        return convertView;
    }


    static class ViewHolder {
        @BindView(R.id.iv_avatar)
        RoundImageView ivAvatar;
        @BindView(R.id.tv_caption)
        TextView tvCaption;
        @BindView(R.id.tv_info)
        TextView tvInfo;
        @BindView(R.id.ll_contactItem)
        LinearLayout llContactItem;
        @BindView(R.id.item_bg)
        LinearLayout itemBg;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
