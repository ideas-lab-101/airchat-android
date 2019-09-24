package com.android.crypt.chatapp.contact.cn;

import java.io.Serializable;

import static com.android.crypt.chatapp.contact.cn.CNPinyinFactory.DEF_CHAR;


/**
 * Created by you on 2017/9/7.
 */

public class CNPinyin <T extends CN> implements Serializable, Comparable<CNPinyin<T>> {

    /**
     * 对应首字首拼音字母
     */
    public char firstChar = '#';
    /**
     * 所有字符中的拼音首字母
     */
    public String firstChars = "";
    /**
     * 对应的所有字母拼音
     */
    public String[] pinyins;

    /**
     * 拼音总长度
     */
    public int pinyinsTotalLength;

    public final T data;

    public CNPinyin(T data) {
        this.data = data;
    }

    public char getFirstChar() {
        return firstChar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("--firstChar--").append(firstChar).append("--pinyins:");
        if (pinyins == null){
            return "";
        }
        for (String str : pinyins) {
            sb.append(str);
        }
        return sb.toString();
    }

    int compareValue() {
        if (firstChar == DEF_CHAR) {
            return 'Z' + 1;
        }
        return firstChar;
    }

    @Override
    public int compareTo(CNPinyin<T> tcnPinyin) {
        int compare = compareValue() - tcnPinyin.compareValue();
        if (compare == 0) {
            if (data == null){
                return 0;
            }
            String chinese1 = data.chinese();
            String chinese2 = tcnPinyin.data.chinese();
            return chinese1.compareTo(chinese2);
        }
        return compare;
    }
}
