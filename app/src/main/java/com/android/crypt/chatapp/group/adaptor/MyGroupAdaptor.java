package com.android.crypt.chatapp.group.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.crypt.chatapp.R;
import com.android.crypt.chatapp.group.model.GroupMember;
import com.android.crypt.chatapp.group.model.MyGroup;
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

public class MyGroupAdaptor extends BaseAdapter {

    private Context context;
    private ArrayList<MyGroup> mListAll;
    private LayoutInflater layoutInflater;

    public MyGroupAdaptor(Context context, ArrayList<MyGroup> mListAll) {
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
            convertView = layoutInflater.inflate(R.layout.group_join_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MyGroup model = mListAll.get(position);
        viewHolder.groupName.setText(model.groupName);

        String grouoAvatar = model.groupIcon;
        if(grouoAvatar == null || grouoAvatar.equalsIgnoreCase("")){//没有群头像
            viewHolder.ivAvatar.setVisibility(View.GONE);
            viewHolder.ivAvatarText.setVisibility(View.VISIBLE);
            String textValue = substring(model.groupName, 0 ,1);
            if(!textValue.equalsIgnoreCase("")){
                viewHolder.ivAvatarText.setText(textValue);
            }
        }else{
            viewHolder.ivAvatar.setVisibility(View.VISIBLE);
            viewHolder.ivAvatarText.setVisibility(View.GONE);
            String headImage = RunningData.getInstance().echoMainPicUrl() + grouoAvatar + "?imageView2/1/w/150/h/150";
            RequestOptions requestOptions = new RequestOptions().placeholder(R.mipmap.ic_icon);
            Glide.with(context).load(headImage).apply(requestOptions).into(viewHolder.ivAvatar);
        }


        String timeString = (model.createTime == null) ? "" : model.createTime;
        if (timeString.length() <= 13 && timeString.length() >= 10){
            if(timeString.length() == 10){
                timeString = timeString + "000";
            }
            long time = Long.parseLong(timeString);
            viewHolder.opTime.setText(TimeStrings.getNewChatTime(time));
        }else{
            viewHolder.opTime.setText("");
        }

        if(model.showTitle == 1){
            viewHolder.cataLog.setVisibility(View.VISIBLE);
            viewHolder.cataLog.setText("加入的群");
        }else if(model.showTitle == 2){
            viewHolder.cataLog.setVisibility(View.VISIBLE);
            viewHolder.cataLog.setText("创建的群");
        }else{
            viewHolder.cataLog.setVisibility(View.GONE);
        }

        return convertView;
    }

    public String substring(String source, int start, int end) {
        String result = "";
        try {
            result = source.substring(source.offsetByCodePoints(0, start),
                    source.offsetByCodePoints(0, end));
        } catch (Exception e) {
            result = "";
        }
        return result;
    }



    static class ViewHolder {
        @BindView(R.id.iv_avatar)
        RoundImageView ivAvatar;
        @BindView(R.id.group_name)
        TextView groupName;
        @BindView(R.id.op_time)
        TextView opTime;
        @BindView(R.id.catalog)
        TextView cataLog;
        @BindView(R.id.iv_avatar_text)
        TextView ivAvatarText;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
