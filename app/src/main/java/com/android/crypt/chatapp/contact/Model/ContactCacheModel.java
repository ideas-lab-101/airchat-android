package com.android.crypt.chatapp.contact.Model;

/**
 * Created by White on 2019/6/26.
 *
 * 直接缓存ArrayList<CNPinyin<ContactModel>> Gson无法解析
 *
 * 改成缓存 直接缓存ArrayList<ContactCacheModel>
 *
 */

public class ContactCacheModel {
    public String avatar_url;
    public String username;
    public String label;
    public String account;
    public String introduction;
    public String public_key;
    public String friend_id;

    public char firstChar;
    public String firstChars;
    public String pinyins;
    public int pinyinsTotalLength;

}
