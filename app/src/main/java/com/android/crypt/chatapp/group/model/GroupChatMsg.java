package com.android.crypt.chatapp.group.model;

/**
 * Created by White on 2020/3/21.
 */
/**
 *
 * 这里保留群聊的必要消息
 * 由于加密群的特殊性，群内备注、群头像、群名称信息随着群一起发出去
 *
 ***/
public class GroupChatMsg {

    public String groupIcon = "";    // 群头像
    public String groupLabel = "";   //群内备注
    public String groupName = "";    //群名称
    public String groupIntro = "";    //群名称
    public Boolean isAdmin = false;     //  是不是群主

}
