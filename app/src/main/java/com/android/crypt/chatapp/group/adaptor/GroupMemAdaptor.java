package com.android.crypt.chatapp.group.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.group.model.GroupMember;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Common.TimeStrings;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by White on 2020/3/20.
 */

public class GroupMemAdaptor extends BaseAdapter {

    private Context context;
    private ArrayList<GroupMember> mListAll;
    private LayoutInflater layoutInflater;

    public GroupMemAdaptor(Context context, ArrayList<GroupMember> mListAll) {
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
            convertView = layoutInflater.inflate(R.layout.group_mem_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        GroupMember model = mListAll.get(position);
        viewHolder.groupName.setText(model.username);

        String headImage = RunningData.getInstance().echoMainPicUrl() + model.avatar_url + "?imageView2/1/w/150/h/150";
        RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.ic_icon);
        Glide.with(context).load(headImage).apply(requestOptions).into(viewHolder.ivAvatar);

        String timeString = (model.created_time == null) ? "" : model.created_time;

        if (timeString.length() <= 13 && timeString.length() >= 10){
            if(timeString.length() == 10){
                timeString = timeString + "000";
            }
            long time = Long.parseLong(timeString);
            viewHolder.opTime.setText(TimeStrings.getNewChatTime(time));
        }else{
            viewHolder.opTime.setText("");
        }

        return convertView;
    }


    static class ViewHolder {
        @BindView(R.id.iv_avatar)
        RoundImageView ivAvatar;

        @BindView(R.id.group_name)
        TextView groupName;

        @BindView(R.id.op_time)
        TextView opTime;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
