package com.android.crypt.chatapp.msgList.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.crypt.chatapp.msgList.model.MessageListModel;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Common.TimeStrings;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.android.crypt.chatapp.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by White on 2019/4/27.
 */

public class MessageListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<MessageListModel> mListAll;
    private LayoutInflater layoutInflater;

    public MessageListAdapter(Context context, ArrayList<MessageListModel> mListAll) {
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
            convertView = layoutInflater.inflate(R.layout.message_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        MessageListModel model = mListAll.get(position);


        //姓名
        String username = model.username;
        String label = model.label;
        if (label != null && label != "") {
            username = label;
        }
        viewHolder.messageName.setText(username);

        viewHolder.messageContent.setText(model.Content);

        if (model.isreaded == true){
            viewHolder.messageTips.setVisibility(View.VISIBLE);
        }else{
            viewHolder.messageTips.setVisibility(View.GONE);
        }

        if (model.MessageSendTime.length() <= 13){
            long time = Long.parseLong(model.MessageSendTime);
            viewHolder.messageTime.setText(TimeStrings.getNewChatTime(time));
        }else{
            viewHolder.messageTime.setText("");
        }

        //头像
        String headImage = RunningData.getInstance().echoMainPicUrl() + model.avatar_url + "?imageView2/1/w/200/h/200";
        RequestOptions requestOptions = new RequestOptions().fitCenter().skipMemoryCache(true).placeholder(R.mipmap.default_image);
        Glide.with(context)
                .load(headImage)
                .apply(requestOptions)
                .into(viewHolder.ivAvatar);

        viewHolder.deleteMsg.setVisibility(View.VISIBLE);
        viewHolder.deleteMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onRightItemClick(v, position);
                }
            }
        });

        return convertView;
    }


    public void updateItem(final View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.messageTips.setVisibility(View.GONE);
    }


    static class ViewHolder {
        @BindView(R.id.iv_avatar)
        RoundImageView ivAvatar;
        @BindView(R.id.message_name)
        TextView messageName;
        @BindView(R.id.message_time)
        TextView messageTime;
        @BindView(R.id.message_content)
        TextView messageContent;
        @BindView(R.id.message_tips)
        TextView messageTips;
        @BindView(R.id.delete_msg)
        TextView deleteMsg;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private onRightItemClickListener mListener = null;
    public void setOnRightItemClickListener(onRightItemClickListener listener) {
        mListener = listener;
    }

    public interface onRightItemClickListener {
        void onRightItemClick(View v, int position);
    }

}
