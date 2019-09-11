package com.android.crypt.chatapp.utility.Cache.CacheClass;

/**
 * Created by White on 2019/4/26.
 *
 * 不要该顺序，只能忘后加
 *
 */

public enum ObjectCacheType {
    cur_account,  //cur_   前缀的的缓存key为其本身
    cur_token,
    cur_pwd,
    pri_key,      //其他缓存key为  account + type
    user_info,
    friend_list,      //好友列表
    message_list,
    config_dic
}
